package org.patientview.importer.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/02/2015
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface EmailService {
    void sendErrorEmail(String errorMessage, String nhsNo, String unitCode);
}
