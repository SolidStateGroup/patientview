package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.service.CodeService;
import org.patientview.api.service.ApiConditionService;
import org.patientview.api.service.FhirLinkService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.LookupService;
import org.patientview.service.PractitionerService;
import org.patientview.api.service.UserService;
import org.patientview.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Service
public class ApiConditionServiceImpl extends AbstractServiceImpl<ApiConditionServiceImpl>
        implements ApiConditionService {

    @Inject
    private CodeService codeService;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirLinkService fhirLinkService;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupService groupService;

    @Inject
    private LookupService lookupService;

    @Inject
    private PractitionerService practitionerService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    private void addCondition(Long patientUserId, String code, DiagnosisTypes diagnosisType)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        User patientUser = userService.get(patientUserId);
        if (!userService.currentUserCanGetUser(patientUser)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        if (CollectionUtils.isEmpty(patientUser.getIdentifiers())) {
            throw new ResourceNotFoundException("Patient must have at least one Identifier (NHS Number or other)");
        }

        // sort identifiers and choose first (must have fhir link with identifier to link to fhir database
        List<Identifier> identifiersSorted = new ArrayList<>(patientUser.getIdentifiers());
        Collections.sort(identifiersSorted);
        Identifier patientIdentifier = identifiersSorted.get(0);

        // get STAFF_ENTERED/PATIENT_ENTERED group
        Group group;

        if (diagnosisType.equals(DiagnosisTypes.DIAGNOSIS_STAFF_ENTERED)) {
            group = groupService.findByCode(HiddenGroupCodes.STAFF_ENTERED.toString());
            if (group == null) {
                throw new ResourceNotFoundException("Group for staff entered data does not exist");
            }
        } else if (diagnosisType.equals(DiagnosisTypes.DIAGNOSIS_PATIENT_ENTERED)) {
            group = groupService.findByCode(HiddenGroupCodes.PATIENT_ENTERED.toString());
            if (group == null) {
                throw new ResourceNotFoundException("Group for patient entered data does not exist");
            }
        } else {
            throw new ResourceForbiddenException("Incorrect diagnosis type");
        }

        // check
        Lookup codeLookup = lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.DIAGNOSIS.toString());
        List<Code> codes = codeService.findAllByCodeAndType(code, codeLookup);
        if (CollectionUtils.isEmpty(codes)) {
            throw new ResourceNotFoundException("Cannot find code");
        }

        List<FhirLink> fhirLinks = fhirLinkRepository.findByUserAndGroupAndIdentifier(
                patientUser, group, identifiersSorted.get(0));

        FhirLink fhirLink;

        if (CollectionUtils.isEmpty(fhirLinks)) {
            // create FHIR Patient and fhirlink if not exists with STAFF_ENTERED/PATIENT_ENTERED group,
            // userId and identifier
            fhirLink = fhirLinkService.createFhirLink(patientUser, patientIdentifier, group);
        } else {
            fhirLink = fhirLinks.get(0);
        }

        // fhir link now exists, get patient reference used when building Condition
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());

        // create new Condition with subject
        Condition condition = new Condition();
        condition.setStatusSimple(Condition.ConditionStatus.confirmed);
        condition.setSubject(patientReference);

        // get or create practitioner reference if does not exist, used to store staff user id in condition asserter
        if (diagnosisType.equals(DiagnosisTypes.DIAGNOSIS_STAFF_ENTERED)) {
            ResourceReference staffReference = Util.createResourceReference(getPractitionerUuid(getCurrentUser()));
            condition.setAsserter(staffReference);
        }

        // set code
        condition.setNotesSimple(code);
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setTextSimple(code);
        condition.setCode(codeableConcept);

        // set category DIAGNOSIS_STAFF_ENTERED or DIAGNOSIS_PATIENT_ENTERED
        CodeableConcept category = new CodeableConcept();
        category.setTextSimple(diagnosisType.toString());
        condition.setCategory(category);

        // set assertion date
        DateAndTime dateAndTime = new DateAndTime(new Date());
        condition.setDateAssertedSimple(dateAndTime);

        // store in FHIR
        fhirResource.createEntity(condition, ResourceType.Condition.name(), "condition");
    }

    private FhirCondition createFhirCondition(Condition condition) throws FhirResourceException {
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

        // set asserter based on practitioner content
        if (condition.getAsserter() != null && condition.getAsserter().getDisplaySimple() != null) {
            Practitioner practitioner = (Practitioner) fhirResource.get(
                    UUID.fromString(condition.getAsserter().getDisplaySimple()), ResourceType.Practitioner);
            if (practitioner != null && practitioner.getName() != null
                    && !CollectionUtils.isEmpty(practitioner.getName().getFamily())) {
                try {
                    User staffUser = userRepository.findOne(
                            Long.parseLong(practitioner.getName().getFamily().get(0).getValue()));
                    if (staffUser != null) {
                        fhirCondition.setAsserter(staffUser.getName());
                    }
                } catch (NumberFormatException nfe) {
                    // incorrect family name, should be Long as stores user id
                    LOG.trace("NumberFormatException getting asserter id");
                }
            }
        }

        return fhirCondition;
    }

    private UUID getPractitionerUuid(User user) throws FhirResourceException {
        List<UUID> practitionerUuids = practitionerService.getPractitionerLogicalUuidsByName(user.getId().toString());

        if (!CollectionUtils.isEmpty(practitionerUuids)) {
            return practitionerUuids.get(0);
        } else {
            // build simple practitioner, storing user id in family name
            Practitioner practitioner = new Practitioner();
            HumanName humanName = new HumanName();
            humanName.addFamilySimple(user.getId().toString());
            practitioner.setName(humanName);

            // native create new FHIR object
            FhirDatabaseEntity entity = fhirResource.createEntity(
                    practitioner, ResourceType.Practitioner.name(), "practitioner");
            return entity.getLogicalId();
        }
    }

    @Override
    public List<FhirCondition> getUserEntered(Long userId, DiagnosisTypes diagnosisType, boolean isLogin)
            throws FhirResourceException, ResourceForbiddenException, ResourceNotFoundException {
        User patientUser = userService.get(userId);
        if (!isLogin && !userService.currentUserCanGetUser(patientUser)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        if (CollectionUtils.isEmpty(patientUser.getIdentifiers())) {
            throw new ResourceNotFoundException("Patient must have at least one Identifier (NHS Number or other)");
        }

        // sort identifiers and choose first (must have fhir link with identifier to link to fhir database
        List<Identifier> identifiersSorted = new ArrayList<>(patientUser.getIdentifiers());
        Collections.sort(identifiersSorted);

        // get STAFF_ENTERED/PATIENT_ENTERED group
        Group group;

        if (diagnosisType.equals(DiagnosisTypes.DIAGNOSIS_STAFF_ENTERED)) {
            group = groupService.findByCode(HiddenGroupCodes.STAFF_ENTERED.toString());
            if (group == null) {
                throw new ResourceNotFoundException("Group for staff entered data does not exist");
            }
        } else if (diagnosisType.equals(DiagnosisTypes.DIAGNOSIS_PATIENT_ENTERED)) {
            group = groupService.findByCode(HiddenGroupCodes.PATIENT_ENTERED.toString());
            if (group == null) {
                throw new ResourceNotFoundException("Group for patient entered data does not exist");
            }
        } else {
            throw new ResourceForbiddenException("Incorrect diagnosis type");
        }

        List<FhirLink> fhirLinks = fhirLinkRepository.findByUserAndGroupAndIdentifier(
                patientUser, group, identifiersSorted.get(0));

        if (!CollectionUtils.isEmpty(fhirLinks)) {
            FhirLink fhirLink = fhirLinks.get(0);
            List<Condition> conditions = new ArrayList<>();

            conditions.addAll(fhirResource.findResourceByQuery("SELECT content::varchar " + "FROM condition "
                    + "WHERE content -> 'subject' ->> 'display' = '"
                    + fhirLink.getResourceId() + "' ", Condition.class));

            List<FhirCondition> fhirConditions = new ArrayList<>();
            for (Condition condition : conditions) {
                fhirConditions.add(createFhirCondition(condition));
            }
            return fhirConditions;
        }

        return new ArrayList<>();
    }

    @Override
    public void patientAddCondition(Long userId, String code)
            throws FhirResourceException, ResourceForbiddenException, ResourceNotFoundException {
        addCondition(userId, code, DiagnosisTypes.DIAGNOSIS_PATIENT_ENTERED);
    }

    @Override
    public void staffAddCondition(Long patientUserId, String code)
            throws ResourceForbiddenException, ResourceNotFoundException, FhirResourceException {
        addCondition(patientUserId, code, DiagnosisTypes.DIAGNOSIS_STAFF_ENTERED);
    }

    @Override
    public void staffRemoveCondition(Long patientUserId) throws Exception {

        User patientUser = userService.get(patientUserId);
        if (!userService.currentUserCanGetUser(patientUser)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        if (CollectionUtils.isEmpty(patientUser.getIdentifiers())) {
            throw new ResourceNotFoundException("Patient must have at least one Identifier (NHS Number or other)");
        }

        // get STAFF_ENTERED group
        Group staffEnteredGroup = groupService.findByCode(HiddenGroupCodes.STAFF_ENTERED.toString());
        if (staffEnteredGroup == null) {
            throw new ResourceNotFoundException("Group for staff entered data does not exist");
        }

        // sort identifiers and choose first (must have fhir link with identifier to link to fhir database
        List<Identifier> identifiersSorted = new ArrayList<>(patientUser.getIdentifiers());
        Collections.sort(identifiersSorted);

        List<FhirLink> fhirLinks = fhirLinkRepository.findByUserAndGroupAndIdentifier(
                patientUser, staffEnteredGroup, identifiersSorted.get(0));

        if (CollectionUtils.isEmpty(fhirLinks)) {
            throw new ResourceNotFoundException("No patient data exists for this patient");
        }

        // get logical ids of patient conditions
        List<UUID> patientConditionUuids
                = fhirResource.getLogicalIdsBySubjectId(
                        ResourceType.Condition.getPath(), fhirLinks.get(0).getResourceId());

        for (UUID uuid : patientConditionUuids) {
            Condition condition
                    = (Condition) DataUtils.getResource(fhirResource.getResource(uuid, ResourceType.Condition));
            if (condition.getStatusSimple() != null
                    && !condition.getStatusSimple().equals(Condition.ConditionStatus.refuted)) {
                condition.setStatusSimple(Condition.ConditionStatus.refuted);
                fhirResource.updateEntity(
                        condition, ResourceType.Condition.getPath(), ResourceType.Condition.getPath(), uuid);
            }
        }
    }
}
