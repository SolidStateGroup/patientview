package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.patientview.api.model.FhirObservation;
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
import org.patientview.persistence.repository.InsDiaryRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.RelapseMedicationRepository;
import org.patientview.persistence.repository.RelapseRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * INS Diary service to handle IDS diary recordings functionality.
 */
@Service
@Transactional
public class InsDiaryServiceImpl extends AbstractServiceImpl<InsDiaryServiceImpl> implements InsDiaryService {

    private static final String SYSTOLIC_BP_CODE = "bpsys";
    private static final String DISATOLIC_BP_CODE = "bpdia";
    private static final String WEIGHT_CODE = "weight";
    @Inject
    private InsDiaryRepository insDiaryRepository;
    @Inject
    private RelapseRepository relapseRepository;
    @Inject
    private RelapseMedicationRepository relapseMedicationRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private ObservationHeadingRepository observationHeadingRepository;
    @Inject
    private ApiObservationService apiObservationService;


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
        PageRequest pageable = createPageRequest(0, 100, "entryDate", "DESC");
        List<InsDiaryRecord> existingRecords = insDiaryRepository.findListByUser(patientUser, pageable);

        // validate form
        validateRecord(record, existingRecords);

        // we can have diary record without Relapse
        if (record.getRelapse() != null) {
            Relapse relapseData = record.getRelapse();

            // create or update Relapse
            Relapse savedRelapse = saveOrUpdateRelapse(patientUser, editor, relapseData);
            record.setRelapse(savedRelapse);
        }

        // create fhir observation and get logical ids back
        Map<String, UUID> resourceMapIds = createFhirRecords(record, patientUser);
        if (!CollectionUtils.isEmpty(resourceMapIds)) {
            record.setSystolicBPResourceId(resourceMapIds.get(SYSTOLIC_BP_CODE));
            record.setDiastolicBPResourceId(resourceMapIds.get(DISATOLIC_BP_CODE));
            record.setWeightResourceId(resourceMapIds.get(WEIGHT_CODE));
        }

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
    public InsDiaryRecord update(Long userId, Long adminId, InsDiaryRecord record)
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

        InsDiaryRecord foundRecord = insDiaryRepository.findOne(record.getId());
        if (foundRecord == null) {
            throw new ResourceNotFoundException("Could not find INS diary record");
        }

        if (!foundRecord.getUser().equals(patientUser)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        // new diary entry date must be greater to "Date" of previous diary entry
        if (foundRecord.getEntryDate().compareTo(record.getEntryDate()) != 0) {
            throw new ResourceInvalidException("Diary entry Date can not be updated.");
        }

        // check for previous records
        PageRequest pageable = createPageRequest(0, 100, "entryDate", "DESC");
        List<InsDiaryRecord> existingRecords = insDiaryRepository.findListByUser(patientUser, pageable);

        // validate form details
        validateRecord(record, existingRecords);

        // we can have diary record without Relapse
        if (record.getRelapse() != null) {
            Relapse relapseData = record.getRelapse();

            // create or update Relapse
            Relapse savedRelapse = saveOrUpdateRelapse(patientUser, editor, relapseData);
            foundRecord.setRelapse(savedRelapse);
        }

        // update fhir results
        // we can update results only if value was added originally

        // update SystolicBP
        if (foundRecord.getSystolicBPExclude() == null || !foundRecord.getSystolicBPExclude()) {

            // check if any changes
            if (foundRecord.getSystolicBP().intValue() != record.getSystolicBP().intValue()) {
                // update fhir result
                updateFhirObservation(patientUser.getId(), editor.getId(), foundRecord.getSystolicBPResourceId(),
                        foundRecord.getEntryDate(), String.valueOf(record.getSystolicBP()));

                foundRecord.setSystolicBP(record.getSystolicBP());
            }
        }

        // update DiastolicBP
        if (foundRecord.getDiastolicBPExclude() == null || !foundRecord.getDiastolicBPExclude()) {

            // check if any changes
            if (foundRecord.getDiastolicBP().intValue() != record.getDiastolicBP().intValue()) {
                // update fhir result
                updateFhirObservation(patientUser.getId(), editor.getId(), foundRecord.getDiastolicBPResourceId(),
                        foundRecord.getEntryDate(), String.valueOf(record.getDiastolicBP()));

                foundRecord.setDiastolicBP(record.getDiastolicBP());
            }
        }

        // update weight
        if (foundRecord.getWeightExclude() == null || !foundRecord.getWeightExclude()) {

            // check if any changes
            if (foundRecord.getWeight().doubleValue() != record.getWeight().doubleValue()) {
                // update result
                updateFhirObservation(patientUser.getId(), editor.getId(), foundRecord.getWeightResourceId(),
                        foundRecord.getEntryDate(), String.valueOf(record.getWeight()));

                foundRecord.setWeight(record.getWeight());
            }
        }

        foundRecord.setDipstickType(record.getDipstickType());
        foundRecord.setOedema(record.getOedema());

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
        PageRequest pageable = createPageRequest(pageConverted, sizeConverted, "entryDate", "DESC");

        return insDiaryRepository.findByUser(user, pageable);
    }

