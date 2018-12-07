package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.persistence.model.enums.QuestionElementTypes;
import org.patientview.persistence.model.enums.QuestionHtmlTypes;

import javax.persistence.CascadeType;
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

    @Column(name = "element_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionElementTypes elementType;

    @Column(name = "html_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionHtmlTypes htmlType;

    /**
     * If the question as not predefined text set this
     * flag to true.
     */
    @Column(name = "custom_question")
    private boolean customQuestion;

    @Column(name = "type")
    private String type;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "description")
    private String description;

    @Column(name = "number")
    private String number;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "range_start")
    private Integer rangeStart;

    @Column(name = "range_end")
    private Integer rangeEnd;

    @Column(name = "range_start_description")
    private String rangeStartDescription;

    @Column(name = "range_end_description")
    private String rangeEndDescription;

    @Column(name = "help_link")
    private String helpLink;

    @OneToMany(mappedBy = "question", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    private List<QuestionOption> questionOptions = new ArrayList<>();

    @Column(name = "required")
    private boolean required;

    public Question() {}

    public Question(String type) {
        this.type = type;
    }

    public QuestionGroup getQuestionGroup() {
        return questionGroup;
    }

    public void setQuestionGroup(QuestionGroup questionGroup) {
        this.questionGroup = questionGroup;
    }

    public QuestionElementTypes getElementType() {
        return elementType;
    }

    public void setElementType(QuestionElementTypes elementType) {
        this.elementType = elementType;
    }

    public QuestionHtmlTypes getHtmlType() {
        return htmlType;
    }

    public void setHtmlType(QuestionHtmlTypes htmlType) {
        this.htmlType = htmlType;
    }

    public boolean getCustomQuestion() {
        return customQuestion;
    }

    public void setCustomQuestion(boolean customQuestion) {
        this.customQuestion = customQuestion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Integer getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(Integer rangeStart) {
        this.rangeStart = rangeStart;
    }

    public Integer getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(Integer rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public String getRangeStartDescription() {
        return rangeStartDescription;
    }

    public void setRangeStartDescription(String rangeStartDescription) {
        this.rangeStartDescription = rangeStartDescription;
    }

    public String getRangeEndDescription() {
        return rangeEndDescription;
    }

    public void setRangeEndDescription(String rangeEndDescription) {
        this.rangeEndDescription = rangeEndDescription;
    }

    public String getHelpLink() {
        return helpLink;
    }

    public void setHelpLink(String helpLink) {
        this.helpLink = helpLink;
    }

    public List<QuestionOption> getQuestionOptions() {
        return questionOptions;
    }

    public void setQuestionOptions(List<QuestionOption> questionOptions) {
        this.questionOptions = questionOptions;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
