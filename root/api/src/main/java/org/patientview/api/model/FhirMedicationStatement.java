package org.patientview.api.model;

import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.patientview.persistence.exception.FhirResourceException;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
public class FhirMedicationStatement {

    // set from FHIR
    private Date startDate;
    private String name;
    private String dose;

    // set from FhirLink
    private Group group;

    public FhirMedicationStatement() {
    }

    // if converting from actual MedicationStatement
    public FhirMedicationStatement(MedicationStatement medicationStatement,
        Medication medication, org.patientview.persistence.model.Group group) throws FhirResourceException {

        if (medicationStatement.getWhenGiven() == null) {
            throw new FhirResourceException("Cannot convert FHIR medication statement, missing when given");
        }

        if (medicationStatement.getWhenGiven().getStartSimple() == null) {
            throw new FhirResourceException("Cannot convert FHIR medication statement, missing start date (startDate)");
        }

        DateAndTime date = medicationStatement.getWhenGiven().getStartSimple();
        setStartDate(new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis()));

        if (medication == null) {
            throw new FhirResourceException("Cannot convert FHIR medication statement, missing medication");
        }

        if (medication.getCode() == null) {
            throw new FhirResourceException("Cannot convert FHIR medication statement, missing medication code (name)");
        }

        setName(medication.getCode().getTextSimple());

        if (medicationStatement.getDosage().isEmpty() || medicationStatement.getDosage().get(0).getRoute() == null) {
            throw new FhirResourceException("Cannot convert FHIR medication statement, missing dosage (dose)");
        }

        setDose(medicationStatement.getDosage().get(0).getRoute().getTextSimple());

        if (group == null) {
            throw new FhirResourceException("Cannot convert FHIR medication statement, missing group");
        }

        setGroup(new Group(group));
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
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

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
