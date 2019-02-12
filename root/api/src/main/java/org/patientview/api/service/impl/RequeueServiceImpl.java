package org.patientview.api.service.impl;

import org.patientview.api.model.RequeueReport;
import org.patientview.api.service.ExternalServiceService;
import org.patientview.api.service.RequeueService;
import org.patientview.api.util.ApiUtil;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.enums.ExternalServices;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.service.UkrdcService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.patientview.persistence.model.enums.SurveyTypes.EQ5D5L;
import static org.patientview.persistence.model.enums.SurveyTypes.POS_S;

@Service
public class RequeueServiceImpl implements RequeueService {

    private final UkrdcService ukrdcService;
    private final SurveyResponseRepository surveyResponseRepository;
    private ExternalServiceService externalServiceService;

    @Inject
    public RequeueServiceImpl(UkrdcService ukrdcService,
                              ExternalServiceService externalServiceService,
                              SurveyResponseRepository surveyResponseRepository) {
        this.ukrdcService = ukrdcService;
        this.externalServiceService = externalServiceService;
        this.surveyResponseRepository = surveyResponseRepository;
    }

    @Override
    public RequeueReport xkrdcSurveys(Date start, Date end, String userId)
            throws JAXBException, DatatypeConfigurationException {

        List<SurveyResponse> surveyResponses = buildSurveyResponses(start, end, userId);

        if (surveyResponses.size() == 0) {

            return new RequeueReport(0);
        }

        for (SurveyResponse surveyResponse : surveyResponses) {

            String xml = ukrdcService.buildSurveyXml(surveyResponse, surveyResponse.getSurvey().getType());
            externalServiceService.addToQueue(ExternalServices.SURVEY_NOTIFICATION, xml, ApiUtil.getCurrentUser(), new Date());
        }

        return new RequeueReport(surveyResponses.size());
    }

    private List<SurveyResponse> buildSurveyResponses(Date start, Date end, String userId) {

        if (userId == null) {
            return surveyResponseRepository
                    .findByDateBetweenAndSurveyIn(start, end, asList(POS_S.getName(), EQ5D5L.getName()));
        }

        return surveyResponseRepository
                .findByDateBetweenAndSurveyInAndUser(start, end, asList(POS_S.getName(), EQ5D5L.getName()), userId);
    }
}
