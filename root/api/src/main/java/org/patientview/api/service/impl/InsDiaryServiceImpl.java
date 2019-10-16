package org.patientview.api.service.impl;

import org.patientview.api.service.InsDiaryService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.InsDiaryRecord;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.InsDiaryRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * Food Diary service, used when patient's enter foods that disagree with them
 *
 * Created by jamesr@solidstategroup.com
 * Created on 21/12/2015
 */
@Service
@Transactional
public class InsDiaryServiceImpl extends AbstractServiceImpl<InsDiaryServiceImpl> implements InsDiaryService {

    @Inject
    private InsDiaryRepository insDiaryRepository;
    @Inject
    private UserRepository userRepository;

    @Override
    public void add(Long userId, InsDiaryRecord record) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        record.setUser(user);
        record.setCreator(user);

        insDiaryRepository.save(record);
    }

    @Override
    public InsDiaryRecord get(Long userId, Long recordId) throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        InsDiaryRecord record = insDiaryRepository.findOne(recordId);
        // make sure the same user
        if (!record.getUser().equals(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return record;
    }

    @Override
    public InsDiaryRecord update(Long userId, InsDiaryRecord record)
            throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        InsDiaryRecord foundRecord = insDiaryRepository.findOne(record.getId());
        if (foundRecord == null) {
            throw new ResourceNotFoundException("Could not find INS diary record");
        }

        if (!foundRecord.getUser().equals(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        // todo: update details
        //foundRecord.setSomething(record.getSomething());

        return insDiaryRepository.save(foundRecord);
    }

    @Override
    public void delete(Long userId, Long recordId) throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        InsDiaryRecord foundRecord = insDiaryRepository.findOne(recordId);
        if (foundRecord == null) {
            throw new ResourceNotFoundException("Could not find INS diary record");
        }

        if (!foundRecord.getUser().equals(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        insDiaryRepository.delete(recordId);
    }

    @Override
    public List<InsDiaryRecord> getList(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        return insDiaryRepository.findByUser(user);
    }

}
