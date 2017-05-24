package org.patientview.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.UUID;

/**
 * FhirObservation, representing an Observation and the Group which provided the data.
 * Created by jamesr@solidstategroup.com
 * Created on 28/10/2014
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirObservation {

    private UUID logicalId;
    private Long id;
    private Date applies;
    private String name;
    private String value;
    private String comparator;
    private String comments;
    private BaseGroup group;
    private String temporaryUuid;
    private String bodySite;
    private String location;
    private String units;

    // only used by my IBD
    private String diagram;

    private boolean editable = true;

    public FhirObservation() {
    }

    public FhirObservation(org.patientview.persistence.model.FhirObservation fhirObservation) {
        this.id = fhirObservation.getId();
        this.temporaryUuid = fhirObservation.getTemporaryUuid();
        this.name = fhirObservation.getName();
        this.comments = fhirObservation.getComments();
        this.value = fhirObservation.getValue();
        this.comparator = fhirObservation.getComparator();
        this.applies = fhirObservation.getApplies();
        if (fhirObservation.getGroup() != null) {
            this.group = new BaseGroup(fhirObservation.getGroup());
        }
        this.bodySite = fhirObservation.getBodySite();
        this.location = fhirObservation.getLocation();
        this.units = fhirObservation.getUnits();
        this.diagram = fhirObservation.getDiagram();
    }

    public UUID getLogicalId() {
        return logicalId;
    }

    public void setLogicalId(UUID logicalId) {
        this.logicalId = logicalId;
    }

    public Long getId() {
        return id;
    }

    public Date getApplies() {
        return applies;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getComparator() {
        return comparator;
    }

    public String getComments() {
        return comments;
    }

    public BaseGroup getGroup() {
        return group;
    }

    public String getTemporaryUuid() {
        return temporaryUuid;
    }

    public String getBodySite() {
        return bodySite;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setApplies(Date applies) {
        this.applies = applies;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setComparator(String comparator) {
        this.comparator = comparator;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setGroup(BaseGroup group) {
        this.group = group;
    }

    public void setTemporaryUuid(String temporaryUuid) {
        this.temporaryUuid = temporaryUuid;
    }

    public void setBodySite(String bodySite) {
        this.bodySite = bodySite;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getDiagram() {
        return diagram;
    }

    public void setDiagram(String diagram) {
        this.diagram = diagram;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
