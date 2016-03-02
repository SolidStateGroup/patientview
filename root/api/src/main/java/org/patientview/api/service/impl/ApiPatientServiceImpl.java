package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.builder.PatientBuilder;
import org.patientview.api.service.ApiConditionService;
import org.patientview.api.service.CodeService;
import org.patientview.api.service.FhirLinkService;
import org.patientview.api.service.FileDataService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.IdentifierService;
import org.patientview.api.service.LetterService;
import org.patientview.api.service.LookupService;
import org.patientview.api.service.ApiObservationService;
import org.patientview.api.service.ApiPatientService;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.service.PatientService;
import org.patientview.service.PractitionerService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Code;
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
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.DiagnosticReportObservationTypes;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.TransplantStatus;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.QuestionOptionRepository;
import org.patientview.persistence.repository.QuestionRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
import org.patientview.service.AllergyService;
import org.patientview.service.ConditionService;
import org.patientview.service.DiagnosticService;
import org.patientview.service.EncounterService;
import org.patientview.service.GpLetterCreationService;
import org.patientview.service.MedicationService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Patient service, for managing the patient records associated with a User, retrieved from FHIR.
 *
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
@Service
public class ApiPatientServiceImpl extends AbstractServiceImpl<ApiPatientServiceImpl> implements ApiPatientService {

    @Inject
    private AllergyService allergyService;

    @Inject
    private ApiConditionService apiConditionService;

    @Inject
    private CodeService codeService;

    @Inject
    private ConditionService conditionService;

    @Inject
    private EncounterService encounterService;

    @Inject
    private DiagnosticService diagnosticService;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirLinkService fhirLinkService;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FileDataService fileDataService;

    @Inject
    private GpLetterCreationService gpLetterCreationService;

    @Inject
    private GpLetterRepository gpLetterRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupService groupService;

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
    private PatientService patientService;

    @Inject
    private ApiObservationService apiObservationService;

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
    private UserService userService;

    @Inject
    private UserRepository userRepository;

    // used during migration
    private HashMap<String, ObservationHeading> observationHeadingMap;
    private static final String GENDER_CODING = "gender";
    private static final String RENAL_GROUP_CODE = "Renal";
    private static final String GEN_CODE = "GEN";

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
                identifier = new Identifier();
                identifier.setIdentifier(fhirPatient.getIdentifier());
                identifier.setCreator(getCurrentUser());
                identifier.setUser(entityUser);
                identifier.setIdentifierType(
                        lookupService.findByTypeAndValue(LookupTypes.IDENTIFIER,
                                CommonUtils.getIdentifierType(fhirPatient.getIdentifier()).toString()));

