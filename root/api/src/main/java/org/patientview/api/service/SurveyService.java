package org.patientview.api.service;

import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.enums.SurveyTypes;

/**
 * Survey service, used by IBD
 *
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
public interface SurveyService {

    Survey getByType(SurveyTypes type);
}
