package org.patientview.api.service;

import org.patientview.api.controller.model.Email;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
public interface EmailService {
    public boolean sendEmail(Email email);
}