                identifier.setCreator(getCurrentUser());
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
            return fhirLinkService.save(fhirLink);
        } else {
            return null;
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
        return fhirLinkService.save(fhirLink);
    }

    /**
     * Delete all Observations from FHIR given a Set of FhirLink, used when deleting a patient and in migration.
     * @param fhirLinks Set of FhirLink
     * @throws FhirResourceException
     */
    @Override
    public void deleteAllExistingObservationData(Set<FhirLink> fhirLinks) throws FhirResourceException {

        if (fhirLinks != null) {
            for (FhirLink fhirLink : fhirLinks) {
                UUID subjectId = fhirLink.getResourceId();
                deleteObservations(fhirResource.getLogicalIdsBySubjectId("observation", subjectId));
            }
        }
    }

    /**
     * Delete all non Observation Patient data stored in Fhir given a Set of FhirLink.
     * @param fhirLinks Set of FhirLink
     * @throws FhirResourceException
     */
    @Override
    public void deleteExistingPatientData(Set<FhirLink> fhirLinks) throws FhirResourceException {

        if (fhirLinks != null) {
            for (FhirLink fhirLink : fhirLinks) {

                UUID subjectId = fhirLink.getResourceId();

                // Patient
                fhirResource.deleteEntity(subjectId, "patient");

                // Conditions
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("condition", subjectId)) {
                    fhirResource.deleteEntity(uuid, "condition");
                }

                // Encounters
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("encounter", subjectId)) {
                    fhirResource.deleteEntity(uuid, "encounter");
                }

                // MedicationStatements (and associated Medicine)
                for (UUID uuid : fhirResource.getLogicalIdsByPatientId("medicationstatement", subjectId)) {

                    // delete medication associated with medication statement
                    MedicationStatement medicationStatement
                            = (MedicationStatement) fhirResource.get(uuid, ResourceType.MedicationStatement);
                    if (medicationStatement != null) {
                        fhirResource.deleteEntity(
                                UUID.fromString(medicationStatement.getMedication().getDisplaySimple()), "medication");

                        // delete medication statement
                        fhirResource.deleteEntity(uuid, "medicationstatement");
                    }
                }

                // DiagnosticReports (and associated Observation)
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("diagnosticreport", subjectId)) {

                    // delete observation (result) associated with diagnostic report
                    DiagnosticReport diagnosticReport
                            = (DiagnosticReport) fhirResource.get(uuid, ResourceType.DiagnosticReport);
                    if (diagnosticReport != null) {
                        fhirResource.deleteEntity(
                                UUID.fromString(diagnosticReport.getResult().get(0).getDisplaySimple()), "observation");

                        // delete diagnostic report
                        fhirResource.deleteEntity(uuid, "diagnosticreport");
                    }
                }

                // DocumentReferences (letters) including associated Media and FileData
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("documentreference", subjectId)) {
                    DocumentReference documentReference
                            = (DocumentReference) fhirResource.get(uuid, ResourceType.DocumentReference);
                    if (documentReference != null) {
                        if (documentReference.getLocation() != null) {
                            // check if media exists, if so try deleting binary data associated with media url
                            Media media = (Media) fhirResource.get(
                                    UUID.fromString(documentReference.getLocationSimple()), ResourceType.Media);

                            if (media != null) {
                                // delete media
                                fhirResource.deleteEntity(
                                        UUID.fromString(documentReference.getLocationSimple()), "media");
                                if (media.getContent() != null && media.getContent().getUrl() != null) {
                                    try {
                                        // delete binary data
                                        fileDataService.delete(Long.valueOf(media.getContent().getUrlSimple()));
                                    } catch (NumberFormatException nfe) {
                                        LOG.error("Error deleting FileData, NumberFormatException for Media url");
                                    } catch (EmptyResultDataAccessException e) {
                                        LOG.error("Error deleting FileData, no entity with id exists");
                                    }
                                }
                            }
                        }
                        // delete DocumentReference
                        fhirResource.deleteEntity(uuid, "documentreference");
                    }
                }

                // AllergyIntolerance and Substance (allergy)
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("allergyintolerance", subjectId)) {

                    // delete Substance associated with AllergyIntolerance
                    AllergyIntolerance allergyIntolerance
                            = (AllergyIntolerance) fhirResource.get(uuid, ResourceType.AllergyIntolerance);
                    if (allergyIntolerance != null) {
                        fhirResource.deleteEntity(UUID.fromString(allergyIntolerance.getSubstance().getDisplaySimple()),
                                "substance");

                        // delete AllergyIntolerance
                        fhirResource.deleteEntity(uuid, "allergyintolerance");
                    }
                }

                // AdverseReaction (allergy)
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("adversereaction", subjectId)) {
                    fhirResource.deleteEntity(uuid, "adversereaction");
                }
            }
        }
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

                deleteObservations(fhirResource.getLogicalIdsBySubjectIdAndNames(
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

                deleteObservations(fhirResource.getLogicalIdsBySubjectIdAppliesIgnoreNames(
                        "observation", subjectId, nonTestObservationTypes, migrationUser.getObservationStartDate(),
                        migrationUser.getObservationEndDate()));
            }
        }
    }

    private void deleteObservations(List<UUID> observationsUuidsToDelete) throws FhirResourceException {
        // natively delete observations
        if (!CollectionUtils.isEmpty(observationsUuidsToDelete)) {
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM observation WHERE logical_id IN (");

            for (int i = 0; i < observationsUuidsToDelete.size(); i++) {
                UUID uuid = observationsUuidsToDelete.get(i);

                sb.append("'").append(uuid).append("'");

                if (i != (observationsUuidsToDelete.size() - 1)) {
                    sb.append(",");
                }
            }

            sb.append(")");
            fhirResource.executeSQL(sb.toString());
        }
    }

    /**
     * Get a list of User patient records, as stored in FHIR and associated with Groups that have imported patient data.
     * Produces a larger object containing all the properties required to populate My Details and My Conditions pages.
     * @param userId ID of User to retrieve patient record for
     * @param groupIds IDs of Groups to retrieve patient records from
     * @return List of Patient objects containing patient encounters, conditions etc
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @Override
    public List<org.patientview.api.model.Patient> get(final Long userId, final List<Long> groupIds)
            throws FhirResourceException, ResourceNotFoundException, ResourceForbiddenException {

        // check User exists
        User user = userService.get(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        // restrict Groups if passed in, only include Groups in the User's current GroupRoles (even if they have
        // FhirLinks from previous Groups)
        boolean restrictGroups = false;
        List<Long> filteredGroupIds = new ArrayList<>();

        if (!CollectionUtils.isEmpty(groupIds)) {
            restrictGroups = true;
            for (GroupRole groupRole : user.getGroupRoles()) {
                Long groupId = groupRole.getGroup().getId();
                if (groupIds.contains(groupId)) {
                    filteredGroupIds.add(groupId);
                }
            }
        }

        List<org.patientview.api.model.Patient> patients = new ArrayList<>();
        List<FhirLink> fhirLinks = new ArrayList<>();
        fhirLinks.addAll(user.getFhirLinks());

        // sort fhirLinks by id
        Collections.sort(fhirLinks, new Comparator<FhirLink>() {
            public int compare(FhirLink f1, FhirLink f2) {
                return f2.getCreated().compareTo(f1.getCreated());
            }
        });

        List<Long> foundFhirLinkGroupIds = new ArrayList<>();

        // get data from FHIR from each unit, ignoring multiple FHIR records per unit (versions)
        for (FhirLink fhirLink : fhirLinks) {
            if ((restrictGroups && filteredGroupIds.contains(fhirLink.getGroup().getId())) || (!restrictGroups)) {
                if (fhirLink.getActive() && !ApiUtil.isInEnum(fhirLink.getGroup().getCode(), HiddenGroupCodes.class)) {
                    Patient fhirPatient = get(fhirLink.getResourceId());
                    foundFhirLinkGroupIds.add(fhirLink.getGroup().getId());

                    if (fhirPatient != null) {

                        // create basic patient with group
                        org.patientview.api.model.Patient patient
                                = new org.patientview.api.model.Patient(fhirPatient, fhirLink.getGroup());

                        // set practitioners
                        patient = setPractitioners(patient, fhirPatient);

                        // set conditions
                        patient = setConditions(patient, conditionService.get(fhirLink.getResourceId()));

                        // set staff entered conditions
                        patient = setStaffEnteredConditions(patient, user.getId());

                        // set encounters (treatment)
                        patient = setEncounters(
                                patient, encounterService.get(fhirLink.getResourceId()), user, fhirLink.getGroup());

                        // set edta diagnosis if present based on available codes in conditions
                        patient = setDiagnosisCodes(patient);

                        // set non test observations
                        patient = setNonTestObservations(patient, fhirLink);

                        // set allergies
                        patient = setAllergies(patient, fhirLink);

                        patients.add(patient);
                    }
                }
            }
        }

        // make sure group links are displayed on screen, even if no fhirlink exists for the patient's groups, ignore
        // specialty groups and general practices
        for (GroupRole groupRole : user.getGroupRoles()) {
            Group group = groupRole.getGroup();
            if ((restrictGroups && filteredGroupIds.contains(group.getId())) || (!restrictGroups)) {
                if (!foundFhirLinkGroupIds.contains(group.getId())
                        && group.getVisible()
                        && !group.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())
                        && !group.getGroupType().getValue().equals(HiddenGroupCodes.GENERAL_PRACTICE.toString())) {
                    org.patientview.api.model.Patient p = new org.patientview.api.model.Patient();
                    p.setFhirPatient(new FhirPatient());
                    p.setGroup(new org.patientview.api.model.Group(group));
                    patients.add(p);
                }
            }
        }

        return patients;
    }

    /**
     * Get a FHIR Patient record given the UUID associated with the Patient in FHIR.
     * @param uuid UUID of Patient in FHIR to retrieve
     * @return FHIR Patient
     * @throws FhirResourceException
     */
    @Override
    public Patient get(final UUID uuid) throws FhirResourceException {
        try {
            return (Patient) DataUtils.getResource(fhirResource.getResource(uuid, ResourceType.Patient));
        } catch (Exception e) {
            throw new FhirResourceException(e);
        }
    }

    @Override
    public List<org.patientview.api.model.Patient> getBasic(Long userId)
            throws FhirResourceException, ResourceNotFoundException {
        // check User exists
        User user = userService.get(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<org.patientview.api.model.Patient> patients = new ArrayList<>();
        List<FhirLink> fhirLinks = new ArrayList<>();
        fhirLinks.addAll(user.getFhirLinks());

        // sort fhirLinks by id
        Collections.sort(fhirLinks, new Comparator<FhirLink>() {
            public int compare(FhirLink f1, FhirLink f2) {
                return f2.getCreated().compareTo(f1.getCreated());
            }
        });

        // get data from FHIR from each unit, ignoring multiple FHIR records per unit (versions)
        for (FhirLink fhirLink : fhirLinks) {
            if (fhirLink.getActive() && !ApiUtil.isInEnum(fhirLink.getGroup().getCode(), HiddenGroupCodes.class)) {
                Patient fhirPatient = get(fhirLink.getResourceId());

                if (fhirPatient != null) {
                    // create basic patient with group
                    org.patientview.api.model.Patient patient
                            = new org.patientview.api.model.Patient(fhirPatient, fhirLink.getGroup());

                    // set practitioners
                    patient = setPractitioners(patient, fhirPatient);

                    // set conditions
                    patient = setConditions(patient, conditionService.get(fhirLink.getResourceId()));

                    // set encounters (treatment)
                    patient = setEncounters(
                            patient, encounterService.get(fhirLink.getResourceId()), user, fhirLink.getGroup());

                    patients.add(patient);
                }
            }
        }

        return patients;
    }

    private Practitioner getPractitioner(final UUID uuid) throws FhirResourceException {
        try {
            return (Practitioner) DataUtils.getResource(fhirResource.getResource(uuid, ResourceType.Practitioner));
        } catch (Exception e) {
            // gracefully throw exception and continue (ignore bad data from migration)
            LOG.error("Get Practitioner error: ", e);
            return null;
        }
    }

    private boolean groupIsRenalChild(Group group) {
        for (GroupRelationship groupRelationship : group.getGroupRelationships()) {
            if (groupRelationship.getRelationshipType().equals(RelationshipTypes.PARENT)
                    && groupRelationship.getObjectGroup().getCode().equals(RENAL_GROUP_CODE)) {
                return true;
            }
        }

        return false;
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
        // generate large sql statement to insert
        if (!CollectionUtils.isEmpty(fhirDatabaseObservations)) {
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
                sb.append(CommonUtils.cleanSql(obs.getContent()));
                sb.append("')");
                if (i != (fhirDatabaseObservations.size() - 1)) {
                    sb.append(",");
                }
            }
            fhirResource.executeSQL(sb.toString());
        }
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
            fhirLinkService.save(fhirLink);
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
                    apiObservationService.buildFhirDatabaseObservation(fhirObservation, observationHeading, fhirLink));
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
                fhirDatabaseObservations.add(
                        apiObservationService.buildFhirDatabaseNonTestObservation(fhirObservation, fhirLink));
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
            Group entityGroup = groupRepository.findOne(fhirEncounter.getGroup().getId());

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
                deleteExistingPatientData(fhirLinks);
                deleteAllExistingObservationData(fhirLinks);
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

    // AllergyIntolerance and Substance (allergy)
    private void migrateAllergies(MigrationUser migrationUser, User entityUser, Set<FhirLink> fhirLinks,
                  HashMap<String, Identifier> identifierMap) throws ResourceNotFoundException, FhirResourceException {
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

            newSurveyResponse.setSurvey(surveyRepository.findOne(surveyResponse.getSurvey().getId()));

            for (QuestionAnswer questionAnswer : surveyResponse.getQuestionAnswers()) {
                QuestionAnswer newQuestionAnswer = new QuestionAnswer();
                newQuestionAnswer.setSurveyResponse(newSurveyResponse);
                if (StringUtils.isNotEmpty(questionAnswer.getValue())) {
                    newQuestionAnswer.setValue(questionAnswer.getValue());
                }
                if (questionAnswer.getQuestionOption() != null) {
                    newQuestionAnswer.setQuestionOption(
                            questionOptionRepository.findOne(questionAnswer.getQuestionOption().getId()));
                }
                newQuestionAnswer.setQuestion(questionRepository.findOne(questionAnswer.getQuestion().getId()));

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

    private org.patientview.api.model.Patient setAllergies(
            org.patientview.api.model.Patient patient, FhirLink fhirLink) throws FhirResourceException {
        patient.setFhirAllergies(allergyService.getBySubject(fhirLink.getResourceId()));
        return patient;
    }

    private org.patientview.api.model.Patient setConditions(org.patientview.api.model.Patient patient,
                                                            List<Condition> conditions) {
        for (Condition condition : conditions) {
            FhirCondition fhirCondition = new FhirCondition(condition);

            // try and set links based on diagnosis code (used by my IBD)
            List<Code> codes = codeService.findAllByCodeAndType(fhirCondition.getCode(),
                    lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.DIAGNOSIS.toString()));
            if (!codes.isEmpty()) {
                Code code = codes.get(0);

                fhirCondition.setDescription(code.getDescription());

                if (!CollectionUtils.isEmpty(code.getLinks())) {
                    fhirCondition.setLinks(codes.get(0).getLinks());
                }
            }
            patient.getFhirConditions().add(fhirCondition);
        }

        return patient;
    }

    private org.patientview.api.model.Patient setStaffEnteredConditions(
            org.patientview.api.model.Patient patient, Long userId)
            throws FhirResourceException, ResourceForbiddenException, ResourceNotFoundException {
            patient.getFhirConditions().addAll(apiConditionService.getStaffEntered(userId));
        return patient;
    }

    private org.patientview.api.model.Patient setDiagnosisCodes(org.patientview.api.model.Patient patient) {
        for (FhirCondition condition : patient.getFhirConditions()) {
            if (condition.getCategory().equals(DiagnosisTypes.DIAGNOSIS_EDTA.toString())) {

                List<Code> codes = codeService.findAllByCodeAndType(condition.getCode(),
                        lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.DIAGNOSIS.toString()));
                if (!codes.isEmpty()) {
                    patient.getDiagnosisCodes().add(codes.get(0));
                }
            }
        }

        return patient;
    }

    private org.patientview.api.model.Patient setEncounters(org.patientview.api.model.Patient patient,
                                List<Encounter> encounters, User user, Group group) throws FhirResourceException {

        boolean hasTreatment = false;
        patient.setFhirEncounters(new ArrayList<FhirEncounter>());

        // replace fhirEncounter type field with a more useful description if it exists in codes
        for (Encounter encounter : encounters) {
            FhirEncounter fhirEncounter = new FhirEncounter(encounter);

            List<Code> codes = codeService.findAllByCodeAndType(fhirEncounter.getStatus(),
                    lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.TREATMENT.toString()));

            if (!codes.isEmpty()) {
                fhirEncounter.setStatus(codes.get(0).getDescription());
                fhirEncounter.setLinks(codes.get(0).getLinks());
            }

            if (fhirEncounter.getEncounterType().contains("TRANSPLANT")) {
                for (TransplantStatus transplantStatus
                        : TransplantStatus.class.getEnumConstants()) {
                    if (fhirEncounter.getStatus().equals(transplantStatus.toString())) {
                        fhirEncounter.setStatus(transplantStatus.getName());
                    }
                }
            }

            patient.getFhirEncounters().add(fhirEncounter);

            if (fhirEncounter.getEncounterType().equals(EncounterTypes.TREATMENT.toString())) {
                hasTreatment = true;
            }
        }

        // Card #314 if no treatment type encounter exists and patient is member of renal specialty, add GEN
        if (!hasTreatment) {
            boolean isMemberOfRenal = false;
            for (GroupRole groupRole : user.getGroupRoles()) {
                if (groupRole.getGroup().getCode().equals(RENAL_GROUP_CODE)) {
                    isMemberOfRenal = true;
                }
            }
            if (isMemberOfRenal && groupIsRenalChild(group)) {
                List<Code> codes = codeService.findAllByCodeAndType(GEN_CODE,
                        lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.TREATMENT.toString()));
                if (!CollectionUtils.isEmpty(codes)) {
                    FhirEncounter fhirEncounter = new FhirEncounter();
                    fhirEncounter.setEncounterType(EncounterTypes.TREATMENT.toString());
                    fhirEncounter.setLinks(codes.get(0).getLinks());
                    fhirEncounter.setStatus(codes.get(0).getDescription());
                    patient.getFhirEncounters().add(fhirEncounter);
                }
            }
        }

        return patient;
    }

    private org.patientview.api.model.Patient setNonTestObservations(org.patientview.api.model.Patient patient,
                                                            FhirLink fhirLink) {
        try {
            List<String> nonTestTypes = new ArrayList<>();
            for (NonTestObservationTypes observationType : NonTestObservationTypes.class.getEnumConstants()) {
                nonTestTypes.add(observationType.toString());
            }
            patient.getFhirObservations().addAll(apiObservationService.getByFhirLinkAndCodes(fhirLink, nonTestTypes));

        } catch (ResourceNotFoundException | FhirResourceException e) {
            LOG.error("Error setting non test observations: " + e.getMessage());
        }

        return patient;
    }

    private org.patientview.api.model.Patient setPractitioners(
            org.patientview.api.model.Patient patient, Patient fhirPatient) throws FhirResourceException {

        if (fhirPatient.getCareProvider() != null && !fhirPatient.getCareProvider().isEmpty()) {
            for (ResourceReference practitionerReference : fhirPatient.getCareProvider()) {
                Practitioner practitioner = getPractitioner(UUID.fromString(practitionerReference.getDisplaySimple()));
                if (practitioner != null) {
                    FhirPractitioner fhirPractitioner = new FhirPractitioner(practitioner);

                    // check if practitioner has been claimed already using gp letter table
                    GpLetter gpLetter = new GpLetter();
                    gpLetter.setGpName(fhirPractitioner.getName());
                    gpLetter.setGpAddress1(fhirPractitioner.getAddress1());
                    gpLetter.setGpAddress2(fhirPractitioner.getAddress2());
                    gpLetter.setGpAddress3(fhirPractitioner.getAddress3());
                    gpLetter.setGpAddress4(fhirPractitioner.getAddress4());
                    gpLetter.setGpPostcode(fhirPractitioner.getPostcode());

                    // check existing gp letter does not exist for postcode and gp name (claimed or unclaimed)
                    // handle entries with and without spaces in postcode
                    List<GpLetter> gpLetters = new ArrayList<>();

                    if (StringUtils.isNotEmpty(gpLetter.getGpPostcode())
                            && StringUtils.isNotEmpty(gpLetter.getGpName())) {
                        gpLetters.addAll(gpLetterRepository.findByPostcodeAndName(
                                gpLetter.getGpPostcode(), gpLetter.getGpName()));
                        gpLetters.addAll(gpLetterRepository.findByPostcodeAndName(
                                gpLetter.getGpPostcode().replace(" ", ""), gpLetter.getGpName()));
                    }

                    if (gpLetters.isEmpty()) {
                        // no current gp letter, check gp details are suitable for creating gp letter
                        if (gpLetterCreationService.hasValidPracticeDetails(gpLetter)
                                || gpLetterCreationService.hasValidPracticeDetailsSingleMaster(gpLetter)) {
                            fhirPractitioner.setAllowInviteGp(true);
                        }
                    } else {
                        // get correct gp letter associated with this patient's identifier
                        if (!CollectionUtils.isEmpty(fhirPatient.getIdentifier())) {
                            for (org.hl7.fhir.instance.model.Identifier identifier : fhirPatient.getIdentifier()) {
                                for (GpLetter gpLetter1 : gpLetters) {
                                    if (gpLetter1.getPatientIdentifier().equals(identifier.getValueSimple())) {
                                        fhirPractitioner.setInviteDate(gpLetter1.getCreated());
                                    }
                                }
                            }
                        }
                    }

                    patient.getFhirPractitioners().add(fhirPractitioner);
                }
            }
        }

        return patient;
    }

    // API
    @Override
    public void update(Long userId, Long groupId, FhirPatient fhirPatient)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        // check current logged in user has rights to this group
        if (!(ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || ApiUtil.doesContainGroupAndRole(groupId, RoleName.UNIT_ADMIN_API))) {
            throw new ResourceForbiddenException("Failed group and role validation");
        }

        // check User exists
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        // check user has fhirLink associated with this group
        FhirLink foundFhirLink = null;
        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getGroup().getId().equals(groupId)) {
                foundFhirLink = fhirLink;
            }
        }
        if (foundFhirLink == null) {
            throw new ResourceForbiddenException("Failed fhirLink validation");
        }

        // check patient exists
        Patient currentPatient = get(foundFhirLink.getResourceId());
        if (currentPatient == null) {
            throw new ResourceNotFoundException("Patient not found");
        }

        // build updated patient
        PatientBuilder patientBuilder = new PatientBuilder(currentPatient, fhirPatient);
        Patient updatedPatient = patientBuilder.build();

        // store updated patient in FHIR
        FhirDatabaseEntity entity
                = fhirResource.updateEntity(
                updatedPatient, ResourceType.Patient.name(), "patient", foundFhirLink.getResourceId());

        // update FhirLink
        foundFhirLink.setVersionId(entity.getVersionId());
        foundFhirLink.setUpdated(entity.getUpdated());
        fhirLinkRepository.save(foundFhirLink);
    }
}
