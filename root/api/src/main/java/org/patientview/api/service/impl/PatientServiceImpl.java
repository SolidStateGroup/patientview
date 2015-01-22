package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.api.service.CodeService;
import org.patientview.api.service.ConditionService;
import org.patientview.api.service.DiagnosticService;
import org.patientview.api.service.EncounterService;
import org.patientview.api.service.FhirLinkService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.IdentifierService;
import org.patientview.api.service.LetterService;
import org.patientview.api.service.LookupService;
import org.patientview.api.service.MedicationService;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.service.ObservationService;
import org.patientview.api.service.PatientService;
import org.patientview.api.service.PractitionerService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirContact;
import org.patientview.persistence.model.FhirDatabaseObservation;
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.persistence.model.FhirDocumentReference;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirIdentifier;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.DiagnosticReportObservationTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.patientview.persistence.model.enums.TransplantStatus;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
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
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
@Service
public class PatientServiceImpl extends AbstractServiceImpl<PatientServiceImpl> implements PatientService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private UserService userService;

    @Inject
    private GroupService groupService;

    @Inject
    private CodeService codeService;

    @Inject
    private ConditionService conditionService;

    @Inject
    private EncounterService encounterService;

    @Inject
    private FhirLinkService fhirLinkService;

    @Inject
    private ObservationHeadingService observationHeadingService;

    @Inject
    private ObservationService observationService;

    @Inject
    private MedicationService medicationService;

    @Inject
    private DiagnosticService diagnosticService;

    @Inject
    private LetterService letterService;

    @Inject
    private LookupService lookupService;

    @Inject
    private PractitionerService practitionerService;

    @Inject
    private IdentifierService identifierService;

    @Inject
    private IdentifierRepository identifierRepository;

    // used during migration
    private HashMap<String, ObservationHeading> observationHeadingMap;
    private static final String GENDER_CODING = "gender";

    @Override
    public List<org.patientview.api.model.Patient> get(final Long userId, final List<Long> groupIds)
            throws FhirResourceException, ResourceNotFoundException {

        boolean restrictGroups = !(groupIds == null || groupIds.isEmpty());
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
            if ((restrictGroups && groupIds.contains(fhirLink.getGroup().getId())) || (!restrictGroups)) {
                if (fhirLink.getActive() && !Util.isInEnum(fhirLink.getGroup().getCode(), HiddenGroupCodes.class)) {
                    Patient fhirPatient = get(fhirLink.getResourceId());

                    Practitioner fhirPractitioner = null;
                    if (fhirPatient.getCareProvider() != null && !fhirPatient.getCareProvider().isEmpty()) {
                        fhirPractitioner
                            = getPractitioner(UUID.fromString(fhirPatient.getCareProvider().get(0).getDisplaySimple()));
                    }

                    org.patientview.api.model.Patient patient = new org.patientview.api.model.Patient(fhirPatient,
                            fhirPractitioner, fhirLink.getGroup());

                    // set conditions
                    patient = setConditions(patient, conditionService.get(fhirLink.getResourceId()));

                    // set encounters
                    patient = setEncounters(patient, encounterService.get(fhirLink.getResourceId()));

                    // set edta diagnosis if present based on available codes
                    patient = setDiagnosisCodes(patient);

                    // set non test observations
                    patient = setNonTestObservations(patient, fhirLink);

                    patients.add(patient);
                }
            }
        }

        return patients;
    }

    @Override
    public Patient get(final UUID uuid) throws FhirResourceException {
        try {
            return (Patient) DataUtils.getResource(fhirResource.getResource(uuid, ResourceType.Patient));
        } catch (Exception e) {
            throw new FhirResourceException(e);
        }
    }

    public Practitioner getPractitioner(final UUID uuid) throws FhirResourceException {
        try {
            return (Practitioner) DataUtils.getResource(fhirResource.getResource(uuid, ResourceType.Practitioner));
        } catch (Exception e) {
            // gracefully throw exception and continue (ignore bad data from migration)
            LOG.error("Get Practitioner error: ", e);
            return null;
        }
    }

    private org.patientview.api.model.Patient setNonTestObservations(org.patientview.api.model.Patient patient,
                                                            FhirLink fhirLink) {
        try {
            List<String> nonTestTypes = new ArrayList<>();
            for (NonTestObservationTypes observationType : NonTestObservationTypes.class.getEnumConstants()) {
                nonTestTypes.add(observationType.toString());
            }
            patient.getFhirObservations().addAll(observationService.getByFhirLinkAndCodes(fhirLink, nonTestTypes));

        } catch (ResourceNotFoundException | FhirResourceException e) {
            LOG.error("Error setting non test observations: " + e.getMessage());
        }

        return patient;
    }

    private org.patientview.api.model.Patient setConditions(org.patientview.api.model.Patient patient,
                                                            List<Condition> conditions) {
        for (Condition condition : conditions) {
            patient.getFhirConditions().add(new FhirCondition(condition));
        }

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
                                              List<Encounter> encounters) throws FhirResourceException {

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
        }

        return patient;
    }

    @Override
    public Patient buildPatient(User user, Identifier identifier) {
        Patient patient = new Patient();
        patient = createHumanName(patient, user);
        patient = addIdentifier(patient, identifier);
        return patient;
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

            // wipe existing patient data, observation data and fhir links to start with fresh data
            deleteExistingPatientData(fhirLinks);
            deleteAllExistingObservationData(fhirLinks);
            userService.deleteFhirLinks(userId);
            fhirLinks = new HashSet<>();
            entityUser.setFhirLinks(new HashSet<FhirLink>());

        }

        // set up map of observation headings
        if (observationHeadingMap == null) {
            observationHeadingMap = new HashMap<>();
            for (ObservationHeading observationHeading : observationHeadingService.findAll()) {
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
            for (ObservationHeading observationHeading : observationHeadingService.findAll()) {
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

    private void migrateFhirPatientsAndPractitioners(MigrationUser migrationUser, User entityUser,
                                                     Set<FhirLink> fhirLinks)
            throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

        // Patient, Practitioner (taken from pv1 patient table)
        for (FhirPatient fhirPatient : migrationUser.getPatients()) {
            // create practitioner in FHIR for this patient's practitioner if it doesn't exist
            if (fhirPatient.getPractitioner() != null
                    && StringUtils.isNotEmpty(fhirPatient.getPractitioner().getName())) {

                UUID practitionerUuid;
                List<UUID> practitionerUuids
                    = practitionerService.getPractitionerLogicalUuidsByName(fhirPatient.getPractitioner().getName());

                if (CollectionUtils.isEmpty(practitionerUuids)) {
                    practitionerUuid = practitionerService.addPractitioner(fhirPatient.getPractitioner());
                } else {
                    practitionerUuid = practitionerUuids.get(0);
                }

                // add patient
                FhirLink newFhirLink = addPatient(fhirPatient, entityUser, practitionerUuid, fhirLinks);
                if (newFhirLink != null) {
                    fhirLinks.add(newFhirLink);
                }
            } else {
                // add patient, with no practitioner
                FhirLink newFhirLink = addPatient(fhirPatient, entityUser, null, fhirLinks);
                if (newFhirLink != null) {
                    fhirLinks.add(newFhirLink);
                }
            }
        }

        // add patient entered results fhirlink
        Group patientEnteredResultsGroup = groupService.findByCode(HiddenGroupCodes.PATIENT_ENTERED.toString());
        if (patientEnteredResultsGroup == null) {
            throw new ResourceNotFoundException("Group for patient entered results does not exist");
        }
        Identifier identifier = entityUser.getIdentifiers().iterator().next();
        Patient patient = buildPatient(entityUser, identifier);
        JSONObject fhirPatient = fhirResource.create(patient);

        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(entityUser);
        fhirLink.setIdentifier(identifier);
        fhirLink.setGroup(patientEnteredResultsGroup);
        fhirLink.setResourceId(FhirResource.getLogicalId(fhirPatient));
        fhirLink.setVersionId(FhirResource.getVersionId(fhirPatient));
        fhirLink.setResourceType(ResourceType.Patient.name());
        fhirLink.setActive(true);
        fhirLinkService.save(fhirLink);
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
                        observationService.buildFhirDatabaseObservation(fhirObservation, observationHeading, fhirLink));
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
                        observationService.buildFhirDatabaseNonTestObservation(fhirObservation, fhirLink));
            }
        }

        insertObservations(fhirDatabaseObservations);
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
                sb.append(obs.getContent());
                sb.append("')");
                if (i != (fhirDatabaseObservations.size() - 1)) {
                    sb.append(",");
                }
            }
            fhirResource.executeSQL(sb.toString());
        }
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

            conditionService.addCondition(fhirCondition, fhirLink);
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

            encounterService.addEncounter(fhirEncounter, fhirLink, organizationUuid);
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

            medicationService.addMedicationStatement(fhirMedicationStatement, fhirLink);
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

            diagnosticService.addDiagnosticReport(fhirDiagnosticReport, fhirLink);
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

    // delete all FHIR related Patient data for this patient (not Observations)
    public void deleteExistingPatientData(Set<FhirLink> fhirLinks) throws FhirResourceException {

        if (fhirLinks != null) {
            for (FhirLink fhirLink : fhirLinks) {

                UUID subjectId = fhirLink.getResourceId();

                // Patient
                fhirResource.delete(subjectId, ResourceType.Patient);

                // Conditions
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("condition", subjectId)) {
                    fhirResource.delete(uuid, ResourceType.Condition);
                }

                // Encounters
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("encounter", subjectId)) {
                    fhirResource.delete(uuid, ResourceType.Encounter);
                }

                // MedicationStatements (and associated Medicine)
                for (UUID uuid : fhirResource.getLogicalIdsByPatientId("medicationstatement", subjectId)) {

                    // delete medication associated with medication statement
                    MedicationStatement medicationStatement
                            = (MedicationStatement) fhirResource.get(uuid, ResourceType.MedicationStatement);
                    fhirResource.delete(UUID.fromString(medicationStatement.getMedication().getDisplaySimple()),
                            ResourceType.Medication);

                    // delete medication statement
                    fhirResource.delete(uuid, ResourceType.MedicationStatement);
                }

                // DiagnosticReports (and associated Observation)
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("diagnosticreport", subjectId)) {

                    // delete observation (result) associated with diagnostic report
                    DiagnosticReport diagnosticReport
                            = (DiagnosticReport) fhirResource.get(uuid, ResourceType.DiagnosticReport);
                    fhirResource.delete(UUID.fromString(diagnosticReport.getResult().get(0).getDisplaySimple()),
                            ResourceType.Observation);

                    // delete diagnostic report
                    fhirResource.delete(uuid, ResourceType.DiagnosticReport);
                }

                // DocumentReferences (letters)
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("documentreference", subjectId)) {
                    fhirResource.delete(uuid, ResourceType.DocumentReference);
                }

                // AllergyIntolerance and Substance (allergy)
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("allergyintolerance", subjectId)) {

                    // delete Substance associated with AllergyIntolerance
                    AllergyIntolerance allergyIntolerance
                            = (AllergyIntolerance) fhirResource.get(uuid, ResourceType.AllergyIntolerance);
                    fhirResource.delete(UUID.fromString(allergyIntolerance.getSubstance().getDisplaySimple()),
                            ResourceType.Substance);

                    // delete AllergyIntolerance
                    fhirResource.delete(uuid, ResourceType.AllergyIntolerance);
                }

                // AdverseReaction (allergy)
                for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("adversereaction", subjectId)) {
                    fhirResource.delete(uuid, ResourceType.AdverseReaction);
                }
            }
        }
    }

    // delete all FHIR related Observation data for this patient (not other patient data)
    public void deleteAllExistingObservationData(Set<FhirLink> fhirLinks) throws FhirResourceException {

        if (fhirLinks != null) {
            for (FhirLink fhirLink : fhirLinks) {
                UUID subjectId = fhirLink.getResourceId();
                deleteObservations(fhirResource.getLogicalIdsBySubjectId("observation", subjectId));
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

    // add FHIR Patient with optional practitioner reference, used during migration
    private FhirLink addPatient(FhirPatient fhirPatient, User entityUser, UUID practitionerUuid,
        Set<FhirLink> fhirLinks) throws ResourceNotFoundException, FhirResourceException, ResourceForbiddenException {

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

        // care provider (practitioner)
        if (practitionerUuid != null) {
            ResourceReference careProvider = patient.addCareProvider();
            careProvider.setReferenceSimple("uuid");
            careProvider.setDisplaySimple(practitionerUuid.toString());
        }

        JSONObject createdPatient = fhirResource.create(patient);

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
            fhirLink.setResourceId(FhirResource.getLogicalId(createdPatient));
            fhirLink.setVersionId(FhirResource.getVersionId(createdPatient));
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

        JSONObject fhirPatient = fhirResource.create(buildPatient(user, identifier));

        // create new FhirLink and add to list of known fhirlinks
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(user);
        fhirLink.setIdentifier(identifier);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(FhirResource.getLogicalId(fhirPatient));
        fhirLink.setVersionId(FhirResource.getVersionId(fhirPatient));
        fhirLink.setResourceType(ResourceType.Patient.name());
        fhirLink.setActive(true);
        return fhirLinkService.save(fhirLink);
    }

    private FhirLink getFhirLink(Group group, String identifierText, Set<FhirLink> fhirLinks) {
        for (FhirLink fhirLink : fhirLinks) {
            if (fhirLink.getGroup().equals(group) && fhirLink.getIdentifier().getIdentifier().equals(identifierText)) {
                return fhirLink;
            }
        }
        return null;
    }

    private Patient createHumanName(Patient patient, User user) {
        HumanName humanName = patient.addName();
        humanName.addFamilySimple(user.getSurname());
        humanName.addGivenSimple(user.getForename());
        Enumeration<HumanName.NameUse> nameUse = new Enumeration<>(HumanName.NameUse.usual);
        humanName.setUse(nameUse);
        return patient;
    }

    private Patient addIdentifier(Patient patient, Identifier identifier) {
        org.hl7.fhir.instance.model.Identifier fhirIdentifier = patient.addIdentifier();
        fhirIdentifier.setLabelSimple(identifier.getIdentifierType().getValue());
        fhirIdentifier.setValueSimple(identifier.getIdentifier());
        return patient;
    }
}
