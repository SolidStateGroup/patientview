package org.patientview.builder;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.ResourceReference;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 17/11/2014
 */
public class AllergyIntoleranceBuilder {

    private Patientview.Patient.Allergydetails.Allergy allergyData;
    private ResourceReference patientReference;

    public AllergyIntoleranceBuilder(Patientview.Patient.Allergydetails.Allergy allergyData,
                                     ResourceReference patientReference) {
        this.allergyData = allergyData;
        this.patientReference = patientReference;
    }

    public AllergyIntolerance build() {
        AllergyIntolerance allergyIntolerance = new AllergyIntolerance();

        if (allergyData.getAllergyrecordeddate() != null) {
            XMLGregorianCalendar start = allergyData.getAllergyrecordeddate();
            DateAndTime dateAndTime = new DateAndTime(start.toGregorianCalendar().getTime());
            allergyIntolerance.setRecordedDateSimple(dateAndTime);
        }

        if (StringUtils.isNotEmpty(allergyData.getAllergystatus())) {
            if (allergyData.getAllergystatus().equals("Active")) {
                allergyIntolerance.setStatusSimple(AllergyIntolerance.Sensitivitystatus.confirmed);
            }
        }

        if (StringUtils.isNotEmpty(allergyData.getAllergyinfosource())) {
            if (allergyData.getAllergyinfosource().equals("Patient")) {
                allergyIntolerance.setRecorder(patientReference);
            }
        }

        allergyIntolerance.setSubject(patientReference);

        return allergyIntolerance;
    }
}
