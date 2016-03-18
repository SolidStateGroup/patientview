package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.builder.PatientBuilder;
import org.patientview.api.service.ApiPatientService;
import org.patientview.api.service.FhirLinkService;
import org.patientview.api.service.PatientManagementService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.PatientManagement;
import org.patientview.persistence.model.enums.DiagnosisSeverityTypes;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.ConditionService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

/**
 * PatientManagement service for validating and saving IBD patient management information
 *
 * Created by jamesr@solidstategroup.com
 * Created on 18/03/2016
 */
@Service
public class PatientManagementServiceImpl extends AbstractServiceImpl<PatientManagementServiceImpl>
        implements PatientManagementService {

    @Inject
    private ApiPatientService apiPatientService;

    @Inject
    private CodeRepository codeRepository;

    @Inject
    private ConditionService conditionService;

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
    private UserRepository userRepository;

    @Override
    public void save(org.patientview.persistence.model.User user, Group group, Identifier identifier,
                     PatientManagement patientManagement) throws ResourceNotFoundException, FhirResourceException {
        if (user == null) {
            throw new ResourceNotFoundException("user must be set");
        }
        if (!userRepository.exists(user.getId())) {
            throw new ResourceNotFoundException("user does not exist");
        }
        if (group == null) {
            throw new ResourceNotFoundException("group must be set");
        }
        if (!groupRepository.exists(group.getId())) {
            throw new ResourceNotFoundException("group does not exist");
        }
        if (identifier == null) {
            throw new ResourceNotFoundException("identifier must be set");
        }
        if (!identifierRepository.exists(identifier.getId())) {
            throw new ResourceNotFoundException("identifier does not exist");
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

        // update FHIR patient
        if (patientManagement.getFhirPatient() != null) {
            savePatientDetails(fhirLink, patientManagement.getFhirPatient());
        }

        // update FHIR Condition (diagnosis)
        if (patientManagement.getFhirCondition() != null) {
            saveConditionDetails(fhirLink, patientManagement.getFhirCondition());
        }

    }

    private void saveConditionDetails(FhirLink fhirLink, FhirCondition fhirCondition) throws FhirResourceException {
        // set as MAIN diagnosis
        fhirCondition.setCategory(DiagnosisTypes.DIAGNOSIS.toString());
        fhirCondition.setSeverity(DiagnosisSeverityTypes.MAIN.toString());

        // get existing DIAGNOSIS Condition with severity of DiagnosisSeverityTypes.MAIN (should only be one)
        List<UUID> existingMainConditionUuids;

        try {
            existingMainConditionUuids = fhirResource.getConditionLogicalIds(
                fhirLink.getResourceId(), DiagnosisTypes.DIAGNOSIS.toString(), DiagnosisSeverityTypes.MAIN.toString());
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
    public void validate(PatientManagement patientManagement) throws VerificationException {
        if (patientManagement.getFhirCondition() == null) {
            throw new VerificationException("Diagnosis not set");
        }
        if (StringUtils.isEmpty(patientManagement.getFhirCondition().getCode())) {
            throw new VerificationException("Diagnosis code not set");
        }
        if (patientManagement.getFhirCondition().getDate() == null) {
            throw new VerificationException("Diagnosis date not set");
        }

        Code code = codeRepository.findOneByCode(patientManagement.getFhirCondition().getCode());
        if (code == null) {
            throw new VerificationException("Invalid diagnosis code");
        }
        if (code.getCodeType() == null) {
            throw new VerificationException("Diagnosis code cannot be verified");
        }
        if (!code.getCodeType().getValue().equals(DiagnosisTypes.DIAGNOSIS.toString())) {
            throw new VerificationException("Diagnosis code is wrong type");
        }
    }
}
