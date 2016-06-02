package org.patientview.service;

import org.patientview.config.exception.ImportResourceException;
import uk.org.rixg.PatientRecord;

/**
 * UKRDC service, used by importer to handle data in UKRDC xml format
 *
 * Created by jamesr@solidstategroup.com
 * Created on 02/06/2016
 */
public interface UkrdcService {

    void process(PatientRecord patientRecord, String xml, Long importerUserId) throws Exception;

    /**
     * Validate UKRDC xml, including basic Patient and Surveys. Errors include:
     *
     * Patient must be defined
     * PatientNumbers must be defined
     * PatientNumbers must have at least one Number
     * PatientNumbers Number must not be empty
     * No patient found with identifier '1111111111'
     * Multiple identifiers found with value '1111111111'
     *
     * SurveyType must be defined
     * SurveyType Code must be defined
     * Survey type 'XXX' is not defined
     * Survey type 'XXX' in database does not have any questions
     * Survey Date must be set
     * Survey must have Questions
     * Survey must have at least one Question
     * All Question must have a QuestionType
     * All Question must have a QuestionType Code
     * Question type 'YYY' does not match any questions for survey type 'XXX'
     * Question type 'YYY' must have a Response set (is a value based question)
     * Question type 'YYY' must have a Response set (is an option based question)
     * Question type 'YYY' must have a known option (is an option based question)
     * Question type 'YYY' is duplicated
     * Must have at least one Question with a Response
     * Score must have ScoreType
     * Score ScoreType must have Code
     * Score must have Value
     * Score Value must be integer
     *
     * @param patientRecord UKRDC xml based object
     * @throws ImportResourceException
     */
    void validate(PatientRecord patientRecord) throws ImportResourceException;
}
