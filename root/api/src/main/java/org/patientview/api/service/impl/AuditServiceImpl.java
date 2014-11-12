package org.patientview.api.service.impl;

import org.patientview.api.service.AuditService;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.repository.AuditRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * TODO Sprint 3 factor into aspect
 * A service to audit when the security context cannot be used (ie Logon
 *
 * Created by james@solidstategroup.com
 * Created on 06/08/2014
 */
@Service
public class AuditServiceImpl extends AbstractServiceImpl<AuditServiceImpl> implements AuditService {

    @Inject
    private AuditRepository auditRepository;

    @Override
    public Audit save(Audit audit) {
        return auditRepository.save(audit);
    }

    @Override
    public List<Audit> findAll() {
        return auditRepository.findAll();
    }

}
