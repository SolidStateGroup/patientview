package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.service.CodeService;
import org.patientview.api.service.ConditionService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.LookupService;
import org.patientview.api.service.PatientService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Service
public class ConditionServiceImpl extends AbstractServiceImpl<ConditionServiceImpl> implements ConditionService {

    @Inject
    private CodeService codeService;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupService groupService;

    @Inject
    private LookupService lookupService;

    @Inject
    private PatientService patientService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Override
    public List<Condition> get(final UUID patientUuid) throws FhirResourceException {
        List<Condition> conditions = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT  content::varchar ");
        query.append("FROM    condition ");
        query.append("WHERE   content -> 'subject' ->> 'display' = '");
        query.append(patientUuid);
        query.append("' ");

        conditions.addAll(fhirResource.findResourceByQuery(query.toString(), Condition.class));

        return conditions;
    }

    @Override
    public void addCondition(FhirCondition fhirCondition, FhirLink fhirLink)
            throws ResourceNotFoundException, FhirResourceException {

        Condition condition = new Condition();
        condition.setStatusSimple(Condition.ConditionStatus.confirmed);
        condition.setSubject(Util.createFhirResourceReference(fhirLink.getResourceId()));

        if (StringUtils.isNotEmpty(fhirCondition.getNotes())) {
            condition.setNotesSimple(fhirCondition.getNotes());
        }

        if (StringUtils.isNotEmpty(fhirCondition.getCode())) {
            CodeableConcept code = new CodeableConcept();
            code.setTextSimple(fhirCondition.getCode());
            condition.setCode(code);
        }

        if (StringUtils.isNotEmpty(fhirCondition.getCategory())) {
            CodeableConcept category = new CodeableConcept();
            category.setTextSimple(fhirCondition.getCategory());
            condition.setCategory(category);
        }

        if (fhirCondition.getDate() != null) {
            DateAndTime dateAndTime = new DateAndTime(fhirCondition.getDate());
            condition.setDateAssertedSimple(dateAndTime);
        }

        fhirResource.createEntity(condition, ResourceType.Condition.name(), "condition");
    }

    @Override
    public void staffAddCondition(Long patientUserId, String code)
            throws ResourceForbiddenException, ResourceNotFoundException, FhirResourceException {

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

        Group staffEnteredGroup = groupService.findByCode(HiddenGroupCodes.STAFF_ENTERED.toString());
        if (staffEnteredGroup == null) {
            throw new ResourceNotFoundException("Group for staff entered data does not exist");
        }

        Lookup codeLookup = lookupService.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.DIAGNOSIS.toString());
        List<Code> codes = codeService.findAllByCodeAndType(code, codeLookup);

        if (CollectionUtils.isEmpty(codes)) {
            throw new ResourceNotFoundException("Cannot find code");
        }

        User staff = getCurrentUser();

        List<FhirLink> fhirLinks = fhirLinkRepository.findByUserAndGroupAndIdentifier(
                patientUser, staffEnteredGroup, identifiersSorted.get(0));

        FhirLink fhirLink;

        if (CollectionUtils.isEmpty(fhirLinks)) {
            // create FHIR Patient & fhirlink if not exists with STAFF_ENTERED group, userId and identifier
            Patient patient = patientService.buildPatient(patientUser, patientIdentifier);
            FhirDatabaseEntity fhirPatient
                    = fhirResource.createEntity(patient, ResourceType.Patient.name(), "patient");

            // create FhirLink to link user to FHIR Patient at group PATIENT_ENTERED
            fhirLink = new FhirLink();
            fhirLink.setUser(patientUser);
            fhirLink.setIdentifier(patientIdentifier);
            fhirLink.setGroup(staffEnteredGroup);
            fhirLink.setResourceId(fhirPatient.getLogicalId());
            fhirLink.setVersionId(fhirPatient.getVersionId());
            fhirLink.setResourceType(ResourceType.Patient.name());
            fhirLink.setActive(true);

            if (CollectionUtils.isEmpty(patientUser.getFhirLinks())) {
                patientUser.setFhirLinks(new HashSet<FhirLink>());
            }

            patientUser.getFhirLinks().add(fhirLink);
            userRepository.save(patientUser);
        } else {
            fhirLink = fhirLinks.get(0);
        }

        // fhir link now exists
        ResourceReference patientReference = Util.createFhirResourceReference(fhirLink.getResourceId());

    }
}
