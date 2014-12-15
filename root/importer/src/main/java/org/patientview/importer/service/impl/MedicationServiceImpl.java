package org.patientview.importer.service.impl;

import generated.Patientview;
import generated.Patientview.Patient.Drugdetails.Drug;
import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.importer.builder.MedicationBuilder;
import org.patientview.importer.builder.MedicationStatementBuilder;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.importer.service.MedicationService;
import org.patientview.importer.Utility.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
@Service
public class MedicationServiceImpl extends AbstractServiceImpl<MedicationService> implements MedicationService {

    @Inject
    private FhirResource fhirResource;

    /**
     * Creates all of the FHIR medicationstatement and medication records from the Patientview object.
     * Links them to the PatientReference.
     *
     * @param data patientview data from xml
     * @param fhirLink FhirLink for user
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException, SQLException {

        LOG.info("Starting Medication Statement and Medication Process");

        if (data.getPatient().getDrugdetails() != null) {
            ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
            int count = 0;
            int success = 0;

            // delete existing
            deleteBySubjectId(fhirLink.getResourceId());

            for (Drug drug : data.getPatient().getDrugdetails().getDrug()) {
                MedicationBuilder medicationBuilder = new MedicationBuilder(drug);
                Medication medication = medicationBuilder.build();

                MedicationStatementBuilder medicationStatementBuilder = new MedicationStatementBuilder(drug);
                MedicationStatement medicationStatement = medicationStatementBuilder.build();

                try {
                    // create medication in FHIR
                    JSONObject storedMedication = fhirResource.create(medication);

                    // get medication reference and add to medication statement
                    medicationStatement.setMedication(Util.createResourceReference(Util.getResourceId(storedMedication)));

                    // set patient reference
                    medicationStatement.setPatient(patientReference);

                    // create medication statement in FHIR
                    fhirResource.create(medicationStatement);

                    success += 1;

                } catch (FhirResourceException e) {
                    LOG.error("Unable to build medication/medication statement");
                }

                LOG.trace("Finished creating medication statement " + count++);
            }

            LOG.info("Processed {} of {} medication", success, count);
        } else {
            LOG.info("No drug details provided");
        }
    }

    public void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        for (UUID uuid : fhirResource.getLogicalIdsByPatientId("medicationstatement", subjectId)) {

            // delete medication associated with medication statement
            MedicationStatement medicationStatement
                    = (MedicationStatement) fhirResource.get(uuid, ResourceType.MedicationStatement);
            fhirResource.delete(UUID.fromString(medicationStatement.getMedication().getDisplaySimple()),
                    ResourceType.Medication);

            // delete medication statement
            fhirResource.delete(uuid, ResourceType.MedicationStatement);
        }
    }
}


