package org.patientview.persistence.model;

import com.google.gson.JsonObject;
import org.hibernate.annotations.Parameter;
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

    @OneToOne
    @JoinColumn(name = "research_study_id")
    private ResearchStudy researchStudy;


    @Column(name = "criteria")
    @Type(type = "org.patientview.persistence.model.types.StringJsonUserType",
            parameters = {@Parameter(name = "classType",
                    value = "org.patientview.persistence.model.ResearchStudyCriteriaData")})
    private JsonObject researchStudyCriterias;


    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @OneToOne
    @JoinColumn(name = "created_by")
    private User creator;

    public ResearchStudyCriteria() {
    }


    public JsonObject getResearchStudyCriterias() {
        return researchStudyCriterias;
    }

    public void setResearchStudyCriterias(JsonObject researchStudyCriterias) {
        this.researchStudyCriterias = researchStudyCriterias;
    }

    public ResearchStudy getResearchStudy() {
        return researchStudy;
    }

    public void setResearchStudy(ResearchStudy researchStudy) {
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
