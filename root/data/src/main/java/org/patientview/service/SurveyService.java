package org.patientview.service;

import org.patientview.config.exception.ImportResourceException;
import org.patientview.persistence.model.Survey;

/**
 * Survey service, used by IBD
 *
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
public interface SurveyService {

    /**
     * Add a PatientView Survey given an XML-based generated Survey, used by importer.
     *
     * @param survey Survey generated from XML
     * @return Saved PatientView Survey object
     * @throws Exception
     */
    Survey add(generated.Survey survey) throws Exception;

    /**
     * Get the first stored instance of a Survey given a type.
     *
     * @param type String type of Survey
     * @return First instance of a Survey given type
     */
    Survey getByType(String type);

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
    void validate(generated.Survey survey) throws ImportResourceException;
}
