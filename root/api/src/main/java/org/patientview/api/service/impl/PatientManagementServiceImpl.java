package org.patientview.api.service.impl;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Procedure;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.builder.PatientBuilder;
import org.patientview.api.service.ApiPatientService;
import org.patientview.service.FhirLinkService;
import org.patientview.api.service.PatientManagementService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.FhirProcedure;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.PatientManagement;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.DiagnosisSeverityTypes;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.PatientManagementObservationTypes;
import org.patientview.persistence.model.enums.PractitionerRoles;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.SurgeryObservationTypes;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.ConditionService;
import org.patientview.service.EncounterService;
import org.patientview.service.ObservationService;
import org.patientview.service.OrganizationService;
import org.patientview.service.PractitionerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * PatientManagement service for validating and saving IBD patient management information
 *
 * Created by jamesr@solidstategroup.com
 * Created on 18/03/2016
 */
@Service
@Transactional
public class PatientManagementServiceImpl extends AbstractServiceImpl<PatientManagementServiceImpl>
        implements PatientManagementService {

    @Inject
    private ApiPatientService apiPatientService;

    @Inject
    private CodeRepository codeRepository;

    @Inject
    private ConditionService conditionService;

    @Inject
    private EncounterService encounterService;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirLinkService fhirLinkService;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private ObservationService observationService;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private PractitionerService practitionerService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Override
    public PatientManagement get(Long userId, Long groupId, Long identifierId)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        Identifier identifier = identifierRepository.findById(identifierId)
                .orElseThrow(() -> new ResourceNotFoundException("Identifier not found"));

        if (!userService.currentUserCanGetUser(user)) {
            throw new ResourceForbiddenException("forbidden");
        }

        List<FhirLink> fhirLinks = fhirLinkRepository.findByUserAndGroupAndIdentifier(user, group, identifier);

        if (CollectionUtils.isEmpty(fhirLinks)) {
            return null;
        }

        FhirLink fhirLink = fhirLinks.get(0);
        PatientManagement patientManagement = new PatientManagement();

        // get fhir patient
        Patient patient = (Patient) fhirResource.get(fhirLink.getResourceId(), ResourceType.Patient);
        if (patient != null) {
            patientManagement.setPatient(new FhirPatient(patient));

            // get practitioners
            if (!CollectionUtils.isEmpty(patient.getCareProvider())) {
                for (ResourceReference practitionerRef : patient.getCareProvider()) {
                    Practitioner practitioner = (Practitioner) fhirResource.get(
                            UUID.fromString(practitionerRef.getDisplaySimple()), ResourceType.Practitioner);

                    // only add if IBD_NURSE or NAMED_CONSULTANT
                    if (practitioner != null
                            && !CollectionUtils.isEmpty(practitioner.getRole())
                            && (practitioner.getRole().get(0).getTextSimple().equals(
                                PractitionerRoles.IBD_NURSE.toString())
                            || practitioner.getRole().get(0).getTextSimple().equals(
                            PractitionerRoles.NAMED_CONSULTANT.toString()))) {
                        if (CollectionUtils.isEmpty(patientManagement.getPractitioners())) {
                            patientManagement.setPractitioners(new ArrayList<FhirPractitioner>());
                        }

                        patientManagement.getPractitioners().add(new FhirPractitioner(practitioner));
                    }
                }
            }
        }

        // get fhir observations, using types in PatientManagementObservationTypes
        List<String> names = new ArrayList<>();
        for (PatientManagementObservationTypes type : PatientManagementObservationTypes.values()) {
            names.add(type.toString());
        }

        List<Observation> observations = fhirResource.getObservationsBySubjectAndName(fhirLink.getResourceId(), names);

        if (!CollectionUtils.isEmpty(observations)) {
            patientManagement.setObservations(new ArrayList<FhirObservation>());

            for (Observation observation : observations) {
                patientManagement.getObservations().add(new FhirObservation(observation));
            }
        }

        // get fhir surgery encounters
        List<UUID> encounterUuids = fhirResource.getLogicalIdsBySubjectIdAndIdentifierValue(
                "encounter", fhirLink.getResourceId(), EncounterTypes.SURGERY.toString());

        if (!CollectionUtils.isEmpty(encounterUuids)) {
            patientManagement.setEncounters(new ArrayList<FhirEncounter>());
            List<String> surgeryObservationNames = new ArrayList<>();
            for (SurgeryObservationTypes surgeryObservationType : SurgeryObservationTypes.values()) {
                surgeryObservationNames.add(surgeryObservationType.toString());
                surgeryObservationNames.add(surgeryObservationType.toString());
            }

            for (UUID encounterUuid : encounterUuids) {
                Encounter encounter = (Encounter) fhirResource.get(encounterUuid, ResourceType.Encounter);
                if (encounter != null) {
                    FhirEncounter fhirEncounter = new FhirEncounter(encounter);

                    // observations (get by performer too slow as searching in array {performer, 0})
                    List<Observation> surgeryObservations = fhirResource.getObservationsBySubjectAndName(
                            fhirLink.getResourceId(), surgeryObservationNames);

                    if (!CollectionUtils.isEmpty(surgeryObservations)) {
                        List<Observation> encounterObservations = new ArrayList<>();
                        for (Observation surgeryObservation : surgeryObservations) {
                            if (!CollectionUtils.isEmpty(surgeryObservation.getPerformer())
                                    && StringUtils.isNotEmpty(
                                        surgeryObservation.getPerformer().get(0).getDisplaySimple())
                                    && surgeryObservation.getPerformer().get(0).getDisplaySimple().equals(
                                    encounterUuid.toString())) {
                                encounterObservations.add(surgeryObservation);
                            }
                        }

                        if (!CollectionUtils.isEmpty(encounterObservations)) {
                            fhirEncounter.setObservations(new HashSet<FhirObservation>());
                            for (Observation observation : encounterObservations) {
                                fhirEncounter.getObservations().add(new FhirObservation(observation));
                            }
                        }
                    }

                    // procedures
                    List<Procedure> encounterProcedures = fhirResource.getProceduresByEncounter(encounterUuid);

                    if (!CollectionUtils.isEmpty(encounterProcedures)) {
                        fhirEncounter.setProcedures(new HashSet<FhirProcedure>());
                        for (Procedure procedure : encounterProcedures) {
                            fhirEncounter.getProcedures().add(new FhirProcedure(procedure));
                        }
                    }

                    patientManagement.getEncounters().add(fhirEncounter);
                }
            }
        }

        // get fhir condition (MAIN DIAGNOSIS), should only be one
        List<UUID> existingMainConditionUuids = fhirResource.getConditionLogicalIds(
                fhirLink.getResourceId(), DiagnosisTypes.DIAGNOSIS.toString(), DiagnosisSeverityTypes.MAIN.toString(),
                null);

        if (!CollectionUtils.isEmpty(existingMainConditionUuids)) {
            Condition condition = (Condition) fhirResource.get(
                    existingMainConditionUuids.get(0), ResourceType.Condition);
            if (condition != null) {
                FhirCondition fhirCondition = new FhirCondition(condition);

                // check if code exists, if so then add links (used in ui)
                Code code = codeRepository.findOneByCode(fhirCondition.getCode());
                if (code != null) {
                    fhirCondition.setLinks(code.getLinks());
                }

                patientManagement.setCondition(fhirCondition);
            }
        }

        return patientManagement;
    }

    @Override
    public ServerResponse importPatientManagement(PatientManagement patientManagement) {
        if (StringUtils.isEmpty(patientManagement.getGroupCode())) {
            return new ServerResponse("group code not set");
        }
        if (StringUtils.isEmpty(patientManagement.getIdentifier())) {
            return new ServerResponse("identifier not set");
        }

        Group group = groupRepository.findByCode(patientManagement.getGroupCode());

        if (group == null) {
            return new ServerResponse("group not found");
        }

        // check current logged in user has rights to this group
        if (!(ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || ApiUtil.doesContainGroupAndRole(group.getId(), RoleName.IMPORTER))) {
            return new ServerResponse("failed group and role validation");
        }

        List<Identifier> identifiers = identifierRepository.findByValue(patientManagement.getIdentifier());

        if (CollectionUtils.isEmpty(identifiers)) {
            return new ServerResponse("identifier not found");
        }
        if (identifiers.size() > 1) {
            return new ServerResponse("identifier not unique");
        }

        Identifier identifier = identifiers.get(0);
        User user = identifier.getUser();

        if (user == null) {
            return new ServerResponse("user not found");
        }

        try {
            validate(patientManagement);
            save(user, group, identifier, patientManagement);
        } catch (FhirResourceException | ResourceNotFoundException
                | ResourceForbiddenException | VerificationException e) {
            return new ServerResponse(e.getMessage());
        }

        return new ServerResponse(null, "saved", true);
    }

    @Override
    public void save(org.patientview.persistence.model.User user, Group group, Identifier identifier,
                     PatientManagement patientManagement)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        if (user == null) {
            throw new ResourceNotFoundException("user must be set");
        }
        if (!userRepository.existsById(user.getId())) {
            throw new ResourceNotFoundException("user does not exist");
        }

        if (!userService.currentUserCanGetUser(user)) {
            throw new ResourceForbiddenException("forbidden");
        }

        if (group == null) {
            throw new ResourceNotFoundException("group must be set");
        }
        if (!groupRepository.existsById(group.getId())) {
            throw new ResourceNotFoundException("group does not exist");
        }

        if (identifier == null) {
            throw new ResourceNotFoundException("identifier must be set");
        }
        if (!identifierRepository.existsById(identifier.getId())) {
            throw new ResourceNotFoundException("identifier does not exist");
        }

        if (!identifier.getUser().equals(user)) {
            throw new ResourceNotFoundException("incorrect user identifier");
        }

        FhirLink fhirLink = null;
        List<FhirLink> fhirLinks = fhirLinkRepository.findByUserAndGroupAndIdentifier(user, group, identifier);

        if (CollectionUtils.isEmpty(fhirLinks)) {
            // fhirlink does not exist
            fhirLink = fhirLinkService.createFhirLink(user, identifier, group);
        } else {
            // fhirlink exists, should only be one
            fhirLink = fhirLinks.get(0);
        }

        if (fhirLink == null) {
            throw new ResourceNotFoundException("error creating FHIR link");
        }
        if (fhirLink.getResourceId() == null) {
            throw new ResourceNotFoundException("error retrieving FHIR patient, no UUID");
        }

        // get FHIR Organization logical id UUID, creating from Group if not present, used by treatment Encounter
        UUID organizationUuid;

        try {
            organizationUuid = organizationService.add(group);
        } catch (FhirResourceException fre) {
            throw new FhirResourceException("error saving organization");
        }

        if (organizationUuid == null) {
            throw new FhirResourceException("error saving organization, is null");
        }

        // update FHIR patient
        if (patientManagement.getPatient() != null) {
            savePatientDetails(fhirLink, patientManagement.getPatient());
        }

        // update FHIR Condition (diagnosis)
        if (patientManagement.getCondition() != null) {
            saveConditionDetails(fhirLink, patientManagement.getCondition());
        }

        // update FHIR Encounters (surgeries)
        saveEncounterDetails(fhirLink, patientManagement.getEncounters(), organizationUuid);

        // update FHIR observations (selects and text fields)
        if (!CollectionUtils.isEmpty(patientManagement.getObservations())) {
            saveObservationDetails(fhirLink, patientManagement.getObservations());
        }

        // update FHIR practitioners (named consultant & ibd nurse)
        if (!CollectionUtils.isEmpty(patientManagement.getPractitioners())) {
            savePractitionerDetails(fhirLink, patientManagement.getPractitioners());
        }
    }

    @Override
    public void save(Long userId, Long groupId, Long identifierId, PatientManagement patientManagement)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        if (userId == null) {
            throw new ResourceNotFoundException("user must be set");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        if (groupId == null) {
            throw new ResourceNotFoundException("group must be set");
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group does not exist"));

        if (identifierId == null) {
            throw new ResourceNotFoundException("identifier must be set");
        }
        Identifier identifier = identifierRepository.findById(identifierId)
                .orElseThrow(() -> new ResourceNotFoundException("Identifier does not exist"));

        if (patientManagement == null) {
            throw new ResourceNotFoundException("patient management data must be set");
        }

        save(user, group, identifier, patientManagement);
    }

    private void saveConditionDetails(FhirLink fhirLink, FhirCondition fhirCondition) throws FhirResourceException {
        // set as MAIN diagnosis
        fhirCondition.setCategory(DiagnosisTypes.DIAGNOSIS.toString());
        fhirCondition.setSeverity(DiagnosisSeverityTypes.MAIN.toString());

        // get existing DIAGNOSIS Condition with severity of DiagnosisSeverityTypes.MAIN (should only be one)
        List<UUID> existingMainConditionUuids;

        try {
            existingMainConditionUuids = fhirResource.getConditionLogicalIds(
                fhirLink.getResourceId(), DiagnosisTypes.DIAGNOSIS.toString(), DiagnosisSeverityTypes.MAIN.toString(),
                    null);
        } catch (FhirResourceException fre) {
            throw new FhirResourceException("error getting existing diagnoses");
        }

        if (!CollectionUtils.isEmpty(existingMainConditionUuids)) {
            // update first Condition (should only be one)
            try {
                conditionService.update(fhirCondition, fhirLink, existingMainConditionUuids.get(0));
            } catch (FhirResourceException fre) {
                throw new FhirResourceException("error updating existing diagnosis");
            }
        } else {
            // create new Condition
            try {
                conditionService.add(fhirCondition, fhirLink);
            } catch (FhirResourceException fre) {
                throw new FhirResourceException("error creating diagnosis");
            }
        }

        // update DIAGNOSIS_EDTA if present, used by units still sending XML data e.g. SALIBD
        List<UUID> existingMainConditionEdtaUuids;

        try {
            existingMainConditionEdtaUuids = fhirResource.getConditionLogicalIds(
                    fhirLink.getResourceId(), DiagnosisTypes.DIAGNOSIS_EDTA.toString(), null, null);
        } catch (FhirResourceException fre) {
            throw new FhirResourceException("error getting existing EDTA diagnoses");
        }

        if (!CollectionUtils.isEmpty(existingMainConditionEdtaUuids)) {
            // update first Condition (should only be one)
            try {
                conditionService.update(fhirCondition, fhirLink, existingMainConditionEdtaUuids.get(0));
            } catch (FhirResourceException fre) {
                throw new FhirResourceException("error updating existing EDTA diagnosis");
            }
        }
    }

    private void saveEncounterDetails(FhirLink fhirLink, List<FhirEncounter> fhirEncounters, UUID organizationUuid)
            throws FhirResourceException {
        // erase existing SURGERY Encounters and associated Observations and Procedures
        encounterService.deleteBySubjectIdAndType(fhirLink.getResourceId(), EncounterTypes.SURGERY);

        // store new
        for (FhirEncounter fhirEncounter : fhirEncounters) {
            encounterService.add(fhirEncounter, fhirLink, organizationUuid);
        }
    }

    private void savePractitionerDetails(FhirLink fhirLink, List<FhirPractitioner> fhirPractitioners)
            throws FhirResourceException {
        Patient currentPatient;

        try {
            currentPatient = apiPatientService.get(fhirLink.getResourceId());
        } catch (FhirResourceException fre) {
            throw new FhirResourceException("error retrieving patient");
        }

        if (currentPatient == null) {
            throw new FhirResourceException("error retrieving current patient");
        }

        // keep any care providers with role not in patient management practitioners list
        if (!CollectionUtils.isEmpty(currentPatient.getCareProvider())) {
            List<ResourceReference> toKeep = new ArrayList<>();

            for (ResourceReference resourceReference : currentPatient.getCareProvider()) {
                Practitioner foundPractitioner = (Practitioner) fhirResource.get(
                        UUID.fromString(resourceReference.getDisplaySimple()), ResourceType.Practitioner);

                if (foundPractitioner != null && !CollectionUtils.isEmpty(foundPractitioner.getRole())) {
                    boolean found = false;
                    for (FhirPractitioner fhirPractitioner : fhirPractitioners) {
                        if (fhirPractitioner.getRole().equals(foundPractitioner.getRole().get(0).getTextSimple())) {
                            found = true;
                        }
                    }

                    if (!found) {
                        toKeep.add(resourceReference);
                    }
                }
            }

            // add existing care providers for patient (with role not in fhirPractitioners)
            currentPatient.getCareProvider().clear();
            currentPatient.getCareProvider().addAll(toKeep);
        }

        // check if practitioner with name and role already exists in fhir (should only be one if so)
        for (FhirPractitioner fhirPractitioner : fhirPractitioners) {
            List<UUID> existingPractitioners = practitionerService.getPractitionerLogicalUuidsByNameAndRole(
                    fhirPractitioner.getName(), fhirPractitioner.getRole());

            if (!CollectionUtils.isEmpty(existingPractitioners)) {
                // practitioner already in fhir (should be only one), add resource reference to patient
                ResourceReference careProvider = currentPatient.addCareProvider();
                careProvider.setDisplaySimple(existingPractitioners.get(0).toString());
                careProvider.setReferenceSimple("uuid");
            } else {
                // practitioner not in fhir, add to fhir and add resource reference to patient
                UUID practitionerUuid = practitionerService.add(fhirPractitioner);
                ResourceReference careProvider = currentPatient.addCareProvider();
                careProvider.setDisplaySimple(practitionerUuid.toString());
                careProvider.setReferenceSimple("uuid");
            }
        }

        // update patient with correct care providers
        fhirResource.updateEntity(
                currentPatient, ResourceType.Patient.name(), "patient", fhirLink.getResourceId());
    }

    private void saveObservationDetails(FhirLink fhirLink, List<FhirObservation> fhirObservations)
            throws FhirResourceException {
        List<String> observationNames = new ArrayList<>();

        // get names of all observations to delete and store new
        for (FhirObservation fhirObservation : fhirObservations) {
            observationNames.add(fhirObservation.getName());
        }

        // delete existing with names
        observationService.deleteObservations(fhirResource.getLogicalIdsBySubjectIdAndNames(
                "observation", fhirLink.getResourceId(), observationNames));

        // store existing
        for (FhirObservation fhirObservation : fhirObservations) {
            observationService.add(fhirObservation, fhirLink);
        }
    }

    private void savePatientDetails(FhirLink fhirLink, FhirPatient fhirPatient) throws FhirResourceException {
        // FhirLink exists, check patient exists
        Patient currentPatient;
        FhirDatabaseEntity entity;

        try {
            currentPatient = apiPatientService.get(fhirLink.getResourceId());
        } catch (FhirResourceException fre) {
            throw new FhirResourceException("error retrieving FHIR patient");
        }

        // build patient
        PatientBuilder patientBuilder = new PatientBuilder(currentPatient, fhirPatient);
        Patient builtPatient = patientBuilder.build();

        if (currentPatient == null) {
            // create patient in FHIR, update FhirLink with newly created resource ID
            try {
                entity = fhirResource.createEntity(builtPatient, ResourceType.Patient.name(), "patient");
                fhirLink.setResourceId(entity.getLogicalId());
                fhirLink.setResourceType(ResourceType.Patient.name());
                fhirLink.setActive(true);
            } catch (FhirResourceException fre) {
                throw new FhirResourceException("error creating FHIR patient");
            }
        } else {
            // store updated patient in FHIR
            try {
                entity = fhirResource.updateEntity(
                        builtPatient, ResourceType.Patient.name(), "patient", fhirLink.getResourceId());
            } catch (FhirResourceException fre) {
                throw new FhirResourceException("error updating FHIR patient");
            }
        }

        if (entity == null) {
            throw new FhirResourceException("error storing FHIR patient");
        }

        // update FhirLink and save
        fhirLink.setVersionId(entity.getVersionId());
        fhirLink.setUpdated(entity.getUpdated());
        fhirLinkRepository.save(fhirLink);
    }

    @Override
    public void saveSurgeries(Long userId, Long groupId, Long identifierId, PatientManagement patientManagement)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        if (userId == null) {
            throw new ResourceNotFoundException("user must be set");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        if (groupId == null) {
            throw new ResourceNotFoundException("group must be set");
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group does not exist"));

        if (identifierId == null) {
            throw new ResourceNotFoundException("identifier must be set");
        }
        Identifier identifier = identifierRepository.findById(identifierId)
                .orElseThrow(() -> new ResourceNotFoundException("Identifier does not exist"));

        if (patientManagement == null) {
            throw new ResourceNotFoundException("patient management data must be set");
        }

        if (!userService.currentUserCanGetUser(user)) {
            throw new ResourceForbiddenException("forbidden");
        }

        FhirLink fhirLink = null;
        List<FhirLink> fhirLinks = fhirLinkRepository.findByUserAndGroupAndIdentifier(user, group, identifier);

        if (CollectionUtils.isEmpty(fhirLinks)) {
            // fhirlink does not exist
            fhirLink = fhirLinkService.createFhirLink(user, identifier, group);
        } else {
            // fhirlink exists, should only be one
            fhirLink = fhirLinks.get(0);
        }

        if (fhirLink == null) {
            throw new ResourceNotFoundException("error creating FHIR link");
        }
        if (fhirLink.getResourceId() == null) {
            throw new ResourceNotFoundException("error retrieving FHIR patient, no UUID");
        }

        // get FHIR Organization logical id UUID, creating from Group if not present, used by treatment Encounter
        UUID organizationUuid;

        try {
            organizationUuid = organizationService.add(group);
        } catch (FhirResourceException fre) {
            throw new FhirResourceException("error saving organization");
        }

        if (organizationUuid == null) {
            throw new FhirResourceException("error saving organization, is null");
        }

        // update FHIR Encounters (surgeries)
        saveEncounterDetails(fhirLink, patientManagement.getEncounters(), organizationUuid);
    }

    @Override
    public void validate(PatientManagement patientManagement) throws VerificationException {
        List<String> exceptions = new ArrayList<>();
        List<PatientManagementObservationTypes> requiredObservationTypes;

        // diagnosis
        if (patientManagement.getCondition() == null) {
            exceptions.add("'Diagnosis' not set");
        } else {
            if (StringUtils.isEmpty(patientManagement.getCondition().getCode())) {
                exceptions.add("Diagnosis code not set");
            }
            if (patientManagement.getCondition().getDate() == null) {
                exceptions.add("'Date of Diagnosis' not set");
            }
            if (patientManagement.getCondition().getDate().after(new Date())) {
                exceptions.add("'Date of Diagnosis' must be today or in the past");
            }

            Code code = codeRepository.findOneByCode(patientManagement.getCondition().getCode());
            if (code == null) {
                exceptions.add("Invalid diagnosis code");
            } else {
                if (code.getCodeType() == null) {
                    exceptions.add("Diagnosis code cannot be verified");
                }
                if (!code.getCodeType().getValue().equals(DiagnosisTypes.DIAGNOSIS.toString())) {
                    exceptions.add("Diagnosis code is wrong type");
                }

                // Crohn's specific observation types, hardcoded
                if (code.getCode().equals("CD")) {
                    requiredObservationTypes = new ArrayList<>();
                    requiredObservationTypes.add(PatientManagementObservationTypes.IBD_CROHNSLOCATION);
                    requiredObservationTypes.add(PatientManagementObservationTypes.IBD_CROHNSPROXIMALTERMINALILEUM);
                    requiredObservationTypes.add(PatientManagementObservationTypes.IBD_CROHNSPERIANAL);
                    requiredObservationTypes.add(PatientManagementObservationTypes.IBD_CROHNSBEHAVIOUR);

                    for (PatientManagementObservationTypes type : requiredObservationTypes) {
                        if (!observationExists(type.toString(), patientManagement.getObservations())) {
                            exceptions.add("'" + type.getName() + "' not set");
                        }
                    }
                }

                // Ulcerative Colitis & IBD Unknown specific observation types, hardcoded
                if (code.getCode().equals("UC") || code.getCode().equals("IBDU")) {
                    requiredObservationTypes = new ArrayList<>();
                    requiredObservationTypes.add(PatientManagementObservationTypes.IBD_UCEXTENT);

                    for (PatientManagementObservationTypes type : requiredObservationTypes) {
                        if (!observationExists(type.toString(), patientManagement.getObservations())) {
                            exceptions.add("'" + type.getName() + "' not set");
                        }
                    }
                }
            }
        }

        // required observation types
        requiredObservationTypes = new ArrayList<>();
        requiredObservationTypes.add(PatientManagementObservationTypes.WEIGHT);
        requiredObservationTypes.add(PatientManagementObservationTypes.IBD_SMOKINGSTATUS);

        for (PatientManagementObservationTypes type : requiredObservationTypes) {
            if (!observationExists(type.toString(), patientManagement.getObservations())) {
                exceptions.add("'" + type.getName() + "' not set");
            }
        }

        // required postcode and gender
        if (patientManagement.getPatient() == null) {
            exceptions.add("Must set Postcode and Gender");
        }
        if (StringUtils.isEmpty(patientManagement.getPatient().getPostcode())) {
            exceptions.add("'Postcode' not set");
        }
        if (StringUtils.isEmpty(patientManagement.getPatient().getGender())) {
            exceptions.add("'Gender' not set");
        }

        if (!CollectionUtils.isEmpty(exceptions)) {
            Collections.sort(exceptions);
            throw new VerificationException(new Gson().toJson(exceptions));
        }
    }

    private boolean observationExists(String name, List<FhirObservation> observations) {
        if (CollectionUtils.isEmpty(observations)) {
            return false;
        }

        for (FhirObservation observation : observations) {
            if (observation.getName().toUpperCase().equals(name.toUpperCase())
                    && StringUtils.isNotEmpty(observation.getValue())) {
                return true;
            }
        }

        return false;
    }
}
