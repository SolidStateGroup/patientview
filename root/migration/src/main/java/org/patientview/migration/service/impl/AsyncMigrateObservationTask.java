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
public class AsyncMigrateObservationTask implements Runnable {

    MigrationUser migrationUser;

    private static final Logger LOG = LoggerFactory.getLogger(AsyncMigrateObservationTask.class);

    public AsyncMigrateObservationTask(MigrationUser migrationUser) {
        this.migrationUser = migrationUser;
    }

    public void run() {
        String url = JsonUtil.pvUrl + "/migrate/observations";
        try {
            LOG.info("Submitting " + migrationUser.getObservations().size() + " observations for Patientview 1 ID: "
                    + migrationUser.getPatientview1Id());
            JsonUtil.jsonRequest(url, Long.class, migrationUser, HttpPost.class, true);
            LOG.info("Migrated {} observations for pv1 ID: {} OK", migrationUser.getObservations().size(),
                    migrationUser.getPatientview1Id());
        } catch (JsonMigrationException jme) {
            LOG.error("Failed to migrate username (JsonMigrationException): {}", migrationUser.getUser().getUsername());
        } catch (JsonMigrationExistsException jee) {
            LOG.info("User {} already exists", migrationUser.getUser().getUsername());
        } catch (Exception e) {
            LOG.error("{}", e);
        }
    }
}
