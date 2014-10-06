package org.patientview.api.model;

import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Observation;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.BaseModel;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 10/09/2014
 */
public class FhirDiagnosticReport extends BaseModel{

    private Date date;
    private String name;
    private FhirObservation result;
    private String type;
    private BaseGroup group;

    public FhirDiagnosticReport() {
    }

    // if converting from actual Observation
    public FhirDiagnosticReport(DiagnosticReport diagnosticReport, Observation result,
                                org.patientview.persistence.model.Group group) throws FhirResourceException {

        if (diagnosticReport.getDiagnostic() == null) {
            throw new FhirResourceException("Cannot convert FHIR DiagnosticReport, missing Diagnostic (date)");
        }

        if (diagnosticReport.getName() == null) {
            throw new FhirResourceException("Cannot convert FHIR DiagnosticReport, missing Name");
        }

        if (diagnosticReport.getServiceCategory() == null) {
            throw new FhirResourceException("Cannot convert FHIR DiagnosticReport, missing ServiceCategory (type)");
        }

        if (result.getValue() == null) {
            throw new FhirResourceException("Cannot convert FHIR DiagnosticReport, missing Observation result");
        }

        // Physiologically Relevant time/time-period for report, dateTime, DiagnosticReport.diagnostic[x]
        DateTime diagnostic = (DateTime) diagnosticReport.getDiagnostic();
        DateAndTime date = diagnostic.getValue();
        setDate(new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis()));

        // Name/Code for this diagnostic report, CodeableConcept, DiagnosticReport.name
        setName(diagnosticReport.getName().getTextSimple());

        // Observation (result)
        setResult(new FhirObservation(result));

        // Type, note: Categories appear to be Imaging and Endoscopy in IBD. No direct code mappings in serviceCategory
        // so might need to be manual.
        setType(diagnosticReport.getServiceCategory().getTextSimple());

        // Group
        setGroup(new BaseGroup(group));
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FhirObservation getResult() {
        return result;
    }

    public void setResult(FhirObservation result) {
        this.result = result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BaseGroup getGroup() {
        return group;
    }

    public void setGroup(BaseGroup group) {
        this.group = group;
    }
}
