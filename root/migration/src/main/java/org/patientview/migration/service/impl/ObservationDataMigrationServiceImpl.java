package org.patientview.migration.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.service.ObservationDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.patientview.model.TestResult;
import org.patientview.patientview.model.User;
import org.patientview.patientview.model.UserMapping;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.patientview.repository.UserDao;
import org.patientview.repository.UserMappingDao;
import org.patientview.service.TestResultManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by james@solidstategroup.com
 * Created on 06/06/2014
 */
@Service
public class ObservationDataMigrationServiceImpl implements ObservationDataMigrationService {

    private static final Logger LOG = LoggerFactory.getLogger(ObservationDataMigrationServiceImpl.class);

    @Inject
    private AdminDataMigrationService adminDataMigrationService;

    @Inject
    private TestResultManager testResultManager;

    @Inject
    private UserDao userDao;

    @Inject
    private UserMappingDao userMappingDao;

    @Inject
    private ExecutorService observationTaskExecutor;

    private List<Group> groups;

    private @Value("${migration.username}") String migrationUsername;
    private @Value("${migration.password}") String migrationPassword;
    private @Value("${patientview.api.url}") String patientviewApiUrl;

    private void init() throws JsonMigrationException {
        try {
            JsonUtil.setPatientviewApiUrl(patientviewApiUrl);
            JsonUtil.token = JsonUtil.authenticate(migrationUsername, migrationPassword);
            groups = JsonUtil.getGroups(JsonUtil.pvUrl + "/group");
        } catch (JsonMigrationException e) {
            LOG.error("Could not authenticate {} ", e.getCause());
            throw new JsonMigrationException(e.getMessage());
        } catch (JsonMigrationExistsException e) {
            LOG.error("Could not authenticate {} ", e.getCause());
        }
    }

    @Override
    public void migrate() throws JsonMigrationException {
        init();

        List<Long> patientview1IdsMigrated
                = JsonUtil.getMigratedPatientview1IdsByStatus(MigrationStatus.PATIENT_MIGRATED);
        List<Long> patientview1IdsFailed
                = JsonUtil.getMigratedPatientview1IdsByStatus(MigrationStatus.OBSERVATIONS_FAILED);

        Set<Long> idSet = new HashSet<Long>(patientview1IdsMigrated);
        idSet.addAll(patientview1IdsFailed);
        List<Long> idList = new ArrayList<Long>(idSet);

        LOG.info(idList.size() + " PATIENT_MIGRATED or OBSERVATION_FAILED records, updating observations");

        for (Long patientview1Id : idList) {
            MigrationUser migrationUser = new MigrationUser();
            migrationUser.setPatientview1Id(patientview1Id);
            migrationUser.setPatient(true);
            migrationUser.setDeleteExistingTestObservations(true);
            migrationUser.setObservationStartDate(0L);
            migrationUser.setObservationEndDate(new Date().getTime());

            // get pv1 user based on id
            User user = userDao.get(patientview1Id);
            if (user != null) {

                // get all user mappings, ignoring those with no/PATIENT unitcode or no nhs number
                for (UserMapping userMapping : userMappingDao.getAll(user.getUsername())) {
                    if (StringUtils.isNotEmpty(userMapping.getUnitcode())
                            && !userMapping.getUnitcode().equalsIgnoreCase("PATIENT")
                            && StringUtils.isNotEmpty(userMapping.getNhsno())) {

                        String nhsNo = userMapping.getNhsno();
                        String unitcode = userMapping.getUnitcode();

                        for (TestResult testResult : testResultManager.get(nhsNo, unitcode)) {
                            Group group = getGroupByCode(testResult.getUnitcode());

                            if (group != null && StringUtils.isNotEmpty(testResult.getValue())) {
                                FhirObservation observation = new FhirObservation();
                                observation.setValue(testResult.getValue());

                                if (testResult.getDatestamped() != null) {
                                    observation.setApplies(testResult.getDatestamped().getTime());
                                }
                                if (StringUtils.isNotEmpty(testResult.getTestcode())) {
                                    observation.setName(testResult.getTestcode().toLowerCase());
                                }
                                if (StringUtils.isNotEmpty(testResult.getPrepost())) {
                                    observation.setComments(testResult.getPrepost());
                                }

                                observation.setGroup(group);
                                observation.setIdentifier(nhsNo);
                                migrationUser.getObservations().add(observation);
                            }
                        }
                    }
                }

                observationTaskExecutor.submit(new AsyncMigrateObservationTask(migrationUser));
            }
        }
    }

    @Override
    public void bulkObservationCreate(String unitCode1, String unitCode2, Long usersToInsertObservations, Long observationCount) {
        // add observations for all patients previously migrated successfully but without observations

        List<Long> patientview1IdsMigrated = JsonUtil.getMigratedPatientview1IdsByStatus(MigrationStatus.PATIENT_MIGRATED);
        List<Long> patientview1IdsFailed = JsonUtil.getMigratedPatientview1IdsByStatus(MigrationStatus.OBSERVATIONS_FAILED);

        Set<Long> idSet = new HashSet<Long>(patientview1IdsMigrated);
        idSet.addAll(patientview1IdsFailed);
        List<Long> idList = new ArrayList<Long>(idSet);

        LOG.info(idList.size() + " PATIENT_MIGRATED or OBSERVATION_FAILED records, updating "
                + usersToInsertObservations + " with " + observationCount + " observations");

        Long month = 2592000000L;
        Group userUnit1 = adminDataMigrationService.getGroupByCode(unitCode1);
        Group userUnit2 = adminDataMigrationService.getGroupByCode(unitCode2);
        Group group1 = new Group();
        group1.setId(userUnit1.getId());
        Group group2 = new Group();
        group2.setId(userUnit2.getId());

        ExecutorService concurrentTaskExecutor = Executors.newFixedThreadPool(10);

        Long now = new Date().getTime();

        if (usersToInsertObservations > idList.size()) {
            usersToInsertObservations = Long.valueOf(idList.size());
        }

        //for (Long patientview1Id : idSet) {
        for (int i = 0; i < usersToInsertObservations; i++) {
            Long patientview1Id = idList.get(i);
            MigrationUser migrationUser = new MigrationUser();
            migrationUser.setPatientview1Id(patientview1Id);
            migrationUser.setPatient(true);

            for (int j = 0; j < observationCount; j++) {
                FhirObservation observation = new FhirObservation();
                observation.setValue(String.valueOf(j));
                observation.setApplies(new Date(now - (j*month)));
                observation.setComments("comment");

                if (j % 2 == 0) {
                    observation.setName("hb");
                    observation.setGroup(group1);
                } else {
                    observation.setName("wbc");
                    observation.setGroup(group2);
                }

                observation.setIdentifier(patientview1Id.toString());
                migrationUser.getObservations().add(observation);
            }

            migrationUser.setObservationEndDate(now);
            migrationUser.setObservationStartDate(0L);

            // set only 3 months to store of results to store
            //migrationUser.setObservationStartDate(now - (3 * month));

            concurrentTaskExecutor.submit(new AsyncMigrateObservationTask(migrationUser));
        }

        try {
            // wait forever until all threads are finished
            concurrentTaskExecutor.shutdown();
            concurrentTaskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    private Group getGroupByCode(String code) {
        for (Group group : groups) {
            if (group.getCode().equalsIgnoreCase(code)) {
                return group;
            }
        }
        return null;
    }
}
