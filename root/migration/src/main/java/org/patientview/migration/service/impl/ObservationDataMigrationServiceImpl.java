package org.patientview.migration.service.impl;

import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.service.ObservationDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.MigrationStatus;
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

    private List<Group> groups;
    private List<Role> roles;
    private List<Lookup> lookups;
    private List<Feature> features;

    private @Value("${migration.username}") String migrationUsername;
    private @Value("${migration.password}") String migrationPassword;
    private @Value("${patientview.api.url}") String patientviewApiUrl;

    private void init() throws JsonMigrationException {
        try {
            JsonUtil.setPatientviewApiUrl(patientviewApiUrl);
            JsonUtil.token = JsonUtil.authenticate(migrationUsername, migrationPassword);
            lookups = JsonUtil.getStaticDataLookups(JsonUtil.pvUrl + "/lookup");
            features = JsonUtil.getStaticDataFeatures(JsonUtil.pvUrl + "/feature");
            roles = JsonUtil.getRoles(JsonUtil.pvUrl + "/role");
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

        List<Long> patientview1IdsMigrated = JsonUtil.getMigratedPatientview1IdsByStatus(MigrationStatus.PATIENT_MIGRATED);
        List<Long> patientview1IdsFailed = JsonUtil.getMigratedPatientview1IdsByStatus(MigrationStatus.OBSERVATIONS_FAILED);

        Set<Long> idSet = new HashSet<Long>(patientview1IdsMigrated);
        idSet.addAll(patientview1IdsFailed);
        List<Long> idList = new ArrayList<Long>(idSet);

        LOG.info(idList.size() + " PATIENT_MIGRATED or OBSERVATION_FAILED records, updating observations");
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
}
