package org.patientview.api.service;

import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Link;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface CodeService extends CrudService<Code> {

    List<Code> getAllCodes();

    Code cloneCode(Long codeId);

    Link addLink(Long codeId, Link link);

}
