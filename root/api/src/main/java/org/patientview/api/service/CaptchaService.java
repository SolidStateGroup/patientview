package org.patientview.api.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/12/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface CaptchaService {

    boolean verify(String captcha);
}
