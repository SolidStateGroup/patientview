package org.patientview.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/06/2016
 */
@Entity
@Table(name = "pv_nhschoices_condition")
public class NhschoicesCondition extends AuditModel {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "description_last_update_date")
    private Date descriptionLastUpdateDate;

    @Column(name = "introduction_url")
    private String introductionUrl;

    @Column(name = "introduction_url_status")
    private Integer introductionUrlStatus;

    @Column(name = "introduction_url_last_update_date")
    private Date introductionUrlLastUpdateDate;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "uri", nullable = false)
    private String uri;

    public NhschoicesCondition() {}

    public NhschoicesCondition(String name, String uri) {
        this.name = name;
        this.uri = uri;
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

    public Date getDescriptionLastUpdateDate() {
        return descriptionLastUpdateDate;
    }

    public void setDescriptionLastUpdateDate(Date descriptionLastUpdateDate) {
        this.descriptionLastUpdateDate = descriptionLastUpdateDate;
    }

    public String getIntroductionUrl() {
        return introductionUrl;
    }

    public void setIntroductionUrl(String introductionUrl) {
        this.introductionUrl = introductionUrl;
    }

    public Integer getIntroductionUrlStatus() {
        return introductionUrlStatus;
    }

    public void setIntroductionUrlStatus(Integer introductionUrlStatus) {
        this.introductionUrlStatus = introductionUrlStatus;
    }

    public Date getIntroductionUrlLastUpdateDate() {
        return introductionUrlLastUpdateDate;
    }

    public void setIntroductionUrlLastUpdateDate(Date introductionUrlLastUpdateDate) {
        this.introductionUrlLastUpdateDate = introductionUrlLastUpdateDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
