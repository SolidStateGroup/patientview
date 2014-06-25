package org.patientview.api.service;

import org.patientview.persistence.model.Code;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface CodeService {
    
    Code createCode(Code code);

    Code getCode(Long codeId);

    Code saveCode(Code code);

    void deleteCode(Long codeId);
}
