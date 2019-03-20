package org.patientview.persistence.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Maps a group a survey was taken under to the associated
 * unit group.
 */
@Entity
@Table(name = "pv_survey_unit")
public class SurveySendingFacility extends BaseModel {

    /**
     * Group maps to a sending facility.
     */
    @OneToOne
    @JoinColumn(name = "unit_id", nullable = false)
    private Group unit;

    /**
     * Group which a survey was taken under.
     */
    @OneToOne
    @JoinColumn(name = "survey_group_id", nullable = false)
    private Group surveyGroup;

    public Group getUnit() {
        return unit;
    }

    public void setUnit(Group unit) {
        this.unit = unit;
    }

    public Group getSurveyGroup() {
        return surveyGroup;
    }

    public void setSurveyGroup(Group surveyGroup) {
        this.surveyGroup = surveyGroup;
    }
}
