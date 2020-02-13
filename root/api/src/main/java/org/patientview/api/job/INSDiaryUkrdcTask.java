package org.patientview.api.job;

import org.patientview.api.service.ExternalServiceService;
import org.patientview.api.service.HospitalisationService;
import org.patientview.api.service.ImmunisationService;
import org.patientview.api.service.InsDiaryAuditService;
import org.patientview.api.service.InsDiaryService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Hospitalisation;
import org.patientview.persistence.model.Immunisation;
import org.patientview.persistence.model.InsDiaryAuditLog;
import org.patientview.persistence.model.InsDiaryRecord;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ExternalServices;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.service.UkrdcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Scheduled tasks to retrieve check any changes to INS diary recordings for the past 24 hour,
 * build entire INS records as XML and send them to the queue to be processed and send out to UKRDC .
 */
@Component
public class INSDiaryUkrdcTask {

    private static final Logger LOG = LoggerFactory.getLogger(INSDiaryUkrdcTask.class);

    @Inject
    private UkrdcService ukrdcService;

    @Inject
    private ExternalServiceService externalServiceService;

    @Inject
    private InsDiaryService insDiaryService;

    @Inject
    private HospitalisationService hospitalisationService;

    @Inject
    private ImmunisationService immunisationService;

    @Inject
    private InsDiaryAuditService insDiaryAuditService;

    @Inject
    private UserRepository userRepository;

    /**
     * Builds XMLs fpr INS diary entries and queue them up to be send to UKRDC
     */
     @Scheduled(cron = "0 */15 * * * ?") // every 15 minutes
    // @Scheduled(cron = "0 0 1 * * ?") // every day at 1:00AM
    @Transactional
    public void insDiaryQueue() throws ResourceNotFoundException {

        long start = System.currentTimeMillis();
        LOG.info("Starting INS Diary UKRDC task");

        // get ids of the patients to be processed
        List<InsDiaryAuditLog> logs = insDiaryAuditService.findAll();
        Set<Long> patientIds = new HashSet<>();
        for (InsDiaryAuditLog log : logs) {
            patientIds.add(log.getPatientId());
        }

        // for each patient find ins diary, hospitalisation and immunisations recordings and build xml
        for (Long patientId : patientIds) {

            try {
                User patient = userRepository.findOne(patientId);
                if (patient == null) {
                    LOG.error("INSDiaryUkrdcTask could not find patient " + patientId);
                    continue;
                }
                List<InsDiaryRecord> insDiaryRecords = insDiaryService.getListByUser(patientId);
                List<Hospitalisation> hospitalisations = hospitalisationService.getListByPatient(patientId);
                List<Immunisation> immunisations = immunisationService.getListByPatient(patientId);

                String xml = ukrdcService.buildInsDiaryXml(patient, insDiaryRecords, hospitalisations, immunisations);
                externalServiceService.addToQueue(ExternalServices.UKRDC_INS_DIARY_NOTIFICATION, xml,
                        patient, new Date());

                // clean up record from audit once queued
                insDiaryAuditService.deleteByPatient(patientId);
            } catch (Exception e) {
                LOG.error("Exception in building INS diary xml for patient {}", patientId, e);
            }
        }

        long stop = System.currentTimeMillis();
        LOG.info("DONE INS Diary UKRDC task, timing {}.", (stop - start));
    }

}
