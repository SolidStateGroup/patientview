package org.patientview.migration.service.impl;

import org.apache.http.client.methods.HttpPost;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.persistence.model.MigrationUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/10/2014
 */
public class AsyncMigrateUserTask implements Runnable {

    MigrationUser migrationUser;

    private static final Logger LOG = LoggerFactory.getLogger(AsyncMigrateUserTask.class);

    public AsyncMigrateUserTask(MigrationUser migrationUser) {
        this.migrationUser = migrationUser;
    }

    public void run() {
        String url = JsonUtil.pvUrl + "/user/migrate";
        try {
            Long userId = JsonUtil.jsonRequest(url, Long.class, migrationUser, HttpPost.class, true);
            //JsonUtil.gsonPost(url, migrationUser);
            LOG.info("Sent user: {} OK, Id: {}", migrationUser.getUser().getUsername(), userId);
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create user: {}", migrationUser.getUser().getUsername());
        } catch (JsonMigrationExistsException jee) {
            LOG.info("User {} already exists", migrationUser.getUser().getUsername());
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
    }
}
