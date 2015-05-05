package org.patientview.api.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For binary files, e.g. Letters.
 * Created by jamesr@solidstategroup.com
 * Created on 05/05/2015
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface FileDataService {
    void delete(Long id);
}
