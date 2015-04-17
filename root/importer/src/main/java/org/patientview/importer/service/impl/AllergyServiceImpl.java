package org.patientview.importer.service.impl;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.AdverseReaction;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.model.Substance;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.importer.Utility.Util;
import org.patientview.importer.builder.AllergyIntoleranceBuilder;
import org.patientview.importer.builder.SubstanceBuilder;
import org.patientview.importer.service.AllergyService;
import org.patientview.persistence.model.FhirDatabaseEntity;
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

    private String nhsno;

    /**
     * Creates all of the FHIR AllergyIntolerance, Substance and AdverseReaction from the Patientview allergy objects.
     * Links them to the PatientReference.
     *
     * @param data patientview data from xml
     * @param fhirLink FhirLink for user
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException, SQLException {

        this.nhsno = data.getPatient().getPersonaldetails().getNhsno();
        LOG.info(nhsno + ": Starting AllergyIntolerance, Substance and AdverseReaction process (allergy)");

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
                FhirDatabaseEntity storedSubstance
                        = fhirResource.createEntity(substance, ResourceType.Substance.name(), "substance");

                // create AdverseReaction in FHIR
                FhirDatabaseEntity storedAdverseReaction = fhirResource.createEntity(
                        adverseReaction, ResourceType.AdverseReaction.name(), "adversereaction");

                // set references to Substance and AdverseReaction in AllergyIntolerance
                allergyIntolerance.setSubstance(Util.createResourceReference(storedSubstance.getLogicalId()));
                ResourceReference reaction = allergyIntolerance.addReaction();
                reaction.setDisplaySimple(storedAdverseReaction.getLogicalId().toString());

                // create AllergyIntolerance in FHIR
                fhirResource.createEntity(
                        allergyIntolerance, ResourceType.AllergyIntolerance.name(), "allergyintolerance");

                success += 1;

            } catch (FhirResourceException e) {
                LOG.error(nhsno + ": Unable to save AllergyIntolerance, Substance or AdverseReaction");
            }

            LOG.trace(nhsno + ": Finished creating AllergyIntolerance " + count++);
        }

        LOG.info(nhsno + ": Finished AllergyIntolerance, Substance and AdverseReaction process (allergy)");
        LOG.info(nhsno + ": Processed {} of {} allergy", success, count);
    }

    private void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {

        // delete AllergyIntolerance and Substance associated with subject
        for (UUID logicalUuid : fhirResource.getLogicalIdsBySubjectId("allergyintolerance", subjectId)) {

            // delete Substance associated with AllergyIntolerance
            AllergyIntolerance allergyIntolerance
                    = (AllergyIntolerance) fhirResource.get(logicalUuid, ResourceType.AllergyIntolerance);
            fhirResource.deleteEntity(UUID.fromString(allergyIntolerance.getSubstance().getDisplaySimple()),
                    "substance");

            // delete AllergyIntolerance
            fhirResource.deleteEntity(logicalUuid, "allergyintolerance");
        }

        // delete AdverseReaction associated with subject
        for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("adversereaction", subjectId)) {
            fhirResource.deleteEntity(uuid, "adversereaction");
        }
    }
}


