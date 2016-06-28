package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
@Entity
@Table(name = "pv_question_group")
public class QuestionGroup extends BaseModel {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "description")
    private String description;

    @Column(name = "number")
    private String number;

    @Column(name = "display_order")
    private Integer displayOrder;

    @OneToMany(mappedBy = "questionGroup", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();

    public QuestionGroup() {}

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
