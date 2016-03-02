package org.patientview.service.impl;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.AdverseReaction;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.model.Substance;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirAllergy;
import org.patientview.persistence.util.DataUtils;
import org.patientview.util.Util;
import org.patientview.builder.AllergyIntoleranceBuilder;
import org.patientview.builder.SubstanceBuilder;
import org.patientview.service.AllergyService;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
        LOG.trace(nhsno + ": Starting AllergyIntolerance, Substance and AdverseReaction process (allergy)");

        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        int count = 0;
        int success = 0;

        // delete existing records
        deleteBySubjectId(fhirLink.getResourceId());

        if (data.getPatient().getAllergydetails() != null) {
            for (Patientview.Patient.Allergydetails.Allergy allergy :
                    data.getPatient().getAllergydetails().getAllergy()) {

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
        }

        LOG.trace(nhsno + ": Finished AllergyIntolerance, Substance and AdverseReaction process (allergy)");
        LOG.info(nhsno + ": Processed {} of {} allergy", success, count);
    }

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
        adverseReaction.setSubject(Util.createResourceReference(fhirLink.getResourceId()));

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
                allergyIntolerance.setRecorder(Util.createResourceReference(fhirLink.getResourceId()));
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

        allergyIntolerance.setSubject(Util.createResourceReference(fhirLink.getResourceId()));

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
        fhirResource.createEntity(allergyIntolerance, ResourceType.AllergyIntolerance.name(), "allergyintolerance");
    }

    private void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        // delete Substance associated with AllergyIntolerance
        fhirResource.executeSQL(
            "DELETE FROM substance WHERE logical_id::TEXT IN (SELECT CONTENT -> 'substance' ->> 'display' " +
            "FROM allergyintolerance WHERE CONTENT -> 'subject' ->> 'display' = '" + subjectId.toString() + "')"
        );

        // delete AllergyIntolerance
        fhirResource.executeSQL(
            "DELETE FROM allergyintolerance WHERE CONTENT -> 'subject' ->> 'display' = '" + subjectId.toString() + "'"
        );

        // delete AdverseReaction
        fhirResource.executeSQL(
            "DELETE FROM adversereaction WHERE CONTENT -> 'subject' ->> 'display' = '" + subjectId.toString() + "'"
        );
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
