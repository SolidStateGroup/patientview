package org.patientview.service.impl;

import generated.Patientview;
import generated.Patientview.Patient.Drugdetails.Drug;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.Period;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.builder.MedicationBuilder;
import org.patientview.builder.MedicationStatementBuilder;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.MedicationService;
import org.patientview.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
@Service
public class MedicationServiceImpl extends AbstractServiceImpl<MedicationService> implements MedicationService {

    @Inject
    private FhirResource fhirResource;

    private String nhsno;

    /**
     * Creates all of the FHIR medicationstatement and medication records from the Patientview object.
     * Links them to the PatientReference.
     *
     * @param data patientview data from xml
     * @param fhirLink FhirLink for user
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException {

        this.nhsno = data.getPatient().getPersonaldetails().getNhsno();
        LOG.trace(nhsno + ": Starting Medication Statement and Medication Process");

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
                    FhirDatabaseEntity storedMedication
                            = fhirResource.createEntity(medication, ResourceType.Medication.name(), "medication");

                    // get medication reference and add to medication statement
                    medicationStatement.setMedication(Util.createResourceReference(storedMedication.getLogicalId()));

                    // set patient reference
                    medicationStatement.setPatient(patientReference);

                    // create medication statement in FHIR
                    fhirResource.createEntity(
                            medicationStatement, ResourceType.MedicationStatement.name(), "medicationstatement");

                    success += 1;

                } catch (FhirResourceException e) {
                    LOG.error(nhsno + ": Unable to build medication/medication statement");
                }

                LOG.trace(nhsno + ": Finished creating medication statement " + count++);
            }

            LOG.info(nhsno + ": Processed {} of {} medication", success, count);
        } else {
            LOG.info(nhsno + ": No drug details provided");
        }
    }

    @Override
    public void add(
            org.patientview.persistence.model.FhirMedicationStatement fhirMedicationStatement, FhirLink fhirLink)
            throws FhirResourceException {

        // Medication, stores name
        Medication medication = new Medication();
        CodeableConcept code = new CodeableConcept();
        code.setTextSimple(fhirMedicationStatement.getName());
        medication.setCode(code);

        FhirDatabaseEntity medicationEntity
                = fhirResource.createEntity(medication, ResourceType.Medication.name(), "medication");

        // Medication statement, stores date, dose
        MedicationStatement medicationStatement = new MedicationStatement();

        if (fhirMedicationStatement.getStartDate() != null) {
            DateAndTime dateAndTime = new DateAndTime(fhirMedicationStatement.getStartDate());
            Period period = new Period();
            period.setStartSimple(dateAndTime);
            period.setEndSimple(dateAndTime);
            medicationStatement.setWhenGiven(period);
        }

        if (StringUtils.isNotEmpty(fhirMedicationStatement.getDose())) {
            MedicationStatement.MedicationStatementDosageComponent dosageComponent
                    = new MedicationStatement.MedicationStatementDosageComponent();
            CodeableConcept concept = new CodeableConcept();
            concept.setTextSimple(fhirMedicationStatement.getDose());
            dosageComponent.setRoute(concept);
            medicationStatement.getDosage().add(dosageComponent);
        }

        medicationStatement.setPatient(Util.createResourceReference(fhirLink.getResourceId()));
        medicationStatement.setMedication(Util.createResourceReference(medicationEntity.getLogicalId()));

        fhirResource.createEntity(medicationStatement, ResourceType.MedicationStatement.name(), "medicationstatement");
    }

    private void deleteBySubjectId(UUID subjectId) throws FhirResourceException {
        // delete medication natively
        fhirResource.executeSQL(
            "DELETE FROM medication WHERE logical_id::TEXT IN (SELECT CONTENT -> 'medication' ->> 'display' " +
            "FROM medicationstatement WHERE CONTENT -> 'patient' ->> 'display' = '" + subjectId.toString() + "')"
        );

        // delete medication statement natively
        fhirResource.executeSQL(
            "DELETE FROM medicationstatement WHERE CONTENT -> 'patient' ->> 'display' = '"
            + subjectId.toString() + "'"
        );
    }

    @Override
    public int deleteBySubjectIdAndDateRange(UUID patientId, Date fromDate, Date toDate)
            throws FhirResourceException {

        // delete medication natively
        fhirResource.executeSQL(
            "DELETE FROM medication WHERE logical_id::TEXT IN (" +
            "SELECT CONTENT -> 'medication' ->> 'display' " +
            "FROM medicationstatement WHERE CONTENT -> 'patient' ->> 'display' = '" + patientId.toString() + "' " +
            "AND CAST(content -> 'whenGiven' ->> 'start' AS TIMESTAMP) >= '" + fromDate + "' " +
            "AND CAST(content -> 'whenGiven' ->> 'end' AS TIMESTAMP) <= '" + toDate + "')"
        );

        // get count of medication to be deleted
        List<UUID> uuidToDelete = fhirResource.getUuidByQuery(
                "SELECT logical_id FROM medicationstatement " +
                "WHERE CONTENT -> 'patient' ->> 'display' = '" + patientId.toString() + "' " +
                "AND CAST(content -> 'whenGiven' ->> 'start' AS TIMESTAMP) >= '" + fromDate + "' " +
                "AND CAST(content -> 'whenGiven' ->> 'end' AS TIMESTAMP) <= '" + toDate + "'"
        );

        // delete medication statement natively
        fhirResource.executeSQL(
            "DELETE FROM medicationstatement " +
            "WHERE CONTENT -> 'patient' ->> 'display' = '" + patientId.toString() + "' " +
            "AND CAST(content -> 'whenGiven' ->> 'start' AS TIMESTAMP) >= '" + fromDate + "' " +
            "AND CAST(content -> 'whenGiven' ->> 'end' AS TIMESTAMP) <= '" + toDate + "'"
        );

        return uuidToDelete.size();
    }
}
