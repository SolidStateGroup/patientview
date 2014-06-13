package org.patientview.api.service;

import org.patientview.persistence.model.UserToken;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AuthenticationService {

    UserToken authenticate(String username, String password);
}
