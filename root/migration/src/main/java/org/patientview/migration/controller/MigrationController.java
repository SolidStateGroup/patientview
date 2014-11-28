package org.patientview.migration.controller;

import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.service.UserDataMigrationService;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
public class MigrationController {

    @Inject
    private UserDataMigrationService userDataMigrationService;

    @Inject
    private AdminDataMigrationService adminDataMigrationService;

    @RequestMapping(value = "/bulkusers", method = RequestMethod.GET)
	public String doBulkUsers(ModelMap model) throws JsonMigrationException {
        Long usersToCreate = 10L;
        Date start = new Date();
        //RoleName role = RoleName.PATIENT;
        RoleName role = RoleName.UNIT_ADMIN;

        adminDataMigrationService.init();
        userDataMigrationService.bulkUserCreate("RENALB", "SGC04", usersToCreate, role);

        String status = "Submission of " + usersToCreate + " "  + role.toString()
                + " took " + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.";

		model.addAttribute("statusMessage", status);
		return "bulkusers";
	}

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
}