    @Override
    public RelapseMedication addRelapseMedication(Long userId, Long relapseId, RelapseMedication medication)
            throws ResourceNotFoundException, ResourceInvalidException, ResourceForbiddenException {
        User patientUser = userRepository.findOne(userId);
        if (patientUser == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Relapse existingRelapse = relapseRepository.findOne(relapseId);
        if (existingRelapse == null) {
            throw new ResourceNotFoundException("Could not find Relapse record");
        }

        if (!existingRelapse.getUser().equals(patientUser)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        validateRelapseMedication(medication);

        medication.setRelapse(existingRelapse);
        RelapseMedication savedMedication = relapseMedicationRepository.save(medication);

        existingRelapse.getMedications().add(savedMedication);
        relapseRepository.save(existingRelapse); // TODO: check if we need to re save this

        return savedMedication;
    }

    @Override
    public void deleteRelapseMedication(Long userId, Long relapseId, Long medicationId)
            throws ResourceNotFoundException, ResourceInvalidException, ResourceForbiddenException {

        User patientUser = userRepository.findOne(userId);
        if (patientUser == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Relapse existingRelapse = relapseRepository.findOne(relapseId);
        if (existingRelapse == null) {
            throw new ResourceNotFoundException("Could not find Relapse record");
        }

        if (!existingRelapse.getUser().equals(patientUser)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        RelapseMedication existingMedication = relapseMedicationRepository.findOne(medicationId);
        if (existingRelapse == null) {
            throw new ResourceNotFoundException("Could not find RelapseMedication record");
        }

        if (!existingMedication.getRelapse().equals(existingRelapse)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        existingRelapse.getMedications().remove(existingMedication);
        relapseRepository.save(existingRelapse); // TODO: check if we need to re save this

        relapseMedicationRepository.delete(existingMedication);
    }

    @Override
    public void deleteInsDiaryRecordsForUser(User user) {
        insDiaryRepository.deleteByUser(user.getId());
    }

    @Override
    public void deleteRelapseRecordsForUser(User user) {
        List<Relapse> relapseList = relapseRepository.findByUser(user);

        for (Relapse relapse : relapseList) {
            relapseMedicationRepository.deleteByRelapse(relapse.getId());
        }

        relapseRepository.deleteByUser(user.getId());
    }

    /**
     * Helper to validate InsDiaryRecord
     *
     * @param record          a record to validate
     * @param existingRecords
     * @throws ResourceInvalidException
     */
    private void validateRecord(InsDiaryRecord record, List<InsDiaryRecord> existingRecords)
            throws ResourceInvalidException {

        LocalDate localNow = DateTime.now().toLocalDate();

        boolean isNewRecord = record.getId() == null;

        // is new record in relapse
        if (record.isInRelapse() && record.getRelapse() == null) {
            throw new ResourceInvalidException("Missing Relapse information.");
        }

        InsDiaryRecord previousRecord = null;
        if (!CollectionUtils.isEmpty(existingRecords)) {

            // creating new record, get the last one from the list
            if (isNewRecord) {
                previousRecord = existingRecords.get(0);
            } else {

                // when updating find last entry before current one
                for (InsDiaryRecord previous : existingRecords) {
                    if (!previous.getId().equals(record.getId())
                            && previous.getId().longValue() < record.getId().longValue()) {
                        previousRecord = previous;
                        break;
                    }
                }
            }
        }

        if (record.getEntryDate() == null) {
            throw new ResourceInvalidException("Please provide Date for diary recording.");
        }

        // diary specific validation
        if (record.getDipstickType() == null) {
            throw new ResourceInvalidException("Please select Urine Protein Dipstick.");
        }

        if ((record.getSystolicBPExclude() == null || !record.getSystolicBPExclude()) &&
                record.getSystolicBP() < 1) {
            throw new ResourceInvalidException("Systolic BP must be greater the 0.");
        }

        if ((record.getDiastolicBPExclude() == null || !record.getDiastolicBPExclude()) &&
                record.getDiastolicBP() < 1) {
            throw new ResourceInvalidException("Diastolic BP must be greater the 0.");
        }

        if ((record.getWeightExclude() == null || !record.getWeightExclude()) &&
                record.getWeight() < 1) {
            throw new ResourceInvalidException("Weight must be greater the 0.");
        }

        if (CollectionUtils.isEmpty(record.getOedema())) {
            throw new ResourceInvalidException("Please select at least one Oedema.");
        }

        // new diary entry date must be greater to "Date" of previous diary entry
        if (previousRecord != null) {
            if (!record.getEntryDate().after(previousRecord.getEntryDate())) {
                throw new ResourceInvalidException("Diary entry Date must be later then previous diary entry date.");
            }
        }

        /**
         * Rules for relapse checks:
         * - A relapse episode is a sequence of Relapse Y entries -> check on Relapse date only
         * - The end of a relapse episode is a switch from Y to N -> check on Relapse and Remission dates
         * - Non-relapse state is a sequence of Relapse N entries outside of a relapse episode
         * (outside of YYYYYN) -> no checks since no dates sent.
         **/

        // is switching from Y to N
        boolean isEndOfEpisode = (!record.isInRelapse() && (previousRecord != null && previousRecord.isInRelapse()));
        if (isEndOfEpisode) {
            if (record.getRelapse() == null) {
                throw new ResourceInvalidException("You must provide Relapse data for the End of Relapse episode.");
            }
            if (record.getRelapse().getRemissionDate() == null) {
                throw new ResourceInvalidException("Missing Date of Remission for the End of Relapse episode.");
            }
        }

        // validate Relapse data
        if (record.getRelapse() != null) {
            DateTime relapseDate = new DateTime(record.getRelapse().getRelapseDate());
            DateTime remissionDate = new DateTime(record.getRelapse().getRemissionDate());

            // ins diary record is In Relapse (Y)
            if (record.isInRelapse()) {

                // Date of Relapse can not be null or in the future
                if (relapseDate == null) {
                    throw new ResourceInvalidException("Please enter Date of Relapse.");
                }
                if (relapseDate.toLocalDate().isAfter(localNow)) {
                    throw new ResourceInvalidException("Date of Relapse can not be in the future.");
                }

                // "Date of Relapse" must be greater or equal to last saved diary
                // entry "Date" where Relapse "N" was entered
                if (!CollectionUtils.isEmpty(existingRecords)) {
                    Date noneRelapseEntryDate = null;

                    // find last None Relapse(N) INS diary record.
                    for (InsDiaryRecord previous : existingRecords) {

                        // adding new Y record
                        if (isNewRecord) {
                            if (!previous.isInRelapse()) {
                                noneRelapseEntryDate = previous.getEntryDate();
                                break;
                            }
                        } else {
                            // Editing existing Y record
                            if (!previous.isInRelapse() && !previous.getId().equals(record.getId())
                                    && previous.getId().longValue() < record.getId().longValue()) {
                                noneRelapseEntryDate = previous.getEntryDate();
                                break;
                            }
                        }

                    }

                    if (noneRelapseEntryDate != null &&
                            !relapseDate.toLocalDate().isAfter(new DateTime(noneRelapseEntryDate).toLocalDate())) {
                        throw new ResourceInvalidException(String.format("The Date of Relapse that you've entered " +
                                "must be later than your most recent non-relapse diary recording of %s " +
                                "(where a Relapse value of 'N' was saved).", noneRelapseEntryDate));
                    }
                }
            }

            //  check if end of relapse episode, switching Y to N
            if (isEndOfEpisode) {

                // Date of Relapse can not be null or in the future
                if (relapseDate == null) {
                    throw new ResourceInvalidException("Please enter Date of Relapse.");
                }
                if (relapseDate.toLocalDate().isAfter(localNow)) {
                    throw new ResourceInvalidException("Date of Relapse can not be in the future.");
                }

                // Date of Remission can not be null or in the future
                if (remissionDate == null) {
                    throw new ResourceInvalidException("Please enter Date of Remission.");
                }
                if (remissionDate.toLocalDate().isAfter(localNow)) {
                    throw new ResourceInvalidException("Date of Remission can not be in the future.");
                }

                // check Date of Remission is not before Date of Relapse
                if (relapseDate.toLocalDate().isAfter(remissionDate.toLocalDate())) {
                    throw new ResourceInvalidException("Date of Relapse must be before the Date of Remission.");
                }

                // "Date of Remission" must be greater or equal to last saved diary
                // entry "Date" where Relapse "Y" was entered.
                if (new DateTime(previousRecord.getEntryDate()).toLocalDate().isAfter(remissionDate.toLocalDate())) {
                    throw new ResourceInvalidException(String.format("The Date of Remission that you've entered " +
                            "must be later than your most recent relapse diary recording of %s" +
                            " (where a Relapse value of 'Y' was saved).", previousRecord.getEntryDate()));
                }
            }

            // Validate Relapse Medication records
            if (!CollectionUtils.isEmpty(record.getRelapse().getMedications())) {
                // validate relapse medications
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

                    // check date stopped is not before date started
                    if (medication.getStarted() != null && medication.getStopped() != null &&
                            medication.getStarted().after(medication.getStopped())) {
                        LOG.error("Medication Date Started must be < then Date Stopped.");
                        throw new ResourceInvalidException("Medication Date Started must be before Date Stopped.");
                    }

                    if (medication.getDoseQuantity() != null &&
                            medication.getDoseQuantity() < 1) {
                        throw new ResourceInvalidException("Dose Quantity must be greater then 0.");
                    }
                }
            }
        }
    }

    /**
     * Helper to validate RelapseMedication details
     *
     * @param medication a RelapseMedication to validate
     * @throws ResourceInvalidException
     */
    private void validateRelapseMedication(RelapseMedication medication)
            throws ResourceInvalidException {

        LocalDate localNow = DateTime.now().toLocalDate();

        if (medication == null) {
            throw new ResourceInvalidException("Missing Relapse Medication details");
        }

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

        // check date stopped is not before date started
        if (medication.getStarted() != null && medication.getStopped() != null &&
                medication.getStarted().after(medication.getStopped())) {
            LOG.error("Medication Date Started must be < then Date Stopped.");
            throw new ResourceInvalidException("Medication Date Started must be before Date Stopped.");
        }
    }

    /**
     * Helper to create or update Relapse details.
     *
     * @param patient     a patient user associated with Relapse
     * @param editor      an admin user (viewing patient) or patient User
     * @param relapseData a Relapse data to create ot update
     * @return
     * @throws ResourceNotFoundException
     */
    private Relapse saveOrUpdateRelapse(User patient, User editor, Relapse relapseData)
            throws ResourceNotFoundException {
        Relapse savedRelapse = null;

        // we can have diary record without Relapse
        if (relapseData != null) {
            // creating new relapse record
            if (relapseData.getId() == null) {

                Relapse relapseToAdd = new Relapse();
                relapseToAdd.setUser(patient);
                relapseToAdd.setRelapseDate(relapseData.getRelapseDate());
                relapseToAdd.setRemissionDate(relapseData.getRemissionDate());
                relapseToAdd.setViralInfection(relapseData.getViralInfection());
                relapseToAdd.setCommonCold(relapseData.isCommonCold());
                relapseToAdd.setHayFever(relapseData.isHayFever());
                relapseToAdd.setAllergicReaction(relapseData.isAllergicReaction());
                relapseToAdd.setAllergicSkinRash(relapseData.isAllergicSkinRash());
                relapseToAdd.setFoodIntolerance(relapseData.isFoodIntolerance());
                relapseToAdd.setCreator(editor);

                savedRelapse = relapseRepository.save(relapseToAdd);

                if (!CollectionUtils.isEmpty(relapseData.getMedications())) {
                    // save relapse medications
                    for (RelapseMedication medication : relapseData.getMedications()) {

                        medication.setRelapse(savedRelapse);
                        RelapseMedication savedMedication = relapseMedicationRepository.save(medication);
                        savedRelapse.getMedications().add(savedMedication);
                    }
                }
            } else {
                // update details
                Relapse existingRelapse = relapseRepository.findOne(relapseData.getId());
                if (existingRelapse == null) {
                    throw new ResourceNotFoundException("Could not find Relapse record");
                }

                existingRelapse.setRelapseDate(relapseData.getRelapseDate());
                existingRelapse.setRemissionDate(relapseData.getRemissionDate());
                existingRelapse.setViralInfection(relapseData.getViralInfection());
                existingRelapse.setCommonCold(relapseData.isCommonCold());
                existingRelapse.setHayFever(relapseData.isHayFever());
                existingRelapse.setAllergicReaction(relapseData.isAllergicReaction());
                existingRelapse.setAllergicSkinRash(relapseData.isAllergicSkinRash());
                existingRelapse.setFoodIntolerance(relapseData.isFoodIntolerance());

                savedRelapse = relapseRepository.save(existingRelapse);
            }
        }

        return savedRelapse;
    }

    /**
     * Adds patient's own results for INS Diary record.
     *
     * @param record
     * @param patientUser
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    private Map<String, UUID> createFhirRecords(InsDiaryRecord record, User patientUser)
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
            userResultCluster.setMonth(String.valueOf(entryDate.getMonthOfYear()));
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

        // save results if not empty
        if (!CollectionUtils.isEmpty(userResultClusters)) {
            return apiObservationService.addUserResultClusters(patientUser.getId(), userResultClusters);
        }

        return new HashMap<>();
    }

    /**
     * Update fhir observation result for patient
     *
     * @param userId   an id of the patient to update result for
     * @param editorId an id of the editor
     * @param uuid     a logical id of the Observation
     * @param date
     * @param value    new value to set
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    private void updateFhirObservation(Long userId, Long editorId, UUID uuid, Date date, String value)
            throws ResourceNotFoundException, ResourceInvalidException {

        try {
            FhirObservation fhirObservation = new FhirObservation();
            fhirObservation.setLogicalId(uuid);
            fhirObservation.setApplies(date);
            fhirObservation.setValue(value);

            apiObservationService.updatePatientEnteredResult(userId, editorId, fhirObservation);
        } catch (FhirResourceException e) {
            LOG.error("FhirResourceException thrown : ", e);
            throw new ResourceInvalidException("Failed to update result.");
        }
    }

}
