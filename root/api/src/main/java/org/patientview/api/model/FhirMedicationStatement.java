package org.patientview.api.model;

import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.BaseModel;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
public class FhirMedicationStatement extends BaseModel{

    private Date start;
    private String name;
    private String dose;

    public FhirMedicationStatement() {
    }

    // if converting from actual MedicationStatement
    public FhirMedicationStatement(MedicationStatement medicationStatement, Medication medication)
            throws FhirResourceException {

        if (medicationStatement.getWhenGiven() == null) {
            throw new FhirResourceException("Cannot convert FHIR medication statement, missing when given");
        }

        if (medicationStatement.getWhenGiven().getStartSimple() == null) {
            throw new FhirResourceException("Cannot convert FHIR medication statement, missing start date");
        }

        DateAndTime date = medicationStatement.getWhenGiven().getStartSimple();
        setStart(new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
            date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis()));

        if (medication == null) {
            throw new FhirResourceException("Cannot convert FHIR medication statement, missing medication");
        }

        if (medication.getCode() == null || medication.getText() == null) {
            throw new FhirResourceException(
                    "Cannot convert FHIR medication statement, missing medication code or text");
        }

        setName(medication.getCode().getTextSimple());
        setDose(medication.getText().toString());
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }
}
