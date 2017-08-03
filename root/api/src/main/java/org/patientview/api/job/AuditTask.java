package org.patientview.api.job;

import org.patientview.service.AuditService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.UktException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled tasks to clean up audit table, clearing XML older than 3 months.
 * Created by jamesr@solidstategroup.com
 * Created on 24/04/2015
 */
@Component
public class AuditTask {

    private static final Logger LOG = LoggerFactory.getLogger(AuditTask.class);

    @Inject
    private AuditService auditService;

    @Inject
    private Properties properties;

    /**
     * Set audit xml column to NULL if older than 90 days, set in properties
     */
    //@Scheduled(cron = "0 */1 * * * ?") // every minute
    @Scheduled(cron = "0 0 2 * * ?") // every day at 02:00
    public void removeOldAuditXml() throws ResourceNotFoundException, FhirResourceException, UktException {
        try {
            LOG.info("Cleaning old xml from audit data");
            Boolean removeOldAuditXml = Boolean.parseBoolean(properties.getProperty("remove.old.audit.xml"));

            if (removeOldAuditXml) {
                Date start = new Date();
                auditService.removeOldAuditXml();
                LOG.info("Cleaning old xml from audit data took "
                        + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.");
            }
        } catch (Exception e) {
            LOG.error("Error cleaning old xml from audit data: " + e.getMessage(), e);
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
}
