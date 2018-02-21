package org.patientview.persistence.model;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.patientview.persistence.model.types.StringJsonUserType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Models a research study that is available to a user
 */
@Entity
@Table(name = "pv_research_study_criteria")
@TypeDefs({ @TypeDef(name = "StringJsonObject", typeClass = StringJsonUserType.class) })
public class ResearchStudyCriteria {

    @OneToOne
    @JoinColumn(name = "research_study_id")
    private ResearchStudy researchStudy;

    @Type(type = "StringJsonObject")
    @Column(name = "criteria")
    private ResearchStudyCriteria researchStudyCriterias;


    public ResearchStudyCriteria getResearchStudyCriterias() {
        return researchStudyCriterias;
    }

    public void setResearchStudyCriterias(ResearchStudyCriteria researchStudyCriterias) {
        this.researchStudyCriterias = researchStudyCriterias;
    }

    public ResearchStudy getResearchStudy() {
        return researchStudy;
    }

    public void setResearchStudy(ResearchStudy researchStudy) {
        this.researchStudy = researchStudy;
    }
}
