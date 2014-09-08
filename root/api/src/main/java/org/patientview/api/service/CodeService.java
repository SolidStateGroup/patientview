package org.patientview.api.service;

import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Lookup;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface CodeService extends CrudService<Code> {

    Code add(Code code) throws EntityExistsException;

    Page<Code> getAllCodes(GetParameters getParameters);

    Code cloneCode(Long codeId);

    Link addLink(Long codeId, Link link);

    List<Code> findAllByCodeAndType(String code, Lookup codeType);
}
