package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.SurveyTypes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
@Entity
@Table(name = "pv_survey")
public class Survey extends BaseModel {

    @Column(name = "description")
    private String description;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SurveyTypes type;

    @OneToMany(mappedBy = "survey")
    private List<QuestionGroup> questionGroups = new ArrayList<>();

    public Survey() {}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SurveyTypes getType() {
        return type;
    }

    public void setType(SurveyTypes type) {
        this.type = type;
    }

    public List<QuestionGroup> getQuestionGroups() {
        return questionGroups;
    }

    public void setQuestionGroups(List<QuestionGroup> questionGroups) {
        this.questionGroups = questionGroups;
    }
}