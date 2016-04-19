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
     * @param survey Object generated from Survey XML, used to describe a new Survey
     * @throws ImportResourceException
     */
    void process(Survey survey) throws ImportResourceException;

    void process(SurveyResponse surveyResponse) throws ImportResourceException;

    /**
     * Validate that imported patient data meets requirements for storing, patient and group exist in PatientView.
     * @param patientview Object generated from imported PatientView 1 XML
     * @throws ImportResourceException
     */
    void validate(Patientview patientview) throws ImportResourceException;

    /**
     * Validate that imported Survey meets requirements for storing.
     * @param survey Object generated from imported Survey description data
     * @throws ImportResourceException
     */
    void validate(Survey survey) throws ImportResourceException;

    void validate(SurveyResponse surveyResponse) throws ImportResourceException;
}
