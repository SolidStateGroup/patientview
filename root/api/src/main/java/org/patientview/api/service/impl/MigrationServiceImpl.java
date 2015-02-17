package org.patientview.api.service.impl;

import org.patientview.api.service.GroupRoleService;
import org.patientview.api.service.MigrationService;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.service.ObservationService;
import org.patientview.api.service.PatientService;
import org.patientview.api.service.UserMigrationService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.MigrationException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirDatabaseObservation;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserMigration;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/11/2014.
 *
 */
@Service
public class MigrationServiceImpl extends AbstractServiceImpl<MigrationServiceImpl> implements MigrationService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private GroupRoleService groupRoleService;

    @Inject
    private UserService userService;

    @Inject
    private PatientService patientService;

    @Inject
    private UserMigrationService userMigrationService;

    @Inject
    private ObservationService observationService;

    @Inject
    private ObservationHeadingService observationHeadingService;

    @Inject
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Inject
    @Named("patientView1")
    private DataSource dataSource;

    private static final String COMMENT_RESULT_HEADING = "resultcomment";
    private static final boolean DELETE_EXISTING = true;
    private static final int THREE = 3;
    private static final int FOUR = 4;

    public Long migrateUser(MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, MigrationException {

        Date start = new Date();
        UserMigration userMigration = null;

        // get User object from MigrationUser (not patient data)
        User user = migrationUser.getUser();
        Long userId;

        org.patientview.api.model.User apiUser = userService.getByUsername(user.getUsername());

        // delete user if already exists (expensive, not to be used for live migration)
        if (apiUser != null && DELETE_EXISTING) {
            try {
                LOG.info("Deleting existing user with id " + apiUser.getId());
                userService.delete(apiUser.getId(), true);
            } catch (ResourceForbiddenException | FhirResourceException e) {
                LOG.error("Cannot delete user with id " + apiUser.getId());
                throw new MigrationException(e);
            }
        }

        // add basic user object
        try {
            userMigration = new UserMigration(migrationUser.getPatientview1Id(), MigrationStatus.USER_STARTED);
            userMigration.setInformation(null);
            userMigration.setCreator(getCurrentUser());
            userMigration.setLastUpdater(getCurrentUser());
            userMigration.setLastUpdate(start);
            userMigration = userMigrationService.save(userMigration);

            // initialise userService if not already done
            if (userService.getGenericGroup() == null) {
                userService.init();
            }

            userId = userService.add(user);

            // add user information if present (convert from Set to ArrayList)
            if (!CollectionUtils.isEmpty(user.getUserInformation())) {
                userService.addOtherUsersInformation(userId, new ArrayList<>(user.getUserInformation()));
            }
        } catch (EntityExistsException e) {
            if (userMigration == null) {
                userMigration = new UserMigration(migrationUser.getPatientview1Id(), MigrationStatus.USER_FAILED);
            } else {
                userMigration.setStatus(MigrationStatus.USER_FAILED);
            }
            userMigration.setCreator(getCurrentUser());
            userMigration.setLastUpdater(getCurrentUser());
            userMigration.setLastUpdate(start);
            userMigration.setInformation(e.getMessage());
            userMigrationService.save(userMigration);
            throw e;
        }

        if (userMigration == null) {
            userMigration = new UserMigration(migrationUser.getPatientview1Id(), MigrationStatus.USER_MIGRATED);
            userMigration.setCreator(getCurrentUser());
        } else {
            userMigration.setStatus(MigrationStatus.USER_MIGRATED);
        }

        userMigration.setInformation(null);
        userMigration.setPatientview2UserId(userId);
        userMigration.setLastUpdate(new Date());
        userMigrationService.save(userMigration);

        String doneMessage;

        // migrate patient related data
        if (migrationUser.isPatient()) {
            try {
                userMigration.setStatus(MigrationStatus.PATIENT_STARTED);
                userMigration.setInformation(null);
                userMigration.setLastUpdate(new Date());
                userMigrationService.save(userMigration);

                LOG.info("{} migrating patient data", userId);
                patientService.migratePatientData(userId, migrationUser);
                doneMessage = userId + " Done, migrated patient data";

                userMigration.setStatus(MigrationStatus.PATIENT_MIGRATED);
                userMigration.setInformation(null);
                userMigration.setLastUpdate(new Date());
                userMigrationService.save(userMigration);
            } catch (Exception e) {
                LOG.error("Could not migrate patient data: {}", e);
                try {
                    // clean up any data created during failed migration
                    patientService.deleteExistingPatientData(userRepository.findOne(userId).getFhirLinks());
                } catch (FhirResourceException fre) {
                    userMigration.setStatus(MigrationStatus.PATIENT_CLEANUP_FAILED);
                    userMigration.setInformation(fre.getMessage());
                    userMigration.setLastUpdate(new Date());
                    userMigrationService.save(userMigration);
                    throw new MigrationException("Error cleaning up failed migration: " + fre.getMessage());
                }

                userMigration.setStatus(MigrationStatus.PATIENT_FAILED);
                userMigration.setInformation(e.getMessage());
                userMigration.setLastUpdate(new Date());
                userMigrationService.save(userMigration);
                throw new MigrationException("Could not migrate patient data for pv1 id "
                        + userMigration.getPatientview1UserId() + ": " + e.getMessage());
            }
        } else {
            doneMessage = userId + " Done";
        }

        Date end = new Date();
        LOG.info(doneMessage + ", took " + Util.getDateDiff(start, end, TimeUnit.SECONDS) + " seconds.");
        return userId;
    }

    public void migrateObservations(MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, MigrationException {
        //LOG.info("1: " + new Date().getTime());

        if (migrationUser.isPatient()) {
            Date start = new Date();
            UserMigration userMigration = userMigrationService.getByPatientview1Id(migrationUser.getPatientview1Id());

            if (userMigration == null) {
                throw new MigrationException("Must migrate user and patient before observations");
            }

            // only continue to migrate observations if successfully migrated patient data or failed previously
            if (!(userMigration.getStatus().equals(MigrationStatus.PATIENT_MIGRATED)
                    || userMigration.getStatus().equals(MigrationStatus.OBSERVATIONS_FAILED)
                    || userMigration.getStatus().equals(MigrationStatus.OBSERVATIONS_MIGRATED)
            )) {
                throw new MigrationException("Cannot migrate observation data if previously failed to migrate "
                        + "patient data or already migrating observation data. Status: "
                        + userMigration.getStatus().toString() + ", PatientView1 ID: "
                        + userMigration.getPatientview1UserId() + ", PatientView2 ID: "
                        + userMigration.getPatientview2UserId());
            }

            userMigration.setLastUpdater(getCurrentUser());
            userMigration.setLastUpdate(start);
            userMigration.setInformation(null);
            if (!CollectionUtils.isEmpty(migrationUser.getObservations())) {
                userMigration.setObservationCount(Long.valueOf(migrationUser.getObservations().size()));
            } else {
                userMigration.setObservationCount(0L);
            }
            userMigration.setStatus(MigrationStatus.OBSERVATIONS_STARTED);
            userMigration = userMigrationService.save(userMigration);

            if (userMigration.getPatientview2UserId() == null) {
                userMigration.setStatus(MigrationStatus.OBSERVATIONS_FAILED);
                userMigration.setInformation("Cannot find corresponding PatientView2 user");
                userMigrationService.save(userMigration);
                throw new ResourceNotFoundException("Cannot find corresponding PatientView2 user");
            }

            //LOG.info("2: " + new Date().getTime());

            if (!CollectionUtils.isEmpty(migrationUser.getObservations())) {
                Long pv2UserId = userMigration.getPatientview2UserId();
                try {
                    //LOG.info("{} migrating {} observations", userId, migrationUser.getObservations().size());
                    patientService.migrateTestObservations(pv2UserId, migrationUser);

                    userMigration.setStatus(MigrationStatus.OBSERVATIONS_MIGRATED);
                    userMigration.setInformation(null);
                    userMigration.setLastUpdate(new Date());
                    userMigrationService.save(userMigration);
                } catch (Exception e) {
                    userMigration.setStatus(MigrationStatus.OBSERVATIONS_FAILED);
                    userMigration.setInformation(e.getMessage());
                    userMigration.setLastUpdate(new Date());
                    userMigrationService.save(userMigration);
                    throw new MigrationException("Could not migrate patient data: " + e.getMessage());
                }
                Date end = new Date();
                LOG.info(pv2UserId + "  migrated " + migrationUser.getObservations().size() + " observations, took "
                        + Util.getDateDiff(start, end, TimeUnit.SECONDS) + " seconds.");
            } else {
                userMigration.setStatus(MigrationStatus.OBSERVATIONS_MIGRATED);
                userMigration.setInformation(null);
                userMigration.setLastUpdate(new Date());
                userMigrationService.save(userMigration);
            }

            //LOG.info("9: " + new Date().getTime());
        }
    }

    @Override
    public void migrateObservationsFast() {
        threadPoolTaskExecutor.execute(new Runnable() {

            public void run() {
                doMigration();
            }

            @Transactional
            private void doMigration() {
                Date start = new Date();

                // get observation headings
                HashMap<String, ObservationHeading> observationHeadingMap = new HashMap<>();
                for (ObservationHeading observationHeading : observationHeadingService.findAll()) {
                    observationHeadingMap.put(observationHeading.getCode().toUpperCase(), observationHeading);
                }

                // log in migration user
                User migrationUser = userService.findByUsernameCaseInsensitive("migration");
                Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
                for (GroupRole groupRole : groupRoleService.findByUser(migrationUser)) {
                    grantedAuthorities.add(groupRole);
                }
                SecurityContext ctx = new SecurityContextImpl();
                ctx.setAuthentication(new UsernamePasswordAuthenticationToken(migrationUser, null, grantedAuthorities));
                SecurityContextHolder.setContext(ctx);

                // get list of pv2 ids to migrate observations for
                List<Long> pv2ids = userMigrationService.getPatientview2IdsByStatus(MigrationStatus.PATIENT_MIGRATED);
                pv2ids.addAll(userMigrationService.getPatientview2IdsByStatus(MigrationStatus.OBSERVATIONS_FAILED));
                LOG.info(pv2ids.size() + " total PATIENT_MIGRATED, OBSERVATIONS_FAILED");
                Connection connection = null;

                try {
                    connection = dataSource.getConnection();
                    for (Long pv2id : pv2ids) {

                        UserMigration userMigration
                                = userMigrationService.getByPatientview2Id(pv2id);
                        userMigration.setLastUpdate(new Date());
                        userMigration.setStatus(MigrationStatus.OBSERVATIONS_STARTED);
                        userMigration = userMigrationService.save(userMigration);

                        List<FhirDatabaseObservation> fhirDatabaseObservations = new ArrayList<>();
                        try {
                            User user = userService.get(pv2id);
                            List<FhirLink> fhirLinks = fhirLinkRepository.findActiveByUser(user);

                            if (!CollectionUtils.isEmpty(fhirLinks)) {
                                for (FhirLink fhirLink : fhirLinks) {

                                    // correctly transfer patient entered results
                                    String groupCode = fhirLink.getGroup().getCode();
                                    if (groupCode.equals(HiddenGroupCodes.PATIENT_ENTERED.toString())) {
                                        groupCode = "PATIENT";
                                    }

                                    String query = "SELECT testcode, datestamp, prepost, value "
                                            + "FROM testresult "
                                            + "WHERE nhsno = '" + fhirLink.getIdentifier().getIdentifier()
                                            + "' AND unitcode = '" + groupCode + "'";

                                    java.sql.Statement statement = connection.createStatement();
                                    ResultSet results = statement.executeQuery(query);

                                    while ((results.next())) {
                                        String testcode = results.getString(1)
                                                .replace("\"", "").replace("}", "").replace("{", "")
                                                .replace(",", "").replace("'", "");
                                        Date datestamp = results.getTimestamp(2);
                                        String prepost = results.getString(THREE)
                                                .replace("\"", "").replace("}", "").replace("{", "")
                                                .replace(",", "").replace("'", "");
                                        String value = results.getString(FOUR)
                                                .replace("\"", "").replace("}", "").replace("{", "")
                                                .replace(",", "").replace("'", "");

                                        FhirObservation fhirObservation = new FhirObservation();
                                        fhirObservation.setApplies(datestamp);
                                        fhirObservation.setComments(prepost);
                                        fhirObservation.setValue(value);
                                        ObservationHeading observationHeading
                                                = observationHeadingMap.get(testcode.toUpperCase());

                                        if (observationHeading == null) {
                                            observationHeading = new ObservationHeading();
                                            observationHeading.setCode(testcode);
                                            observationHeading.setName(testcode);
                                            LOG.info("ObservationHeading not found (adding anyway): " + testcode);
                                        }

                                        fhirDatabaseObservations.add(observationService.buildFhirDatabaseObservation(
                                                fhirObservation, observationHeading, fhirLink));
                                    }
                                }

                                // result comments in pv1 are not attached to a certain group, so choose FhirLink
                                // that isn't PATIENT_ENTERED
                                FhirLink commentFhirLink = null;
                                for (FhirLink fhirLink : fhirLinks) {
                                    if (!fhirLink.getGroup().getCode()
                                            .equals(HiddenGroupCodes.PATIENT_ENTERED.toString())) {
                                        commentFhirLink = fhirLink;
                                    }
                                }

                                if (commentFhirLink != null) {
                                    String query = "SELECT datestamp, body "
                                            + "FROM comment "
                                            + "WHERE nhsno = '" + fhirLinks.get(0).getIdentifier().getIdentifier()
                                            + "'";

                                    java.sql.Statement statement = connection.createStatement();
                                    ResultSet results = statement.executeQuery(query);

                                    while ((results.next())) {
                                        Date datestamp = results.getTimestamp(1);
                                        String body = results.getString(2)
                                                .replace("\"", "").replace("}", "").replace("{", "")
                                                .replace(",", " ").replace("'", "''");

                                        FhirObservation fhirObservation = new FhirObservation();
                                        fhirObservation.setApplies(datestamp);
                                        fhirObservation.setComments(body);
                                        fhirObservation.setValue(body);
                                        ObservationHeading observationHeading
                                                = observationHeadingMap.get(COMMENT_RESULT_HEADING.toUpperCase());

                                        fhirDatabaseObservations.add(observationService.buildFhirDatabaseObservation(
                                                fhirObservation, observationHeading, commentFhirLink));
                                    }
                                }
                            }

                            try {
                                if (!CollectionUtils.isEmpty(fhirDatabaseObservations)) {
                                    insertObservations(fhirDatabaseObservations);
                                }

                                userMigration.setStatus(MigrationStatus.OBSERVATIONS_MIGRATED);
                                userMigration.setObservationCount(Long.valueOf(fhirDatabaseObservations.size()));
                                userMigration.setInformation(null);
                                userMigration.setLastUpdate(new Date());
                                userMigrationService.save(userMigration);
                            } catch (FhirResourceException fre) {
                                userMigration.setStatus(MigrationStatus.OBSERVATIONS_FAILED);
                                userMigration.setInformation(fre.getMessage());
                                userMigration.setLastUpdate(new Date());
                                userMigrationService.save(userMigration);
                            }

                        } catch (ResourceNotFoundException rnf) {
                            LOG.error("user with pv2 id " + pv2id + " not found");
                        } catch (FhirResourceException fre) {
                            LOG.error("cannot build observations for user with pv2 id " + pv2id);
                            userMigration.setStatus(MigrationStatus.OBSERVATIONS_FAILED);
                            userMigration.setInformation(fre.getMessage());
                            userMigration.setLastUpdate(new Date());
                            userMigrationService.save(userMigration);
                        }
                    }

                    connection.close();
                } catch (SQLException e) {
                    LOG.error("MySQL exception", e);
                    try {
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (SQLException e2) {
                        LOG.error("Cannot close MySQL connection ", e2);
                    }
                }

                LOG.info("Migration of Observations took "
                        + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.");
            }
        });
    }

    private void insertObservations(List<FhirDatabaseObservation> fhirDatabaseObservations)
        throws FhirResourceException {

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO observation (logical_id, version_id, resource_type, published, updated, content) ");
        sb.append("VALUES ");

        for (int i = 0; i < fhirDatabaseObservations.size(); i++) {
            FhirDatabaseObservation obs = fhirDatabaseObservations.get(i);
            sb.append("(");
            sb.append("'").append(obs.getLogicalId().toString()).append("','");
            sb.append(obs.getVersionId().toString()).append("','");
            sb.append(obs.getResourceType()).append("','");
            sb.append(obs.getPublished().toString()).append("','");
            sb.append(obs.getUpdated().toString()).append("','");
            sb.append(obs.getContent());
            sb.append("')");
            if (i != (fhirDatabaseObservations.size() - 1)) {
                sb.append(",");
            }
        }
        fhirResource.executeSQL(sb.toString());
    }

    // Migration Only
    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
}
