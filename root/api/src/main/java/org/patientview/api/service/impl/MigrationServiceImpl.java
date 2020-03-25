package org.patientview.api.service.impl;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.service.GroupRoleService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.IdentifierService;
import org.patientview.api.service.LetterService;
import org.patientview.api.service.LookupService;
import org.patientview.api.service.MigrationService;
import org.patientview.api.service.UserMigrationService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.MigrationException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirAllergy;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirContact;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirDatabaseObservation;
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.persistence.model.FhirDocumentReference;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirIdentifier;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserMigration;
import org.patientview.persistence.model.enums.DiagnosticReportObservationTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.QuestionOptionRepository;
import org.patientview.persistence.repository.QuestionRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.AllergyService;
import org.patientview.service.ConditionService;
import org.patientview.service.DiagnosticService;
import org.patientview.service.EncounterService;
import org.patientview.service.MedicationService;
import org.patientview.service.ObservationService;
import org.patientview.service.PatientService;
import org.patientview.service.PractitionerService;
import org.patientview.util.Util;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/11/2014.
 *
 */
@Service
public class MigrationServiceImpl extends AbstractServiceImpl<MigrationServiceImpl> implements MigrationService {

    @Inject
    private AllergyService allergyService;

    @Inject
    private ConditionService conditionService;

    @Inject
    @Named("patientView1")
    private DataSource dataSource;

    @Inject
    private DiagnosticService diagnosticService;

    @Inject
    private EncounterService encounterService;

    @Inject
    private GroupService groupService;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupRoleService groupRoleService;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private IdentifierService identifierService;

    @Inject
    private LetterService letterService;

    @Inject
    private LookupService lookupService;

    @Inject
    private MedicationService medicationService;

    @Inject
    private ObservationHeadingRepository observationHeadingRepository;

    @Inject
    private ObservationService observationService;

    @Inject
    private PatientService patientService;

    @Inject
    private PractitionerService practitionerService;

    @Inject
    private QuestionOptionRepository questionOptionRepository;

    @Inject
    private QuestionRepository questionRepository;

    @Inject
    private SurveyRepository surveyRepository;

    @Inject
    private SurveyResponseRepository surveyResponseRepository;

    @Inject
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Inject
    private UserMigrationService userMigrationService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    private HashMap<String, ObservationHeading> observationHeadingMap;

    private static final String COMMENT_RESULT_HEADING = "resultcomment";
    private static final boolean DELETE_EXISTING = false;
    private static final int FOUR = 4;
    private static final String GENDER_CODING = "gender";
    private static final int THREE = 3;

    // add FHIR Patient with optional practitioner reference, used during migration
    private FhirLink addPatient(FhirPatient fhirPatient, User entityUser, List<UUID> practitionerUuids,
                                Set<FhirLink> fhirLinks)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        Patient patient = new Patient();

        // name
        if (StringUtils.isNotEmpty(fhirPatient.getForename()) || StringUtils.isNotEmpty(fhirPatient.getSurname())) {
            HumanName humanName = patient.addName();
            if (StringUtils.isNotEmpty(fhirPatient.getSurname())) {
                humanName.addFamilySimple(fhirPatient.getSurname());
            }

            if (StringUtils.isNotEmpty(fhirPatient.getForename())) {
                humanName.addGivenSimple(fhirPatient.getForename());
            }

            Enumeration<HumanName.NameUse> nameUse = new Enumeration<>(HumanName.NameUse.usual);
            humanName.setUse(nameUse);
        }

        // date of birth
        if (fhirPatient.getDateOfBirth() != null) {
            patient.setBirthDateSimple(new DateAndTime(fhirPatient.getDateOfBirth()));
        }

        // gender
        if (fhirPatient.getGender() != null) {
            CodeableConcept gender = new CodeableConcept();
            gender.setTextSimple(fhirPatient.getGender());
            gender.addCoding().setDisplaySimple(GENDER_CODING);
            patient.setGender(gender);
        }

