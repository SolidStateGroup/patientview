package org.patientview.api.service.impl;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.patientview.api.model.BaseGroup;
import org.patientview.api.model.FhirObservationPage;
import org.patientview.api.model.IdValue;
import org.patientview.api.model.ObservationSummary;
import org.patientview.api.model.UserResultCluster;
import org.patientview.api.service.ApiObservationService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.ApiUtil;
import org.patientview.builder.TestObservationsBuilder;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirDatabaseObservation;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.FhirObservationRange;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.DiagnosticReportObservationTypes;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.IbdDiseaseExtent;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.AuditService;
import org.patientview.service.FhirLinkService;
import org.patientview.service.ObservationService;
import org.patientview.service.PatientService;
import org.patientview.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Observation service, for management and retrieval of observations (test results), stored in FHIR.
 * <p/>
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
@Service
@Transactional(readOnly = true)
public class ApiObservationServiceImpl extends AbstractServiceImpl<ApiObservationServiceImpl>
        implements ApiObservationService {

    @Inject
    private AlertRepository alertRepository;

    @Inject
    private AuditService auditService;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private FhirLinkService fhirLinkService;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupService groupService;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private ObservationService observationService;

    @Inject
    private ObservationHeadingRepository observationHeadingRepository;

    @Inject
    private PatientService patientService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    private static final Logger LOG = LoggerFactory.getLogger(ApiObservationServiceImpl.class);
    private static final String COMMENT_RESULT_HEADING = "resultcomment";
    private static final String COMMENT_PRE = "PRE";
    private static final String COMMENT_POST = "POST";
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final int EIGHT = 8;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addTestObservations(Long userId, Long groupId, FhirObservationRange fhirObservationRange)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {

        // check current logged in user has rights to this group
        if (!(ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || ApiUtil.doesContainGroupAndRole(groupId, RoleName.UNIT_ADMIN_API))) {
            throw new ResourceForbiddenException("Failed group and role validation");
        }

        // check dates exist
        if (fhirObservationRange.getStartDate() == null || fhirObservationRange.getEndDate() == null) {
            throw new ResourceForbiddenException("Failed date validation");
        }

        // check code (could also check against existing ObservationHeadings)
        if (StringUtils.isEmpty(fhirObservationRange.getCode())) {
            throw new ResourceForbiddenException("Failed code validation");
        }

        // check User exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // check user has fhirLink associated with this group
        FhirLink foundFhirLink = null;
        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getGroup().getId().equals(groupId)) {
                foundFhirLink = fhirLink;
            }
        }
        if (foundFhirLink == null) {
            throw new ResourceForbiddenException("Failed fhirLink validation");
        }

        // check there are any observations
        if (CollectionUtils.isEmpty(fhirObservationRange.getObservations())) {
            throw new ResourceNotFoundException("Observations not found");
        }

        // check code is a test type observation
        if (Util.isInEnum(fhirObservationRange.getCode(), NonTestObservationTypes.class)
                || Util.isInEnum(fhirObservationRange.getCode(), DiagnosticReportObservationTypes.class)) {
            throw new ResourceForbiddenException("Non-test or DiagnosticReport type Observations");
        }

        // build FHIR observation objects
        ResourceReference patientReference = Util.createResourceReference(foundFhirLink.getResourceId());
        TestObservationsBuilder testObservationsBuilder
                = new TestObservationsBuilder(fhirObservationRange, patientReference);
        testObservationsBuilder.build();
        List<Observation> observations = testObservationsBuilder.getObservations();

        // get existing observation UUIDs by date range (to remove) and then delete
        List<UUID> existingObservations = fhirResource.getObservationUuidsBySubjectNameDateRange(
                foundFhirLink.getResourceId(), fhirObservationRange.getCode(), fhirObservationRange.getStartDate(),
                fhirObservationRange.getEndDate());
        LOG.info("Deleting " + existingObservations.size() + " existing observation(s).");
        observationService.deleteObservations(existingObservations);

        // add new observations
        LOG.info("Inserting " + observations.size() + " observation(s).");
        List<FhirDatabaseObservation> fhirDatabaseObservations = new ArrayList<>();
        for (Observation observation : observations) {
            fhirDatabaseObservations.add(new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation)));
        }
        observationService.insertFhirDatabaseObservations(fhirDatabaseObservations);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Map<String, UUID> addUserResultClusters(Long userId, List<UserResultCluster> userResultClusters)
            throws ResourceNotFoundException, FhirResourceException {

        Map<String, UUID> resourceMapIds = new HashMap<>();

        // Patient adds his own results
        User patientUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        Group patientEnteredResultsGroup = groupRepository.findByCode(HiddenGroupCodes.PATIENT_ENTERED.toString());
        if (patientEnteredResultsGroup == null) {
            throw new ResourceNotFoundException("Group for patient entered results does not exist");
        }

        List<ObservationHeading> commentObservationHeadings
                = observationHeadingRepository.findByCode(COMMENT_RESULT_HEADING);
        if (CollectionUtils.isEmpty(commentObservationHeadings)) {
            throw new ResourceNotFoundException("Comment type observation heading does not exist");
        }

        if (CollectionUtils.isEmpty(patientUser.getIdentifiers())) {
            throw new ResourceNotFoundException("Patient must have at least one Identifier (NHS Number or other)");
        }

        // use first identifier for patient
        Identifier patientIdentifier = patientUser.getIdentifiers().iterator().next();

        // saves results, only if observation values are present or comment found
        for (UserResultCluster userResultCluster : userResultClusters) {

            DateTime applies = createDateTime(userResultCluster);

            List<Observation> fhirObservations = new ArrayList<>();

            // build observations
            for (IdValue idValue : userResultCluster.getValues()) {
                ObservationHeading observationHeading = observationHeadingRepository.findById(idValue.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Observation Heading not found"));

                if (!idValue.getValue().isEmpty()) {
                    fhirObservations.add(observationService.buildObservation(applies, idValue.getValue(), null,
                            userResultCluster.getComments(), observationHeading, true));
                }
            }

            if (!fhirObservations.isEmpty()
                    || !(userResultCluster.getComments() == null || userResultCluster.getComments().isEmpty())) {

                // create FHIR Patient & fhirlink if not exists with PATIENT_ENTERED group, userId and identifier
                FhirLink fhirLink = Util.getFhirLink(
                        patientEnteredResultsGroup, patientIdentifier.getIdentifier(), patientUser.getFhirLinks());

                if (fhirLink == null) {
                    Patient patient = patientService.buildPatient(patientUser, patientIdentifier);
                    FhirDatabaseEntity fhirPatient
                            = fhirResource.createEntity(patient, ResourceType.Patient.name(), "patient");

                    // create FhirLink to link user to FHIR Patient at group PATIENT_ENTERED
                    fhirLink = new FhirLink();
                    fhirLink.setUser(patientUser);
                    fhirLink.setIdentifier(patientIdentifier);
                    fhirLink.setGroup(patientEnteredResultsGroup);
                    fhirLink.setResourceId(fhirPatient.getLogicalId());
                    fhirLink.setVersionId(fhirPatient.getVersionId());
                    fhirLink.setResourceType(ResourceType.Patient.name());
                    fhirLink.setActive(true);

                    if (CollectionUtils.isEmpty(patientUser.getFhirLinks())) {
                        patientUser.setFhirLinks(new HashSet<FhirLink>());
                    }

                    patientUser.getFhirLinks().add(fhirLink);
                    userRepository.save(patientUser);
                }

                ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());

                // store observations ready for native creation rather than fhir_create
                List<FhirDatabaseObservation> fhirDatabaseObservations = new ArrayList<>();

                // save observations
                for (Observation observation : fhirObservations) {
                    observation.setSubject(patientReference);

                    if (!(userResultCluster.getComments() == null || userResultCluster.getComments().isEmpty())) {
                        observation.setCommentsSimple(userResultCluster.getComments());
                    }
                    FhirDatabaseObservation fhirDatabaseObservation =
                            new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation));

                    fhirDatabaseObservations.add(fhirDatabaseObservation);

                    // add to map to return observation heading code and fhir logical id
                    resourceMapIds.put(observation.getName().getTextSimple(), fhirDatabaseObservation.getLogicalId());
                }

                // create comment observation based on patient entered comments
                if (!(userResultCluster.getComments() == null || userResultCluster.getComments().isEmpty())) {

                    Observation observation =
                            observationService.buildObservation(applies, userResultCluster.getComments(), null,
                                    userResultCluster.getComments(), commentObservationHeadings.get(0), true);

                    observation.setSubject(patientReference);
                    fhirDatabaseObservations.add(
                            new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation)));
                }

                observationService.insertFhirDatabaseObservations(fhirDatabaseObservations);
            }
        }

        return resourceMapIds;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addUserDialysisTreatmentResult(Long userId, Map<String, String> resultClusterMap)
            throws ResourceNotFoundException, FhirResourceException, ResourceInvalidException {

        // Patient adds his own results
        User patientUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        Group patientEnteredResultsGroup = groupRepository.findByCode(HiddenGroupCodes.PATIENT_ENTERED.toString());
        if (patientEnteredResultsGroup == null) {
            throw new ResourceNotFoundException("Group for patient entered results does not exist");
        }

        List<ObservationHeading> commentObservationHeadings
                = observationHeadingRepository.findByCode(COMMENT_RESULT_HEADING);
        if (CollectionUtils.isEmpty(commentObservationHeadings)) {
            throw new ResourceNotFoundException("Comment type observation heading does not exist");
        }

        if (CollectionUtils.isEmpty(patientUser.getIdentifiers())) {
            throw new ResourceNotFoundException("Patient must have at least one Identifier (NHS Number or other)");
        }
        // use first identifier for patient
        Identifier patientIdentifier = patientUser.getIdentifiers().iterator().next();
        String commentsResult = resultClusterMap.get("comments");

        /**
         * Dialysis Treatment results require entering all values
         * Using custom form to handle Pre and Post values for already existing
         * observation codes (e.g weight) and adding Pre or Post as comment to fhirobservation
         */
        DateTime applies = createDateTime(resultClusterMap);
        List<Observation> fhirObservations = new ArrayList<>();

        for (Map.Entry<String, String> result : resultClusterMap.entrySet()) {

            String key = result.getKey();
            // observation code defaults to key value, except some cases (pre/post)
            String code = key;
            String value = result.getValue();
            String comments = null;

            if (null == value || value.isEmpty()) {
                LOG.error("Missing value for key {}", code);
                throw new ResourceInvalidException("Missing required value");
            }

            // ignore time fields
            if (key.equals("day") || key.equals("month") || key.equals("year") ||
                    key.equals("hour") || key.equals("minute") || key.equals("comments")) {
                continue;
            }

            // need to handle Post and Pre and overwrite the codes
            switch (key) {
                case "PreWeight":
                    code = "weight";
                    comments = COMMENT_PRE;
                    break;
                case "PostWeight":
                    code = "weight";
                    comments = COMMENT_POST;
                    break;
                case "PreBpsys":
                    code = "bpsys";
                    comments = COMMENT_PRE;
                    break;
                case "PreBpdia":
                    code = "bpdia";
                    comments = COMMENT_PRE;
                    break;
                case "PostBpsys":
                    code = "bpsys";
                    comments = COMMENT_POST;
                    break;
                case "PostBpdia":
                    code = "bpdia";
                    comments = COMMENT_POST;
                    break;
                default:
                    comments = commentsResult;
            }

            ObservationHeading observationHeading = observationHeadingRepository.findOneByCode(code);
            if (observationHeading == null) {
                throw new ResourceNotFoundException("Observation Heading not found for code " + code);
            }

            // now build fhir observation
            fhirObservations.add(observationService.buildObservation(applies, value, null,
                    comments, observationHeading, false));
        }

        if (!fhirObservations.isEmpty()) {

            // create FHIR Patient & fhirlink if not exists with PATIENT_ENTERED group, userId and identifier
            FhirLink fhirLink = Util.getFhirLink(
                    patientEnteredResultsGroup, patientIdentifier.getIdentifier(), patientUser.getFhirLinks());

            if (fhirLink == null) {
                Patient patient = patientService.buildPatient(patientUser, patientIdentifier);
                FhirDatabaseEntity fhirPatient
                        = fhirResource.createEntity(patient, ResourceType.Patient.name(), "patient");

                // create FhirLink to link user to FHIR Patient at group PATIENT_ENTERED
                fhirLink = new FhirLink();
                fhirLink.setUser(patientUser);
                fhirLink.setIdentifier(patientIdentifier);
                fhirLink.setGroup(patientEnteredResultsGroup);
                fhirLink.setResourceId(fhirPatient.getLogicalId());
                fhirLink.setVersionId(fhirPatient.getVersionId());
                fhirLink.setResourceType(ResourceType.Patient.name());
                fhirLink.setActive(true);

                if (CollectionUtils.isEmpty(patientUser.getFhirLinks())) {
                    patientUser.setFhirLinks(new HashSet<FhirLink>());
                }

                patientUser.getFhirLinks().add(fhirLink);
                userRepository.save(patientUser);
            }

            ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());

            // store observations ready for native creation rather than fhir_create
            List<FhirDatabaseObservation> fhirDatabaseObservations = new ArrayList<>();

            // save observations
            for (Observation observation : fhirObservations) {
                observation.setSubject(patientReference);

                fhirDatabaseObservations.add(
                        new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation)));
            }

            // create comment observation based on patient entered comments
            if (!(commentsResult == null || commentsResult.isEmpty())) {

                Observation observation =
                        observationService.buildObservation(applies, commentsResult, null,
                                commentsResult, commentObservationHeadings.get(0), false);

                observation.setSubject(patientReference);
                fhirDatabaseObservations.add(
                        new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation)));
            }

            observationService.insertFhirDatabaseObservations(fhirDatabaseObservations);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updatePatientEnteredResult(Long userId, Long adminId,
                                           org.patientview.api.model.FhirObservation enteredResult)
            throws ResourceNotFoundException, FhirResourceException {

        // TODO: connection leak detected
        // Patient updates his own results
        User patientUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        // check if admin is viewing patient, otherwise editor is patient
        User editor;
        if (adminId != null && !adminId.equals(userId)) {
            editor = userRepository.findById(adminId).get();
        } else {
            editor = patientUser;
        }

        if (editor == null) {
            throw new ResourceNotFoundException("Editor User does not exist");
        }

        Group patientEnteredResultsGroup = groupRepository.findByCode(HiddenGroupCodes.PATIENT_ENTERED.toString());
        if (patientEnteredResultsGroup == null) {
            throw new ResourceNotFoundException("Group for patient entered results does not exist");
        }

        if (CollectionUtils.isEmpty(patientUser.getIdentifiers())) {
            throw new ResourceNotFoundException("Patient must have at least one Identifier (NHS Number or other)");
        }

        List<UUID> uuids = new ArrayList<>();
        for (FhirLink fhirLink : patientUser.getFhirLinks()) {
            UUID subjectId = fhirLink.getResourceId();

            if (fhirLink.getGroup().getCode().equals(HiddenGroupCodes.PATIENT_ENTERED.toString())) {
                List<UUID> foundIds = fhirResource.getLogicalIdsBySubjectId("observation", subjectId);
                uuids.addAll(foundIds);
            }
        }

        // make sure we have record in db
        if (!uuids.contains(enteredResult.getLogicalId())) {
            throw new ResourceNotFoundException("Could not find observation in the list.");
        }
        Observation observation
                = (Observation) fhirResource.get(enteredResult.getLogicalId(), ResourceType.Observation);
        if (observation == null) {
            throw new ResourceNotFoundException("Could not find observation.");
        }
        Observation updatedObservation = observationService.copyObservation(observation, enteredResult.getApplies(),
                enteredResult.getValue());

        fhirResource.updateEntity(updatedObservation, ResourceType.Observation.getPath(),
                ResourceType.Observation.getPath(), enteredResult.getLogicalId());

        // Build information for audit action
        StringBuilder information = new StringBuilder();
        information.append("Result : ['" + updatedObservation.getName().getTextSimple() + "'] ");
        information.append("Old values: ['");
        information.append(getObservationDate(observation));
        information.append("',  '");
        information.append(getObservationValue(observation));
        information.append("']   New values: ['");
        information.append(enteredResult.getApplies());
        information.append("', '");
        information.append(enteredResult.getValue());
        information.append("']");
        LOG.info("Result updated: " + information.toString());

        // Record audit action
        Audit audit = new Audit();
        audit.setAuditActions(AuditActions.PATIENT_ENTERED_RESULT_EDITED);
        audit.setUsername(patientUser.getUsername());
        audit.setActorId(editor.getId());
        // audit.setGroup(group);
        audit.setSourceObjectId(patientUser.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setInformation(information.toString());
        auditService.save(audit);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deletePatientEnteredResult(Long userId, Long adminId, String uuid)
            throws ResourceNotFoundException, FhirResourceException {

        User patientUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        // check if admin is viewing patient, otherwise editor is patient
        User editor;
        if (adminId != null && !adminId.equals(userId)) {
            editor = userRepository.findById(adminId).get();
        } else {
            editor = patientUser;
        }

        if (editor == null) {
            throw new ResourceNotFoundException("Editor User does not exist");
        }

        Group patientEnteredResultsGroup = groupRepository.findByCode(HiddenGroupCodes.PATIENT_ENTERED.toString());
        if (patientEnteredResultsGroup == null) {
            throw new ResourceNotFoundException("Group for patient entered results does not exist");
        }
        if (CollectionUtils.isEmpty(patientUser.getIdentifiers())) {
            throw new ResourceNotFoundException("Patient must have at least one Identifier (NHS Number or other)");
        }

        List<UUID> uuids = new ArrayList<>();
        for (FhirLink fhirLink : patientUser.getFhirLinks()) {
            UUID subjectId = fhirLink.getResourceId();

            if (fhirLink.getGroup().getCode().equals(HiddenGroupCodes.PATIENT_ENTERED.toString())) {
                List<UUID> foundIds = fhirResource.getLogicalIdsBySubjectId("observation", subjectId);
                uuids.addAll(foundIds);
            }
        }

        // make sure we have record in db
        if (!uuids.contains(UUID.fromString(uuid))) {
            throw new ResourceNotFoundException("Could not find observation in the list.");
        }
        Observation observation = (Observation) fhirResource.get(UUID.fromString(uuid), ResourceType.Observation);
        if (observation == null) {
            throw new ResourceNotFoundException("Could not find observation.");
        }
        // need to convert to fhir observation to record old values in audit log
        FhirObservation fhirObservation = new FhirObservation(observation);

        // delete Observation
        fhirResource.deleteEntity(UUID.fromString(uuid), "observation");

        // Record audit action
        Audit audit = new Audit();
        audit.setAuditActions(AuditActions.PATIENT_ENTERED_RESULT_DELETED);
        audit.setUsername(patientUser.getUsername());
        audit.setActorId(editor.getId());
        // audit.setGroup(group);
        audit.setSourceObjectId(patientUser.getId());
        audit.setSourceObjectType(AuditObjectTypes.User);
        audit.setInformation("Date: " + fhirObservation.getApplies() + " value: " + fhirObservation.getValue());
        auditService.save(audit);
    }

    private org.patientview.api.model.ObservationHeading buildSummaryHeading(Long panel, Long panelOrder,
                                                                             ObservationHeading observationHeading) {
        org.patientview.api.model.ObservationHeading summaryHeading =
                new org.patientview.api.model.ObservationHeading(observationHeading);

        summaryHeading.setPanel(panel);
        summaryHeading.setPanelOrder(panelOrder);
        return summaryHeading;
    }

    private DateTime createDateTime(UserResultCluster resultCluster) throws FhirResourceException {
        try {
            DateTime dateTime = new DateTime();
            DateAndTime dateAndTime = DateAndTime.now();
            dateAndTime.setYear(Integer.parseInt(resultCluster.getYear()));
            dateAndTime.setMonth(Integer.parseInt(resultCluster.getMonth()));
            dateAndTime.setDay(Integer.parseInt(resultCluster.getDay()));
            if (StringUtils.isNotEmpty(resultCluster.getHour())) {
                dateAndTime.setHour(Integer.parseInt(resultCluster.getHour()));
            } else {
                dateAndTime.setHour(0);
            }
            if (StringUtils.isNotEmpty(resultCluster.getMinute())) {
                dateAndTime.setMinute(Integer.parseInt(resultCluster.getMinute()));
            } else {
                dateAndTime.setMinute(0);
            }
            dateAndTime.setSecond(0);
            dateTime.setValue(dateAndTime);
            return dateTime;
        } catch (Exception e) {
            throw new FhirResourceException("Error converting date");
        }
    }

    private DateTime createDateTime(Map<String, String> resultMap) throws FhirResourceException {
        try {
            DateTime dateTime = new DateTime();
            DateAndTime dateAndTime = DateAndTime.now();
            dateAndTime.setYear(Integer.parseInt(resultMap.get("year")));
            dateAndTime.setMonth(Integer.parseInt(resultMap.get("month")));
            dateAndTime.setDay(Integer.parseInt(resultMap.get("day")));
            if (StringUtils.isNotEmpty(resultMap.get("hour"))) {
                dateAndTime.setHour(Integer.parseInt(resultMap.get("hour")));
            } else {
                dateAndTime.setHour(0);
            }
            if (StringUtils.isNotEmpty(resultMap.get("minute"))) {
                dateAndTime.setMinute(Integer.parseInt(resultMap.get("minute")));
            } else {
                dateAndTime.setMinute(0);
            }
            dateAndTime.setSecond(0);
            dateTime.setValue(dateAndTime);
            return dateTime;
        } catch (Exception e) {
            throw new FhirResourceException("Error converting date");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<org.patientview.api.model.FhirObservation> get(final Long userId,
                                                               final String code,
                                                               final String orderBy,
                                                               final String orderDirection,
                                                               final Long limit)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        // check user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        // check either current user or API user with rights to a User's groups
        if (!(getCurrentUser().getId().equals(userId) || ApiUtil.isCurrentUserApiUserForUser(user))) {
            throw new ResourceForbiddenException("Forbidden");
        }

        String codeCleaned = StringUtils.isNotEmpty(code) ? Jsoup.clean(code, Whitelist.relaxed()) : "";

        List<ObservationHeading> observationHeadings = observationHeadingRepository.findByCode(codeCleaned);
        // we should find Observations heading if code valid
        if (CollectionUtils.isEmpty(observationHeadings)) {
            LOG.error("Could not find ObservationHeading, for code " + codeCleaned);
            throw new ResourceNotFoundException("Could nto find observation headings for code");
        }
        List<org.patientview.api.model.FhirObservation> fhirObservations = new ArrayList<>();
        // JPA will ignore Read only
        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content::varchar ");
                query.append("FROM    observation ");
                query.append("WHERE   content -> 'subject' ->> 'display' = '");
                query.append(fhirLink.getResourceId().toString());
                query.append("' ");

                if (StringUtils.isNotEmpty(codeCleaned)) {
                    query.append("AND UPPER(content-> 'name' ->> 'text') = '");
                    query.append(codeCleaned.toUpperCase());
                    query.append("' ");
                }

                if (StringUtils.isNotEmpty(orderBy)) {
                    query.append("ORDER BY content-> '");
                    query.append(orderBy);
                    query.append("' ");
                }

                if (StringUtils.isNotEmpty(orderDirection)) {
                    query.append(orderDirection);
                    query.append(" ");
                }

                if (StringUtils.isNotEmpty(orderBy)) {
                    query.append("LIMIT ");
                    query.append(limit);
                }

                List<Observation> observations = fhirResource.findResourceByQuery(query.toString(), Observation.class);

                Long decimalPlaces = null;
                if (!CollectionUtils.isEmpty(observationHeadings)) {
                    decimalPlaces = observationHeadings.get(0).getDecimalPlaces();
                }

                // convert to transport observations
                for (Observation observation : observations) {
                    FhirObservation fhirObservation = new FhirObservation(observation);

                    if (StringUtils.isNotEmpty(fhirObservation.getValue())) {
                        // set correct number of decimal places
                        try {
                            if (decimalPlaces != null) {
                                fhirObservation.setValue(
                                        new BigDecimal(fhirObservation.getValue()).setScale(decimalPlaces.intValue(),
                                                BigDecimal.ROUND_HALF_UP).toString());
                            } else {
                                fhirObservation.setValue(
                                        new DecimalFormat("0.#####")
                                                .format(Double.valueOf(fhirObservation.getValue())));
                            }
                        } catch (NumberFormatException ignore) {
                            // do not update if cant convert to double or big decimal (string based value)
                            LOG.trace("NumberFormatException", ignore);
                        }
                    }

                    Group fhirGroup = fhirLink.getGroup();
                    if (fhirGroup != null) {
                        fhirObservation.setGroup(fhirGroup);
                    }
                    fhirObservations.add(new org.patientview.api.model.FhirObservation(fhirObservation));
                }
            }
        }

        return fhirObservations;
    }

    @Override
    public List<org.patientview.api.model.FhirObservation> getPatientEnteredByCode(final Long userId,
                                                                                   final String code)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {

        // check user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        // check either current user or API user with rights to a User's groups
        if (!(getCurrentUser().getId().equals(userId) || ApiUtil.isCurrentUserApiUserForUser(user))) {
            throw new ResourceForbiddenException("Forbidden");
        }

        List<ObservationHeading> observationHeadings = observationHeadingRepository.findByCode(code);
        List<org.patientview.api.model.FhirObservation> fhirObservations = new ArrayList<>();

        Map<String, ObservationHeading> observationHeadingMap = new HashMap<>();
        for (ObservationHeading observationHeading : observationHeadings) {
            observationHeadingMap.put(observationHeading.getCode(), observationHeading);
        }

        for (FhirLink fhirLink : user.getFhirLinks()) {

            if (fhirLink.getActive()
                    && fhirLink.getGroup().getCode().equals(HiddenGroupCodes.PATIENT_ENTERED.toString())) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  ");
                query.append("logical_id, ");
                query.append("CONTENT ->> 'appliesDateTime', ");
                query.append("CONTENT -> 'name' ->> 'text', ");
                query.append("CONTENT -> 'valueQuantity' ->> 'value', ");
                query.append("CONTENT -> 'valueQuantity' ->> 'comparator', ");
                query.append("CONTENT -> 'valueCodeableConcept' ->> 'text', ");
                query.append("CONTENT ->> 'status' ");
                query.append("FROM   observation ");
                query.append("WHERE  CONTENT -> 'subject' ->> 'display' = '");
                query.append(fhirLink.getResourceId().toString());
                query.append("' ");

                if (StringUtils.isNotEmpty(code)) {
                    query.append("AND UPPER(content-> 'name' ->> 'text') = '");
                    query.append(code.toUpperCase());
                    query.append("' ");
                }

                // Get a list of values for observation
                List<String[]> observationValues = fhirResource.findValuesByQueryAndArray(query.toString(), 7);

                // convert to transport observations
                for (String[] json : observationValues) {
                    if (!StringUtils.isEmpty(json[0])) {
                        try {
                            org.patientview.api.model.FhirObservation fhirObservation =
                                    new org.patientview.api.model.FhirObservation();

                            // remove timezone and parse date
                            String dateString = json[1];
                            XMLGregorianCalendar xmlDate
                                    = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
                            Date date = xmlDate.toGregorianCalendar().getTime();

                            // convert logical id 
                            fhirObservation.setLogicalId(UUID.fromString(json[0]));
                            fhirObservation.setApplies(date);
                            fhirObservation.setName(json[2]);

                            // handle decimal points if set for this observation type
                            if (StringUtils.isNotEmpty(json[3])) {
                                try {
                                    ObservationHeading observationHeading = observationHeadingMap.get(json[2]);
                                    if (observationHeading != null) {
                                        if (observationHeading.getDecimalPlaces() != null) {
                                            fhirObservation.setValue(new BigDecimal(json[3]).setScale(
                                                    observationHeading.getDecimalPlaces().intValue(),
                                                    BigDecimal.ROUND_HALF_UP).toString());
                                        } else {
                                            fhirObservation.setValue(
                                                    new DecimalFormat("0.#####").format(Double.valueOf(json[3])));
                                        }
                                    } else {
                                        fhirObservation.setValue(
                                                new DecimalFormat("0.#####").format(Double.valueOf(json[3])));
                                    }
                                } catch (NumberFormatException nfe) {
                                    fhirObservation.setValue(json[2]);
                                }
                            } else {
                                // textual value, trim if larger than size
                                if (json.length >= 6 && StringUtils.isNotEmpty(json[5])) {
                                    fhirObservation.setValue(json[5]);
                                }
                            }

                            // need status to check if editable
                            if (StringUtils.isNotEmpty(json[6])) {
                                String status = json[6];
                                if (status.equals(Observation.ObservationStatus.final_.toCode())) {
                                    fhirObservation.setEditable(false);
                                } else {
                                    fhirObservation.setEditable(true);
                                }
                            }

                            Group fhirGroup = fhirLink.getGroup();
                            if (fhirGroup != null) {
                                fhirObservation.setGroup(new BaseGroup(fhirGroup));
                            }

                            fhirObservations.add(fhirObservation);
                        } catch (DatatypeConfigurationException e) {
                            LOG.error(e.getMessage());
                        } catch (Exception e) {
                            LOG.error(e.getMessage());
                        }
                    }
                }
            }
        }
        return fhirObservations;
    }

    @Override
    public List<org.patientview.api.model.FhirObservation> getPatientEnteredDialysisTreatment(final Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {

        // check user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        // check either current user or API user with rights to a User's groups
        if (!(getCurrentUser().getId().equals(userId) || ApiUtil.isCurrentUserApiUserForUser(user))) {
            throw new ResourceForbiddenException("Forbidden");
        }

        /**
         * build list of ObservationHeading codes we need for retrieving patient entered results.
         * Make sure all are in lower case to match
         */
        String[] codesArr = {"hdhours", "hdlocation", "eprex", "targetweight", "weight", "ufvolume", "pulse",
                "bpsys", "bodytemperature", "hypotension", "bps", "dialflow", "litresprocessed"};

        List<ObservationHeading> observationHeadings = observationHeadingRepository.findAllByCode(
                new ArrayList<>(Arrays.asList(codesArr)));
        List<org.patientview.api.model.FhirObservation> fhirObservations = new ArrayList<>();

        Map<String, ObservationHeading> observationHeadingMap = new HashMap<>();
        for (ObservationHeading observationHeading : observationHeadings) {
            observationHeadingMap.put(observationHeading.getCode(), observationHeading);
        }

        for (ObservationHeading heading : observationHeadings) {

            for (FhirLink fhirLink : user.getFhirLinks()) {

                if (fhirLink.getActive()
                        && fhirLink.getGroup().getCode().equals(HiddenGroupCodes.PATIENT_ENTERED.toString())) {
                    StringBuilder query = new StringBuilder();
                    query.append("SELECT  ");
                    query.append("logical_id, ");
                    query.append("CONTENT ->> 'appliesDateTime', ");
                    query.append("CONTENT -> 'name' ->> 'text', ");
                    query.append("CONTENT -> 'valueQuantity' ->> 'value', ");
                    query.append("CONTENT -> 'valueQuantity' ->> 'comparator', ");
                    query.append("CONTENT -> 'valueCodeableConcept' ->> 'text', ");
                    query.append("CONTENT ->> 'status', ");
                    query.append("CONTENT ->> 'comments' ");
                    query.append("FROM   observation ");
                    query.append("WHERE  CONTENT -> 'subject' ->> 'display' = '");
                    query.append(fhirLink.getResourceId().toString());
                    query.append("' ");

                    if (StringUtils.isNotEmpty(heading.getCode())) {
                        query.append("AND UPPER(content-> 'name' ->> 'text') = '");
                        query.append(heading.getCode().toUpperCase());
                        query.append("' ");
                    }

                    // Get a list of values for observation
                    List<String[]> observationValues = fhirResource.findValuesByQueryAndArray(query.toString(), 8);

                    // convert to transport observations
                    for (String[] json : observationValues) {
                        if (!StringUtils.isEmpty(json[0])) {
                            try {
                                org.patientview.api.model.FhirObservation fhirObservation =
                                        new org.patientview.api.model.FhirObservation();

                                // remove timezone and parse date
                                String dateString = json[1];
                                XMLGregorianCalendar xmlDate
                                        = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
                                Date date = xmlDate.toGregorianCalendar().getTime();

                                ObservationHeading observationHeading = observationHeadingMap.get(json[2]);
                                // convert logical id 
                                fhirObservation.setLogicalId(UUID.fromString(json[0]));
                                fhirObservation.setApplies(date);

                                /**
                                 * if we have Pre/Post comments append to name
                                 * ignore if it's not Pre/Post comment as can be user comments
                                 */
                                if (StringUtils.isNotEmpty(json[7]) &&
                                        (COMMENT_PRE.equalsIgnoreCase(json[7]) ||
                                                COMMENT_POST.equalsIgnoreCase(json[7]))) {
                                    String comment = json[7];
                                    fhirObservation.setName("(" + comment + ") " + observationHeading.getHeading());
                                } else {
                                    fhirObservation.setName(observationHeading.getHeading());
                                }

                                fhirObservation.setUnits(observationHeading.getUnits());

                                // handle decimal points if set for this observation type
                                if (StringUtils.isNotEmpty(json[3])) {
                                    try {

                                        if (observationHeading != null) {
                                            if (observationHeading.getDecimalPlaces() != null) {
                                                fhirObservation.setValue(new BigDecimal(json[3]).setScale(
                                                        observationHeading.getDecimalPlaces().intValue(),
                                                        BigDecimal.ROUND_HALF_UP).toString());
                                            } else {
                                                fhirObservation.setValue(
                                                        new DecimalFormat("0.#####").format(Double.valueOf(json[3])));
                                            }
                                        } else {
                                            fhirObservation.setValue(
                                                    new DecimalFormat("0.#####").format(Double.valueOf(json[3])));
                                        }
                                    } catch (NumberFormatException nfe) {
                                        fhirObservation.setValue(json[2]);
                                    }
                                } else {
                                    // textual value, trim if larger than size
                                    if (json.length >= 6 && StringUtils.isNotEmpty(json[5])) {
                                        fhirObservation.setValue(json[5]);
                                    }
                                }

                                Group fhirGroup = fhirLink.getGroup();
                                if (fhirGroup != null) {
                                    fhirObservation.setGroup(new BaseGroup(fhirGroup));
                                }

                                /**
                                 * Results (weight, pulse etc) can be also be from results cluster other then
                                 * Dialysis Treatment hence we need to check if they are read only,
                                 * which would indicate they belong to Dialysis Treatment cluster
                                 * TODO: need to find better solution to handle this
                                 */
                                if (StringUtils.isNotEmpty(json[6])) {
                                    String status = json[6];
                                    if (status.equals(Observation.ObservationStatus.final_.toCode())) {
                                        fhirObservation.setEditable(false);
                                        fhirObservations.add(fhirObservation);
                                    }
                                }
                            } catch (DatatypeConfigurationException e) {
                                LOG.error(e.getMessage());
                            } catch (Exception e) {
                                LOG.error(e.getMessage());
                            }
                        }
                    }
                }
            }

        }
        return fhirObservations;
    }

    @Override
    public List<org.patientview.api.model.FhirObservation> getByFhirLinkAndCodes(final FhirLink fhirLink,
                                                                                 final List<String> codes)
            throws ResourceNotFoundException, FhirResourceException {

        List<org.patientview.api.model.FhirObservation> fhirObservations = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT  content::varchar ");
        query.append("FROM    observation ");
        query.append("WHERE   content -> 'subject' ->> 'display' = '");
        query.append(fhirLink.getResourceId().toString());
        query.append("' ");

        if (!codes.isEmpty()) {
            query.append("AND UPPER(content-> 'name' ->> 'text') IN (");

            for (int i = 0; i < codes.size(); i++) {

                query.append("'");
                query.append(codes.get(i).toUpperCase());
                query.append("' ");

                if (i != codes.size() - 1) {
                    query.append(",");
                }
            }
            query.append(") ");
        }

        List<Observation> observations = fhirResource.findResourceByQuery(query.toString(), Observation.class);

        // convert to transport observations
        for (Observation observation : observations) {
            FhirObservation fhirObservation = new FhirObservation(observation);
            Group fhirGroup = fhirLink.getGroup();
            if (fhirGroup != null) {
                fhirObservation.setGroup(fhirGroup);
            }

            // handle my IBD IBD_DISEASE_EXTENT observations with a diagram
            if (fhirObservation.getValue() != null
                    && fhirObservation.getName().equals(NonTestObservationTypes.IBD_DISEASE_EXTENT.toString())) {
                fhirObservation.setDiagram(IbdDiseaseExtent.getDiagramByName(fhirObservation.getValue()));
            }

            fhirObservations.add(new org.patientview.api.model.FhirObservation(fhirObservation));
        }
        return fhirObservations;
    }

    // gets all latest observations in single query per fhirlink
    private Map<String, org.patientview.api.model.FhirObservation> getLastObservations(final Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        Map<String, ObservationHeading> observationHeadingMap = new HashMap<>();
        for (ObservationHeading observationHeading : observationHeadingRepository.findAll()) {
            observationHeadingMap.put(observationHeading.getCode(), observationHeading);
        }

        Map<String, org.patientview.api.model.FhirObservation> latestObservations = new HashMap<>();
        Map<String, Date> latestObservationDates = new HashMap<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT DISTINCT ON (2) ");
                query.append("CONTENT ->> 'appliesDateTime', ");
                query.append("CONTENT -> 'name' ->> 'text', ");
                query.append("CONTENT -> 'valueQuantity' ->> 'value', ");
                query.append("CONTENT -> 'valueQuantity' ->> 'comparator', ");
                query.append("CONTENT -> 'valueCodeableConcept' ->> 'text' ");
                query.append("FROM   observation ");
                query.append("WHERE  CONTENT -> 'subject' ->> 'display' = '");
                query.append(fhirLink.getResourceId().toString());
                query.append("' ");
                query.append("ORDER  BY 2, 1 DESC");

                List<String[]> observationValues = fhirResource.findLatestObservationsByQuery(query.toString());

                // convert to transport observations
                for (String[] json : observationValues) {
                    if (!StringUtils.isEmpty(json[0])) {
                        try {
                            org.patientview.api.model.FhirObservation fhirObservation
                                    = new org.patientview.api.model.FhirObservation();

                            // remove timezone and parse date
                            String dateString = json[0];
                            XMLGregorianCalendar xmlDate
                                    = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
                            Date date = xmlDate.toGregorianCalendar().getTime();

                            fhirObservation.setApplies(date);
                            fhirObservation.setName(json[1]);

                            // handle decimal points if set for this observation type
                            if (StringUtils.isNotEmpty(json[2])) {
                                try {
                                    ObservationHeading observationHeading = observationHeadingMap.get(json[1]);
                                    if (observationHeading != null) {
                                        if (observationHeading.getDecimalPlaces() != null) {
                                            fhirObservation.setValue(new BigDecimal(json[2]).setScale(
                                                    observationHeading.getDecimalPlaces().intValue(),
                                                    BigDecimal.ROUND_HALF_UP).toString());
                                        } else {
                                            fhirObservation.setValue(
                                                    new DecimalFormat("0.#####").format(Double.valueOf(json[2])));
                                        }
                                    } else {
                                        fhirObservation.setValue(
                                                new DecimalFormat("0.#####").format(Double.valueOf(json[2])));
                                    }
                                } catch (NumberFormatException nfe) {
                                    fhirObservation.setValue(json[2]);
                                }
                            } else {
                                // textual value, trim if larger than size
                                if (json.length >= FIVE && StringUtils.isNotEmpty(json[FOUR])) {
                                    if (json[FOUR].length() > EIGHT) {
                                        fhirObservation.setValue(json[FOUR].subSequence(0, EIGHT).toString() + "..");
                                    } else {
                                        fhirObservation.setValue(json[FOUR]);
                                    }
                                }
                            }

                            if (StringUtils.isNotEmpty(json[THREE])) {
                                fhirObservation.setComparator(json[THREE]);
                            }
                            fhirObservation.setGroup(new BaseGroup(fhirLink.getGroup()));

                            String code = json[1].toUpperCase();

                            if (latestObservationDates.get(code) != null) {
                                if (latestObservationDates.get(code).getTime() < date.getTime()) {
                                    latestObservations.put(code, fhirObservation);
                                    latestObservationDates.put(code, date);
                                }
                            } else {
                                latestObservations.put(code, fhirObservation);
                                latestObservationDates.put(code, date);
                            }

                        } catch (DatatypeConfigurationException e) {
                            LOG.error(e.getMessage());
                        }
                    }
                }
            }
        }

        return latestObservations;
    }

    @Override
    public FhirObservationPage getMultipleByCode(Long userId, List<String> codes, Long limit,
                                                 Long offset, String orderDirection)
            throws ResourceNotFoundException, FhirResourceException {

        // parse pagination
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        Map<String, ObservationHeading> observationHeadingMap = new HashMap<>();
        for (ObservationHeading observationHeading : observationHeadingRepository.findAll()) {
            observationHeadingMap.put(observationHeading.getCode().toUpperCase(), observationHeading);
        }

        if (!(orderDirection.equals("ASC") || orderDirection.equals("DESC"))) {
            orderDirection = "DESC";
        }

        StringBuilder codeString = new StringBuilder();
        StringBuilder fhirLinkString = new StringBuilder();

        for (int i = 0; i < codes.size(); i++) {
            codeString.append("'");
            codeString.append(codes.get(i).toUpperCase());
            codeString.append("'");
            if (i != codes.size() - 1) {
                codeString.append(",");
            }
        }

        List<FhirLink> fhirLinks = new ArrayList<>(user.getFhirLinks());
        Map<String, Group> subjectGroupMap = new HashMap<>();

        for (int i = 0; i < fhirLinks.size(); i++) {
            FhirLink fhirLink = fhirLinks.get(i);
            if (fhirLink.getActive()) {
                if (!subjectGroupMap.containsKey(fhirLink.getResourceId().toString())) {
                    subjectGroupMap.put(fhirLink.getResourceId().toString(), fhirLink.getGroup());
                }

                fhirLinkString.append("'");
                fhirLinkString.append(fhirLink.getResourceId().toString());
                fhirLinkString.append("'");
                if (i != fhirLinks.size() - 1) {
                    fhirLinkString.append(",");
                }
            }
        }

        StringBuilder query = new StringBuilder();
        query.append("SELECT  content::varchar ");
        query.append("FROM    observation ");
        query.append("WHERE   content -> 'subject' ->> 'display' IN (");
        query.append(fhirLinkString);
        query.append(") ");
        query.append("AND UPPER(content-> 'name' ->> 'text') IN (");
        query.append(codeString);
        query.append(") ");
        query.append("ORDER BY content-> 'appliesDateTime' ");
        query.append(orderDirection);

        List<Observation> observations = fhirResource.findResourceByQuery(query.toString(), Observation.class);

        Map<Long, Map<String, List<org.patientview.api.model.FhirObservation>>> tempMap = new TreeMap<>();

        // convert to transport object
        for (Observation observation : observations) {

            // convert from FHIR observation to FhirObservation and add group
            FhirObservation fhirObservation = new FhirObservation(observation);
            if (subjectGroupMap.containsKey(observation.getSubject().getDisplaySimple())) {
                fhirObservation.setGroup(subjectGroupMap.get(observation.getSubject().getDisplaySimple()));
            }

            // set correct number of decimal places
            if (StringUtils.isNotEmpty(fhirObservation.getValue())) {
                try {
                    ObservationHeading observationHeading = observationHeadingMap.get(fhirObservation.getName());
                    if (observationHeading != null) {
                        if (observationHeading.getDecimalPlaces() != null) {
                            fhirObservation.setValue(new BigDecimal(fhirObservation.getValue()).setScale(
                                    observationHeading.getDecimalPlaces().intValue(),
                                    BigDecimal.ROUND_HALF_UP).toString());
                        } else {
                            fhirObservation.setValue(
                                    new DecimalFormat("0.#####").format(Double.valueOf(fhirObservation.getValue())));
                        }
                    } else {
                        fhirObservation.setValue(
                                new DecimalFormat("0.#####").format(Double.valueOf(fhirObservation.getValue())));
                    }
                } catch (NumberFormatException ignore) {
                    // do not update if cant convert to double or big decimal (string based value)
                    LOG.trace("NumberFormatException", ignore);
                }
            }

            // add to output for this date, overriding this observation type if present
            Long applies = fhirObservation.getApplies().getTime();
            if (!tempMap.containsKey(applies)) {
                tempMap.put(applies, new HashMap<String, List<org.patientview.api.model.FhirObservation>>());
            }

            if (tempMap.get(applies).get(fhirObservation.getName()) == null) {
                tempMap.get(applies).put(fhirObservation.getName(),
                        new ArrayList<org.patientview.api.model.FhirObservation>());
            }
            tempMap.get(applies).get(fhirObservation.getName()).add(
                    new org.patientview.api.model.FhirObservation(fhirObservation));
        }

        // now reduce
        Map<Long, Map<String, List<org.patientview.api.model.FhirObservation>>> output;
        ArrayList<Long> keys = new ArrayList<>(tempMap.keySet());
        Long count = Long.valueOf(keys.size());

        if (orderDirection.equals("DESC")) {
            output = new TreeMap<>(Collections.reverseOrder());
            if ((offset == 0 && limit == 1)) {
                output.put(keys.get(keys.size() - 1), tempMap.get(keys.get(keys.size() - 1)));
            } else {
                for (int i = 0; i < keys.size(); i++) {
                    if (count >= offset && count < (offset + limit)) {
                        output.put(keys.get(i), tempMap.get(keys.get(i)));
                    }
                    count--;
                }
            }
        } else {
            output = new TreeMap<>();
            if ((offset == 0 && limit == 1)) {
                output.put(keys.get(0), tempMap.get(keys.get(0)));
            } else {
                for (int i = keys.size() - 1; i >= 0; i--) {
                    if (count >= offset && count < (offset + limit)) {
                        output.put(keys.get(i), tempMap.get(keys.get(i)));
                    }
                    count--;
                }
            }
        }

        int pages = ((tempMap.entrySet().size() - 1) / limit.intValue()) + 1;
        return new FhirObservationPage(output, Long.valueOf(tempMap.entrySet().size()), Long.valueOf(pages));
    }

    @Override
    public Map<Long, Map<String, List<org.patientview.api.model.FhirObservation>>> getObservationsByMultipleCodeAndDate(
            Long userId,
            List<String> codes,
            String orderDirection,
            String fromDate,
            String toDate)
            throws ResourceNotFoundException, FhirResourceException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        Map<String, ObservationHeading> observationHeadingMap = new HashMap<>();
        for (ObservationHeading observationHeading : observationHeadingRepository.findAll()) {
            observationHeadingMap.put(observationHeading.getCode().toUpperCase(), observationHeading);
        }

        if (!(orderDirection.equals("ASC") || orderDirection.equals("DESC"))) {
            orderDirection = "DESC";
        }

        if (codes.size() == 0) {
            for (String observationCode : observationHeadingMap.keySet()) {
                codes.add(observationCode);
            }
        }
        StringBuilder codeString = new StringBuilder();
        StringBuilder fhirLinkString = new StringBuilder();

        for (int i = 0; i < codes.size(); i++) {
            codeString.append("'");
            codeString.append(codes.get(i).toUpperCase());
            codeString.append("'");
            if (i != codes.size() - 1) {
                codeString.append(",");
            }
        }

        List<FhirLink> fhirLinks = new ArrayList<>(user.getFhirLinks());
        Map<String, Group> subjectGroupMap = new HashMap<>();

        for (int i = 0; i < fhirLinks.size(); i++) {
            FhirLink fhirLink = fhirLinks.get(i);
            if (fhirLink.getActive()) {
                if (!subjectGroupMap.containsKey(fhirLink.getResourceId().toString())) {
                    subjectGroupMap.put(fhirLink.getResourceId().toString(), fhirLink.getGroup());
                }

                fhirLinkString.append("'");
                fhirLinkString.append(fhirLink.getResourceId().toString());
                fhirLinkString.append("'");
                if (i != fhirLinks.size() - 1) {
                    fhirLinkString.append(",");
                }
            }
        }

        StringBuilder query = new StringBuilder();
        query.append("SELECT  content::varchar ");
        query.append("FROM    observation ");
        query.append("WHERE   content -> 'subject' ->> 'display' IN (");
        query.append(fhirLinkString);
        query.append(") ");
        query.append("AND UPPER(content-> 'name' ->> 'text') IN (");
        query.append(codeString);
        query.append(") ");
        if (fromDate != null && toDate != null) {
            query.append("AND CONTENT ->> 'appliesDateTime' >= '" + fromDate + "' ");
            query.append("AND CONTENT ->> 'appliesDateTime' <= '" + toDate + "' ");
        }
        query.append("ORDER BY content-> 'appliesDateTime' ");
        query.append(orderDirection);

        query.append(", CONTENT -> 'comments' ASC");

        List<Observation> observations = fhirResource.findResourceByQuery(query.toString(), Observation.class);

        Map<Long, Map<String, List<org.patientview.api.model.FhirObservation>>> tempMap = new TreeMap<>();

        // convert to transport object
        for (Observation observation : observations) {

            // convert from FHIR observation to FhirObservation and add group
            FhirObservation fhirObservation = new FhirObservation(observation);
            if (subjectGroupMap.containsKey(observation.getSubject().getDisplaySimple())) {
                fhirObservation.setGroup(subjectGroupMap.get(observation.getSubject().getDisplaySimple()));
            }

            // set correct number of decimal places
            if (StringUtils.isNotEmpty(fhirObservation.getValue())) {
                try {
                    ObservationHeading observationHeading = observationHeadingMap.get(fhirObservation.getName());
                    if (observationHeading != null) {
                        if (observationHeading.getDecimalPlaces() != null) {
                            fhirObservation.setValue(new BigDecimal(fhirObservation.getValue()).setScale(
                                    observationHeading.getDecimalPlaces().intValue(),
                                    BigDecimal.ROUND_HALF_UP).toString());
                        } else {
                            fhirObservation.setValue(
                                    new DecimalFormat("0.#####").format(Double.valueOf(fhirObservation.getValue())));
                        }
                    } else {
                        fhirObservation.setValue(
                                new DecimalFormat("0.#####").format(Double.valueOf(fhirObservation.getValue())));
                    }
                } catch (NumberFormatException ignore) {
                    // do not update if cant convert to double or big decimal (string based value)
                    LOG.trace("NumberFormatException", ignore);
                }
            }

            // add to output for this date, overriding this observation type if present
            Long applies = fhirObservation.getApplies().getTime();
            if (!tempMap.containsKey(applies)) {
                tempMap.put(applies, new HashMap<String, List<org.patientview.api.model.FhirObservation>>());
            }

            if (tempMap.get(applies).get(fhirObservation.getName()) == null) {
                tempMap.get(applies).put(fhirObservation.getName(),
                        new ArrayList<org.patientview.api.model.FhirObservation>());
            }
            tempMap.get(applies).get(fhirObservation.getName()).add(
                    new org.patientview.api.model.FhirObservation(fhirObservation));
        }

        if (orderDirection.equals("DESC")) {
            Map<Long, Map<String, List<org.patientview.api.model.FhirObservation>>> reverseMap
                    = new TreeMap<>(Collections.reverseOrder());
            reverseMap.putAll(tempMap);
            return reverseMap;
        } else {
            return tempMap;
        }

    }

    @Transactional(readOnly = true)
    @Override
    public List<ObservationSummary> getObservationSummary(Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        List<Group> groups = groupService.findGroupsByUser(user);
        List<Group> specialties = new ArrayList<>();

        for (Group group : groups) {
            if (group.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                specialties.add(group);
            }
        }

        List<ObservationHeading> observationHeadings = Util.convertIterable(observationHeadingRepository.findAll());
        List<ObservationSummary> observationData = new ArrayList<>();
        Map<String, org.patientview.api.model.FhirObservation> latestObservationMap = getLastObservations(user.getId());

        for (Group specialty : specialties) {
            observationData.add(getObservationSummaryMap(specialty, observationHeadings, latestObservationMap));
        }

        return observationData;
    }

    // note: doesn't return change since last observation, must be retrieved separately
    private ObservationSummary getObservationSummaryMap(
            Group group, List<ObservationHeading> observationHeadings,
            Map<String, org.patientview.api.model.FhirObservation> latestObservations)
            throws ResourceNotFoundException, FhirResourceException {
        group = groupRepository.findById(group.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Could not find Group"));

        ObservationSummary observationSummary = new ObservationSummary();
        observationSummary.setPanels(new HashMap<Long, List<org.patientview.api.model.ObservationHeading>>());
        observationSummary.setGroup(new org.patientview.api.model.Group(group));

        for (ObservationHeading observationHeading : observationHeadings) {

            // get panel and panel order for this specialty if available, otherwise use default
            Long panel = observationHeading.getDefaultPanel();
            Long panelOrder = observationHeading.getDefaultPanelOrder();

            for (ObservationHeadingGroup observationHeadingGroup : observationHeading.getObservationHeadingGroups()) {
                if (observationHeadingGroup.getGroup().getId().equals(group.getId())) {
                    panel = observationHeadingGroup.getPanel();
                    panelOrder = observationHeadingGroup.getPanelOrder();
                }
            }

            // don't include any observation heading with panel = 0
            if (panel != null && panel != 0L) {

                // create transport observation heading
                org.patientview.api.model.ObservationHeading transportObservationHeading =
                        buildSummaryHeading(panel, panelOrder, observationHeading);

                // add latest observation
                transportObservationHeading.setLatestObservation(
                        latestObservations.get(observationHeading.getCode().toUpperCase()));

                // add panel if not present and add transport observation heading
                if (observationSummary.getPanels().get(panel) == null) {
                    List<org.patientview.api.model.ObservationHeading> summaryHeadings = new ArrayList<>();
                    summaryHeadings.add(transportObservationHeading);
                    observationSummary.getPanels().put(panel, summaryHeadings);
                } else {
                    observationSummary.getPanels().get(panel).add(transportObservationHeading);
                }
            }
        }

        return observationSummary;
    }

    private Date getObservationDate(Observation observation) {
        if (observation.getApplies() != null) {
            DateTime applies = (DateTime) observation.getApplies();
            DateAndTime date = applies.getValue();
            return new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                    date.getDay(), date.getHour(), date.getMinute(),
                    date.getSecond()).getTimeInMillis());
        }

        return null;
    }

    private String getObservationValue(Observation observation) {
        String valueString = null;

        if (observation.getValue() != null) {
            if (observation.getValue().getClass().equals(Quantity.class)) {
                // value
                Quantity value = (Quantity) observation.getValue();
                if (value.getValueSimple() != null) {
                    valueString = value.getValueSimple().toString();
                }

                // comparator
                Quantity.QuantityComparator quantityComparator = value.getComparatorSimple();
                if (quantityComparator != null && !quantityComparator.equals(Quantity.QuantityComparator.Null)) {
                    valueString = valueString + quantityComparator.toCode();
                }
            } else if (observation.getValue().getClass().equals(CodeableConcept.class)) {
                CodeableConcept value = (CodeableConcept) observation.getValue();
                valueString = value.getTextSimple();
            }
        }

        return valueString;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public ServerResponse importObservations(FhirObservationRange fhirObservationRange) {
        boolean deleteObservations = false;
        boolean insertObservations = false;

        if (StringUtils.isEmpty(fhirObservationRange.getGroupCode())) {
            return new ServerResponse("group code not set");
        }
        if (StringUtils.isEmpty(fhirObservationRange.getIdentifier())) {
            return new ServerResponse("identifier not set");
        }
        if (fhirObservationRange.getStartDate() == null && fhirObservationRange.getEndDate() != null) {
            return new ServerResponse("start date not set");
        }
        if (fhirObservationRange.getStartDate() != null && fhirObservationRange.getEndDate() == null) {
            return new ServerResponse("end date not set");
        }
        if (fhirObservationRange.getStartDate() != null) {
            deleteObservations = true;
        }
        if (StringUtils.isEmpty(fhirObservationRange.getCode())) {
            return new ServerResponse("observation code not set");
        }
        if (!CollectionUtils.isEmpty(fhirObservationRange.getObservations())) {
            insertObservations = true;
        }
        if (!deleteObservations && !insertObservations) {
            return new ServerResponse("must enter either a date range or a list of observations to add");
        }

        if (Util.isInEnum(fhirObservationRange.getCode(), NonTestObservationTypes.class)
                || Util.isInEnum(fhirObservationRange.getCode(), DiagnosticReportObservationTypes.class)) {
            return new ServerResponse("non-test or DiagnosticReport type observations");
        }

        List<ObservationHeading> observationHeadings
                = observationHeadingRepository.findByCode(fhirObservationRange.getCode());

        if (CollectionUtils.isEmpty(observationHeadings)) {
            return new ServerResponse("observation code not found");
        }
        if (observationHeadings.size() > 1) {
            return new ServerResponse("observation code not unique");
        }

        ObservationHeading observationHeading = observationHeadings.get(0);

        Group group = groupRepository.findByCode(fhirObservationRange.getGroupCode());

        if (group == null) {
            return new ServerResponse("group not found");
        }

        // check current logged in user has rights to this group
        if (!(ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || ApiUtil.doesContainGroupAndRole(group.getId(), RoleName.IMPORTER))) {
            return new ServerResponse("failed group and role validation");
        }

        List<Identifier> identifiers = identifierRepository.findByValue(fhirObservationRange.getIdentifier());

        if (CollectionUtils.isEmpty(identifiers)) {
            return new ServerResponse("identifier not found");
        }
        if (identifiers.size() > 1) {
            return new ServerResponse("identifier not unique");
        }

        Identifier identifier = identifiers.get(0);
        User user = identifier.getUser();

        if (user == null) {
            return new ServerResponse("user not found");
        }

        // get fhirlink, create one if not present
        FhirLink fhirLink = Util.getFhirLink(group, fhirObservationRange.getIdentifier(), user.getFhirLinks());

        if (fhirLink == null && insertObservations) {
            try {
                fhirLink = fhirLinkService.createFhirLink(user, identifier, group);
            } catch (FhirResourceException fre) {
                return new ServerResponse(fre.getMessage());
            }
        }

        StringBuilder info = new StringBuilder();

        // delete existing observations in date range
        if (fhirLink != null && deleteObservations) {
            List<UUID> existingObservations = null;

            // get existing observation UUIDs by date range (to remove) and then delete
            try {
                existingObservations = fhirResource.getObservationUuidsBySubjectNameDateRange(
                        fhirLink.getResourceId(), fhirObservationRange.getCode(), fhirObservationRange.getStartDate(),
                        fhirObservationRange.getEndDate());
            } catch (FhirResourceException fre) {
                return new ServerResponse("error getting existing observations");
            }

            if (!CollectionUtils.isEmpty(existingObservations)) {
                try {
                    observationService.deleteObservations(existingObservations);
                    info.append(", deleted ").append(existingObservations.size());
                } catch (FhirResourceException fre) {
                    return new ServerResponse("error deleting existing observations");
                }
            }
        }

        // insert new observations
        if (insertObservations) {
            // build FHIR observation objects
            ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
            TestObservationsBuilder testObservationsBuilder
                    = new TestObservationsBuilder(fhirObservationRange, patientReference);
            try {
                testObservationsBuilder.build();
            } catch (FhirResourceException fre) {
                return new ServerResponse("error building observations");
            }

            List<Observation> observations = testObservationsBuilder.getObservations();

            if (!CollectionUtils.isEmpty(observations)) {
                // handle alerts
                Alert alert = null;

                // get current alert for this result type, only take first (should only be one)
                List<Alert> alerts = alertRepository.findByUserAndObservationHeading(user, observationHeading);
                if (!CollectionUtils.isEmpty(alerts)) {
                    alert = alerts.get(0);
                }

                // prepare new observations for insertion into FHIR
                List<FhirDatabaseObservation> fhirDatabaseObservations = new ArrayList<>();

                try {
                    for (Observation observation : observations) {
                        // must have value
                        if (observation.getValue() != null) {
                            FhirDatabaseObservation fhirDatabaseObservation
                                    = new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation));
                            fhirDatabaseObservations.add(fhirDatabaseObservation);

                            // handle alerts
                            if (alert != null) {
                                Date observationDate = getObservationDate(observation);
                                String observationValue = getObservationValue(observation);

                                if (observationDate != null && observationValue != null) {
                                    if (alert.getLatestDate() == null) {
                                        // is the first time a result has come in for this alert
                                        alert.setLatestDate(observationDate);
                                        alert.setLatestValue(observationValue);
                                        alert.setEmailAlertSent(false);
                                        alert.setMobileAlertSent(false);
                                        alert.setWebAlertViewed(false);
                                        alert.setUpdated(true);
                                    } else {
                                        // previous result has been alerted, check if this one is newer
                                        if (alert.getLatestDate().getTime() < observationDate.getTime()) {
                                            alert.setLatestDate(observationDate);
                                            alert.setLatestValue(observationValue);
                                            alert.setEmailAlertSent(false);
                                            alert.setMobileAlertSent(false);
                                            alert.setWebAlertViewed(false);
                                            alert.setUpdated(true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (FhirResourceException | NullArgumentException e) {
                    return new ServerResponse("error marshalling records");
                }

                if (CollectionUtils.isEmpty(fhirDatabaseObservations)) {
                    return new ServerResponse("error preparing observations");
                }

                try {
                    // insert observations
                    observationService.insertFhirDatabaseObservations(fhirDatabaseObservations);
                    info.append(", added ").append(fhirDatabaseObservations.size());

                    // set alert if present and updated
                    if (alert != null && alert.isUpdated()) {
                        Alert entityAlert = alertRepository.findById(alert.getId()).get();
                        entityAlert.setLatestValue(alert.getLatestValue());
                        entityAlert.setLatestDate(alert.getLatestDate());
                        entityAlert.setWebAlertViewed(alert.isWebAlertViewed());
                        entityAlert.setEmailAlertSent(alert.isEmailAlertSent());
                        entityAlert.setLastUpdate(new Date());
                        alertRepository.save(entityAlert);
                    }

                    // audit as patient data load
                    auditService.createAudit(AuditActions.PATIENT_DATA_SUCCESS, user.getUsername(),
                            getCurrentUser(), user.getId(), AuditObjectTypes.User, group);
                } catch (FhirResourceException fre) {
                    return new ServerResponse("error inserting observations");
                }
            } else {
                return new ServerResponse("error, no observations built");
            }
        }

        return new ServerResponse(null, "done" + info.toString(), true);
    }


    @Override
    public List<org.patientview.api.model.ObservationHeading> getPatientEnteredObservations(
            String identifier, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException,
            ResourceForbiddenException, ResourceInvalidException {


        List<User> patients = userRepository.findByIdentifier(identifier);

        if (patients == null || patients.isEmpty()) {
            throw new ResourceNotFoundException("Could not find patient user");
        }

        // we should only have one patient for NHS number, throw exception if there is data inconsistency
        if (patients.size() > 1) {
            throw new ResourceInvalidException("Found multiple users for identifier " + identifier);
        }

        User patientUser = patients.get(0);

        if (patientUser == null) {
            throw new ResourceNotFoundException("Could not find patient user");
        }

        if (!userService.currentUserSameUnitGroup(patientUser, RoleName.IMPORTER)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        List<ObservationHeading> observationHeadings = Util.convertIterable(observationHeadingRepository.findAll());

        Map<String, List<org.patientview.api.model.FhirObservation>> observationList =
                getPatientObservations(patientUser.getFhirLinks(), fromDate, toDate);
        List<org.patientview.api.model.ObservationHeading> observationHeadingData =
                buildObservationHeadingList(observationHeadings, observationList);

        return observationHeadingData;
    }

    /**
     * Gets a list of patient entered observations in single query per fhirlink
     *
     * @param fhirLinks
     * @return
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    private Map<String, List<org.patientview.api.model.FhirObservation>> getPatientObservations(
            Set<FhirLink> fhirLinks, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException {

        Map<String, ObservationHeading> observationHeadingMap = new HashMap<>();
        for (ObservationHeading observationHeading : observationHeadingRepository.findAll()) {
            observationHeadingMap.put(observationHeading.getCode(), observationHeading);
        }

        Map<String, List<org.patientview.api.model.FhirObservation>> patientObservations = new HashMap<>();

        for (FhirLink fhirLink : fhirLinks) {

            // get only patient entered data
            if (fhirLink.getActive()
                    && fhirLink.getGroup().getCode().equals(HiddenGroupCodes.PATIENT_ENTERED.toString())) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  ");
                query.append("CONTENT ->> 'appliesDateTime', ");
                query.append("CONTENT -> 'name' ->> 'text', ");
                query.append("CONTENT -> 'valueQuantity' ->> 'value', ");
                query.append("CONTENT -> 'valueQuantity' ->> 'comparator', ");
                query.append("CONTENT -> 'valueCodeableConcept' ->> 'text' ");
                query.append("FROM   observation ");
                query.append("WHERE  CONTENT -> 'subject' ->> 'display' = '");
                query.append(fhirLink.getResourceId().toString());
                query.append("' ");
                if (fromDate != null && toDate != null) {
                    query.append("AND CONTENT ->> 'appliesDateTime' >= '" + fromDate + "' ");
                    query.append("AND CONTENT ->> 'appliesDateTime' <= '" + toDate + "' ");
                }
                query.append("ORDER  BY 2, 1 DESC");

                List<String[]> observationValues = fhirResource.findLatestObservationsByQuery(query.toString());

                // convert to transport observations
                for (String[] json : observationValues) {
                    if (!StringUtils.isEmpty(json[0])) {
                        try {
                            org.patientview.api.model.FhirObservation fhirObservation
                                    = new org.patientview.api.model.FhirObservation();

                            // remove timezone and parse date
                            String dateString = json[0];
                            XMLGregorianCalendar xmlDate
                                    = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
                            Date date = xmlDate.toGregorianCalendar().getTime();

                            fhirObservation.setApplies(date);
                            fhirObservation.setName(json[1]);

                            // handle decimal points if set for this observation type
                            if (StringUtils.isNotEmpty(json[2])) {
                                try {
                                    ObservationHeading observationHeading = observationHeadingMap.get(json[1]);
                                    if (observationHeading != null) {
                                        if (observationHeading.getDecimalPlaces() != null) {
                                            fhirObservation.setValue(new BigDecimal(json[2]).setScale(
                                                    observationHeading.getDecimalPlaces().intValue(),
                                                    BigDecimal.ROUND_HALF_UP).toString());
                                        } else {
                                            fhirObservation.setValue(
                                                    new DecimalFormat("0.#####").format(Double.valueOf(json[2])));
                                        }
                                    } else {
                                        fhirObservation.setValue(
                                                new DecimalFormat("0.#####").format(Double.valueOf(json[2])));
                                    }
                                } catch (NumberFormatException nfe) {
                                    fhirObservation.setValue(json[2]);
                                }
                            } else {
                                // textual value, trim if larger than size
                                if (json.length >= FIVE && StringUtils.isNotEmpty(json[FOUR])) {
                                    if (json[FOUR].length() > EIGHT) {
                                        fhirObservation.setValue(json[FOUR].subSequence(0, EIGHT).toString() + "..");
                                    } else {
                                        fhirObservation.setValue(json[FOUR]);
                                    }
                                }
                            }

                            String code = json[1].toUpperCase();

                            if (patientObservations.get(code) != null) {
                                // will need to add all entered results not just latest
                                patientObservations.get(code).add(fhirObservation);
                            } else {
                                List<org.patientview.api.model.FhirObservation> newList = new ArrayList<>();
                                newList.add(fhirObservation);
                                patientObservations.put(code, newList);
                            }

                        } catch (DatatypeConfigurationException e) {
                            LOG.error(e.getMessage());
                        }
                    }
                }
            }
        }

        return patientObservations;
    }

    private List<org.patientview.api.model.ObservationHeading> buildObservationHeadingList(
            List<ObservationHeading> observationHeadings,
            Map<String, List<org.patientview.api.model.FhirObservation>> observationList)
            throws ResourceNotFoundException, FhirResourceException {

        List<org.patientview.api.model.ObservationHeading> headingList = new ArrayList<>();

        for (ObservationHeading observationHeading : observationHeadings) {
            // create transport observation heading
            org.patientview.api.model.ObservationHeading transportObservationHeading =
                    new org.patientview.api.model.ObservationHeading(observationHeading);

            List<org.patientview.api.model.FhirObservation> observations =
                    observationList.get(observationHeading.getCode().toUpperCase());

            if (observations != null && !observations.isEmpty()) {
                transportObservationHeading.setObservations(observations);
                headingList.add(transportObservationHeading);
            }
        }

        return headingList;
    }

}
