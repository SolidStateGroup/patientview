package org.patientview.persistence.model;

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
public class ResearchStudyCriteria extends BaseModel {

//    @OneToOne
//    @JoinColumn(name = "research_study_id")
//    private ResearchStudy researchStudy;


    @Column(name = "gender")
    private char gender;

    @Column(name = "from_age")
    private Integer fromAge;

    @Column(name = "to_age")
    private Integer toAge;



    public Integer getFromAge() {
        return fromAge;
    }

    public void setFromAge(Integer fromAge) {
        this.fromAge = fromAge;
    }

    public Integer getToAge() {
        return toAge;
    }

    public void setToAge(Integer toAge) {
        this.toAge = toAge;
    }
}
