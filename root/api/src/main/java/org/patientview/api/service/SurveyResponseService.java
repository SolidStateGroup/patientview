package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.enums.SurveyTypes;

import java.util.List;

/**
 * SurveyResponse service, used by IBD
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2015
 */
public interface SurveyResponseService {

    @UserOnly
    void add(Long userId, SurveyResponse surveyResponse) throws ResourceForbiddenException, ResourceNotFoundException;

    @UserOnly
    List<SurveyResponse> getByUserIdAndSurveyType(Long userId, SurveyTypes surveyType) throws ResourceNotFoundException;

    @UserOnly
    SurveyResponse getSurveyResponse(Long userId, Long surveyResponseId) throws ResourceNotFoundException;
}