        // address
        if (StringUtils.isNotEmpty(fhirPatient.getAddress1())
                || StringUtils.isNotEmpty(fhirPatient.getAddress2())
                || StringUtils.isNotEmpty(fhirPatient.getAddress3())
                || StringUtils.isNotEmpty(fhirPatient.getAddress4())
                || StringUtils.isNotEmpty(fhirPatient.getPostcode())) {

            Address address = patient.addAddress();

            if (StringUtils.isNotEmpty(fhirPatient.getAddress1())) {
                address.addLineSimple(fhirPatient.getAddress1());
            }
            if (StringUtils.isNotEmpty(fhirPatient.getAddress2())) {
                address.setCitySimple(fhirPatient.getAddress2());
            }
            if (StringUtils.isNotEmpty(fhirPatient.getAddress3())) {
                address.setStateSimple(fhirPatient.getAddress3());
            }
            if (StringUtils.isNotEmpty(fhirPatient.getAddress4())) {
                address.setCountrySimple(fhirPatient.getAddress4());
            }
            if (StringUtils.isNotEmpty(fhirPatient.getPostcode())) {
                address.setZipSimple(fhirPatient.getPostcode());
            }
        }

        // contact details
        if (!fhirPatient.getContacts().isEmpty()) {
            Patient.ContactComponent contactComponent = patient.addContact();
            for (FhirContact fhirContact : fhirPatient.getContacts()) {
                Contact contact = contactComponent.addTelecom();
                if (StringUtils.isNotEmpty(fhirContact.getSystem())) {
                    contact.setSystem(new Enumeration<>(Contact.ContactSystem.valueOf(fhirContact.getSystem())));
                }
                if (StringUtils.isNotEmpty(fhirContact.getValue())) {
                    contact.setValueSimple(fhirContact.getValue());
                }
                if (StringUtils.isNotEmpty(fhirContact.getUse())) {
                    contact.setUse(new Enumeration<>(Contact.ContactUse.valueOf(fhirContact.getUse())));
                }
            }
        }

        // identifiers (note: FHIR identifiers attached to FHIR patient not PatientView Identifiers)
        for (FhirIdentifier fhirIdentifier : fhirPatient.getIdentifiers()) {
            org.hl7.fhir.instance.model.Identifier identifier = patient.addIdentifier();
            if (StringUtils.isNotEmpty(fhirIdentifier.getLabel())) {
                identifier.setLabelSimple(fhirIdentifier.getLabel());
            }
            if (StringUtils.isNotEmpty(fhirIdentifier.getValue())) {
                identifier.setValueSimple(fhirIdentifier.getValue());
            }
        }

        // care providers (practitioner)
        if (!practitionerUuids.isEmpty()) {
            for (UUID practitionerUuid : practitionerUuids) {
                ResourceReference careProvider = patient.addCareProvider();
                careProvider.setReferenceSimple("uuid");
                careProvider.setDisplaySimple(practitionerUuid.toString());
            }
        }

        //JSONObject createdPatient = fhirResource.create(patient);
        FhirDatabaseEntity createdPatient = fhirResource.createEntity(patient, ResourceType.Patient.name(), "patient");

