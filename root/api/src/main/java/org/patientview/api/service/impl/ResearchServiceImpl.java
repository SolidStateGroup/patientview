package org.patientview.api.service.impl;

import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.patientview.api.model.Patient;
import org.patientview.api.service.ApiPatientService;
import org.patientview.api.service.ResearchService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.NewsLink;
import org.patientview.persistence.model.ResearchStudy;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.ResearchStudyRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Class to control the crud operations of the News.
 * <p>
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Service
public class ResearchServiceImpl extends AbstractServiceImpl<ResearchServiceImpl> implements ResearchService {

    @Inject
    private ResearchStudyRepository researchStudyRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ApiPatientService apiPatientService;


    @Override
    public Long add(ResearchStudy researchStudy) {
        // set updater and update time (used for ordering correctly)
        User currentUser = getCurrentUser();
        researchStudy.setCreator(currentUser);
        researchStudy.setCreatedDate(new Date());
        researchStudy.setLastUpdater(currentUser);
        researchStudy.setLastUpdate(researchStudy.getCreatedDate());

        return researchStudyRepository.save(researchStudy).getId();
    }

    @Override
    public void delete(Long researchItemId) throws ResourceNotFoundException, ResourceForbiddenException {
        ResearchStudy researchStudy = researchStudyRepository.findOne(researchItemId);
        if (researchStudy == null) {
            throw new ResourceNotFoundException("Research Study does not exist");
        }
        
        if (!canModifyResearchStudy(researchStudy)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        researchStudyRepository.delete(researchItemId);
    }

    @Override
    public ResearchStudy get(Long researchItemId) throws ResourceNotFoundException, ResourceForbiddenException {
        ResearchStudy researchStudy = researchStudyRepository.findOne(researchItemId);
        if (researchStudy == null) {
            throw new ResourceNotFoundException("Research Study does not exist");
        }

        return researchStudy;
    }

    @Override
    public Page<ResearchStudy> getAll() throws ResourceNotFoundException, ResourceForbiddenException {

        List<ResearchStudy> list = Lists.newArrayList(researchStudyRepository.findAll());
        PageRequest pageable = createPageRequest(1, list.size(), null, null);

        return new PageImpl<>(list, pageable, list.size());
    }

    @Override
    public Page<ResearchStudy> getAllForUser(Long userId) throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        User user = userRepository.findOne(userId);

        //Get the user
        List<Patient> patients = apiPatientService.get(user.getId(), null);

        //Create the criteria that they will match
        Date date = user.getDateOfBirth();
        if (date == null) {
            date = new Date();
        }
        LocalDate birthdate = LocalDate.fromDateFields(date);
        LocalDate now = new LocalDate();
        int age = Years.yearsBetween(birthdate, now).getYears();
        String gender = null;

        //Get te groups they are members of
        List<Group> groups = new ArrayList<>();
        for (GroupRole groupRole : user.getGroupRoles()) {
            groups.add(groupRole.getGroup());
        }

        //Get diagnosis codes for user
        //Get treatment codes for user
        List<Code> diagnosisCodes = new ArrayList<>();
        List<FhirEncounter> treatmentCodes = new ArrayList<>();
        for (Patient patient : patients) {
            diagnosisCodes.addAll(patient.getDiagnosisCodes());
            treatmentCodes.addAll(patient.getFhirEncounters());
            if (gender == null) {
                patient.getFhirPatient().getGender();
            }
        }

        //Make the request
        List<ResearchStudy> list = researchStudyRepository.findByUser();
        PageRequest pageable = createPageRequest(1, list.size(), null, null);

        return new PageImpl<>(list, pageable, list.size());
    }

    @Override
    public void save(ResearchStudy researchItem) throws ResourceNotFoundException, ResourceForbiddenException {

    }

    private boolean canModifyResearchStudy(ResearchStudy researchStudy) {
        if (ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)) {
            return true;
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return (researchStudy.getCreator() != null && researchStudy.getCreator().getId().equals(currentUser.getId()))
                || (researchStudy.getLastUpdater() != null && researchStudy.getLastUpdater().getId().equals(currentUser.getId()));
    }

}
