package org.patientview.persistence.model;

import java.util.HashSet;
import java.util.Set;

/**
 * GpPatient, reduced set of data provided when creating a GP account
 * Created by jamesr@solidstategroup.com
 * Created on 08/02/2016
 */
public class GpPatient {

    private Long id;
    private Set<Identifier> identifiers = new HashSet<>();
    private String gpName;

    public GpPatient() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public String getGpName() {
        return gpName;
    }

    public void setGpName(String gpName) {
        this.gpName = gpName;
    }
}
