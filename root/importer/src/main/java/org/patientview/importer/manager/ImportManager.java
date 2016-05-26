package org.patientview.importer.manager;

import generated.Patientview;
import generated.Survey;
import generated.SurveyResponse;
import org.patientview.config.exception.ImportResourceException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ImportManager {

    /**
     * Process a PatientView object containing patient data for import, generated from imported PatientView 1 XML.
     * @param patientview Object generated from PatientView 1 XML
     * @param xml String of XML
     * @param importerUserId Long ID of Importer User
     * @throws ImportResourceException
     */
    void process(Patientview patientview, String xml, Long importerUserId) throws ImportResourceException;

    /**
     * Process a Survey object, used when adding new Surveys, generated from imported Survey XML.
     * @param survey Survey generated from Survey XML, used to describe a new Survey
     * @param xml String of XML
     * @param importerUserId Long ID of Importer User
     * @throws ImportResourceException
     */
    void process(Survey survey, String xml, Long importerUserId) throws ImportResourceException;

    /**
     * Process a SurveyResponse object, used when adding responses to surveys, generated from imported SurveyResponse
     * XML.
     * @param surveyResponse SurveyResponse generated from SurveyResponse XML, used to describe responses to a Survey
     * @param xml String of XML
     * @param importerUserId Long ID of Importer User
     * @throws ImportResourceException
     */
    void process(SurveyResponse surveyResponse, String xml, Long importerUserId) throws ImportResourceException;

    /**
     * Validate that imported patient data meets requirements for storing, patient and group exist in PatientView.
     * @param patientview Object generated from imported PatientView 1 XML
     * @throws ImportResourceException
     */
    void validate(Patientview patientview) throws ImportResourceException;

    /**
     * Validate Survey, Errors include:
     *
     * Survey type must be defined
     * Survey type 'SURVEY_TYPE' already defined
     * Survey must have question groups
     * Survey must at least one question group
     * All question groups must contain questions
     * All question groups must contain at least one question
     * All question groups must contain text
     * All questions must have an element type
     * All questions must have a valid element type
     * All questions must have an html type
     * All questions must have a valid html type
     * All questions must contain text
     * All question options must contain text
     *
     * @param survey Survey generated from imported Survey description data
     * @throws ImportResourceException
     */
    void validate(Survey survey) throws ImportResourceException;
}
