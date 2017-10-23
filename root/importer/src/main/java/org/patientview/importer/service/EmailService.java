package org.patientview.importer.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/02/2015
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface EmailService {

    /**
     * Send importer error message to pv admins or central support using email service. If onlyToCentralSupport is true
     * then no attempt will be made to contact unit admins.
     * @param message String error message
     * @param identifier String patient identifier, usually nhs number
     * @param groupCode String group code
     * @param onlyToCentralSupport true if email should only be sent to central support
     */
    void sendErrorEmail(String message, String identifier, String groupCode, boolean onlyToCentralSupport);
}
