package org.patientview.persistence.model;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Models a research study that is available to a user
 */
@Entity
@Table(name = "pv_research_study_criteria")
public class ResearchStudyCriteria extends BaseModel{

    @Column(name = "research_study_id")
    private Long researchStudy;

    @Type(type = "jsonb")
    @Column(name = "criteria", columnDefinition = "jsonb")
    private ResearchStudyCriteriaData researchStudyCriterias;


    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @OneToOne
    @JoinColumn(name = "created_by")
    private User creator;

    public ResearchStudyCriteria() {
    }

    public ResearchStudyCriteriaData getResearchStudyCriterias() {
        return researchStudyCriterias;
    }

    public void setResearchStudyCriterias(ResearchStudyCriteriaData researchStudyCriterias) {
        this.researchStudyCriterias = researchStudyCriterias;
    }

    public Long getResearchStudy() {
        return researchStudy;
    }

    public void setResearchStudy(Long researchStudy) {
        this.researchStudy = researchStudy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
}
