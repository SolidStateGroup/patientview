package org.patientview.api.model;

import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/05/2016
 */
public class SurveyFeedback {

    private String feedback;
    private BaseUser user;
    private Date created;
    private BaseUser creator;

    public SurveyFeedback(org.patientview.persistence.model.SurveyFeedback surveyFeedback) {
        setFeedback(surveyFeedback.getFeedback());
        if (surveyFeedback.getUser() != null) {
            setUser(new BaseUser(surveyFeedback.getUser()));
        }
        setCreated(surveyFeedback.getCreated());
        if (surveyFeedback.getCreator() != null) {
            setCreator(new BaseUser(surveyFeedback.getCreator()));
        }
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public BaseUser getUser() {
        return user;
    }

    public void setUser(BaseUser user) {
        this.user = user;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public BaseUser getCreator() {
        return creator;
    }

    public void setCreator(BaseUser creator) {
        this.creator = creator;
    }
}
