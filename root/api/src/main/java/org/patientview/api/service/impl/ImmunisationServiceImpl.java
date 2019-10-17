package org.patientview.api.service.impl;

import org.patientview.api.service.ImmunisationService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Immunisation;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.ImmunisationRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
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

    @Override
    public void add(Long userId, Long adminId, Immunisation record) throws ResourceNotFoundException {
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

        record.setUser(patientUser);
        record.setCreator(editor);

        immunisationRepository.save(record);
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
    public Immunisation update(Long recordId, Long userId, Long adminId, Immunisation record)
            throws ResourceNotFoundException, ResourceForbiddenException {
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

        foundRecord.setCodelist(record.getCodelist());
        foundRecord.setImmunisationDate(record.getImmunisationDate());
        foundRecord.setOther(record.getOther());
        foundRecord.setCreator(editor);

        return immunisationRepository.save(foundRecord);
    }

    @Override
    public void delete(Long recordId, Long userId, Long adminId) throws ResourceNotFoundException, ResourceForbiddenException {
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

}