package org.patientview.api.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Captcha service, used when dealing with Google reCaptcha.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 04/12/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface CaptchaService {

    /**
     * Verify the captcha String as produced by the front end by communicating with Google servers.
     * @param captcha String captcha
     * @return True if captcha is verified as ok, false if not
     */
    boolean verify(String captcha);
}
