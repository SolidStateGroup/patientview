package org.patientview.service;

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
}
