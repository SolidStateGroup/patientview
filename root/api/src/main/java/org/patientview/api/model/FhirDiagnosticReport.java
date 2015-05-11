package org.patientview.api.model;

import java.util.Date;

/**
 * FhirDiagnosticReport, representing a diagnostic report and the associated Group which provided the data.
 * Created by jamesr@solidstategroup.com
 * Created on 28/10/2014
 */
public class FhirDiagnosticReport {

    private Long id;
    private Date date;
    private String name;
    private FhirObservation result;
    private String type;
    private BaseGroup group;

    // only present for DocumentReference with associated Media and binary FileData
    private String filename;
    private String filetype;
    private Long filesize;
    private Long fileDataId;

    public FhirDiagnosticReport() {
    }

    public FhirDiagnosticReport(org.patientview.persistence.model.FhirDiagnosticReport fhirDiagnosticReport) {
        this.date = fhirDiagnosticReport.getDate();
        this.name = fhirDiagnosticReport.getName();
        if (fhirDiagnosticReport.getResult() != null) {
            this.result = new FhirObservation(fhirDiagnosticReport.getResult());
        }
        this.type = fhirDiagnosticReport.getType();
        if (fhirDiagnosticReport.getGroup() != null) {
            this.group = new BaseGroup(fhirDiagnosticReport.getGroup());
        }
        this.filename = fhirDiagnosticReport.getFilename();
        this.filetype = fhirDiagnosticReport.getFiletype();
        this.filesize = fhirDiagnosticReport.getFilesize();
        this.fileDataId = fhirDiagnosticReport.getFileDataId();
    }

    public Long getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public FhirObservation getResult() {
        return result;
    }

    public String getType() {
        return type;
    }

    public BaseGroup getGroup() {
        return group;
    }

    public Long getFileDataId() {
        return fileDataId;
    }

    public Long getFilesize() {
        return filesize;
    }

    public String getFiletype() {
        return filetype;
    }

    public String getFilename() {
        return filename;
    }
}
