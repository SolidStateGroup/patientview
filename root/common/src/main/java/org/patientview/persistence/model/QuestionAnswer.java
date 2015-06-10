package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 10/06/2015
 */
@Entity
@Table(name = "pv_question_answer")
public class QuestionAnswer extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "question_option_id")
    private QuestionOption questionOption;

    @ManyToOne
    @JoinColumn(name = "symptom_score_id")
    private SymptomScore symptomScore;

    @Column(name = "value")
    private String value;

    public QuestionAnswer() {}

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public QuestionOption getQuestionOption() {
        return questionOption;
    }

    public void setQuestionOption(QuestionOption questionOption) {
        this.questionOption = questionOption;
    }

    @JsonIgnore
    public SymptomScore getSymptomScore() {
        return symptomScore;
    }

    public void setSymptomScore(SymptomScore symptomScore) {
        this.symptomScore = symptomScore;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
