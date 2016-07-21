package org.patientview.api.model;

import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Link;

import java.util.HashSet;
import java.util.Set;

/**
 * BaseCode, reduced data for patient select own code
 * Created by jamesr@solidstategroup.com
 * Created on 27/08/2014
 */
public class BaseCode {

    private Long id;
    private String code;
    // called Name in ui
    private String description;
    // from NHS choices initially
    private String fullDescription;
    private boolean hideFromPatients = false;
    private Set<Link> links = new HashSet<>();
    private String patientFriendlyName;

    public BaseCode() {
    }

    public BaseCode(Code code) {
        this.id = code.getId();
        this.code = code.getCode();
        this.description = code.getDescription();
        this.fullDescription = code.getFullDescription();
        this.hideFromPatients = code.isHideFromPatients();
        this.links = code.getLinks();
        this.patientFriendlyName = code.getPatientFriendlyName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public boolean isHideFromPatients() {
        return hideFromPatients;
    }

    public void setHideFromPatients(boolean hideFromPatients) {
        this.hideFromPatients = hideFromPatients;
    }

    public Set<Link> getLinks() {
        return links;
    }

    public void setLinks(Set<Link> links) {
        this.links = links;
    }

    public String getPatientFriendlyName() {
        return patientFriendlyName;
    }

    public void setPatientFriendlyName(String patientFriendlyName) {
        this.patientFriendlyName = patientFriendlyName;
    }
}
