package org.patientview.api.service.impl;

import org.patientview.api.service.InsDiaryAuditService;
import org.patientview.persistence.model.InsDiaryAuditLog;
import org.patientview.persistence.repository.InsDiaryAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * INS Diary audit service to capture changes to the INS recordings.
 *
 * This does not capture the actual changes just records patient reference who has changed in the past 24 hour.
 */
@Service
@Transactional
public class InsDiaryAuditServiceImpl extends AbstractServiceImpl<InsDiaryAuditServiceImpl>
        implements InsDiaryAuditService {

    @Inject
    private InsDiaryAuditLogRepository insDiaryAuditLogRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public InsDiaryAuditLog add(Long patientId) {

        List<InsDiaryAuditLog> logs = insDiaryAuditLogRepository.findByPatientId(patientId);
        if (CollectionUtils.isEmpty(logs)) {
            InsDiaryAuditLog logToAdd = new InsDiaryAuditLog();
            logToAdd.setPatientId(patientId);
            logToAdd.setCreationDate(new Date());
            return insDiaryAuditLogRepository.save(logToAdd);
        } else {
            return logs.get(0);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteByPatient(Long patientId) {
        insDiaryAuditLogRepository.deleteByPatientId(patientId);
    }

    @Override
    public List<InsDiaryAuditLog> findAll() {
        return insDiaryAuditLogRepository.findAll();
    }
}
