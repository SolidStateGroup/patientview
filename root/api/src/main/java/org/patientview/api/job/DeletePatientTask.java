package org.patientview.api.job;

import org.patientview.api.service.UserService;
import org.patientview.persistence.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * DeletePatientTask is an async task to delete patient.
 */
@Component
public class DeletePatientTask {

    private static final Logger LOG = LoggerFactory.getLogger(DeletePatientTask.class);

    @Inject
    private UserService userService;


    @Async
    public void deletePatient(Long patientId, User admin) {

        try {
            userService.deletePatient(patientId, admin);
        } catch (Exception e) {
            LOG.error("Error in DeletePatientTask.deletePatient() task {}", e);
        }
    }
}
