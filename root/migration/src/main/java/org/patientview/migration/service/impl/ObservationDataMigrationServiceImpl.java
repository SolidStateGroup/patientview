package org.patientview.migration.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.service.AsyncService;
import org.patientview.migration.service.ObservationDataMigrationService;
import org.patientview.migration.service.UserDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.patientview.model.SpecialtyUserRole;
import org.patientview.patientview.model.UserMapping;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirContact;
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.persistence.model.FhirDocumentReference;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirIdentifier;
import org.patientview.persistence.model.FhirMedicationStatement;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.BodySites;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.DiagnosticReportObservationTypes;
import org.patientview.persistence.model.enums.DiagnosticReportTypes;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LetterTypes;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.model.enums.UserInformationTypes;
import org.patientview.repository.SpecialtyUserRoleDao;
import org.patientview.repository.TestResultDao;
import org.patientview.repository.UserDao;
import org.patientview.repository.UserMappingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
