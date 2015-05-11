package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.controller.BaseController;
import org.patientview.api.service.ConditionService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Service
public class ConditionServiceImpl extends BaseController<ConditionServiceImpl> implements ConditionService {

    @Inject
    private FhirResource fhirResource;

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
}
