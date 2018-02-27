package org.patientview.api.service.impl;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.patientview.api.model.BaseCode;
import org.patientview.api.model.Patient;
import org.patientview.api.service.ApiPatientService;
import org.patientview.api.service.CodeService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.ResearchService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.ResearchStudy;
import org.patientview.persistence.model.ResearchStudyCriteria;
import org.patientview.persistence.model.ResearchStudyCriteriaData;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.ResearchStudyCriteriaRepository;
import org.patientview.persistence.repository.ResearchStudyRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Class to control the crud operations of the Research Studies.
 * <p>
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Service
public class ResearchServiceImpl extends AbstractServiceImpl<ResearchServiceImpl> implements ResearchService {


    @Inject
    private EntityManager entityManager;

    @Inject
    private ResearchStudyRepository researchStudyRepository;

    @Inject
    private CodeService codeService;

    @Inject
    private GroupService groupService;

    @Inject
    private ResearchStudyCriteriaRepository researchStudyCriteriaRepository;

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
        researchStudyRepository.save(researchStudy).getId();

        ResearchStudyCriteria[] criteriaArray = researchStudy.getCriteria();
        if (criteriaArray != null) {
            for (ResearchStudyCriteria criteria : criteriaArray) {
                criteria.setResearchStudy(researchStudy.getId());
                criteria.setCreatedDate(new Date());
                if (criteria.getResearchStudyCriterias().getGender().equals("Any")) {
                    criteria.getResearchStudyCriterias().setGender(null);
                }
                criteria.setCreator(currentUser);
                researchStudyCriteriaRepository.save(criteria);
            }
        }
        return researchStudy.getId();
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

        List<ResearchStudyCriteria> criteriaList =
                researchStudyCriteriaRepository.getByResearchStudyId(researchStudy.getId());

        if (criteriaList != null) {
            for (ResearchStudyCriteria researchStudyCriteria : criteriaList) {
                ResearchStudyCriteriaData data = researchStudyCriteria.getResearchStudyCriterias();
                data.setGroups(new ArrayList<org.patientview.api.model.Group>());
                data.setTreatments(new ArrayList<Code>());
                data.setDiagnosis(new ArrayList<Code>());

                if (data.getGroupIds() != null) {
                    for (Long code : data.getGroupIds()) {
                        Group group = groupService.get(code);
                        org.patientview.api.model.Group groupDto = new org.patientview.api.model.Group();
                        groupDto.setCode(group.getCode());
                        groupDto.setId(group.getId());
                        groupDto.setName(group.getName());
                        groupDto.setGroupType(group.getGroupType());
                        groupDto.setShortName(group.getShortName());
                        data.getGroups().add(groupDto);
                    }
                }
                if (data.getTreatmentIds() != null) {
                    for (Long code : data.getTreatmentIds()) {
                        Code savedCode = codeService.get(code);
                        BaseCode treatmentCode = new BaseCode();
                        treatmentCode.setId(savedCode.getId());
                        treatmentCode.setDescription(savedCode.getDescription());
                        treatmentCode.setCode(savedCode.getCode());

                        data.getTreatments().add(treatmentCode);
                    }
                }
                if (data.getDiagnosisIds() != null) {
                    for (Long code : data.getDiagnosisIds()) {
                        Code savedCode = codeService.get(code);
                        BaseCode diagnosisCode = new BaseCode();
                        diagnosisCode.setId(savedCode.getId());
                        diagnosisCode.setDescription(savedCode.getDescription());
                        diagnosisCode.setCode(savedCode.getCode());

                        data.getDiagnosis().add(diagnosisCode);
                    }
                }

            }
        }

        researchStudy.setCriteria(criteriaList.toArray(new ResearchStudyCriteria[criteriaList.size()]));

        if (researchStudy == null) {
            throw new ResourceNotFoundException("Research Study does not exist");
        }

