package org.patientview.migration.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
public class UserDataMigrationServiceImpl implements UserDataMigrationService {

    private static final Logger LOG = LoggerFactory.getLogger(UserDataMigrationServiceImpl.class);
    private static final String COMMENT_RESULT_HEADING = "resultcomment";

    @Inject
    private UserDao userDao;

    @Inject
    private TestResultDao testResultDao;

    @Inject
    private UserMappingDao userMappingDao;

    @Inject
    private AdminDataMigrationService adminDataMigrationService;

    @Inject
    private SpecialtyUserRoleDao specialtyUserRoleDao;

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

    // migrate all user data, not including observations
    public void migrate() throws JsonMigrationException {

        init();

        Role patientRole = adminDataMigrationService.getRoleByName(RoleName.PATIENT);
        Lookup nhsNumberIdentifier = adminDataMigrationService.getLookupByName(IdentifierTypes.NHS_NUMBER.toString());

        ExecutorService concurrentTaskExecutor = Executors.newFixedThreadPool(10);

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
                    concurrentTaskExecutor.submit(new AsyncMigrateUserTask(migrationUser));
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
            }

        }

        try {
            // wait forever until all threads are finished
            concurrentTaskExecutor.shutdown();
            concurrentTaskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    public void bulkUserCreate(String unitCode1, String unitCode2, Long count, RoleName roleName) {
        LOG.info("Starting creation of " + count
                + " generated users, must have -Durl=\"http://localhost:8080/api\" or equivalent");

        ExecutorService concurrentTaskExecutor = Executors.newFixedThreadPool(10);
        Group userUnit1 = adminDataMigrationService.getGroupByCode(unitCode1);
        Group userUnit2 = adminDataMigrationService.getGroupByCode(unitCode2);
        Role userRole = adminDataMigrationService.getRoleByName(roleName);

        List<MigrationUser> migrationUsers = new ArrayList<MigrationUser>();

        if (userUnit1 != null && userUnit2 != null && userRole != null) {
            Date now = new Date();
            LOG.info("Sending " + count + " users to REST service");

            // create users based on Date.now + count increment
            for (Long time = now.getTime(); time<now.getTime() + count; time++) {

                // testing only
                //Long time = 1234L;

                // create user
                User newUser = new User();
                newUser.setForename("sfore" + time.toString());
                newUser.setSurname("ssur");
                newUser.setChangePassword(true);
                newUser.setPassword("pppppp");
                newUser.setLocked(false);
                newUser.setDummy(true);
                newUser.setFailedLogonAttempts(0);
                newUser.setEmail("test" + time.toString() + "@solidstategroup.com");
                newUser.setEmailVerified(false);
                // todo: needs consideration during migration
                newUser.setVerificationCode("emailverify" + time);
                newUser.setUsername(time.toString());
                newUser.setIdentifiers(new HashSet<Identifier>());
                newUser.setLastLogin(now);
                newUser.setDateOfBirth(now);

                // todo: do we need to migrate user.accounthidden?

                // add group role (specialty is added automatically when creating user within a UNIT group)
                Role role = new Role();
                role.setId(userRole.getId());

                Group group1 = new Group();
                group1.setId(userUnit1.getId());
                GroupRole groupRole1 = new GroupRole();
                groupRole1.setGroup(group1);
                groupRole1.setRole(role);

                Group group2 = new Group();
                group2.setId(userUnit2.getId());
                GroupRole groupRole2 = new GroupRole();
                groupRole2.setGroup(group2);
                groupRole2.setRole(role);

                newUser.setGroupRoles(new HashSet<GroupRole>());
                newUser.getGroupRoles().add(groupRole1);
                newUser.getGroupRoles().add(groupRole2);

                // add user feature (usually for staff)
                newUser.setUserFeatures(new HashSet<UserFeature>());
                UserFeature userFeature = new UserFeature();
                Feature feature = adminDataMigrationService.getFeatureByName(FeatureType.MESSAGING.toString());
                userFeature.setFeature(feature);
                newUser.getUserFeatures().add(userFeature);

                if (userRole.getRoleType().getValue().equals(RoleType.PATIENT)) {
                    // add user information (assuming just one SHOULD_KNOW and TALK_ABOUT per user)
                    newUser.setUserInformation(new HashSet<UserInformation>());
                    UserInformation shouldKnow = new UserInformation();
                    shouldKnow.setType(UserInformationTypes.SHOULD_KNOW);
                    shouldKnow.setValue("Should know about me...");
                    newUser.getUserInformation().add(shouldKnow);
                    UserInformation talkAbout = new UserInformation();
                    talkAbout.setType(UserInformationTypes.TALK_ABOUT);
                    talkAbout.setValue("Would like to talk about...");
                    newUser.getUserInformation().add(talkAbout);
                }

                MigrationUser migrationUser = new MigrationUser(newUser);

                // set patientview1 id
                migrationUser.setPatientview1Id(time);

                if (userRole.getRoleType().getValue().equals(RoleType.PATIENT)) {
                    // add comment Observation (comment table in pv1), attach to group (not present in pv1)
                    FhirObservation comment = new FhirObservation();
                    comment.setValue("a patient entered comment about my results");
                    comment.setApplies(new Date(time));
                    comment.setGroup(group1);
                    comment.setComments("a patient entered comment about my results");
                    comment.setName(COMMENT_RESULT_HEADING);
                    comment.setIdentifier(time.toString());
                    migrationUser.getObservations().add(comment);

                    // add Condition / generic diagnosis (pv1 diagnosis table)
                    FhirCondition condition = new FhirCondition();
                    condition.setCategory(DiagnosisTypes.DIAGNOSIS.toString());
                    condition.setCode("Something else 1");
                    condition.setNotes("Something else 1");
                    condition.setGroup(userUnit1);
                    condition.setIdentifier(time.toString());
                    migrationUser.getConditions().add(condition);

                    FhirCondition condition2 = new FhirCondition();
                    condition2.setCategory(DiagnosisTypes.DIAGNOSIS.toString());
                    condition2.setCode("Something else 2");
                    condition2.setNotes("Something else 2");
                    condition2.setGroup(userUnit2);
                    condition2.setIdentifier(time.toString());
                    migrationUser.getConditions().add(condition);

                    // add Condition / EDTA diagnosis (pv1 patient table)
                    FhirCondition conditionEdta = new FhirCondition();
                    conditionEdta.setCategory(DiagnosisTypes.DIAGNOSIS_EDTA.toString());
                    conditionEdta.setCode("00");
                    conditionEdta.setNotes("00");
                    conditionEdta.setGroup(userUnit1);
                    conditionEdta.setIdentifier(time.toString());
                    migrationUser.getConditions().add(conditionEdta);

                    FhirCondition conditionEdta2 = new FhirCondition();
                    conditionEdta2.setCategory(DiagnosisTypes.DIAGNOSIS_EDTA.toString());
                    conditionEdta2.setCode("00");
                    conditionEdta2.setNotes("00");
                    conditionEdta2.setGroup(userUnit2);
                    conditionEdta2.setIdentifier(time.toString());
                    migrationUser.getConditions().add(conditionEdta2);

                    // add Encounter / transplant status (pv1 patient table)
                    FhirEncounter transplant = new FhirEncounter();
                    transplant.setEncounterType(EncounterTypes.TRANSPLANT_STATUS.toString());
                    transplant.setStatus("Live donor transplant");
                    transplant.setIdentifier(time.toString());
                    migrationUser.getEncounters().add(transplant);

                    // add Encounter / treatment  (pv1 patient table)
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
                    medicationStatement.setGroup(userUnit1);
                    medicationStatement.setIdentifier(time.toString());
                    migrationUser.getMedicationStatements().add(medicationStatement);

                    FhirMedicationStatement medicationStatement2 = new FhirMedicationStatement();
                    medicationStatement2.setDose("500g 2");
                    medicationStatement2.setName("Paracetemol 2");
                    medicationStatement2.setStartDate(now);
                    medicationStatement2.setGroup(userUnit2);
                    medicationStatement2.setIdentifier(time.toString());
                    migrationUser.getMedicationStatements().add(medicationStatement2);

                    // add DiagnosticReport and associated Observation (diagnostics, originally IBD now generic)
                    FhirObservation observation = new FhirObservation();
                    observation.setValue("1234567890");
                    observation.setName(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.toString());

                    FhirDiagnosticReport diagnosticReport = new FhirDiagnosticReport();
                    diagnosticReport.setGroup(userUnit1);
                    diagnosticReport.setDate(now);
                    diagnosticReport.setType(DiagnosticReportTypes.IMAGING.toString());
                    diagnosticReport.setName("Photo of patient");
                    diagnosticReport.setResult(observation);
                    diagnosticReport.setIdentifier(time.toString());
                    migrationUser.getDiagnosticReports().add(diagnosticReport);

                    FhirObservation observation2 = new FhirObservation();
                    observation2.setValue("1234567890 2");
                    observation2.setName(DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.toString());

                    FhirDiagnosticReport diagnosticReport2 = new FhirDiagnosticReport();
                    diagnosticReport2.setGroup(userUnit2);
                    diagnosticReport2.setDate(now);
                    diagnosticReport2.setType(DiagnosticReportTypes.IMAGING.toString());
                    diagnosticReport2.setName("Photo of patient 2");
                    diagnosticReport2.setResult(observation2);
                    diagnosticReport2.setIdentifier(time.toString());
                    migrationUser.getDiagnosticReports().add(diagnosticReport2);

                    // add DocumentReference / letter
                    FhirDocumentReference documentReference = new FhirDocumentReference();
                    documentReference.setGroup(userUnit1);
                    documentReference.setDate(now);
                    documentReference.setType(LetterTypes.GENERAL_LETTER.getName());
                    documentReference.setContent("Letter content: text text text " + time + " etc.");
                    documentReference.setIdentifier(time.toString());
                    migrationUser.getDocumentReferences().add(documentReference);

                    FhirDocumentReference documentReference2 = new FhirDocumentReference();
                    documentReference2.setGroup(userUnit2);
                    documentReference2.setDate(now);
                    documentReference2.setType(LetterTypes.GENERAL_LETTER.getName());
                    documentReference2.setContent("Letter content: text text text " + time + " etc. 2");
                    documentReference2.setIdentifier(time.toString());
                    migrationUser.getDocumentReferences().add(documentReference2);

                    // PatientView identifiers
                    // - nhs number (pv1 table)
                    Identifier identifier = new Identifier();
                    identifier.setIdentifier(time.toString());
                    identifier.setIdentifierType(
                            adminDataMigrationService.getLookupByName(IdentifierTypes.NHS_NUMBER.toString()));
                    newUser.getIdentifiers().add(identifier);

                    // - Radar No (pv1 patient table)
                    Identifier radarNo = new Identifier();
                    radarNo.setIdentifier("radar" + time.toString());
                    radarNo.setIdentifierType(
                            adminDataMigrationService.getLookupByName(IdentifierTypes.RADAR_NUMBER.toString()));
                    newUser.getIdentifiers().add(radarNo);

                    // - Hospital No (pv1 patient table)
                /*Identifier hospitalNo = new Identifier();
                hospitalNo.setIdentifier("hospital" + time.toString());
                hospitalNo.setIdentifierType(
                        adminDataMigrationService.getLookupByName(IdentifierTypes.HOSPITAL_NUMBER.toString()));
                newUser.getIdentifiers().add(hospitalNo);*/

                    // add Patient / pv1 patient table data (iterate through pv1 patient)
                    // - basic patient data
                    FhirPatient patient = new FhirPatient();
                    patient.setForename("forename");
                    patient.setSurname("surname");
                    patient.setGender("Male");
                    patient.setAddress1("address1");
                    patient.setAddress2("address2");
                    patient.setAddress3("address3");
                    patient.setAddress4("address4");
                    patient.setPostcode("postcode");
                    patient.setDateOfBirth(now);
                    patient.setIdentifier(time.toString());
                    patient.setGroup(userUnit1);

                    // - patient contact data
                    patient.setContacts(new ArrayList<FhirContact>());
                    FhirContact fhirContact = new FhirContact();
                    fhirContact.setUse("home");
                    fhirContact.setSystem("phone");
                    fhirContact.setValue("01234 56789012");
                    patient.getContacts().add(fhirContact);

                    // - patient identifiers (FHIR identifiers to be stored in FHIR patient record, not patientview identifiers)
                    FhirIdentifier nhsNumber = new FhirIdentifier();
                    nhsNumber.setValue(time.toString());
                    nhsNumber.setLabel(IdentifierTypes.NHS_NUMBER.toString());
                    patient.getIdentifiers().add(nhsNumber);

                    FhirIdentifier hospitalNumber = new FhirIdentifier();
                    hospitalNumber.setValue("hospital" + time.toString());
                    hospitalNumber.setLabel(IdentifierTypes.HOSPITAL_NUMBER.toString());
                    patient.getIdentifiers().add(hospitalNumber);

                /*FhirIdentifier radarNumber = new FhirIdentifier();
                radarNumber.setValue("radar" + time.toString());
                radarNumber.setLabel(IdentifierTypes.RADAR_NUMBER.toString());
                patient.getIdentifiers().add(radarNumber);*/

                    // - practitioner (gp)
                    FhirPractitioner practitioner = new FhirPractitioner();
                    practitioner.setName("gpname");
                    practitioner.setAddress1("gpaddress1");
                    practitioner.setAddress2("gpaddress2");
                    practitioner.setAddress3("gpaddress3");
                    practitioner.setPostcode("gppostcode");
                    practitioner.setContacts(new ArrayList<FhirContact>());

                    // - practitioner contact data
                    FhirContact practitionerContact = new FhirContact();
                    practitionerContact.setUse("work");
                    practitionerContact.setSystem("phone");
                    practitionerContact.setValue("09876 54321098");
                    practitioner.getContacts().add(practitionerContact);
                    patient.setPractitioner(practitioner);

                    migrationUser.getPatients().add(patient);

                    // second group patient data (less data)
                    // - basic patient data
                    FhirPatient patient2 = new FhirPatient();
                    patient2.setForename("forename 2");
                    patient2.setSurname("surname 2");
                    patient2.setGender("Male 2");
                    patient2.setAddress1("address1 2");
                    patient2.setAddress2("address2 2");
                    patient2.setAddress3("address3 2");
                    patient2.setAddress4("address4 2");
                    patient2.setPostcode("postcode 2");
                    patient2.setDateOfBirth(now);
                    patient2.setIdentifier(time.toString());
                    patient2.setGroup(userUnit2);

                    // - patient contact data
                    patient2.setContacts(new ArrayList<FhirContact>());
                    FhirContact fhirContact2 = new FhirContact();
                    fhirContact2.setUse("home");
                    fhirContact2.setSystem("phone");
                    fhirContact2.setValue("01234 56789012 2");
                    patient2.getContacts().add(fhirContact2);

                    // - patient identifiers (FHIR identifiers to be stored in FHIR patient record, not patientview identifiers)
                    FhirIdentifier nhsNumber2 = new FhirIdentifier();
                    nhsNumber2.setValue(time.toString());
                    nhsNumber2.setLabel(IdentifierTypes.NHS_NUMBER.toString());
                    patient.getIdentifiers().add(nhsNumber);

                    migrationUser.getPatients().add(patient2);

                    // Non test observation types
                    // foot checkup
                    FhirObservation footCheckup1 = new FhirObservation();
                    footCheckup1.setBodySite(BodySites.LEFT_FOOT.toString());
                    footCheckup1.setIdentifier(time.toString());
                    footCheckup1.setName(NonTestObservationTypes.DPPULSE.toString());
                    footCheckup1.setValue("O/E - L.dorsalis pedis absent");
                    footCheckup1.setGroup(group1);
                    footCheckup1.setApplies(new Date(time));
                    migrationUser.getObservations().add(footCheckup1);

                    // eye checkup
                    FhirObservation eyeCheckup1 = new FhirObservation();
                    eyeCheckup1.setBodySite(BodySites.LEFT_EYE.toString());
                    eyeCheckup1.setIdentifier(time.toString());
                    eyeCheckup1.setName(NonTestObservationTypes.MGRADE.toString());
                    eyeCheckup1.setValue("Mx");
                    eyeCheckup1.setGroup(group1);
                    eyeCheckup1.setApplies(new Date(time));
                    migrationUser.getObservations().add(eyeCheckup1);

                    // blood group
                    FhirObservation bloodGroup = new FhirObservation();
                    bloodGroup.setIdentifier(time.toString());
                    bloodGroup.setName(NonTestObservationTypes.BLOOD_GROUP.toString());
                    bloodGroup.setValue("A");
                    bloodGroup.setGroup(group1);
                    bloodGroup.setApplies(new Date(time));
                    migrationUser.getObservations().add(bloodGroup);

                    // set to a patient user
                    migrationUser.setPatient(true);
                } else {
                    // set to a staff user
                    migrationUser.setPatient(false);
                }

                migrationUsers.add(migrationUser);

                // add task and run
                concurrentTaskExecutor.submit(new AsyncMigrateUserTask(migrationUser));
            }

            try {
                // wait forever until all threads are finished
                concurrentTaskExecutor.shutdown();
                concurrentTaskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }

        } else {
            LOG.error("unitcode1: " + unitCode1 + " or unitcode2: " + unitCode1 + ", or role: " + roleName + " do not exist");
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



        newUser.setLastLogin(user.getLastlogon());
        if (StringUtils.isNotEmpty(user.getDateofbirthFormatted())) {
            newUser.setDateOfBirth(new Date(user.getDateofbirthFormatted()));
        }

        newUser.setIdentifiers(new HashSet<Identifier>());
        newUser.setGroupRoles(new HashSet<GroupRole>());
        newUser.setUserFeatures(new HashSet<UserFeature>());

        return newUser;

        // create user
       /* User newUser = new User();
        newUser.setForename("sfore" + time.toString());
        newUser.setSurname("ssur");
        newUser.setChangePassword(true);
        newUser.setPassword("pppppp");
        newUser.setLocked(false);
        newUser.setDummy(true);
        newUser.setFailedLogonAttempts(0);
        newUser.setEmail("test" + time.toString() + "@solidstategroup.com");
        newUser.setEmailVerified(false);
        // todo: needs consideration during migration
        newUser.setVerificationCode("emailverify" + time);
        newUser.setUsername(time.toString());
        newUser.setIdentifiers(new HashSet<Identifier>());
        newUser.setLastLogin(now);
        newUser.setDateOfBirth(now);*/
    }
}
