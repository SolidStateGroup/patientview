package org.patientview.service.impl;

import org.patientview.builder.SurveyBuilder;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.service.SurveyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public Survey add(generated.Survey survey) throws Exception {
        return surveyRepository.save(new SurveyBuilder(survey).build());
    }

    @Override
    @Transactional
    public Survey getByType(final String type) {
        List<Survey> surveys = surveyRepository.findByType(type);
        return !CollectionUtils.isEmpty(surveys) ? surveys.get(0) : null;
    }
}
