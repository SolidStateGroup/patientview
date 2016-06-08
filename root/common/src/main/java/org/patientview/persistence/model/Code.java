package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.CodeSourceTypes;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Entity
@Table(name = "pv_code")
public class Code extends AuditModel {

    @Column(name = "code")
    private String code;

    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup codeType;

    @Column(name = "display_order" )
    private Integer displayOrder;

    // called Name in ui
    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "code", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<CodeExternalStandard> externalStandards = new HashSet<>();

    // from NHS choices initially
    @Column(name = "full_description")
    private String fullDescription;

    @Column(name = "hide_from_patients")
    private boolean hideFromPatients = false;

    @OneToMany(mappedBy = "code", cascade = CascadeType.ALL)
    private Set<Link> links = new HashSet<>();

    // used for PATIENTVIEW code standard Codes, from NHS choices initially
    @Column(name = "patient_friendly_name")
    private String patientFriendlyName;

    // used when comparing to NHS Choices
    @Column(name = "removed_externally")
    private boolean removedExternally = false;

    @Column(name = "source_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CodeSourceTypes sourceType = CodeSourceTypes.PATIENTVIEW;

    @OneToOne
    @JoinColumn(name = "standard_type_id")
    private Lookup standardType;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Lookup getCodeType() {
        return codeType;
    }

    public void setCodeType(Lookup codeType) {
        this.codeType = codeType;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<CodeExternalStandard> getExternalStandards() {
        return externalStandards;
    }

    public void setExternalStandards(Set<CodeExternalStandard> externalStandards) {
        this.externalStandards = externalStandards;
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

    public boolean isRemovedExternally() {
        return removedExternally;
    }

    public void setRemovedExternally(boolean removedExternally) {
        this.removedExternally = removedExternally;
    }

    public CodeSourceTypes getSourceType() {
        return sourceType;
    }

    public void setSourceType(CodeSourceTypes sourceType) {
        this.sourceType = sourceType;
    }

    public Lookup getStandardType() {
        return standardType;
    }

    public void setStandardType(Lookup standardType) {
        this.standardType = standardType;
    }
}
