package org.patientview.importer.service.impl;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.AdverseReaction;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.model.Substance;
import org.json.JSONObject;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.importer.Utility.Util;
import org.patientview.importer.builder.AllergyIntoleranceBuilder;
import org.patientview.importer.builder.SubstanceBuilder;
import org.patientview.importer.service.AllergyService;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
@Service
public class AllergyServiceImpl extends AbstractServiceImpl<AllergyService> implements AllergyService {

    @Inject
    private FhirResource fhirResource;

    /**
     * Creates all of the FHIR AllergyIntolerance, Substance and AdverseReaction from the Patientview allergy objects.
     * Links them to the PatientReference.
     *
     * @param data patientview data from xml
     * @param fhirLink FhirLink for user
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException, SQLException {

        LOG.info("Starting AllergyIntolerance, Substance and AdverseReaction process (allergy)");

        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        int count = 0;
        int success = 0;

        // delete existing records
        deleteBySubjectId(fhirLink.getResourceId());

        for (Patientview.Patient.Allergy allergy : data.getPatient().getAllergy()) {

            // build Substance
            SubstanceBuilder substanceBuilder = new SubstanceBuilder(allergy);
            Substance substance = substanceBuilder.build();

            // build AdverseReaction
            AdverseReaction adverseReaction = new AdverseReaction();
            adverseReaction.setSubject(patientReference);

            if (StringUtils.isNotEmpty(allergy.getAllergyreaction())) {
                AdverseReaction.AdverseReactionSymptomComponent symptomComponent = adverseReaction.addSymptom();
                CodeableConcept code = new CodeableConcept();
                code.setTextSimple(CommonUtils.cleanSql(allergy.getAllergyreaction()));
                symptomComponent.setCode(code);
            }

            // build AllergyIntolerance
            AllergyIntoleranceBuilder allergyIntoleranceBuilder
                    = new AllergyIntoleranceBuilder(allergy, patientReference);
            AllergyIntolerance allergyIntolerance = allergyIntoleranceBuilder.build();

            try {
                // create Substance in FHIR
                JSONObject storedSubstance = fhirResource.create(substance);

                // create AdverseReaction in FHIR
                JSONObject storedAdverseReaction = fhirResource.create(adverseReaction);

                // set references to Substance and AdverseReaction in AllergyIntolerance
                allergyIntolerance.setSubstance(Util.createResourceReference(Util.getResourceId(storedSubstance)));
                ResourceReference reaction = allergyIntolerance.addReaction();
                reaction.setDisplaySimple(Util.getResourceId(storedAdverseReaction).toString());

                // create AllergyIntolerance in FHIR
                fhirResource.create(allergyIntolerance);

                success += 1;

            } catch (FhirResourceException e) {
                LOG.error("Unable to build AllergyIntolerance, Substance or AdverseReaction");
            }

            LOG.trace("Finished creating AllergyIntolerance " + count++);
        }

        LOG.info("Processed {} of {} allergy", success, count);
    }

    private void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {

        // delete AllergyIntolerance and Substance associated with subject
        for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("allergyintolerance", subjectId)) {

            // delete Substance associated with AllergyIntolerance
            AllergyIntolerance allergyIntolerance
                    = (AllergyIntolerance) fhirResource.get(uuid, ResourceType.AllergyIntolerance);
            fhirResource.delete(UUID.fromString(allergyIntolerance.getSubstance().getDisplaySimple()),
                    ResourceType.Substance);

            // delete AllergyIntolerance
            fhirResource.delete(uuid, ResourceType.AllergyIntolerance);
        }

        // delete AdverseReaction associated with subject
        for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("adversereaction", subjectId)) {
            fhirResource.delete(uuid, ResourceType.AdverseReaction);
        }
    }
}


