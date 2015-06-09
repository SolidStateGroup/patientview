package org.patientview.api.service.impl;

import org.patientview.api.service.SurveyService;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.persistence.repository.SurveyRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
@Service
public class SurveyServiceImpl extends AbstractServiceImpl<SurveyServiceImpl> implements SurveyService {

    @Inject
    private SurveyRepository surveyRepository;

    @Override
    public Survey getByType(final SurveyTypes type) {
        List<Survey> surveys = surveyRepository.findByType(type);
        if (!CollectionUtils.isEmpty(surveys)) {
            return surveys.get(0);
        } else {
            return null;
        }
    }
}
