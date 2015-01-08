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
        String url = JsonUtil.pvUrl + "/migrate/user";
        try {
            Long userId = JsonUtil.jsonRequest(url, Long.class, migrationUser, HttpPost.class, true);
            if (userId != null) {
                LOG.info("Migrated username: {} OK, Pv2 Id: {}", migrationUser.getUser().getUsername(), userId);
            } else {
                LOG.error("Failed to migrate username: {}", migrationUser.getUser().getUsername());
            }
        } catch (JsonMigrationException jme) {
            LOG.error("Failed to migrate username (JsonMigrationException): {}", migrationUser.getUser().getUsername());
        } catch (JsonMigrationExistsException jee) {
            LOG.info("User {} already exists", migrationUser.getUser().getUsername());
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
    }
}
