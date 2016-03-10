package org.patientview.persistence.model;

import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Observation;
import org.patientview.config.exception.FhirResourceException;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 10/09/2014
 */
public class FhirDiagnosticReport {

    private Date date;
    private String name;
    private FhirObservation result;
    private String type;
    private Group group;

    // used by migration
    private String identifier;

    // only present for DiagnosticReport with associated Media and binary FileData
    private String filename;
    private String filetype;
    private Long fileDataId;
    private Long filesize;

    // used for import of binary data
    private String fileBase64;

    public FhirDiagnosticReport() {
    }

    // if converting from actual Observation
    public FhirDiagnosticReport(DiagnosticReport diagnosticReport, Observation result, Group group)
            throws FhirResourceException {

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
        setGroup(group);
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

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public Long getFileDataId() {
        return fileDataId;
    }

    public void setFileDataId(Long fileDataId) {
        this.fileDataId = fileDataId;
    }

    public Long getFilesize() {
        return filesize;
    }

    public void setFilesize(Long filesize) {
        this.filesize = filesize;
    }

    public String getFileBase64() {
        return fileBase64;
    }

    public void setFileBase64(String fileBase64) {
        this.fileBase64 = fileBase64;
    }
}
