package org.patientview.service;

import org.patientview.config.exception.ImportResourceException;
import org.patientview.persistence.model.SurveyResponse;

/**
 * SurveyResponse service, used by importer
 *
 * Created by jamesr@solidstategroup.com
 * Created on 26/05/2016
 */
public interface SurveyResponseService {

    /**
     * Add a PatientView SurveyResponse given an XML-based generated SurveyResponse, used by importer.
     *
     * @param surveyResponse SurveyResponse generated from XML
     * @return Saved PatientView SurveyResponse object
     * @throws Exception
     */
    SurveyResponse add(generated.SurveyResponse surveyResponse) throws Exception;

    /**
     * Validate a SurveyResponse. Errors include:
     *
     * Survey type must be defined
     * Survey type 'SURVEY_TYPE' is not defined
     * Survey type 'SURVEY_TYPE' does not have any questions
     * Identifier must be set
     * Date must be set
     * No patient found with identifier '1111111111'
     * Multiple identifiers found with value '1111111111'
     * Must have survey question answers
     * Must have at least one survey question answer
     * All answers must have a question type
     * Question type 'XXX1' does not match any questions for survey type 'SURVEY_TYPE'
     * Question type 'XXX1' must have a value set (is a value based question)
     * Question type 'XXX1' must have an option set (is an option based question)
     * Question type 'XXX1' must have a known option (is an option based question)
     * Question type 'XXX1' is duplicated
     * Scores must be defined
     * Score type must be defined
     * Score for type 'SCORE_TYPE' must be defined
     * Score severity must be a known severity type
     *
     * @param surveyResponse SurveyResponse to validate
     * @throws ImportResourceException
     */
    void validate(generated.SurveyResponse surveyResponse) throws ImportResourceException;

    /**
     * Hard delete all SurveyResponse entries associated with a User.
     * This will also cascade and delete SurveyResponseScore and QuestionAnswer associated
     * with deleted SurveyResponse
     *
     * @param userId a User ID to delete SurveyResponse entries for
     */
    void deleteForUser(Long userId);
}
