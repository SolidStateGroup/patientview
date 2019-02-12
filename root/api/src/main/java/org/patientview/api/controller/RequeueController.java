package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.RequeueReport;
import org.patientview.api.service.RequeueService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Date;

@RestController
@ExcludeFromApiDoc
public class RequeueController extends BaseController<RequeueController> {

    private final RequeueService requeueService;

    @Inject
    public RequeueController(RequeueService requeueService) {

        this.requeueService = requeueService;
    }


    @RequestMapping(value = "/requeue/xkrdcsurveys", method = RequestMethod.GET)
    public RequeueReport requeueXkrdcSurveys(
            @RequestParam(value = "userId", required = false) Long userId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("start") Date start,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("end") Date end)
            throws JAXBException, DatatypeConfigurationException {

        return requeueService.xkrdcSurveys(start, end, userId);
    }
}
