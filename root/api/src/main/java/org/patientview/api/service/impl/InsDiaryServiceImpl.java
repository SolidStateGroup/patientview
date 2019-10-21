package org.patientview.api.service.impl;

import org.patientview.api.service.InsDiaryService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.InsDiaryRecord;
import org.patientview.persistence.model.Relapse;
import org.patientview.persistence.model.RelapseMedication;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.InsDiaryRepository;
import org.patientview.persistence.repository.RelapseMedicationRepository;
import org.patientview.persistence.repository.RelapseRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.List;

/**
 * INS Diary service to handle IDS diary recordings functionality.
 */
@Service
@Transactional
public class InsDiaryServiceImpl extends AbstractServiceImpl<InsDiaryServiceImpl> implements InsDiaryService {

    @Inject
    private InsDiaryRepository insDiaryRepository;
    @Inject
    private RelapseRepository relapseRepository;
    @Inject
    private RelapseMedicationRepository relapseMedication;
    @Inject
    private UserRepository userRepository;

    @Override
    public InsDiaryRecord add(Long userId, Long adminId, InsDiaryRecord record) throws ResourceNotFoundException {
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

        if (record.getRelapse() != null) {
            Relapse relapseData = record.getRelapse();
            Relapse relapseToAdd = new Relapse();
            relapseToAdd.setUser(patientUser);
            relapseToAdd.setRelapseDate(relapseData.getRelapseDate());
            relapseToAdd.setRemissionDate(relapseData.getRemissionDate());
            relapseToAdd.setViralInfection(relapseData.getViralInfection());
            relapseToAdd.setCommonCold(relapseData.isCommonCold());
            relapseToAdd.setHayFever(relapseData.isHayFever());
            relapseToAdd.setAllergicReaction(relapseData.isAllergicReaction());
            relapseToAdd.setAllergicSkinRash(relapseData.isAllergicSkinRash());
            relapseToAdd.setFoodIntolerance(relapseData.isFoodIntolerance());
            relapseToAdd.setCreator(editor);


            Relapse savedRelapse = relapseRepository.save(relapseToAdd);
            record.setRelapse(savedRelapse);

            if (!CollectionUtils.isEmpty(relapseData.getMedications())) {
                // save relapse medications
                for (RelapseMedication medication : relapseData.getMedications()) {
                    medication.setRelapse(savedRelapse);
                    RelapseMedication savedMedication = relapseMedication.save(medication);
                    savedRelapse.getMedications().add(savedMedication);
                }
            }
        }

        // save results into fhir db as well

        record.setUser(patientUser);
        record.setCreator(editor);

        return insDiaryRepository.save(record);
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
