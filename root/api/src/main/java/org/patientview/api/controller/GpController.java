package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.GpService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * RESTful interface for handling GP logins and managing required data, including GP data from external sources
 *
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@ExcludeFromApiDoc
@RestController
public class GpController extends BaseController<GpController> {

    @Inject
    private GpService gpService;

    /**
     * Update the GP master table from external sources (XLS files)
     */
    @RequestMapping(value = "/gp/updatemastertable", method = RequestMethod.POST)
    @ResponseBody
    public void updateMasterTable() {
        gpService.updateMasterTable();
    }
}
