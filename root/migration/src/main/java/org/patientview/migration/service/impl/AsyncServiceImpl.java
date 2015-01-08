package org.patientview.migration.service.impl;

import org.apache.http.client.methods.HttpPost;
import org.patientview.migration.service.AsyncService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.persistence.model.MigrationUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/10/2014
 */
public class AsyncServiceImpl implements AsyncService {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncServiceImpl.class);

    @Async
    public void callApiMigrateUser(MigrationUser user) throws Exception {
        String url = JsonUtil.pvUrl + "/user/migrate";
        try {
            JsonUtil.jsonRequest(url, Long.class, user, HttpPost.class, true);
            //JsonUtil.gsonPost(url, user);
            LOG.info("Sent user: {} OK", user.getUser().getUsername());
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create user: {}", user.getUser().getUsername());
        } catch (JsonMigrationExistsException jee) {
            LOG.info("User {} already exists", user.getUser().getUsername());
        }
    }
}
