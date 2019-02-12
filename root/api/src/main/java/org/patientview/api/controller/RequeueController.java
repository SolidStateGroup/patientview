package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.RequeueReport;
import org.patientview.api.service.ExternalServiceService;
import org.patientview.api.util.ApiUtil;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.enums.ExternalServices;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.service.UkrdcService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.patientview.persistence.model.enums.SurveyTypes.EQ5D5L;
import static org.patientview.persistence.model.enums.SurveyTypes.POS_S;

@RestController
@ExcludeFromApiDoc
public class RequeueController extends BaseController<RequeueController> {

    private final UkrdcService ukrdcService;
    private ExternalServiceService externalServiceService;
    private final SurveyResponseRepository surveyResponseRepository;

    @Inject
    public RequeueController(UkrdcService ukrdcService,
                             ExternalServiceService externalServiceService,
                             SurveyResponseRepository surveyResponseRepository) {

        this.surveyResponseRepository = surveyResponseRepository;
        this.externalServiceService = externalServiceService;
        this.ukrdcService = ukrdcService;
    }

    @RequestMapping(value = "/requeue/xkrdcsurveys", method = RequestMethod.GET)
    public RequeueReport requeueXkrdcSurveys(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("start") Date start,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("end") Date end)
            throws JAXBException, DatatypeConfigurationException {

        List<SurveyResponse> surveyResponses = surveyResponseRepository
                .findByDateBetweenAndSurveyIn(start, end, asList(POS_S.getName(), EQ5D5L.getName()));

        if (surveyResponses.size() == 0) {

            return new RequeueReport(0);
        }

        for (SurveyResponse surveyResponse : surveyResponses) {

            String xml = ukrdcService.buildSurveyXml(surveyResponse, surveyResponse.getSurvey().getType());
            externalServiceService.addToQueue(ExternalServices.SURVEY_NOTIFICATION, xml, ApiUtil.getCurrentUser(), new Date());
        }

        return new RequeueReport(surveyResponses.size());
    }
}
