package org.patientview.migration.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class StatusController {

    @RequestMapping(value = "/status", method = RequestMethod.GET)
	public String getStatus(ModelMap model) {

		model.addAttribute("statusMessage", "migration server online");
		return "status";
	}
}