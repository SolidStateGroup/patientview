package org.patientview.builder;

import generated.SurveyResponse;

/**
 * Build a PatientView SurveyResponse given XML based generated SurveyResponse object.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 19/04/2016
 */
public class SurveyResponseBuilder {

    private SurveyResponse surveyResponse;

    public SurveyResponseBuilder(SurveyResponse surveyResponse) {
        this.surveyResponse = surveyResponse;
    }

    public org.patientview.persistence.model.SurveyResponse build() throws Exception {
        org.patientview.persistence.model.SurveyResponse surveyResponse
                = new org.patientview.persistence.model.SurveyResponse();

        // todo

        return surveyResponse;
    }
}
