package org.patientview.api.job;

import org.patientview.api.service.AlertService;
import org.patientview.api.service.UserService;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AlertTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;

/**
 * DeletePatientTask is an async task to delete patient.
 */
@Component
public class DeletePatientTask {

    private static final Logger LOG = LoggerFactory.getLogger(DeletePatientTask.class);

    @Inject
    private UserService userService;

    @Lazy
    @Inject
    private AlertService alertService;


    @Async
    public void deletePatient(User patient, User admin) {

        String message;

        Alert newAlert = new Alert();
        newAlert.setUser(admin);
        newAlert.setWebAlert(true);
        newAlert.setWebAlertViewed(false);
        newAlert.setEmailAlert(false);
        newAlert.setEmailAlertSent(true);
        newAlert.setMobileAlert(false);
        newAlert.setMobileAlertSent(true);
        newAlert.setCreated(new Date());
        newAlert.setCreator(admin);

        try {
            userService.deletePatient(patient.getId(), admin);

            message = "Patient with Username <b>" + patient.getUsername() + "</b> has been permanently deleted.";
            newAlert.setAlertType(AlertTypes.PATIENT_DELETED);
            newAlert.setLatestValue(message);
        } catch (Exception e) {
            LOG.error("Error in DeletePatientTask.deletePatient() task {}", e);
            message = "There was an error deleting Patient with Username <b>" + patient.getUsername() + "</b>. " +
                    "If this problem persists, please contact PatientView Central Support.";
            newAlert.setAlertType(AlertTypes.PATIENT_DELETE_FAILED);
            newAlert.setLatestValue(message);
        }

        // create Patient Delete alert
        alertService.saveAlert(newAlert);
    }
}
