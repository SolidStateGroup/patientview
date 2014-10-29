package org.patientview.api.model;

import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 28/10/2014
 *
 * Reduced Group information FhirObservation, for transport use
 */
public class FhirObservation {

    private Long id;
    private Date applies;
    private String name;
    private String value;
    private String comparator;
    private String comments;
    private BaseGroup group;
    private String temporaryUuid;

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
}
