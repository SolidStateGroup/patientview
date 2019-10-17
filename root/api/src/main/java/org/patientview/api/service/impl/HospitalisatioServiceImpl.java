package org.patientview.api.service.impl;

import org.patientview.api.service.HospitalisationService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Hospitalisation;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.HospitalisationRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * An implementation of HospitalisationService, used to record patient's  Hospitalisation records
 */
@Service
@Transactional
public class HospitalisatioServiceImpl extends
        AbstractServiceImpl<HospitalisatioServiceImpl> implements HospitalisationService {

    @Inject
    private HospitalisationRepository hospitalisationRepository;
    @Inject
    private UserRepository userRepository;

    @Override
    public Hospitalisation add(Long userId, Long adminId, Hospitalisation record) throws ResourceNotFoundException {
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

        return hospitalisationRepository.save(record);
    }

    @Override
    public Hospitalisation get(Long recordId, Long userId) throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Hospitalisation record = hospitalisationRepository.findOne(recordId);
        // make sure the same user
        if (!record.getUser().equals(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return record;
    }

    @Override
    public Hospitalisation update(Long recordId, Long userId, Long adminId, Hospitalisation record)
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

        Hospitalisation foundRecord = hospitalisationRepository.findOne(recordId);
        if (foundRecord == null) {
            throw new ResourceNotFoundException("Could not find Hospitalisation record");
        }

        if (!foundRecord.getUser().equals(patientUser)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        foundRecord.setDateAdmitted(record.getDateAdmitted());
        foundRecord.setReason(record.getReason());
        foundRecord.setDateDischarged(record.getDateDischarged());

        return hospitalisationRepository.save(foundRecord);
    }

    @Override
    public void delete(Long recordId, Long userId, Long adminId) throws ResourceNotFoundException, ResourceForbiddenException {
        User patient = userRepository.findOne(userId);
        if (patient == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Hospitalisation foundRecord = hospitalisationRepository.findOne(recordId);
        if (foundRecord == null) {
            throw new ResourceNotFoundException("Could not find Hospitalisation record");
        }

        if (!foundRecord.getUser().equals(patient)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        hospitalisationRepository.delete(recordId);
    }

    @Override
    public List<Hospitalisation> getList(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        return hospitalisationRepository.findByUser(user);
    }

}
