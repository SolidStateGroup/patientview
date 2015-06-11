package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.persistence.model.enums.QuestionOptionTypes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
@Entity
@Table(name = "pv_question_option")
public class QuestionOption extends BaseModel {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "description")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "score")
    private Integer score;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private QuestionOptionTypes type;

    public QuestionOption() {}

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public QuestionOptionTypes getType() {
        return type;
    }

    public void setType(QuestionOptionTypes type) {
        this.type = type;
    }
}
