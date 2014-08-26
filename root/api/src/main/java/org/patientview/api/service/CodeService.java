package org.patientview.api.service;

import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Link;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface CodeService extends CrudService<Code> {

    Page<Code> getAllCodes(Pageable pageable, String filterText, String[] codeTypes, String[] standardTypes);

    Code cloneCode(Long codeId);

    Link addLink(Long codeId, Link link);

}
