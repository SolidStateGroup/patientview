package org.patientview.migration.controller;

import org.patientview.migration.service.GroupDataMigrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

@Controller
public class StatusController {

    @Inject
    private GroupDataMigrationService groupDataMigrationService;

    @RequestMapping(value = "/status", method = RequestMethod.GET)
	public String getStatus(ModelMap model) {

        try {
            int groupCount = groupDataMigrationService.getGroupCount();
            model.addAttribute("statusMessage", "migration server online, " + groupCount + " pv1 groups");
        } catch (Exception e) {
            model.addAttribute("statusMessage", "cannot connect to MySql");
        }

		return "status";
	}
}