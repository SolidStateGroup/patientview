package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.patientview.api.service.DiagnosticService;
import org.patientview.api.service.FhirLinkService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.MedicationService;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.service.ObservationService;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.api.service.CodeService;
import org.patientview.api.service.ConditionService;
import org.patientview.api.service.EncounterService;
import org.patientview.api.service.LookupService;
import org.patientview.api.service.PatientService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.UserRepository;
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
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

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
    private LookupService lookupService;

    // used during migration
    private HashMap<String, ObservationHeading> observationHeadingMap;

    @Override
    public List<org.patientview.api.model.Patient> get(final Long userId, final List<Long> groupIds)
            throws FhirResourceException, ResourceNotFoundException {

        boolean restrictGroups = !(groupIds == null || groupIds.isEmpty());
        User user = userRepository.findOne(userId);

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
                    if (!fhirPatient.getCareProvider().isEmpty()) {
                        fhirPractitioner
                            = getPractitioner(UUID.fromString(fhirPatient.getCareProvider().get(0).getDisplaySimple()));
                    }

                    org.patientview.api.model.Patient patient = new org.patientview.api.model.Patient(fhirPatient,
                            fhirPractitioner, fhirLink.getGroup(), conditionService.get(fhirLink.getResourceId()));

                    // set encounters
                    patient.getFhirEncounters().addAll(setEncounters(fhirLink.getResourceId()));

                    // set edta diagnosis if present based on available codes
                    patients.add(setDiagnosisCodes(patient));
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
            throw new FhirResourceException(e);
        }
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

    private List<FhirEncounter> setEncounters(UUID patientUuid) throws FhirResourceException {
        List<FhirEncounter> fhirEncounters = new ArrayList<>();

        // replace fhirEncounter type field with a more useful description if it exists in codes
        for (Encounter encounter : encounterService.get(patientUuid)) {
            FhirEncounter fhirEncounter = new FhirEncounter(encounter);

            List<Code> codes = codeService.findAllByCodeAndType(fhirEncounter.getStatus(),
                    lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.TREATMENT.toString()));

            if (!codes.isEmpty()) {
                fhirEncounter.setStatus(codes.get(0).getDescription());
            }

            fhirEncounters.add(fhirEncounter);
        }

        return fhirEncounters;
    }

    @Override
    public Patient buildPatient(User user, Identifier identifier) {
        Patient patient = new Patient();

        patient = createHumanName(patient, user);
        patient = addIdentifier(patient, identifier);

        return patient;
    }

    // migration only
    @Override
    public void migratePatientData(Long userId, MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, FhirResourceException {

        User entityUser = userRepository.findOne(userId);
        Set<FhirLink> fhirLinks = entityUser.getFhirLinks();
        if (fhirLinks == null) {
            fhirLinks = new HashSet<>();
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
        LOG.info(userId + " has " + migrationUser.getObservations().size() + " Observations");
        for (FhirObservation fhirObservation : migrationUser.getObservations()) {

            // get identifier for this user and observation heading for this observation
            Identifier identifier = identifierMap.get(fhirObservation.getIdentifier());
            ObservationHeading observationHeading = observationHeadingMap.get(fhirObservation.getName().toUpperCase());

            //LOG.info("1 " + new Date().getTime());
            if (identifier != null && observationHeading != null) {
                FhirLink fhirLink = getFhirLink(fhirObservation.getGroup(), fhirObservation.getIdentifier(), fhirLinks);

                if (fhirLink == null) {
                    // create FHIR patient and fhirlink
                    fhirLink = createPatientAndFhirLink(entityUser, fhirObservation.getGroup(), identifier);
                    fhirLinks.add(fhirLink);
                }

                //LOG.info("2 " + new Date().getTime());
                observationService.addObservation(fhirObservation, observationHeading, fhirLink);
            } else {
                LOG.error("");
            }
        }

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

        // store Encounters (treatment and transplant status)
        // note: the unit that provided this information is not stored in PatientView 1, attach to first fhirlink
        for (FhirEncounter fhirEncounter : migrationUser.getEncounters()) {
            Identifier identifier = identifierMap.get(fhirEncounter.getIdentifier());
            Group firstUserGroup = migrationUser.getUser().getGroupRoles().iterator().next().getGroup();
            FhirLink fhirLink = getFhirLink(firstUserGroup, fhirEncounter.getIdentifier(), fhirLinks);
            Group entityGroup = groupRepository.findOne(firstUserGroup.getId());

            if (entityGroup != null) {
                if (fhirLink == null) {
                    fhirLink = createPatientAndFhirLink(entityUser, entityGroup, identifier);
                    fhirLinks.add(fhirLink);
                }

                // create organization in FHIR for this group if it doesn't exist
                UUID organizationUuid;

                List<UUID> organizationUuids = groupService.getOrganizationLogicalUuidsByCode(entityGroup.getCode());
                if (CollectionUtils.isEmpty(organizationUuids)) {
                    organizationUuid = groupService.addOrganization(entityGroup);
                } else {
                    organizationUuid = organizationUuids.get(0);
                }

                encounterService.addEncounter(fhirEncounter, fhirLink, organizationUuid);
            }
        }

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

    private FhirLink createPatientAndFhirLink(User user, Group group, Identifier identifier)
            throws ResourceNotFoundException, FhirResourceException {

        JSONObject fhirPatient = fhirResource.create(buildPatient(user, identifier));

        // create new FhirLink and add to list of known fhirlinks
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(user);
        fhirLink.setIdentifier(identifier);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(getResourceId(fhirPatient));
        fhirLink.setVersionId(getVersionId(fhirPatient));
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

    private UUID getVersionId(final JSONObject bundle) {
        JSONArray resultArray = (JSONArray) bundle.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);
        JSONArray links = (JSONArray) resource.get("link");
        JSONObject link = (JSONObject)  links.get(0);
        String[] href = link.getString("href").split("/");
        return UUID.fromString(href[href.length - 1]);
    }

    private UUID getResourceId(final JSONObject bundle) {
        JSONArray resultArray = (JSONArray) bundle.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);
        return UUID.fromString(resource.get("id").toString());
    }

    private Patient createHumanName(Patient patient, User user) {
        HumanName humanName = patient.addName();
        humanName.addFamilySimple(user.getSurname());
        humanName.addGivenSimple(user.getForename());
        Enumeration<HumanName.NameUse> nameUse = new Enumeration(HumanName.NameUse.usual);
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
