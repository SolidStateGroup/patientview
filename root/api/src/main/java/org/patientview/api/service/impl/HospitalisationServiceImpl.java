package org.patientview.api.service.impl;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.patientview.api.service.HospitalisationService;
import org.patientview.api.service.InsDiaryAuditService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Hospitalisation;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.HospitalisationRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An implementation of HospitalisationService, used to record patient's  Hospitalisation records
 */
@Service
@Transactional
public class HospitalisationServiceImpl extends
        AbstractServiceImpl<HospitalisationServiceImpl> implements HospitalisationService {

    @Inject
    private HospitalisationRepository hospitalisationRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private InsDiaryAuditService insDiaryAuditService;

    @Override
    public Hospitalisation add(Long userId, Long adminId, Hospitalisation record) throws ResourceNotFoundException,
            ResourceForbiddenException, ResourceInvalidException {

        User patientUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        // check if admin is viewing patient, otherwise editor is patient
        User editor;
        if (adminId != null && !adminId.equals(userId)) {
            editor = userRepository.findById(adminId).orElse(null);
        } else {
            editor = patientUser;
        }

        if (editor == null) {
            throw new ResourceNotFoundException("Editor User does not exist");
        }

        // check ongoing hospitalisation, make sure we cannot have more then one ongoing
        List<Hospitalisation> activeList = hospitalisationRepository.findActiveByUser(patientUser);
        if (!CollectionUtils.isEmpty(activeList) && record.getDateDischarged() == null) {
            throw new ResourceInvalidException("Please enter Discharge Date for last hospitalisation " +
                    "entry before creating a new one.");
        }

        // check make sure records not overlapping and set
        validateRecords(record, patientUser);

        record.setUser(patientUser);
        record.setCreator(editor);

        insDiaryAuditService.add(patientUser.getId());

        return hospitalisationRepository.save(record);
    }

    @Override
    public Hospitalisation get(Long userId, Long recordId) throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        Hospitalisation record = hospitalisationRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find Hospitalisation record"));
        // make sure the same user
        if (!record.getUser().equals(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return record;
    }

    @Override
    public Hospitalisation update(Long userId, Long recordId, Long adminId, Hospitalisation record)
            throws ResourceNotFoundException, ResourceForbiddenException, ResourceInvalidException {
        User patientUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        // check if admin is viewing patient, otherwise editor is patient
        User editor;
        if (adminId != null && !adminId.equals(userId)) {
            editor = userRepository.findById(adminId).orElse(null);
        } else {
            editor = patientUser;
        }

        if (editor == null) {
            throw new ResourceNotFoundException("Editor User does not exist");
        }

        Hospitalisation foundRecord = hospitalisationRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find Hospitalisation record"));

        if (!foundRecord.getUser().equals(patientUser)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        foundRecord.setDateAdmitted(record.getDateAdmitted());
        foundRecord.setReason(record.getReason());
        foundRecord.setDateDischarged(record.getDateDischarged());

        // check make sure records not overlapping and set
        validateRecords(foundRecord, patientUser);

        insDiaryAuditService.add(patientUser.getId());

        return hospitalisationRepository.save(foundRecord);
    }

    @Override
    public void delete(Long userId, Long recordId, Long adminId) throws ResourceNotFoundException, ResourceForbiddenException {
        User patient = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        Hospitalisation foundRecord = hospitalisationRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find Hospitalisation record"));

        if (!foundRecord.getUser().equals(patient)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        LOG.info("Deleting Hospitalisation id: {}, user id {}, admin id {}", recordId, userId, adminId);

        insDiaryAuditService.add(userId);

        hospitalisationRepository.deleteById(recordId);
    }

    @Override
    public List<Hospitalisation> getList(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        return hospitalisationRepository.findByUser(user);
    }

    @Override
    public List<Hospitalisation> getListByPatient(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }

        return hospitalisationRepository.findByUser(user);
    }


    @Override
    public void deleteRecordsForUser(User user) {
        hospitalisationRepository.deleteByUser(user.getId());
    }

    /**
     * Helper to validate dates and check Hospitalisation record not overlapping existing records.
     *
     * @param record      a hospitalisation record to check
     * @param patientUser a patient user to check hospitalisation records for
     */
    private void validateRecords(Hospitalisation record, User patientUser) throws ResourceInvalidException {

        if (record.getDateAdmitted() == null) {
            throw new ResourceInvalidException("Please enter Hospitalisation Date.");
        }

        LocalDate localNow = DateTime.now().toLocalDate();

        // can not be in the future
        if (new DateTime(record.getDateAdmitted()).toLocalDate().isAfter(localNow)) {
            throw new ResourceInvalidException("Hospitalisation Date can not be in the future.");
        }

        if (record.getDateDischarged() != null) {

            // cannot be in the future
            if (new DateTime(record.getDateDischarged()).toLocalDate().isAfter(localNow)) {
                LOG.error("Hospitalisation record discharged date is in the future.");
                throw new ResourceInvalidException("Discharge Date can not be in the future.");
            }

            // check date discharged is not before date admitted
            if (record.getDateAdmitted().after(record.getDateDischarged())) {
                LOG.error("Hospitalisation record discharged date must be < then Hospitalisation date.");
                throw new ResourceInvalidException("Hospitalisation Date must be before Discharge Date.");
            }
        }

        List<Hospitalisation> records = hospitalisationRepository.findByUser(patientUser);
        for (Hospitalisation existing : records) {
            // If editing remove from check
            if (!existing.getId().equals(record.getId())) {

                if (existing.getDateAdmitted().before(endDateMask(record.getDateDischarged()))
                        && record.getDateAdmitted().before(endDateMask(existing.getDateDischarged()))) {
                    throw new ResourceInvalidException("Hospitalisation records cannot overlap");
                }

            }
        }
    }

    /**
     * If date has been set return date, but if date object is null return the maximum
     * date.
     *
     * @param date Data to check
     * @return either set date or the maximum date provided by {@link Date}
     */
    private Date endDateMask(Date date) {

        if (date == null) {
            return new Date(Long.MAX_VALUE);
        }

        return date;
    }

}