        return researchStudy;
    }

    @Override
    public Page<ResearchStudy> getAll() throws ResourceNotFoundException, ResourceForbiddenException {

        List<ResearchStudy> list = Lists.newArrayList(researchStudyRepository.findAll());
        Collections.reverse(list);
        PageRequest pageable = createPageRequest(1, list.size(), null, null);

        return new PageImpl<>(list, pageable, list.size());
    }

    @Override
    public Page<ResearchStudy> getAllForUser(Long userId, boolean limitResults, Pageable pageable) throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        User user = userRepository.findOne(userId);
        // get role, group and grouprole specific news (directly accessed through newsLink)
        PageRequest pageableAll = new PageRequest(0, Integer.MAX_VALUE);
        Set<ResearchStudy> researchStudySet = new HashSet<>();

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
        List<Long> groups = new ArrayList<>();
        for (GroupRole groupRole : user.getGroupRoles()) {
            groups.add(groupRole.getGroup().getId());
        }

        //Get diagnosis codes for user
        //Get treatment codes for user
        List<Long> diagnosisCodes = new ArrayList<>();
        List<Long> treatmentCodes = new ArrayList<>();

        for (Patient patient : patients) {
            for (Code diagnosis : patient.getDiagnosisCodes()) {
                diagnosisCodes.add(diagnosis.getId());
            }
            for (FhirEncounter encounter : patient.getFhirEncounters()) {
                if (encounter.getEncounterType().equals(EncounterTypes.TREATMENT)) {
                    treatmentCodes.add(encounter.getId());
                }
            }
            if (gender == null) {
                gender = patient.getFhirPatient().getGender();
            }
        }

        gender = "Male";

        if (ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN) ||
                ApiUtil.currentUserHasRole(RoleName.UNIT_ADMIN) ||
                ApiUtil.currentUserHasRole(RoleName.GP_ADMIN) ||
                ApiUtil.currentUserHasRole(RoleName.SPECIALTY_ADMIN) ||
                ApiUtil.currentUserHasRole(RoleName.DISEASE_GROUP_ADMIN)) {
            return manuallyPage(getAllResearchStudies(), pageable);
        } else {
            String query =
                    "SELECT r.* " +
                            " FROM pv_research_study r " +
                            "join pv_research_study_criteria rc on rc.research_study_id = r.id WHERE ";

            if (gender == null) {
                query += "(rc.criteria->>'gender' IS null) AND ";
            } else {
                query += "(rc.criteria->>'gender'= '" + gender + "' OR rc.criteria->>'gender' IS null) AND \n";
            }
            query += "(CAST(rc.criteria->>'toAge' AS INT) >= " + age + " OR (CAST(rc.criteria ->> 'toAge' AS TEXT) IS NULL)) AND \n" +
                    "(CAST(rc.criteria->>'fromAge' AS INT) <= " + age + " OR (CAST(rc.criteria ->> 'fromAge' AS TEXT) IS NULL)) \n " +

                    "AND (((rc.criteria ->> 'groupIds') IS NULL) OR " +
                    "((CAST(TRANSLATE(CAST(CAST(rc.criteria->'groupIds' AS jsonb) AS text), '[]','{}') AS INT[])) && '{" + StringUtils.join(groups, ",") + "}')) \n" +

                    "AND (((rc.criteria ->> 'treatmentIds') IS NULL) OR " +
                    "((CAST(TRANSLATE(CAST(CAST(rc.criteria->'treatmentIds' AS jsonb) AS text), '[]','{}') AS INT[])) && '{" + StringUtils.join(treatmentCodes, ",") + "}')) \n" +

                    "AND (((rc.criteria ->> 'diagnosisIds') IS NULL) OR " +
                    "((CAST(TRANSLATE(CAST(CAST(rc.criteria->'diagnosisIds' AS jsonb) AS text), '[]','{}') AS INT[])) && '{" + StringUtils.join(diagnosisCodes, ",") + "}')) \n" +

                    "AND available_from <= NOW() AND available_to >= NOW() " +
                    "ORDER BY r.created_date DESC";

            List<ResearchStudy> studies;
            query = query.replaceAll(":", "\\\\:");
            studies = entityManager.createNativeQuery(query, ResearchStudy.class).getResultList();
            Collections.reverse(studies);

            return manuallyPage(studies, pageable);
        }
    }

    @Override
    public void save(ResearchStudy researchStudy) throws ResourceNotFoundException, ResourceForbiddenException {
        ResearchStudy savedStudy = researchStudyRepository.findOne(researchStudy.getId());

        researchStudy.setCreatedDate(savedStudy.getCreatedDate());
        researchStudy.setCreator(savedStudy.getCreator());
        researchStudy.setLastUpdate(new Date());
        researchStudy.setLastUpdater(userRepository.findOne(getCurrentUser().getId()));
        researchStudyRepository.save(researchStudy);

        //Remove the existing criteria
        List<ResearchStudyCriteria> criteriaList =
                researchStudyCriteriaRepository.getByResearchStudyId(researchStudy.getId());

        for (ResearchStudyCriteria criteria : criteriaList) {
            researchStudyCriteriaRepository.delete(criteria.getId());
        }

        //Create the new criteria
        ResearchStudyCriteria[] criteriaArray = researchStudy.getCriteria();
        if (criteriaArray != null) {
            for (ResearchStudyCriteria criteria : criteriaArray) {
                ResearchStudyCriteria newCriteria = new ResearchStudyCriteria();
                newCriteria.setResearchStudy(researchStudy.getId());
                newCriteria.setCreatedDate(new Date());
                newCriteria.setResearchStudyCriterias(criteria.getResearchStudyCriterias());
                newCriteria.setCreator(criteria.getCreator());
                newCriteria.setCreator(getCurrentUser());

                if (criteria.getResearchStudyCriterias().getGender() != null &&
                        criteria.getResearchStudyCriterias().getGender().equals("Any")) {
                    newCriteria.getResearchStudyCriterias().setGender(null);
                }
                researchStudyCriteriaRepository.save(newCriteria);
            }
        }
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


    private List<ResearchStudy> getAllResearchStudies() {
        List<ResearchStudy> list = new ArrayList<>();
        for (ResearchStudy researchStudy : Lists.newArrayList(researchStudyRepository.findAll())) {
            if (canModifyResearchStudy(researchStudy)) {
                list.add(researchStudy);
            }
        }

        Collections.reverse(list);
        return list;
    }


    private PageImpl<ResearchStudy> manuallyPage(List<ResearchStudy> list, Pageable pageable) {
        // manually do pagination
        int startIndex = pageable.getOffset();
        int endIndex;

        if ((startIndex + pageable.getPageSize()) > list.size()) {
            endIndex = list.size();
        } else {
            endIndex = startIndex + pageable.getPageSize();
        }

        List<ResearchStudy> pagedNewsItems = new ArrayList<>();

        if (!list.isEmpty()) {
            pagedNewsItems = list.subList(startIndex, endIndex);
        }


        return new PageImpl<>(pagedNewsItems, pageable, pagedNewsItems.size());
    }

}
