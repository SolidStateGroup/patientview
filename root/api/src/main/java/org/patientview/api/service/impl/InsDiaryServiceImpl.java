package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.patientview.api.model.IdValue;
import org.patientview.api.model.UserResultCluster;
import org.patientview.api.service.ApiObservationService;
import org.patientview.api.service.InsDiaryService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.InsDiaryRecord;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Relapse;
import org.patientview.persistence.model.RelapseMedication;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.InsDiaryRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.RelapseMedicationRepository;
import org.patientview.persistence.repository.RelapseRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.service.ObservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
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

    @Inject
    private ObservationService observationService;

    @Inject
    private ObservationHeadingRepository observationHeadingRepository;

    @Inject
    private GroupRepository groupRepository;


    @Inject
    private ApiObservationService apiObservationService;

    private static final String SYSTOLIC_BP_CODE = "bpsys";
    private static final String DISATOLIC_BP_CODE = "bpdia";
    private static final String WEIGHT_CODE = "weight";


    private void validateRecord(InsDiaryRecord record, List<InsDiaryRecord> existingRecords) throws ResourceInvalidException {

        LocalDate localNow = DateTime.now().toLocalDate();

        // is new record in relapse
        //if (record.isInRelapse() && record.getRelapse() == null) {
        if (record.getRelapse() == null) {
            throw new ResourceInvalidException("Missing Relapse information.");
        }

        InsDiaryRecord previousRecord = null;
        if (!CollectionUtils.isEmpty(existingRecords)) {
            previousRecord = existingRecords.get(0);
        }

        if (record.getEntryDate() == null) {
            throw new ResourceInvalidException("Please provide Date for diary recording.");
        }

        // diary specific validation
        if (record.getDipstickType() == null) {
            throw new ResourceInvalidException("Please select Urine Protein Dipstick.");
        }

        if ((record.getSystolicBPExclude() == null || !record.getSystolicBPExclude()) &&
                record.getSystolicBP() == null) {
            throw new ResourceInvalidException("Please enter value for Systolic BP.");
        }

        if ((record.getDiastolicBPExclude() == null || !record.getDiastolicBPExclude()) &&
                record.getDiastolicBP() == null) {
            throw new ResourceInvalidException("Please enter value for Diastolic BP.");
        }

        if ((record.getWeightExclude() == null || !record.getWeightExclude()) &&
                record.getWeight() == null) {
            throw new ResourceInvalidException("Please enter value for Weight.");
        }

        if (CollectionUtils.isEmpty(record.getOedema())) {
            throw new ResourceInvalidException("Please select at least one Oedema.");
        }

        // we should always have relapse info
        if (record.getRelapse() == null) {
            throw new ResourceInvalidException("Please provide Relapse information.");
        }

        // can not be in the future
        if (new DateTime(record.getRelapse().getRelapseDate()).toLocalDate().isAfter(localNow)) {
            throw new ResourceInvalidException("Date of Relapse can not be in the future.");
        }
        if (record.getRelapse().getRemissionDate() != null &&
                new DateTime(record.getRelapse().getRemissionDate()).toLocalDate().isAfter(localNow)) {
            throw new ResourceInvalidException("Date of Relapse can not be in the future.");
        }

        if (previousRecord != null) {

            // new diary entry date must be greater to "Date" of previous diary entry
            if (!record.getEntryDate().after(previousRecord.getEntryDate())) {
                throw new ResourceInvalidException("Diary entry Date must be later then previous diary entry date.");
            }
        }

        // "Date of Relapse" must be greater or equal to last saved diary entry "Date" where Relapse "N" was entered
        if (!CollectionUtils.isEmpty(existingRecords)) {

            DateTime relapseDate = new DateTime(record.getRelapse().getRelapseDate());

            // find the last one with N
            for (InsDiaryRecord previousN : existingRecords) {
                if (!previousN.isInRelapse() &&
                        new DateTime(previousN.getEntryDate()).toLocalDate().isAfter(relapseDate.toLocalDate())) {
                    throw new ResourceInvalidException("Date of Relapse must be greater or equal " +
                            "to last saved diary entry without Relapse.");

                }
            }
        }

        // "Date of Remission" must be greater or equal to last saved diary entry "Date" where Relapse "Y" was entered.


        // validate medication data
        if (!CollectionUtils.isEmpty(record.getRelapse().getMedications())) {
            // save relapse medications
            for (RelapseMedication medication : record.getRelapse().getMedications()) {


                if (medication.getName() == null) {
                    throw new ResourceInvalidException("Please select Name for Medication");
                }

                if (medication.getStarted() != null &&
                        new DateTime(medication.getStarted()).toLocalDate().isAfter(localNow)) {
                    throw new ResourceInvalidException("Medication Date Started can not be in the future.");
                }

                if (medication.getStopped() != null &&
                        new DateTime(medication.getStopped()).toLocalDate().isAfter(localNow)) {
                    throw new ResourceInvalidException("Medication Date Stopped can not be in the future.");
                }

                // check date discharged is not before date admitted
                if (medication.getStarted() != null && medication.getStopped() != null &&
                        medication.getStarted().after(medication.getStopped())) {
                    LOG.error("Medication Date Started must be < then Date Stopped.");
                    throw new ResourceInvalidException("Medication Date Started must be before Date Stopped.");
                }

            }

        }

    }

    @Override
    public InsDiaryRecord add(Long userId, Long adminId, InsDiaryRecord record)
            throws ResourceNotFoundException, ResourceInvalidException, FhirResourceException {
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

        // check for previous records
        List<InsDiaryRecord> existingRecords = insDiaryRepository.findListByUser(patientUser,
                new PageRequest(0, 100));

        // validate form
        validateRecord(record, existingRecords);

        // create fhir results
        createFhirRecords(record, patientUser);


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
    public Page<InsDiaryRecord> findByUser(Long userId, GetParameters getParameters) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        String size = getParameters.getSize();
        String page = getParameters.getPage();
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;
        PageRequest pageable = createPageRequest(pageConverted, sizeConverted, null, null);

        return insDiaryRepository.findByUser(user, pageable);
    }


    /**
     * Adds patient's own results for INS Diary record.
     *
     * @param record
     * @param patientUser
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    private void createFhirRecords(InsDiaryRecord record, User patientUser)
            throws ResourceNotFoundException, FhirResourceException {

        List<UserResultCluster> userResultClusters = new ArrayList<>();

        DateTime entryDate = new DateTime(record.getEntryDate());

        // create Systolic BP User
        if (record.getSystolicBP() != null) {
            ObservationHeading observationHeading = observationHeadingRepository.findOneByCode(SYSTOLIC_BP_CODE);
            if (observationHeading == null) {
                throw new ResourceNotFoundException("Observation Heading not found for code " + SYSTOLIC_BP_CODE);
            }
            IdValue value = new IdValue();
            value.setId(observationHeading.getId());
            value.setValue(record.getSystolicBP().toString());


            UserResultCluster userResultCluster = new UserResultCluster();
            userResultCluster.setDay(String.valueOf(entryDate.getDayOfMonth()));
            userResultCluster.setMonth(String.valueOf(entryDate.getDayOfMonth()));
            userResultCluster.setYear(String.valueOf(entryDate.getYear()));
            userResultCluster.setValues(new ArrayList<IdValue>());
            userResultCluster.getValues().add(value);

            userResultClusters.add(userResultCluster);
        }

        // create Diastolic BP User
        if (record.getDiastolicBP() != null) {
            ObservationHeading observationHeading = observationHeadingRepository.findOneByCode(DISATOLIC_BP_CODE);
            if (observationHeading == null) {
                throw new ResourceNotFoundException("Observation Heading not found for code " + DISATOLIC_BP_CODE);
            }
            IdValue value = new IdValue();
            value.setId(observationHeading.getId());
            value.setValue(record.getDiastolicBP().toString());

            UserResultCluster userResultCluster = new UserResultCluster();
            userResultCluster.setDay(String.valueOf(entryDate.getDayOfMonth()));
            userResultCluster.setMonth(String.valueOf(entryDate.getMonthOfYear()));
            userResultCluster.setYear(String.valueOf(entryDate.getYear()));
            userResultCluster.setValues(new ArrayList<IdValue>());
            userResultCluster.getValues().add(value);

            userResultClusters.add(userResultCluster);
        }

        // create weight
        if (record.getWeight() != null) {
            ObservationHeading observationHeading = observationHeadingRepository.findOneByCode(WEIGHT_CODE);
            if (observationHeading == null) {
                throw new ResourceNotFoundException("Observation Heading not found for code " + WEIGHT_CODE);
            }
            IdValue value = new IdValue();
            value.setId(observationHeading.getId());
            value.setValue(record.getWeight().toString());

            UserResultCluster userResultCluster = new UserResultCluster();
            userResultCluster.setDay(String.valueOf(entryDate.getDayOfMonth()));
            userResultCluster.setMonth(String.valueOf(entryDate.getMonthOfYear()));
            userResultCluster.setYear(String.valueOf(entryDate.getYear()));
            userResultCluster.setValues(new ArrayList<IdValue>());
            userResultCluster.getValues().add(value);

            userResultClusters.add(userResultCluster);
        }

        apiObservationService.addUserResultClusters(patientUser.getId(), userResultClusters);
    }


}
