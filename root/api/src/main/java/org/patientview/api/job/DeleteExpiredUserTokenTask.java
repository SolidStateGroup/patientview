package org.patientview.api.job;

import org.patientview.persistence.repository.UserTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Scheduled tasks to delete expired UserToken, left when users do not log out and just close browser.
 * Expired UserTokens are removed by user on login and logout but some users may not login more than once.
 * Created by jamesr@solidstategroup.com
 * Created on 31/07/2017
 */
@Component
public class DeleteExpiredUserTokenTask {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteExpiredUserTokenTask.class);

    @Inject
    private UserTokenRepository userTokenRepository;

    /**
     * Delete expired UserToken
     */
    //@Scheduled(cron = "0 */1 * * * ?") // every minute
    @Scheduled(cron = "0 0 1 * * ?") // every day at 01:00
    @Transactional
    public void deleteExpiredUserTokens() {
        userTokenRepository.deleteExpired();
        LOG.info("Deleted expired UserTokens");
    }
}
