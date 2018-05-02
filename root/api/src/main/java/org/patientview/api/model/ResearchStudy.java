package org.patientview.api.model;

import lombok.Getter;
import lombok.Setter;
import org.patientview.persistence.model.BaseModel;
import org.patientview.persistence.model.ResearchStudyCriteria;
import org.patientview.persistence.model.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;

/**
 * Models a research study criteria that is available to a user
 */
@Getter
@Setter
public class ResearchStudy extends BaseModel {
    private String researchName;
    private String description;
    private Date createdDate;
    private BaseUser creator;
    private Date lastUpdate;
    private BaseUser lastUpdater;
    private Date availableFrom;
    private Date availableTo;
    private String contactAddress;
    private String contactEmail;
    private String contactName;
    private String contactPhone;
    private ResearchStudyCriteria[] criteria;


    public ResearchStudy(org.patientview.persistence.model.ResearchStudy researchStudy) {
        this.researchName = researchStudy.getResearchName();
        this.description = researchStudy.getDescription();
        this.createdDate = researchStudy.getCreatedDate();
        this.creator = new BaseUser(researchStudy.getCreator());
        this.lastUpdate = researchStudy.getLastUpdate();
        this.lastUpdater = new BaseUser(researchStudy.getLastUpdater());
        this.availableFrom = researchStudy.getAvailableFrom();
        this.availableTo = researchStudy.getAvailableTo();
        this.contactAddress = researchStudy.getContactAddress();
        this.contactEmail = researchStudy.getContactEmail();
        this.contactName = researchStudy.getContactName();
        this.contactPhone = researchStudy.getContactPhone();
        this.criteria = researchStudy.getCriteria();
    }
}
