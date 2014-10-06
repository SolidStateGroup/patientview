package org.patientview.api.model;

import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Observation;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.BaseModel;

import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 10/09/2014
 */
public class FhirDiagnostic extends BaseModel{

    private Date date;
    private String name;
    private String value;
    private String type;
    private Group group;
    private String temporaryUuid;

    public FhirDiagnostic() {
    }

    // if converting from actual Observation
    public FhirDiagnostic(DiagnosticReport diagnosticReport, Observation result,
                          org.patientview.persistence.model.Group group) throws FhirResourceException {

        if (diagnosticReport.getIssued() == null) {
            throw new FhirResourceException("Cannot convert FHIR DiagnosticReport, missing date issued");
        }

        if (result.getValue() == null) {
            throw new FhirResourceException("Cannot convert FHIR DiagnosticReport, missing result");
        }


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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getTemporaryUuid() {
        return temporaryUuid;
    }

    public void setTemporaryUuid(String temporaryUuid) {
        this.temporaryUuid = temporaryUuid;
    }
}
