package org.patientview.importer.builder;

import generated.Patientview.Patient.Drugdetails.Drug;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
public class MedicationStatementBuilder {

    private final Logger LOG = LoggerFactory.getLogger(MedicationStatementBuilder.class);

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

        return medicationStatement;
    }
}
