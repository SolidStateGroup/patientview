package org.patientview.api.service;

import org.patientview.api.model.Email;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
public interface EmailService {
    boolean sendEmail(Email email);
}
