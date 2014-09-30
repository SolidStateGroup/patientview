package org.patientview.importer.builder;

import generated.Patientview.Patient.Drugdetails.Drug;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.MedicationStatement.MedicationStatementDosageComponent;
import org.hl7.fhir.instance.model.Period;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
public class MedicationStatementBuilder {

    private Drug data;

    public MedicationStatementBuilder(Drug data) {
        this.data = data;
    }

    public MedicationStatement build() {
        MedicationStatement medicationStatement = new MedicationStatement();

        XMLGregorianCalendar start = data.getDrugstartdate();
        DateAndTime dateAndTime = new DateAndTime(start.toGregorianCalendar().getTime());
        Period period = new Period();
        period.setStartSimple(dateAndTime);
        period.setEndSimple(dateAndTime);
        medicationStatement.setWhenGiven(period);

        MedicationStatementDosageComponent dosageComponent = new MedicationStatementDosageComponent();
        CodeableConcept concept = new CodeableConcept();
        concept.setTextSimple(data.getDrugdose());
        dosageComponent.setRoute(concept);

        medicationStatement.getDosage().add(dosageComponent);

        return medicationStatement;
    }
}
