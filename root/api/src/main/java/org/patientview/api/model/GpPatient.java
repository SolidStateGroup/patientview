package org.patientview.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * GpPatient, reduced set of data provided when creating a GP account
 * Created by jamesr@solidstategroup.com
 * Created on 08/02/2016
 */
public class GpPatient {

    private Long id;
    private List<String> identifiers = new ArrayList<>();
    private String gpName;

    public GpPatient() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<String> identifiers) {
        this.identifiers = identifiers;
    }

    public String getGpName() {
        return gpName;
    }

    public void setGpName(String gpName) {
        this.gpName = gpName;
    }
}
