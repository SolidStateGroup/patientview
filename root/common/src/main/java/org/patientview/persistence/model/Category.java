package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/06/2016
 */
@Entity
@Table(name = "pv_category")
public class Category extends BaseModel {

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "icd10_description", nullable = false)
    private String icd10Description;

    @Column(name = "friendly_description", nullable = false)
    private String friendlyDescription;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;

    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = {CascadeType.ALL})
    private Set<CodeCategory> codeCategories = new HashSet<>();

    public Category() {}

    public Category(Integer number, String icd10Description, String friendlyDescription) {
        this.number = number;
        this.icd10Description = icd10Description;
        this.friendlyDescription = friendlyDescription;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getIcd10Description() {
        return icd10Description;
    }

    public void setIcd10Description(String icd10Description) {
        this.icd10Description = icd10Description;
    }

    public String getFriendlyDescription() {
        return friendlyDescription;
    }

    public void setFriendlyDescription(String friendlyDescription) {
        this.friendlyDescription = friendlyDescription;
    }

    public Set<CodeCategory> getCodeCategories() {
        return codeCategories;
    }

    public void setCodeCategories(Set<CodeCategory> codeCategories) {
        this.codeCategories = codeCategories;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
