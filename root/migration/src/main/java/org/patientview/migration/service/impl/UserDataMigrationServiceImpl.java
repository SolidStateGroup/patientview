package org.patientview.migration.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPut;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.service.AsyncService;
import org.patientview.migration.service.UserDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.patientview.model.SpecialtyUserRole;
import org.patientview.patientview.model.UserMapping;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.persistence.model.FhirDocumentReference;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirMedicationStatement;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.DiagnosticReportTypes;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LetterTypes;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.patientview.persistence.model.enums.RoleName;
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
public class UserDataMigrationServiceImpl implements UserDataMigrationService {

    private static final Logger LOG = LoggerFactory.getLogger(UserDataMigrationServiceImpl.class);

    @Inject
    private UserDao userDao;

    @Inject
    private TestResultDao testResultDao;

    @Inject
    private UserMappingDao userMappingDao;

    @Inject
    private AdminDataMigrationService adminDataMigrationService;

    @Inject
    private AsyncService asyncService;

    @Inject
    private SpecialtyUserRoleDao specialtyUserRoleDao;

    public void migrate() {

        Role patientRole = adminDataMigrationService.getRoleByName(RoleName.PATIENT);
        Lookup nhsNumberIdentifier = adminDataMigrationService.getLookupByName(IdentifierTypes.NHS_NUMBER.toString());

        for (org.patientview.patientview.model.User oldUser : userDao.getAll()) {
            Set<String> identifiers = new HashSet<String>();

            // basic user information
            User newUser = createUser(oldUser);
            boolean isPatient = false;

            for (UserMapping userMapping : userMappingDao.getAll(oldUser.getUsername())) {
                if (!userMapping.getUnitcode().equalsIgnoreCase("PATIENT") && newUser != null) {

                    // assume usermapping with nhsnumber is a patient
                    if (StringUtils.isNotEmpty(userMapping.getNhsno())) {

                        // is a patient
                        identifiers.add(userMapping.getNhsno());

                        // add group (specialty is added automatically when creating user within a UNIT group)
                        Group group = adminDataMigrationService.getGroupByCode(userMapping.getUnitcode());

                        if (group != null && patientRole != null) {
                            GroupRole groupRole = new GroupRole();
                            groupRole.setGroup(group);
                            groupRole.setRole(patientRole);
                            newUser.getGroupRoles().add(groupRole);
                        }
                    } else {

                        // is a staff member
                        Role role = null;
                        List<SpecialtyUserRole> specialtyUserRoles = specialtyUserRoleDao.get(oldUser);
                        // TODO: try and fix this - get the first role and apply it to all group (no group role mapping)
                        // TODO: required hack from original PatientView
                        if (CollectionUtils.isNotEmpty(specialtyUserRoles)) {
                            String roleName = specialtyUserRoles.get(0).getRole();

                            if (roleName.equals("unitadmin")) {
                                role = adminDataMigrationService.getRoleByName(RoleName.UNIT_ADMIN);
                            } else if (roleName.equals("unitstaff")) {
                                role = adminDataMigrationService.getRoleByName(RoleName.STAFF_ADMIN);
                            }

                            // add group (specialty is added automatically when creating user within a UNIT group)
                            Group group = adminDataMigrationService.getGroupByCode(userMapping.getUnitcode());

                            if (group != null && role != null) {
                                GroupRole groupRole = new GroupRole();
                                groupRole.setGroup(group);
                                groupRole.setRole(role);
                                newUser.getGroupRoles().add(groupRole);
                            }
                        }
                    }
                }
            }

            if (newUser != null) {
                // identifiers (will only be for patient)
                for (String identifierText : identifiers) {
                    Identifier identifier = new Identifier();
                    identifier.setIdentifier(identifierText);
                    identifier.setIdentifierType(nhsNumberIdentifier);
                    newUser.getIdentifiers().add(identifier);
                }

                // features
                if (oldUser.isIsrecipient()) {
                    Feature feature = adminDataMigrationService.getFeatureByName(FeatureType.MESSAGING.toString());
                    if (feature != null) {
                        newUser.getUserFeatures().add(new UserFeature(feature));
                    }
                }
                if (oldUser.isFeedbackRecipient()) {
                    Feature feature = adminDataMigrationService.getFeatureByName(FeatureType.MESSAGING.toString());
                    if (feature != null) {
                        newUser.getUserFeatures().add(new UserFeature(feature));
                    }
                }

                // convert to transport object
                MigrationUser migrationUser = new MigrationUser(newUser);
                migrationUser.setPatient(isPatient);

                // call REST to store migrated patient
                try {
                    asyncService.callApiMigrateUser(migrationUser);
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }

    public void bulkUserCreate(String unitCode, Long count, RoleName roleName, Long observationCount,
                               String observationName) {
        LOG.info("Starting creation of " + count
                + " generated users, must have -Durl=\"http://localhost:8080/api\" or equivalent");

        ExecutorService concurrentTaskExecutor = Executors.newFixedThreadPool(20);
        Group userUnit = adminDataMigrationService.getGroupByCode(unitCode);
        Role userRole = adminDataMigrationService.getRoleByName(roleName);

        if (userUnit != null && userRole != null) {
            Date now = new Date();

            // create users based on Date.now + count increment
            for (Long time = now.getTime(); time<now.getTime() + count; time++) {

                // create user
                User newUser = new User();
                newUser.setForename("test" + time.toString());
                newUser.setSurname("test");
                newUser.setChangePassword(true);
                newUser.setPassword("pppppp");
                newUser.setLocked(false);
                newUser.setDummy(true);
                newUser.setFailedLogonAttempts(0);
                newUser.setEmail("test" + time.toString() + "@solidstategroup.com");
                newUser.setEmailVerified(false);
                newUser.setUsername(time.toString());
                newUser.setIdentifiers(new HashSet<Identifier>());

                // if role is RoleName.PATIENT add identifier
                if (roleName.equals(RoleName.PATIENT)) {
                    Identifier identifier = new Identifier();
                    identifier.setIdentifier(time.toString());
                    identifier.setIdentifierType(adminDataMigrationService.getLookupByName("NHS_NUMBER"));
                    newUser.getIdentifiers().add(identifier);
                }

                // add group role (specialty is added automatically when creating user within a UNIT group)
                Group group = new Group();
                group.setId(userUnit.getId());
                Role role = new Role();
                role.setId(userRole.getId());
                GroupRole groupRole = new GroupRole();
                groupRole.setGroup(group);
                groupRole.setRole(role);
                newUser.setGroupRoles(new HashSet<GroupRole>());
                newUser.getGroupRoles().add(groupRole);

                // add user feature (usually for staff)
                newUser.setUserFeatures(new HashSet<UserFeature>());
                UserFeature userFeature = new UserFeature();
                Feature feature = adminDataMigrationService.getFeatureByName(FeatureType.MESSAGING.toString());
                userFeature.setFeature(feature);
                newUser.getUserFeatures().add(userFeature);

                MigrationUser migrationUser = new MigrationUser(newUser);

                // add Observations / results
                for (int j = 0; j < observationCount; j++) {
                    FhirObservation observation = new FhirObservation();
                    observation.setValue(String.valueOf(time + j));
                    observation.setApplies(new Date(time + j));
                    observation.setGroup(group);
                    observation.setComparator(">");
                    observation.setComments("comment");
                    observation.setName(observationName);
                    observation.setIdentifier(time.toString());
                    migrationUser.getObservations().add(observation);
                }

                // add Condition / generic diagnosis
                FhirCondition condition = new FhirCondition();
                condition.setCategory(DiagnosisTypes.DIAGNOSIS.toString());
                condition.setCode("Something else");
                condition.setNotes("Something else");
                condition.setGroup(userUnit);
                condition.setIdentifier(time.toString());
                migrationUser.getConditions().add(condition);

                // add Condition / EDTA diagnosis
                FhirCondition conditionEdta = new FhirCondition();
                conditionEdta.setCategory(DiagnosisTypes.DIAGNOSIS_EDTA.toString());
                conditionEdta.setCode("00");
                conditionEdta.setNotes("00");
                conditionEdta.setGroup(userUnit);
                conditionEdta.setIdentifier(time.toString());
                migrationUser.getConditions().add(conditionEdta);

                // add Encounter / transplant status
                FhirEncounter transplant = new FhirEncounter();
                transplant.setEncounterType(EncounterTypes.TRANSPLANT_STATUS.toString());
                transplant.setStatus("Live donor transplant");
                transplant.setIdentifier(time.toString());
                migrationUser.getEncounters().add(transplant);

                // add Encounter / treatment
                FhirEncounter treatment = new FhirEncounter();
                treatment.setEncounterType(EncounterTypes.TREATMENT.toString());
                treatment.setStatus("TP");
                treatment.setIdentifier(time.toString());
                migrationUser.getEncounters().add(treatment);

                // add MedicationStatement
                FhirMedicationStatement medicationStatement = new FhirMedicationStatement();
                medicationStatement.setDose("500g");
                medicationStatement.setName("Paracetemol");
                medicationStatement.setStartDate(now);
                medicationStatement.setGroup(userUnit);
                medicationStatement.setIdentifier(time.toString());
                migrationUser.getMedicationStatements().add(medicationStatement);

                // add DiagnosticReport and associated Observation (diagnostics, originally IBD now generic)
                FhirObservation observation = new FhirObservation();
                observation.setValue("1234567890");
                observation.setName(NonTestObservationTypes.DIAGNOSTIC_RESULT.toString());

                FhirDiagnosticReport diagnosticReport = new FhirDiagnosticReport();
                diagnosticReport.setGroup(userUnit);
                diagnosticReport.setDate(now);
                diagnosticReport.setType(DiagnosticReportTypes.IMAGING.toString());
                diagnosticReport.setName("Photo of patient");
                diagnosticReport.setResult(observation);
                diagnosticReport.setIdentifier(time.toString());
                migrationUser.getDiagnosticReports().add(diagnosticReport);

                // add DocumentReference / letter
                FhirDocumentReference documentReference = new FhirDocumentReference();
                documentReference.setGroup(userUnit);
                documentReference.setDate(now);
                documentReference.setType(LetterTypes.GENERAL_LETTER.getName());
                documentReference.setContent("Letter content: " + time + " etc.");
                documentReference.setIdentifier(time.toString());
                migrationUser.getDocumentReferences().add(documentReference);

                // set to a patient user
                migrationUser.setPatient(true);

                // add task and run
                concurrentTaskExecutor.submit(new AsyncMigrateUserTask(migrationUser));
            }

            LOG.info("Sending " + count + " to REST service");

            try {
                // wait forever until all threads are finished
                concurrentTaskExecutor.shutdown();
                concurrentTaskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }

            LOG.info("Submitted " + count);
        }
    }

    public User createUser(org.patientview.patientview.model.User user) {
        User newUser = new User();
        newUser.setForename(user.getFirstName());
        newUser.setSurname(user.getLastName());
        newUser.setChangePassword(user.isFirstlogon());
        newUser.setPassword(user.getPassword());
        newUser.setLocked(user.isAccountlocked());
        newUser.setDummy(user.isDummypatient());
        newUser.setFailedLogonAttempts(user.getFailedlogons());

        if (StringUtils.isEmpty(user.getEmail())) {
            newUser.setEmail("unknown@patientview.org");
        } else{
            newUser.setEmail(user.getEmail());
        }
        newUser.setUsername(user.getUsername());
        newUser.setEmailVerified(user.isEmailverified());

        newUser.setIdentifiers(new HashSet<Identifier>());
        newUser.setGroupRoles(new HashSet<GroupRole>());
        newUser.setUserFeatures(new HashSet<UserFeature>());

        return newUser;
    }

    // deprecated as specialty added automatically for child groups during user creation
    public List<Group> getUserSpecialty(org.patientview.patientview.model.User oldUser) {

        List<Group> groups = new ArrayList<Group>();

        for (SpecialtyUserRole specialtyUserRole : specialtyUserRoleDao.get(oldUser)) {

            if (specialtyUserRole.getSpecialty().getContext().equalsIgnoreCase("ibd")) {
                groups.add(adminDataMigrationService.getGroupByName("idb"));
            }

            if (specialtyUserRole.getSpecialty().getContext().equalsIgnoreCase("renal")) {
                groups.add(adminDataMigrationService.getGroupByName("renal"));
            }

            if (specialtyUserRole.getSpecialty().getContext().equalsIgnoreCase("diabetes")) {
                groups.add(adminDataMigrationService.getGroupByName("diabetes"));
            }
        }

        return groups;
    }

    // deprecated as specialty added automatically for child groups during user creation
    private void addSpecialty(org.patientview.patientview.model.User user, Long userId) {

        List<Group> groups = getUserSpecialty(user);
        for (Group group : groups) {
            Role role = adminDataMigrationService.getRoleByName(RoleName.PATIENT);
            if (userId != null && group != null && role != null) {
                callApiAddGroupRole(userId, group.getId(), role.getId());
            }
        }
    }

    // deprecated as specialty added automatically for child groups during user creation
    private void callApiAddGroupRole(Long userId, Long groupId, Long roleId) {
        String url = JsonUtil.pvUrl + "/user/" + userId + "/group/" + groupId + "/role/" + roleId;
        try {
            JsonUtil.jsonRequest(url, GroupRole.class, null, HttpPut.class, true);
            LOG.info("Created group and role for user");
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to add user to group");
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to add user to group");
        } catch (Exception e) {
            LOG.error("Unable to add group role");
            e.printStackTrace();
        }
    }
}
