package org.patientview.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Models a research study that is available to a user
 */
@Entity
@Table(name = "pv_research_study")
public class ResearchStudy extends BaseModel {

    @Column(name = "name")
    private String researchName;


    public String getResearchName() {
        return researchName;
    }

    public void setResearchName(String researchName) {
        this.researchName = researchName;
    }
}
