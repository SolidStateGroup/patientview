package org.patientview.api.service.impl;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.patientview.api.service.ImmunisationService;
import org.patientview.api.service.InsDiaryAuditService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Immunisation;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.ImmunisationRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of Immunisation service, used to record patient's  Immunisation records
 */
@Service
@Transactional
public class ImmunisationServiceImpl extends
        AbstractServiceImpl<ImmunisationServiceImpl> implements ImmunisationService {

    @Inject
    private ImmunisationRepository immunisationRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private InsDiaryAuditService insDiaryAuditService;

    @Override
    public Immunisation add(Long userId, Long adminId, Immunisation record) throws ResourceNotFoundException,
            ResourceInvalidException {
        User patientUser = userRepository.findOne(userId);
        if (patientUser == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        // check if admin is viewing patient, otherwise editor is patient
        User editor;
        if (adminId != null && !adminId.equals(userId)) {
            editor = userRepository.findOne(adminId);
        } else {
            editor = patientUser;
        }

        if (editor == null) {
            throw new ResourceNotFoundException("Editor User does not exist");
        }

        validateRecord(record);

        record.setUser(patientUser);
        record.setCreator(editor);

        insDiaryAuditService.add(patientUser.getId());

        return immunisationRepository.save(record);
    }

    @Override
    public Immunisation get(Long userId, Long recordId) throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Immunisation record = immunisationRepository.findOne(recordId);
        // make sure the same user
        if (!record.getUser().equals(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return record;
    }

    @Override
    public Immunisation update(Long userId, Long recordId, Long adminId, Immunisation record)
            throws ResourceNotFoundException, ResourceForbiddenException, ResourceInvalidException {
        User patientUser = userRepository.findOne(userId);
        if (patientUser == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        // check if admin is viewing patient, otherwise editor is patient
        User editor;
        if (adminId != null && !adminId.equals(userId)) {
            editor = userRepository.findOne(adminId);
        } else {
            editor = patientUser;
        }

        if (editor == null) {
            throw new ResourceNotFoundException("Editor User does not exist");
        }

        Immunisation foundRecord = immunisationRepository.findOne(recordId);
        if (foundRecord == null) {
            throw new ResourceNotFoundException("Could not find Immunisation record");
        }

        if (!foundRecord.getUser().equals(patientUser)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        validateRecord(record);

        foundRecord.setCodelist(record.getCodelist());
        foundRecord.setImmunisationDate(record.getImmunisationDate());
        foundRecord.setOther(record.getOther());
        foundRecord.setLastUpdater(editor);

        insDiaryAuditService.add(patientUser.getId());

        return immunisationRepository.save(foundRecord);
    }

    @Override
    public void delete(Long userId, Long recordId, Long adminId) throws ResourceNotFoundException, ResourceForbiddenException {
        User patient = userRepository.findOne(userId);
        if (patient == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Immunisation foundRecord = immunisationRepository.findOne(recordId);
        if (foundRecord == null) {
            throw new ResourceNotFoundException("Could not find Immunisation record");
        }

        if (!foundRecord.getUser().equals(patient)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        LOG.info("Deleting Immunisation id: {}, user id {}, admin id {}", recordId, userId, adminId);

        insDiaryAuditService.add(userId);

        immunisationRepository.delete(recordId);
    }

    @Override
    public List<Immunisation> getList(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        return immunisationRepository.findByUser(user);
    }

    @Override
    public List<Immunisation> getListByPatient(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            return new ArrayList<>();
        }

        return immunisationRepository.findByUser(user);
    }

    @Override
    public void deleteRecordsForUser(User user) {
        immunisationRepository.deleteByUser(user.getId());
    }


    /**
     * Helper to validate dates .
     *
     * @param record a Immunisation record to check
     */
    private void validateRecord(Immunisation record) throws ResourceInvalidException {

        if (record.getImmunisationDate() == null) {
            throw new ResourceInvalidException("Please enter Immunisation date.");
        }

        LocalDate localNow = DateTime.now().toLocalDate();

        // can not be in the future
        if (new DateTime(record.getImmunisationDate()).toLocalDate().isAfter(localNow)) {
            throw new ResourceInvalidException("Immunisation Date can not be in the future.");
        }
    }

}
