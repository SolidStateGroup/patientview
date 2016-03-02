package org.patientview.persistence.model;

import java.util.Date;
import java.util.List;

/**
 * FhirObservationRange, representing a List of Observations and a date range, all observations that currently exist for
 * this User and Group will be removed from FHIR.
 * Created by jamesr@solidstategroup.com
 * Created on 02/03/2015
 */
public class FhirObservationRange extends BaseImport {

    private String code;
    private Date endDate;
    private List<FhirObservation> observations;
    private Date startDate;

    public FhirObservationRange() { }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<FhirObservation> getObservations() {
        return observations;
    }

    public void setObservations(List<FhirObservation> observations) {
        this.observations = observations;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