        if (getFhirLink(fhirPatient.getGroup(), fhirPatient.getIdentifier(), fhirLinks) == null) {

            // create new FhirLink and add to list of known fhirlinks if doesn't already exist, including identifier
            Identifier identifier;

            try {
                // should only ever return 1
                List<Identifier> identifiers = identifierService.getIdentifierByValue(fhirPatient.getIdentifier());
                identifier = identifiers.get(0);
            } catch (ResourceNotFoundException e) {
                User currentUser = getCurrentUser();

                identifier = new Identifier();
                identifier.setIdentifier(fhirPatient.getIdentifier());
                identifier.setCreator(currentUser);
                identifier.setUser(entityUser);
                identifier.setIdentifierType(
                        lookupService.findByTypeAndValue(LookupTypes.IDENTIFIER,
                                CommonUtils.getIdentifierType(fhirPatient.getIdentifier()).toString()));

                identifier.setCreator(currentUser);
                identifier.setUser(entityUser);
                identifier = identifierRepository.save(identifier);
            }

            FhirLink fhirLink = new FhirLink();
            fhirLink.setUser(entityUser);
            fhirLink.setIdentifier(identifier);
            fhirLink.setGroup(fhirPatient.getGroup());
            fhirLink.setResourceId(createdPatient.getLogicalId());
            fhirLink.setVersionId(createdPatient.getVersionId());
            fhirLink.setResourceType(ResourceType.Patient.name());
            fhirLink.setActive(true);
            return fhirLinkRepository.save(fhirLink);
        } else {
            return null;
        }
    }

    private FhirDatabaseObservation buildFhirDatabaseNonTestObservation(
            FhirObservation fhirObservation, FhirLink fhirLink)
            throws ResourceNotFoundException, FhirResourceException {

        // build actual FHIR observation and set subject
        Observation observation = observationService.buildNonTestObservation(fhirObservation);
        observation.setSubject(Util.createResourceReference(fhirLink.getResourceId()));

        // return new FhirDatabaseObservation with correct JSON content
        try {
            return new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation));
        } catch (NullArgumentException nae) {
            throw new FhirResourceException(nae.getMessage());
        }
    }

    private FhirDatabaseObservation buildFhirDatabaseObservation(FhirObservation fhirObservation,
                                                         ObservationHeading observationHeading,
                                                         FhirLink fhirLink)
            throws ResourceNotFoundException, FhirResourceException {

        // build actual FHIR observation and set subject
        DateTime dateTime = new DateTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fhirObservation.getApplies());
        DateAndTime dateAndTime = new DateAndTime(calendar);
        dateTime.setValue(dateAndTime);

        Observation observation = observationService.buildObservation(dateTime,
                fhirObservation.getValue(), fhirObservation.getComparator(), fhirObservation.getComments(),
                observationHeading, true);

        observation.setSubject(Util.createResourceReference(fhirLink.getResourceId()));

        // return new FhirDatabaseObservation with correct JSON content
        try {
            return new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation));
        } catch (NullArgumentException nae) {
            throw new FhirResourceException(nae.getMessage());
        }
    }

    // used as backup if missing data in pv1 patient table
    private FhirLink createPatientAndFhirLink(User user, Group group, Identifier identifier)
            throws ResourceNotFoundException, FhirResourceException {

        FhirDatabaseEntity fhirPatient = fhirResource.createEntity(
                patientService.buildPatient(user, identifier), ResourceType.Patient.name(), "patient");

        // create new FhirLink and add to list of known fhirlinks
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(user);
        fhirLink.setIdentifier(identifier);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(fhirPatient.getLogicalId());
        fhirLink.setVersionId(fhirPatient.getVersionId());
        fhirLink.setResourceType(ResourceType.Patient.name());
        fhirLink.setActive(true);
        return fhirLinkRepository.save(fhirLink);
    }

    // delete all FHIR related Observation data for this patient (not other patient data), non test data only
    private void deleteExistingNonTestObservationData(Set<FhirLink> fhirLinks)
            throws FhirResourceException {

        if (fhirLinks != null) {
            for (FhirLink fhirLink : fhirLinks) {
                UUID subjectId = fhirLink.getResourceId();

                // only delete non test observation types
                List<String> nonTestObservationTypes = new ArrayList<>();
                for (NonTestObservationTypes observationType : NonTestObservationTypes.class.getEnumConstants()) {
                    nonTestObservationTypes.add(observationType.toString());
                }

                observationService.deleteObservations(fhirResource.getLogicalIdsBySubjectIdAndNames(
                        "observation", subjectId, nonTestObservationTypes));
            }
        }
    }

    // delete all FHIR related Observation data for this patient (not other patient data), tests only
    private void deleteExistingTestObservationData(MigrationUser migrationUser, Set<FhirLink> fhirLinks)
            throws FhirResourceException {

        if (fhirLinks != null) {
            for (FhirLink fhirLink : fhirLinks) {
                UUID subjectId = fhirLink.getResourceId();

                // do not delete non test observation types
                List<String> nonTestObservationTypes = new ArrayList<>();
                for (NonTestObservationTypes observationType : NonTestObservationTypes.class.getEnumConstants()) {
                    nonTestObservationTypes.add(observationType.toString());
                }

                observationService.deleteObservations(fhirResource.getLogicalIdsBySubjectIdAppliesIgnoreNames(
                        "observation", subjectId, nonTestObservationTypes, migrationUser.getObservationStartDate(),
                        migrationUser.getObservationEndDate()));
            }
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }

    private FhirLink getFhirLink(Group group, String identifierText, Set<FhirLink> fhirLinks) {
        for (FhirLink fhirLink : fhirLinks) {
            if (fhirLink.getGroup().equals(group) && fhirLink.getIdentifier().getIdentifier().equals(identifierText)) {
                return fhirLink;
            }
        }
        return null;
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
                    migrateTestObservations(pv2UserId, migrationUser);

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
                        + ApiUtil.getDateDiff(start, end, TimeUnit.SECONDS) + " seconds.");
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
                for (ObservationHeading observationHeading : observationHeadingRepository.findAll()) {
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

                                        fhirDatabaseObservations.add(buildFhirDatabaseObservation(
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

                                        fhirDatabaseObservations.add(buildFhirDatabaseObservation(
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

    @Override
    public Long migrateUser(MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException,
            MigrationException, FhirResourceException {

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
                migratePatientData(userId, migrationUser);
                doneMessage = userId + " Done, migrated patient data";

                userMigration.setStatus(MigrationStatus.PATIENT_MIGRATED);
                userMigration.setInformation(null);
                userMigration.setLastUpdate(new Date());
                userMigrationService.save(userMigration);
            } catch (Exception e) {
                LOG.error("Could not migrate patient data: {}", e);
                try {
                    // clean up any data created during failed migration
                    patientService.deleteExistingPatientData(userRepository.findById(userId).get().getFhirLinks());
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
        LOG.info(doneMessage + ", took " + ApiUtil.getDateDiff(start, end, TimeUnit.SECONDS) + " seconds.");
        return userId;
    }

    @Override
    public Long migrateUserExisting(MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, MigrationException {
        Date start = new Date();
        UserMigration userMigration = null;

        // get User object from MigrationUser (not patient data)
        User user = migrationUser.getUser();

        org.patientview.api.model.User pv2User = userService.getByUsername(user.getUsername());

        userMigration = userMigrationService.getByPatientview2Id(pv2User.getId());

        if (userMigration == null) {
            userMigration = new UserMigration(migrationUser.getPatientview1Id(), MigrationStatus.USER_STARTED);
        } else {
            userMigration.setPatientview1UserId(migrationUser.getPatientview1Id());
            userMigration.setStatus(MigrationStatus.USER_STARTED);
        }

        userMigration.setInformation(null);
        userMigration.setCreator(getCurrentUser());
        userMigration.setLastUpdater(getCurrentUser());
        userMigration.setLastUpdate(new Date());
        userMigration = userMigrationService.save(userMigration);

        if (pv2User == null) {
            userMigration.setStatus(MigrationStatus.USER_FAILED);
            userMigration.setInformation("Existing user's username does not exist");
            userMigration.setLastUpdate(new Date());
            userMigrationService.save(userMigration);
            throw new MigrationException("Existing user's username does not exist");
        }

        userMigration.setStatus(MigrationStatus.USER_MIGRATED);
        userMigration.setInformation(null);
        userMigration.setPatientview2UserId(pv2User.getId());
        userMigration.setLastUpdate(new Date());
        userMigrationService.save(userMigration);

        // have successfully found existing patient in pv2 based on username from MigrationUser, now migrate across
        // any group specific patient data, letters etc
        String doneMessage;

        // migrate patient related data
        if (migrationUser.isPatient()) {
            try {
                userMigration.setStatus(MigrationStatus.PATIENT_STARTED);
                userMigration.setInformation(null);
                userMigration.setLastUpdate(new Date());
                userMigrationService.save(userMigration);

                LOG.info("{} migrating patient data", pv2User.getId());
                migratePatientData(pv2User.getId(), migrationUser);
                doneMessage = pv2User.getId() + " Done, migrated patient data";

                userMigration.setStatus(MigrationStatus.PATIENT_MIGRATED);
                userMigration.setInformation(null);
                userMigration.setLastUpdate(new Date());
                userMigrationService.save(userMigration);
            } catch (Exception e) {
                LOG.error("Could not migrate patient data: {}", e);
                userMigration.setStatus(MigrationStatus.PATIENT_FAILED);
                userMigration.setInformation(e.getMessage());
                userMigration.setLastUpdate(new Date());
                userMigrationService.save(userMigration);
                throw new MigrationException("Could not migrate patient data for pv1 id "
                        + userMigration.getPatientview1UserId() + ": " + e.getMessage());
            }
        } else {
            doneMessage = pv2User.getId() + " Done";
        }

        Date end = new Date();
        LOG.info(doneMessage + ", took " + ApiUtil.getDateDiff(start, end, TimeUnit.SECONDS) + " seconds.");

        return pv2User.getId();
    }


    // migration only, wipe any existing patient data for this user and then create data in fhir
    @Override
    public void migratePatientData(Long userId, MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        User entityUser = userService.get(userId);
        Set<FhirLink> fhirLinks = entityUser.getFhirLinks();

        if (fhirLinks == null) {
            fhirLinks = new HashSet<>();
        } else {
            if (!migrationUser.isPartialMigration()) {
                // wipe existing patient data, observation data and fhir links to start with fresh data
                patientService.deleteExistingPatientData(fhirLinks);
                observationService.deleteAllExistingObservationData(fhirLinks);
                userService.deleteFhirLinks(userId);
                fhirLinks = new HashSet<>();
                entityUser.setFhirLinks(new HashSet<FhirLink>());
            }
        }

        // set up map of observation headings
        if (observationHeadingMap == null) {
            observationHeadingMap = new HashMap<>();
            for (ObservationHeading observationHeading : observationHeadingRepository.findAll()) {
                observationHeadingMap.put(observationHeading.getCode().toUpperCase(), observationHeading);
            }
        }

        // map of identifiers
        HashMap<String, Identifier> identifierMap = new HashMap<>();
        for (Identifier identifier : entityUser.getIdentifiers()) {
            identifierMap.put(identifier.getIdentifier(), identifier);
        }

        // Patient, Practitioner (taken from pv1 patient table)
        migrateFhirPatientsAndPractitioners(migrationUser, entityUser, fhirLinks);

        // store Conditions (diagnoses and diagnosis edta)
        migrateFhirConditions(migrationUser, entityUser, fhirLinks, identifierMap);

        // store Encounters (treatment and transplant status)
        // note: the unit that provided this information is not stored in PatientView 1, attach to first fhirlink
        migrateFhirEncounters(migrationUser, entityUser, fhirLinks, identifierMap);

        // MedicationStatements (also adds Medicines)
        migrateFhirMedicationStatements(migrationUser, entityUser, fhirLinks, identifierMap);

        // DiagnosticReports (and associated Observation)
        migrateFhirDiagnosticReports(migrationUser, entityUser, fhirLinks, identifierMap);

        // DocumentReferences (letters)
        migrateFhirDocumentReferences(migrationUser, entityUser, fhirLinks, identifierMap);

        // non test observation types (delete first)
        deleteExistingNonTestObservationData(fhirLinks);
        migrateFhirNonTestObservations(migrationUser, entityUser, fhirLinks, identifierMap);

        // survey responses
        migrateSurveyResponses(migrationUser, entityUser);

        // allergies
        migrateAllergies(migrationUser, entityUser, fhirLinks, identifierMap);
    }

    private void migrateFhirPatientsAndPractitioners(MigrationUser migrationUser, User entityUser,
                                                     Set<FhirLink> fhirLinks)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        // Patient, Practitioner (taken from pv1 patient table)
        for (FhirPatient fhirPatient : migrationUser.getPatients()) {
            FhirPractitioner gp = null;
            List<FhirPractitioner> otherPractitioners = new ArrayList<>();

            if (!fhirPatient.getPractitioners().isEmpty()) {
                // set GP and other (IBD) practitioners
                for (FhirPractitioner fhirPractitioner : fhirPatient.getPractitioners()) {
                    if (StringUtils.isEmpty(fhirPractitioner.getRole())) {
                        gp = fhirPractitioner;
                    } else {
                        otherPractitioners.add(fhirPractitioner);
                    }
                }
            }

            List<UUID> practitionerUuids = new ArrayList<>();
            if (gp != null && StringUtils.isNotEmpty(gp.getName())) {
                List<UUID> existingPractitionerUuids =
                        practitionerService.getPractitionerLogicalUuidsByName(gp.getName());

                if (CollectionUtils.isEmpty(existingPractitionerUuids)) {
                    practitionerUuids.add(practitionerService.add(gp));
                } else {
                    practitionerUuids.add(existingPractitionerUuids.get(0));
                }
            }

            // add other practitioners (IBD)
            if (!otherPractitioners.isEmpty()) {
                for (FhirPractitioner fhirPractitioner : otherPractitioners) {
                    List<UUID> existingPractitionerUuids =
                            practitionerService.getPractitionerLogicalUuidsByName(fhirPractitioner.getName());

                    if (CollectionUtils.isEmpty(existingPractitionerUuids)) {
                        practitionerUuids.add(practitionerService.add(fhirPractitioner));
                    } else {
                        practitionerUuids.add(existingPractitionerUuids.get(0));
                    }
                }
            }

            FhirLink newFhirLink = addPatient(fhirPatient, entityUser, practitionerUuids, fhirLinks);

            if (newFhirLink != null) {
                fhirLinks.add(newFhirLink);
            }
        }

        // add patient entered results fhirlink (if not an existing user as they have already)
        if (!migrationUser.isPartialMigration()) {
            Group patientEnteredResultsGroup = groupService.findByCode(HiddenGroupCodes.PATIENT_ENTERED.toString());
            if (patientEnteredResultsGroup == null) {
                throw new ResourceNotFoundException("Group for patient entered results does not exist");
            }
            Identifier identifier = entityUser.getIdentifiers().iterator().next();
            Patient patient = patientService.buildPatient(entityUser, identifier);
            FhirDatabaseEntity fhirPatient = fhirResource.createEntity(patient, ResourceType.Patient.name(), "patient");

            FhirLink fhirLink = new FhirLink();
            fhirLink.setUser(entityUser);
            fhirLink.setIdentifier(identifier);
            fhirLink.setGroup(patientEnteredResultsGroup);
            fhirLink.setResourceId(fhirPatient.getLogicalId());
            fhirLink.setVersionId(fhirPatient.getVersionId());
            fhirLink.setResourceType(ResourceType.Patient.name());
            fhirLink.setActive(true);
            fhirLinkRepository.save(fhirLink);
        }
    }

    // fast method, inserts observations in bulk after converting to correct JSON
    private void migrateFhirTestObservations(MigrationUser migrationUser, User entityUser,
                                             Set<FhirLink> fhirLinks, HashMap<String, Identifier> identifierMap)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        List<FhirDatabaseObservation> fhirDatabaseObservations = new ArrayList<>();
        Long start = migrationUser.getObservationStartDate();
        Long end = migrationUser.getObservationEndDate();

        // only migrate test observation types (not non-test and diagnostic)
        List<String> nonTestObservationTypes = new ArrayList<>();
        for (NonTestObservationTypes observationType : NonTestObservationTypes.class.getEnumConstants()) {
            nonTestObservationTypes.add(observationType.toString());
        }

        for (DiagnosticReportObservationTypes observationType
                : DiagnosticReportObservationTypes.class.getEnumConstants()) {
            nonTestObservationTypes.add(observationType.toString());
        }

        // store test Observations (results), creating FHIR Patients and FhirLinks if not present
        for (FhirObservation fhirObservation : migrationUser.getObservations()) {

            // only add test observations between start and end
            if (fhirObservation.getApplies() != null
                    && fhirObservation.getApplies().getTime() >= start
                    && fhirObservation.getApplies().getTime() <= end
                    && StringUtils.isNotEmpty(fhirObservation.getName())
                    && !nonTestObservationTypes.contains(fhirObservation.getName().toUpperCase())) {

                // get identifier for this user and observation heading for this observation
                Identifier identifier = identifierMap.get(fhirObservation.getIdentifier());
                ObservationHeading observationHeading
                        = observationHeadingMap.get(fhirObservation.getName().toUpperCase());

                if (identifier == null) {
                    throw new FhirResourceException("Identifier not found");
                }

                if (observationHeading == null) {
                    throw new FhirResourceException("ObservationHeading not found: " + fhirObservation.getName());
                }

                FhirLink fhirLink = getFhirLink(fhirObservation.getGroup(), fhirObservation.getIdentifier(), fhirLinks);

                if (fhirLink == null) {
                    fhirLink = createPatientAndFhirLink(entityUser, fhirObservation.getGroup(), identifier);
                    fhirLinks.add(fhirLink);
                }

                // create FHIR database observation with correct JSON content
                fhirDatabaseObservations.add(
                        buildFhirDatabaseObservation(fhirObservation, observationHeading, fhirLink));
            }
        }

        insertObservations(fhirDatabaseObservations);
    }

    // natively insert non test observations
    private void migrateFhirNonTestObservations(MigrationUser migrationUser, User entityUser,
                                                Set<FhirLink> fhirLinks, HashMap<String, Identifier> identifierMap)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        List<FhirDatabaseObservation> fhirDatabaseObservations = new ArrayList<>();

        // only migrate non test observation types (not test and diagnostic)
        List<String> nonTestObservationTypes = new ArrayList<>();
        for (NonTestObservationTypes observationType : NonTestObservationTypes.class.getEnumConstants()) {
            nonTestObservationTypes.add(observationType.toString());
        }

        // Observations (non test)
        for (FhirObservation fhirObservation : migrationUser.getObservations()) {
            if (nonTestObservationTypes.contains(fhirObservation.getName().toUpperCase())) {
                Identifier identifier = identifierMap.get(fhirObservation.getIdentifier());

                if (identifier == null) {
                    throw new FhirResourceException("Identifier not found: " + fhirObservation.getIdentifier());
                }

                FhirLink fhirLink
                        = getFhirLink(fhirObservation.getGroup(), fhirObservation.getIdentifier(), fhirLinks);
                if (fhirLink == null) {
                    fhirLink = createPatientAndFhirLink(entityUser, fhirObservation.getGroup(), identifier);
                    fhirLinks.add(fhirLink);
                }

                // create FHIR database observation with correct JSON content
                fhirDatabaseObservations.add(buildFhirDatabaseNonTestObservation(fhirObservation, fhirLink));
            }
        }

        insertObservations(fhirDatabaseObservations);
    }

    private void migrateFhirConditions(MigrationUser migrationUser, User entityUser,
                                       Set<FhirLink> fhirLinks, HashMap<String, Identifier> identifierMap)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {
        // store Conditions (diagnoses and diagnosis edta)
        for (FhirCondition fhirCondition : migrationUser.getConditions()) {
            Identifier identifier = identifierMap.get(fhirCondition.getIdentifier());
            FhirLink fhirLink = getFhirLink(fhirCondition.getGroup(), fhirCondition.getIdentifier(), fhirLinks);

            if (fhirLink == null) {
                fhirLink = createPatientAndFhirLink(entityUser, fhirCondition.getGroup(), identifier);
                fhirLinks.add(fhirLink);
            }

            conditionService.add(fhirCondition, fhirLink);
        }
    }

    private void migrateFhirEncounters(MigrationUser migrationUser, User entityUser,
                                       Set<FhirLink> fhirLinks, HashMap<String, Identifier> identifierMap)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        // store Encounters (treatment and transplant status)
        for (FhirEncounter fhirEncounter : migrationUser.getEncounters()) {
            Identifier identifier = identifierMap.get(fhirEncounter.getIdentifier());
            Group entityGroup = groupRepository.findById(fhirEncounter.getGroup().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Group does not exist"));

            FhirLink fhirLink
                    = getFhirLink(fhirEncounter.getGroup(), fhirEncounter.getIdentifier(), fhirLinks);
            if (fhirLink == null) {
                fhirLink = createPatientAndFhirLink(entityUser, fhirEncounter.getGroup(), identifier);
                fhirLinks.add(fhirLink);
            }

            UUID organizationUuid;

            List<UUID> organizationUuids
                    = groupService.getOrganizationLogicalUuidsByCode(entityGroup.getCode());

            if (CollectionUtils.isEmpty(organizationUuids)) {
                organizationUuid = groupService.addOrganization(entityGroup);
            } else {
                organizationUuid = organizationUuids.get(0);
            }

            encounterService.add(fhirEncounter, fhirLink, organizationUuid);
        }
    }

    private void migrateFhirMedicationStatements(MigrationUser migrationUser, User entityUser,
                                                 Set<FhirLink> fhirLinks, HashMap<String, Identifier> identifierMap)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        // MedicationStatements (also adds Medicines)
        for (org.patientview.persistence.model.FhirMedicationStatement fhirMedicationStatement
                : migrationUser.getMedicationStatements()) {
            Identifier identifier = identifierMap.get(fhirMedicationStatement.getIdentifier());
            FhirLink fhirLink
                = getFhirLink(fhirMedicationStatement.getGroup(), fhirMedicationStatement.getIdentifier(), fhirLinks);

            if (fhirLink == null) {
                fhirLink = createPatientAndFhirLink(entityUser, fhirMedicationStatement.getGroup(), identifier);
                fhirLinks.add(fhirLink);
            }

            medicationService.add(fhirMedicationStatement, fhirLink);
        }
    }

    private void migrateFhirDiagnosticReports(MigrationUser migrationUser, User entityUser,
                                              Set<FhirLink> fhirLinks, HashMap<String, Identifier> identifierMap)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        // DiagnosticReports (and associated Observation)
        for (FhirDiagnosticReport fhirDiagnosticReport : migrationUser.getDiagnosticReports()) {
            Identifier identifier = identifierMap.get(fhirDiagnosticReport.getIdentifier());
            FhirLink fhirLink
                    = getFhirLink(fhirDiagnosticReport.getGroup(), fhirDiagnosticReport.getIdentifier(), fhirLinks);

            if (fhirLink == null) {
                fhirLink = createPatientAndFhirLink(entityUser, fhirDiagnosticReport.getGroup(), identifier);
                fhirLinks.add(fhirLink);
            }

            diagnosticService.add(fhirDiagnosticReport, fhirLink);
        }
    }

    private void migrateFhirDocumentReferences(MigrationUser migrationUser, User entityUser,
                                               Set<FhirLink> fhirLinks, HashMap<String, Identifier> identifierMap)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        // DocumentReferences (letters)
        for (FhirDocumentReference documentReference : migrationUser.getDocumentReferences()) {
            Identifier identifier = identifierMap.get(documentReference.getIdentifier());
            FhirLink fhirLink
                    = getFhirLink(documentReference.getGroup(), documentReference.getIdentifier(), fhirLinks);
            if (fhirLink == null) {
                fhirLink = createPatientAndFhirLink(entityUser, documentReference.getGroup(), identifier);
                fhirLinks.add(fhirLink);
            }

            letterService.addLetter(documentReference, fhirLink);
        }
    }

    // AllergyIntolerance and Substance (allergy)
    private void migrateAllergies(MigrationUser migrationUser, User entityUser, Set<FhirLink> fhirLinks,
                                  HashMap<String, Identifier> identifierMap)
            throws ResourceNotFoundException, FhirResourceException {
        // DiagnosticReports (and associated Observation)
        for (FhirAllergy fhirAllergy : migrationUser.getAllergies()) {
            Identifier identifier = identifierMap.get(fhirAllergy.getIdentifier());
            FhirLink fhirLink
                    = getFhirLink(fhirAllergy.getGroup(), fhirAllergy.getIdentifier(), fhirLinks);

            if (fhirLink == null) {
                fhirLink = createPatientAndFhirLink(entityUser, fhirAllergy.getGroup(), identifier);
                fhirLinks.add(fhirLink);
            }

            allergyService.add(fhirAllergy, fhirLink);
        }
    }

    private void migrateSurveyResponses(MigrationUser migrationUser, User entityUser) {
        for (SurveyResponse surveyResponse : migrationUser.getSurveyResponses()) {
            SurveyResponse newSurveyResponse = new SurveyResponse(
                    entityUser, surveyResponse.getSurveyResponseScores().get(0).getScore(),
                    surveyResponse.getSurveyResponseScores().get(0).getSeverity(),
                    surveyResponse.getDate(), surveyResponse.getSurveyResponseScores().get(0).getType());

            newSurveyResponse.setSurvey(surveyRepository.findById(surveyResponse.getSurvey().getId()).get());

            for (QuestionAnswer questionAnswer : surveyResponse.getQuestionAnswers()) {
                QuestionAnswer newQuestionAnswer = new QuestionAnswer();
                newQuestionAnswer.setSurveyResponse(newSurveyResponse);
                if (StringUtils.isNotEmpty(questionAnswer.getValue())) {
                    newQuestionAnswer.setValue(questionAnswer.getValue());
                }
                if (questionAnswer.getQuestionOption() != null) {
                    newQuestionAnswer.setQuestionOption(
                            questionOptionRepository.findById(questionAnswer.getQuestionOption().getId()).get());
                }
                newQuestionAnswer.setQuestion(questionRepository.findById(questionAnswer.getQuestion().getId()).get());

                newSurveyResponse.getQuestionAnswers().add(newQuestionAnswer);
            }

            surveyResponseRepository.save(newSurveyResponse);
        }
    }

    // migration only, migrate test observations
    @Override
    public void migrateTestObservations(Long userId, MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        User entityUser = userService.get(userId);
        Set<FhirLink> fhirLinks = entityUser.getFhirLinks();

        if (fhirLinks == null) {
            fhirLinks = new HashSet<>();
        } else {
            if (migrationUser.isDeleteExistingTestObservations()) {
                deleteExistingTestObservationData(migrationUser, fhirLinks);
            }
        }

        // set up map of observation headings
        if (observationHeadingMap == null) {
            observationHeadingMap = new HashMap<>();
            for (ObservationHeading observationHeading : observationHeadingRepository.findAll()) {
                observationHeadingMap.put(observationHeading.getCode().toUpperCase(), observationHeading);
            }
        }

        // map of identifiers
        HashMap<String, Identifier> identifierMap = new HashMap<>();
        for (Identifier identifier : entityUser.getIdentifiers()) {
            identifierMap.put(identifier.getIdentifier(), identifier);
        }

        // store Observations (results), creating FHIR Patients and FhirLinks if not present
        // native sql statement method, much faster but doesn't use stored procedure
        migrateFhirTestObservations(migrationUser, entityUser, fhirLinks, identifierMap);
    }
}
