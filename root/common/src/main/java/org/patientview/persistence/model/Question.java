package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.persistence.model.enums.QuestionHtmlTypes;
import org.patientview.persistence.model.enums.QuestionTypes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "pv_question")
public class Question extends BaseModel {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_group_id", nullable = false)
    private QuestionGroup questionGroup;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionTypes type;

    @Column(name = "html_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionHtmlTypes htmlType;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "description")
    private String description;

    @Column(name = "number")
    private String number;

    @Column(name = "display_order")
    private Integer displayOrder;

    @OneToMany(mappedBy = "question")
    private List<QuestionOption> questionOptions = new ArrayList<>();

    public Question() {}

    public QuestionGroup getQuestionGroup() {
        return questionGroup;
    }

    public void setQuestionGroup(QuestionGroup questionGroup) {
        this.questionGroup = questionGroup;
    }

    public QuestionTypes getType() {
        return type;
    }

    public void setType(QuestionTypes type) {
        this.type = type;
    }

    public QuestionHtmlTypes getHtmlType() {
        return htmlType;
    }

    public void setHtmlType(QuestionHtmlTypes htmlType) {
        this.htmlType = htmlType;
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

    public List<QuestionOption> getQuestionOptions() {
        return questionOptions;
    }

    public void setQuestionOptions(List<QuestionOption> questionOptions) {
        this.questionOptions = questionOptions;
    }
}
