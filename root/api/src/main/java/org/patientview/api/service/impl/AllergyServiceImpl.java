package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.AdverseReaction;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.model.Substance;
import org.patientview.api.service.AllergyService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirAllergy;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/06/2015
 */
@Service
public class AllergyServiceImpl extends AbstractServiceImpl<AllergyServiceImpl> implements AllergyService {

    @Inject
    private FhirResource fhirResource;

    @Override
    public void addAllergy(FhirAllergy fhirAllergy, FhirLink fhirLink) throws FhirResourceException {

        // Substance
        Substance substance = new Substance();

        if (StringUtils.isNotEmpty(fhirAllergy.getType())) {
            CodeableConcept type = new CodeableConcept();
            type.setTextSimple(CommonUtils.cleanSql(fhirAllergy.getType()));
            substance.setType(type);
        }

        if (StringUtils.isNotEmpty(fhirAllergy.getSubstance())) {
            substance.setDescriptionSimple(CommonUtils.cleanSql(fhirAllergy.getSubstance()));
        }

        // AdverseReaction
        AdverseReaction adverseReaction = new AdverseReaction();
        adverseReaction.setSubject(Util.createFhirResourceReference(fhirLink.getResourceId()));

        if (StringUtils.isNotEmpty(fhirAllergy.getReaction())) {
            AdverseReaction.AdverseReactionSymptomComponent symptomComponent = adverseReaction.addSymptom();
            CodeableConcept code = new CodeableConcept();
            code.setTextSimple(CommonUtils.cleanSql(fhirAllergy.getReaction()));
            symptomComponent.setCode(code);
        }

        // AllergyIntolerance
        AllergyIntolerance allergyIntolerance = new AllergyIntolerance();

        if (fhirAllergy.getRecordedDate() != null) {
            DateAndTime dateAndTime = new DateAndTime(fhirAllergy.getRecordedDate());
            allergyIntolerance.setRecordedDateSimple(dateAndTime);
        }

        if (StringUtils.isNotEmpty(fhirAllergy.getStatus())) {
            if (fhirAllergy.getStatus().equals("Active")) {
                allergyIntolerance.setStatusSimple(AllergyIntolerance.Sensitivitystatus.confirmed);
            }
        }

        // todo: fhir mapping for non patient info source
        if (StringUtils.isNotEmpty(fhirAllergy.getInfoSource())) {
            if (fhirAllergy.getInfoSource().equals("Patient")) {
                allergyIntolerance.setRecorder(Util.createFhirResourceReference(fhirLink.getResourceId()));
            } else {
                // todo
                LOG.trace("Mapping non patient info source");
            }
        }

        // todo: fhir mapping for confidence level
        if (StringUtils.isNotEmpty(fhirAllergy.getConfidenceLevel())) {
            // todo
            LOG.trace("Mapping confidence level");
        }

        allergyIntolerance.setSubject(Util.createFhirResourceReference(fhirLink.getResourceId()));

        // create Substance in FHIR
        FhirDatabaseEntity storedSubstance
                = fhirResource.createEntity(substance, ResourceType.Substance.name(), "substance");

        // create AdverseReaction in FHIR
        FhirDatabaseEntity storedAdverseReaction = fhirResource.createEntity(
                adverseReaction, ResourceType.AdverseReaction.name(), "adversereaction");

        // set references to Substance and AdverseReaction in AllergyIntolerance
        allergyIntolerance.setSubstance(Util.createFhirResourceReference(storedSubstance.getLogicalId()));
        ResourceReference reaction = allergyIntolerance.addReaction();
        reaction.setDisplaySimple(storedAdverseReaction.getLogicalId().toString());

        // create AllergyIntolerance in FHIR
        fhirResource.createEntity(allergyIntolerance, ResourceType.AllergyIntolerance.name(), "allergyintolerance");
    }

    @Override
    public List<FhirAllergy> getBySubject(UUID subjectUuid) throws FhirResourceException {
        List<FhirAllergy> fhirAllergies = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT  content::varchar ");
        query.append("FROM    allergyintolerance ");
        query.append("WHERE   content -> 'subject' ->> 'display' = '");
        query.append(subjectUuid.toString());
        query.append("' ");

        // get list of AllergyIntolerance
        List<AllergyIntolerance> allergyIntolerances
                = fhirResource.findResourceByQuery(query.toString(), AllergyIntolerance.class);

        if (!CollectionUtils.isEmpty(allergyIntolerances)) {
            for (AllergyIntolerance allergyIntolerance : allergyIntolerances) {
                Substance substance = null;
                AdverseReaction adverseReaction = null;

                // substance
                if (allergyIntolerance.getSubstance() != null) {
                    try {
                        substance = (Substance) DataUtils.getResource(fhirResource.getResource(
                            UUID.fromString(allergyIntolerance.getSubstance().getDisplaySimple()),
                                ResourceType.Substance));
                    } catch (Exception e) {
                        LOG.error("Could not get substance for AllergyIntolerance, continuing: " + e.getMessage());
                    }
                }

                // first adverse reaction
                if (!CollectionUtils.isEmpty(allergyIntolerance.getReaction())) {
                    try {
                        adverseReaction = (AdverseReaction) DataUtils.getResource(fhirResource.getResource(
                            UUID.fromString(allergyIntolerance.getReaction().get(0).getDisplaySimple()),
                                ResourceType.AdverseReaction));
                    } catch (Exception e) {
                        LOG.error("Could not get adverse reaction for AllergyIntolerance, continuing: "
                            + e.getMessage());
                    }
                }

                fhirAllergies.add(new FhirAllergy(allergyIntolerance, substance, adverseReaction));
            }
        }

        return fhirAllergies;
    }
}
