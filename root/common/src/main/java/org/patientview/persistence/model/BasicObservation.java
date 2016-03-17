package org.patientview.persistence.model;

import java.util.Date;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 22/10/2014
 */
public class BasicObservation {

    private UUID logicalId;
    private Date applies;
    private String code;

    public BasicObservation() { }

    public BasicObservation(UUID logicalId, Date applies, String code) {
        this.logicalId = logicalId;
        this.applies = applies;
        this.code = code;
    }

    public UUID getLogicalId() {
        return logicalId;
    }

    public void setLogicalId(UUID logicalId) {
        this.logicalId = logicalId;
    }

    public Date getApplies() {
        return applies;
    }

    public void setApplies(Date applies) {
        this.applies = applies;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
