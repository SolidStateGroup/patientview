package org.patientview.migration.controller;

import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.service.GroupDataMigrationService;
import org.patientview.migration.service.UserDataMigrationService;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.persistence.model.enums.RoleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
    private GroupDataMigrationService groupDataMigrationService;

    @Inject
    private AdminDataMigrationService adminDataMigrationService;

    @Inject
    private ThreadPoolTaskExecutor asyncTaskExecutor;

    private static final Logger LOG = LoggerFactory.getLogger(MigrationController.class);

    @RequestMapping(value = "/step1-groups", method = RequestMethod.GET)
    public String doStep1Groups(ModelMap modelMap) throws JsonMigrationException {
        Date start = new Date();
        groupDataMigrationService.createGroups();

        String status = "Migration of Groups "
                + " took " + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.";

        modelMap.addAttribute("statusMessage", status);
        return "groups";
    }

    @RequestMapping(value = "/step2-static-data", method = RequestMethod.GET)
    public String doStep2StaticData(ModelMap modelMap) throws JsonMigrationException {
        Date start = new Date();
        adminDataMigrationService.init();
        adminDataMigrationService.migrate();

        String status = "Migration of Codes, Result Headings, Join Requests "
                + " took " + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.";

        modelMap.addAttribute("statusMessage", status);
        return "staticdata";
    }

    @RequestMapping(value = "/step3-group-stats", method = RequestMethod.GET)
    public String doStep3groupstats(ModelMap modelMap) throws JsonMigrationException {
        Date start = new Date();
        groupDataMigrationService.createStatistics();

        String status = "Migration of Group Statistics "
                + " took " + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.";

        modelMap.addAttribute("statusMessage", status);
        return "groupstats";
    }

    @RequestMapping(value = "/step4-users", method = RequestMethod.GET)
    public String doStep4Users(ModelMap modelMap) throws JsonMigrationException {
        asyncTaskExecutor.submit(new Runnable() {
            public void run() {
                try {
                    Date start = new Date();

                    userDataMigrationService.migrate();

                    LOG.info("Migration of Users took "
                            + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.");
                } catch (JsonMigrationException jme) {
                    LOG.error("User Migration exception: {}", jme);
                }
            }
        });

        modelMap.addAttribute("statusMessage", "Started User Migration");
        return "users";
    }

    @RequestMapping(value = "/bulkusers", method = RequestMethod.GET)
	public String doBulkUsers(ModelMap modelMap) throws JsonMigrationException {
        Long usersToCreate = 10L;
        Date start = new Date();
        //RoleName role = RoleName.PATIENT;
        RoleName role = RoleName.UNIT_ADMIN;

        adminDataMigrationService.init();
        userDataMigrationService.bulkUserCreate("RENALB", "SGC04", usersToCreate, role);

        String status = "Submission of " + usersToCreate + " "  + role.toString()
                + " took " + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.";

        modelMap.addAttribute("statusMessage", status);
		return "bulkusers";
	}

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
}