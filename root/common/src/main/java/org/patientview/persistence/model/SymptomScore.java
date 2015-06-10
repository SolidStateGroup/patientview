package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.ScoreSeverity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2015
 */
@Entity
@Table(name = "pv_symptom_score")
public class SymptomScore extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "score", columnDefinition="numeric", precision=10, scale=4)
    private Double score;

    @Column(name = "severity")
    @Enumerated(EnumType.STRING)
    private ScoreSeverity severity;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @OneToMany(mappedBy = "symptomScore")
    private List<QuestionAnswer> questionAnswers = new ArrayList<>();

    public SymptomScore() {}

    public SymptomScore(User user, Double score, ScoreSeverity severity, Date date) {
        this.user = user;
        this.score = score;
        this.severity = severity;
        this.date = date;
    }

    public Double getScore() {
        return score;
    }

    public ScoreSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ScoreSeverity severity) {
        this.severity = severity;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public List<QuestionAnswer> getQuestionAnswers() {
        return questionAnswers;
    }

    public void setQuestionAnswers(List<QuestionAnswer> questionAnswers) {
        this.questionAnswers = questionAnswers;
    }
}
