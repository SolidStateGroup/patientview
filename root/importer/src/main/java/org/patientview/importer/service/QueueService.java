package org.patientview.importer.service;

import generated.Patientview;
import generated.Survey;
import generated.SurveyResponse;
import org.patientview.config.exception.ImportResourceException;
import uk.org.rixg.PatientRecord;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
public interface QueueService {
    // UKRDC
    void importRecord(PatientRecord patientRecord) throws ImportResourceException;

    void importRecord(Patientview patientview) throws ImportResourceException;

    void importRecord(Survey survey) throws ImportResourceException;

    void importRecord(SurveyResponse surveyResponse) throws ImportResourceException;
}
