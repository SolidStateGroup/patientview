package org.patientview.api.service.impl;

import org.patientview.api.service.MigrationService;
import org.patientview.api.service.PatientService;
import org.patientview.api.service.UserMigrationService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.MigrationException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserMigration;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.Date;
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
    private UserRepository userRepository;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Inject
    private UserService userService;

    @Inject
    private PatientService patientService;

    @Inject
    private UserMigrationService userMigrationService;

    @Inject
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public Long migrateUser(MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, MigrationException {

        Date start = new Date();
        UserMigration userMigration;

        // get User object from MigrationUser (not patient data)
        User user = migrationUser.getUser();
        Long userId;

        org.patientview.api.model.User apiUser = userService.getByUsername(user.getUsername());

        if (apiUser != null) {
            // todo: deal with updating migration users, currently just get Id of existing
            userId = apiUser.getId();
            userMigration = userMigrationService.getByPatientview2Id(userId);
        } else {
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
                userMigration = new UserMigration(migrationUser.getPatientview1Id(), MigrationStatus.USER_FAILED);
                userMigration.setInformation(e.getMessage());
                userMigrationService.save(userMigration);
                throw e;
            }
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
                LOG.error("Could not migrate patient data: {} {}", e.getClass(), e.getMessage());
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
                Date start = new Date();

                User migrationUser = userService.findByUsernameCaseInsensitive("migration");
                Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

                for (GroupRole groupRole : groupRoleRepository.findByUser(migrationUser)) {
                    grantedAuthorities.add(groupRole);
                }

                SecurityContext ctx = new SecurityContextImpl();
                ctx.setAuthentication(new UsernamePasswordAuthenticationToken(migrationUser, null, grantedAuthorities));
                SecurityContextHolder.setContext(ctx);

                List<Long> pv2ids = userMigrationService.getPatientview2IdsByStatus(MigrationStatus.PATIENT_MIGRATED);
                LOG.info(pv2ids.size() + " total PATIENT_MIGRATED");


                LOG.info("Migration of Observations took "
                        + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.");
            }
        });
    }

    // Migration Only
    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
}
