package org.patientview.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.builder.DocumentReferenceBuilder;
import org.patientview.builder.MediaBuilder;
import org.patientview.builder.SurveyResponseBuilder;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.Hospitalisation;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Immunisation;
import org.patientview.persistence.model.InsDiaryRecord;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.QuestionGroup;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.Relapse;
import org.patientview.persistence.model.RelapseMedication;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.SurveySendingFacility;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.ImmunisationCodelist;
import org.patientview.persistence.model.enums.OedemaTypes;
import org.patientview.persistence.model.enums.RelapseMedicationTypes;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.persistence.repository.SurveySendingFacilityRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.AuditService;
import org.patientview.service.FhirLinkService;
import org.patientview.service.SurveyService;
import org.patientview.service.UkrdcService;
import org.patientview.util.UUIDType5;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.org.rixg.CFSNOMED;
import uk.org.rixg.CodedField;
import uk.org.rixg.Diagnosis;
import uk.org.rixg.Document;
import uk.org.rixg.DrugProduct;
import uk.org.rixg.Encounter;
import uk.org.rixg.Location;
import uk.org.rixg.Medication;
import uk.org.rixg.Name;
import uk.org.rixg.Observation;
import uk.org.rixg.Observations;
import uk.org.rixg.Patient;
import uk.org.rixg.PatientNumber;
import uk.org.rixg.PatientNumbers;
import uk.org.rixg.PatientRecord;
import uk.org.rixg.Procedure;
import uk.org.rixg.ProgramMembership;
import uk.org.rixg.SendingExtract;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/06/2016
 */
@Service
public class UkrdcServiceImpl extends AbstractServiceImpl<UkrdcServiceImpl> implements UkrdcService {

    private static final String YOUR_HEALTH = "YOUR_HEALTH";
    private static final String INS_APP_CODE = "INS_APP";
    private static final String INS_APP_DESC = "INS App";
    private static final String INS_WEB_CODE = "INS_WEB";
    private static final String INS_WEB_DESC = "INS Website";
    private static final String PV_CODING_STANDARDS = "PV";
    private static final String PV_CODE = "PatientView";
    private static final String INS_HOSP_CODE = "INS_HOSPITALISATION";
    private static final String INS_HOSP_CODE_DESC = "INS Hospitalisation";
    private static final String INS_RELAPSE_CODE = "INS_RELAPSE";
    private static final String INS_RELAPSE_CODE_DESC = "INS Relapse";
    private static final String ePro = "ePro";
    private static final String eProMembership = "EPro";
    private static final String EPRO_FALLBACK = "optepro";
    private static final String INS_GROUP_FACILITY = "INS_GROUP";

    public static final String SYSTOLIC_BP_CODE = "bpsys";
    public static final String DISATOLIC_BP_CODE = "bpdia";
    public static final String WEIGHT_CODE = "weight";
    public static final String PROTEIN_DIPSTICK_CODE = "updipstick";
    public static final String ODEMA_CODE = "odema";


    /**
     * Trigger Types for Relapse recording.
     * Mapping these manually as no PV codes exist.
     */
    public enum RelapseTriggerTypes {
        VIRAL_INFECTION("Viral Infection", "34014006", "Viral Disease"),
        COMMON_COLD("Common Cold", "82272006", "Common Cold"),
        HAY_FEVER("Hay Fever", "367498001", "Seasonal allergic rhinitis"),
        ALLERGIC_REACTION("Allergic Reaction", "419076005", "Allergic Reaction"),
        ALLERGIC_SKIN_RASH("Allergic Skin Rash", "21626009", "Cutaneous hypersensitivity"),
        FOOD_INTOLERANCE("Food intolerance", "235719002", "Food intolerance");

        private String name;
        private String code;
        private String description;

        RelapseTriggerTypes(String name, String code, String description) {
            this.name = name;
            this.code = code;
            this.description = description;
        }

        public String getName() {
            return this.name;
        }

        public String getId() {
            return this.name();
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    @Inject
    AuditService auditService;

    @Inject
    FhirLinkService fhirLinkService;

    @Inject
    FhirResource fhirResource;

    @Inject
    FileDataRepository fileDataRepository;

    @Inject
    GroupRepository groupRepository;

    @Inject
    IdentifierRepository identifierRepository;

    @Inject
    SurveyResponseRepository surveyResponseRepository;

    @Inject
    SurveySendingFacilityRepository surveySendingFacilityRepository;

    @Inject
    SurveyService surveyService;

    @Inject
    private ObservationHeadingRepository observationHeadingRepository;

    static String generateExternalId(String nhsNumber, String membership) {

        UUID uuid = UUIDType5.nameUUIDFromNamespaceAndBytes(
                UUIDType5.NAMESPACE_YHS, (nhsNumber + membership).getBytes(
                        Charset.defaultCharset()));

        return uuid.toString().replace("-", "");
    }

    @Override
    public void process(PatientRecord patientRecord, String xml, String identifier, Long importerUserId)
            throws Exception {
        // identifier
        List<Identifier> identifiers = identifierRepository.findByValue(identifier);
        Identifier foundIdentifier = identifiers.get(0);
        String foundIdentifierStr = foundIdentifier.getIdentifier();

        // user
        User user = foundIdentifier.getUser();

        // if surveys, then process surveys
        if (patientRecord.getSurveys() != null && !CollectionUtils.isEmpty(patientRecord.getSurveys().getSurvey())) {
            for (uk.org.rixg.Survey survey : patientRecord.getSurveys().getSurvey()) {
                try {
                    processSurvey(survey, user);
                    LOG.info(foundIdentifierStr + ": survey response type '"
                            + survey.getSurveyType().getCode() + "' added");
                    // audit
                    auditService.createAudit(AuditActions.SURVEY_RESPONSE_SUCCESS, foundIdentifierStr,
                            null, null, xml, importerUserId);
                } catch (Exception e) {
                    // audit
                    auditService.createAudit(AuditActions.SURVEY_RESPONSE_FAIL, foundIdentifierStr,
                            null, e.getMessage(), xml, importerUserId);
                    throw (e);
                }
            }
        }

        // if documents, process
        if (patientRecord.getDocuments() != null
                && !CollectionUtils.isEmpty(patientRecord.getDocuments().getDocument())) {
            // need group for documents
            Group group = groupRepository.findByCode(patientRecord.getSendingFacility().getValue());

            for (Document document : patientRecord.getDocuments().getDocument()) {
                try {
                    processDocument(document, user, foundIdentifier, group);
                    LOG.info(foundIdentifierStr + ": document type '"
                            + document.getDocumentType().getCode() + "' added");
                } catch (Exception e) {
                    throw (e);
                }
            }
        }
    }

    private void processDocument(Document document, User user, Identifier identifier, Group group) throws Exception {
        // get FhirLink if exists
        FhirLink fhirLink = Util.getFhirLink(group, identifier.getIdentifier(), user.getFhirLinks());

        if (fhirLink == null) {
            // FhirLink doesn't exist, create including patient record in FHIR
            try {
                fhirLink = fhirLinkService.createFhirLink(user, identifier, group);
            } catch (FhirResourceException fre) {
                LOG.error("FhirResourceException creating FhirLink");
                fre.printStackTrace();
                throw new ImportResourceException("FhirResourceException creating FhirLink: " + fre.getMessage());
            }
        }

        // delete existing with same patient reference, date and type
        Map<String, String> logicalLocationMap = fhirResource.getDocumentReferenceUuidAndMediaUuid(
                fhirLink.getResourceId(), document.getDocumentType().getCode(),
                document.getDocumentTime().toGregorianCalendar().getTime());

        if (!logicalLocationMap.keySet().isEmpty()) {
            for (String logicalId : logicalLocationMap.keySet()) {
                String locationUuid = logicalLocationMap.get(logicalId);

                if (locationUuid != null) {
                    // delete associated media and binary data if present
                    Media media = (Media) fhirResource.get(UUID.fromString(locationUuid), ResourceType.Media);

                    if (media != null) {
                        // delete media
                        fhirResource.deleteEntity(UUID.fromString(locationUuid), "media");

                        // delete binary data
                        try {
                            if (fileDataRepository.exists(Long.valueOf(media.getContent().getUrlSimple()))) {
                                fileDataRepository.delete(Long.valueOf(media.getContent().getUrlSimple()));
                            }
                        } catch (NumberFormatException nfe) {
                            LOG.info("Error deleting existing binary data, " +
                                    "Media reference to binary data is not Long, ignoring");
                        }
                    }
                }

                fhirResource.deleteEntity(UUID.fromString(logicalId), "documentreference");
            }
        }

        // create reference to patient
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());

        // build FHIR DocumentReference
        DocumentReferenceBuilder documentReferenceBuilder = new DocumentReferenceBuilder(document, patientReference);
        DocumentReference documentReference = documentReferenceBuilder.build();

        // build FHIR Media
        MediaBuilder mediaBuilder = new MediaBuilder(document);
        mediaBuilder.build();

        Media media = mediaBuilder.getMedia();

        // create binary file
        Date now = new Date();
        FileData fileData = new FileData();
        fileData.setCreated(now);

        if (media.getContent().getTitle() != null) {
            fileData.setName(media.getContent().getTitleSimple());
        } else {
            fileData.setName(String.valueOf(now.getTime()));
        }
        if (media.getContent().getContentType() != null) {
            fileData.setType(media.getContent().getContentTypeSimple());
        } else {
            fileData.setType("application/unknown");
        }

        // set binary data
        fileData.setContent(document.getStream());
        fileData.setSize(Long.valueOf(document.getStream().length));

        // store binary data
        fileData = fileDataRepository.save(fileData);

        if (fileData == null) {
            throw new FhirResourceException("error adding file data");
        }

        // set Media file data ID and size
        media = mediaBuilder.setFileDataId(media, fileData.getId());
        media = mediaBuilder.setFileSize(media, document.getStream().length);

        // create Media and set DocumentReference location to newly created Media logicalId
        try {
            // create Media
            FhirDatabaseEntity createdMedia
                    = fhirResource.createEntity(media, ResourceType.Media.name(), "media");

            // add as location to document reference
            documentReference.setLocationSimple(createdMedia.getLogicalId().toString());
        } catch (FhirResourceException e) {
            fileDataRepository.delete(fileData);
            throw new FhirResourceException("Unable to create Media, cleared binary data");
        }

        // create new DocumentReference
        try {
            fhirResource.createEntity(
                    documentReference, ResourceType.DocumentReference.name(), "documentreference");
        } catch (FhirResourceException e) {
            fileDataRepository.delete(fileData);
            throw new FhirResourceException("Unable to create DocumentReference, cleared binary data");
        }
    }

    private void processSurvey(uk.org.rixg.Survey survey, User user) throws Exception {
        // survey
        String surveyType = survey.getSurveyType().getCode();
        Date surveyDate = survey.getSurveyTime().toGregorianCalendar().getTime();
        Survey entitySurvey = surveyService.getByType(surveyType);

        // build
        SurveyResponse newSurveyResponse = new SurveyResponseBuilder(survey, entitySurvey, user).build();

        // delete existing by user, type, date
        surveyResponseRepository.delete(surveyResponseRepository.findByUserAndSurveyTypeAndDate(
                user, surveyType, surveyDate));

        // save new
        surveyResponseRepository.save(newSurveyResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildSurveyXml(SurveyResponse surveyResponse, String type)
            throws DatatypeConfigurationException, JAXBException {

        User user = surveyResponse.getUser();

        PatientRecord patientRecord = new PatientRecord();

        PatientRecord.SendingFacility sendingFacility = new PatientRecord.SendingFacility();
        patientRecord.setSendingFacility(sendingFacility);
        patientRecord.setSendingExtract(SendingExtract.SURVEY);

        Patient patient = new Patient();

        PatientNumbers patientNumbers = new PatientNumbers();
        Set<Identifier> identifiers = user.getIdentifiers();

        List<PatientNumber> patientNumberList = new ArrayList<>();

        Group unitCode = null;
        for (Group group : groupRepository.findGroupByUser(user)) {

            for (GroupFeature groupFeature : group.getGroupFeatures()) {
                if (groupFeature.getFeature().getName().equals(FeatureType.OPT_EPRO.toString())) {

                    unitCode = group;
                }
            }
        }

        String nhsNumber = null;
        if (CollectionUtils.isEmpty(identifiers)) {
            LOG.error("Cannot build PatientNumbers, missing identifiers, patient id {}", user.getId());
        }

        /*
            Builds a list of PatientNumber based on MRN rule: if a patient has multiple NHS identifiers
            the one to be used should be selected using the order NHS -> CHI -> HSC
         */

        Map<String, Identifier> identifierMap = new HashMap<>();
        for (Identifier identifier : identifiers) {
            // We ignore NON_UK_UNIQUE, HOSPITAL_NUMBER and RADAR_NUMBER identifiers
            if (identifier.getIdentifierType().getValue().equals(IdentifierTypes.NON_UK_UNIQUE.getId()) ||
                    identifier.getIdentifierType().getValue().equals(IdentifierTypes.HOSPITAL_NUMBER.getId()) ||
                    identifier.getIdentifierType().getValue().equals(IdentifierTypes.RADAR_NUMBER.getId())) {
                continue;
            }
            identifierMap.put(identifier.getIdentifierType().getValue(), identifier);

            // Add none MRN type Identifiers
            // If a patient has an NHS_NO and a CHI_NO
            // the output should be NHS_NO (Type MRN), NHS_NO (Type NI) and CHI_NO (Type NI).
            PatientNumber patientNumber = new PatientNumber();
            patientNumber.setNumber(identifier.getIdentifier());
            patientNumber.setOrganization(generateOrganization(identifier.getIdentifierType().getValue()));
            patientNumber.setNumberType("NI");

            patientNumberList.add(patientNumber);
        }

        // Add MRN type Identifier
        // MRN rule: if a patient has multiple NHS identifiers the one to be used should
        // be selected using the order NHS -> CHI -> HSC.
        if (!CollectionUtils.isEmpty(identifierMap)) {

            PatientNumber mrnPatientNumber = new PatientNumber();
            Identifier identifier = null;

            // selection should be in this order NHS -> CHI -> HSC
            if (identifierMap.get(IdentifierTypes.NHS_NUMBER.getId()) != null) {
                identifier = identifierMap.get(IdentifierTypes.NHS_NUMBER.getId());

            } else if (identifierMap.get(IdentifierTypes.CHI_NUMBER.getId()) != null) {
                identifier = identifierMap.get(IdentifierTypes.CHI_NUMBER.getId());

            } else if (identifierMap.get(IdentifierTypes.HSC_NUMBER.getId()) != null) {
                identifier = identifierMap.get(IdentifierTypes.HSC_NUMBER.getId());
            }

            String organization =
                    generateOrganization(identifier.getIdentifierType().getValue());
            mrnPatientNumber.setNumber(identifier.getIdentifier());
            mrnPatientNumber.setOrganization(organization);
            mrnPatientNumber.setNumberType("MRN");

            nhsNumber = identifier.getIdentifier();

            patientNumberList.add(mrnPatientNumber);
        }

        Group facilityCode = getSendingFacilityCode(unitCode.getId());

        sendingFacility.setValue(
                facilityCode != null ? facilityCode.getCode() : EPRO_FALLBACK);

        patientNumbers.getPatientNumber().addAll(patientNumberList);
        patient.setPatientNumbers(patientNumbers);
        Patient.Names names = new Patient.Names();
        Name name = new Name();
        name.setUse("L");
        name.setFamily(user.getSurname());
        name.setGiven(user.getForename());
        names.getName().add(name);

        patient.setNames(names);

        // Hardcode to 9 - UNKNOWN
        patient.setGender("9");

        GregorianCalendar birthTime = new GregorianCalendar();
        birthTime.setTime(user.getDateOfBirth());
        XMLGregorianCalendar birthCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(birthTime);
        birthCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        patient.setBirthTime(birthCalendar);
        patientRecord.setPatient(patient);

        ProgramMembership programMembership = new ProgramMembership();
        programMembership.setProgramName(ePro);
        programMembership.setExternalId(generateExternalId(nhsNumber, eProMembership));

        GregorianCalendar fromTime = new GregorianCalendar();
        fromTime.setTime(surveyResponse.getDate());
        XMLGregorianCalendar xMLGregorianCalendar =
                DatatypeFactory.newInstance().newXMLGregorianCalendar(fromTime);
        xMLGregorianCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        programMembership.setFromTime(xMLGregorianCalendar);

        programMembership.setUpdatedOn(xMLGregorianCalendar);

        PatientRecord.ProgramMemberships programMemberships = new PatientRecord.ProgramMemberships();
        programMemberships.getProgramMembership().add(programMembership);
        patientRecord.setProgramMemberships(programMemberships);

        uk.org.rixg.Survey survey = new uk.org.rixg.Survey();

        GregorianCalendar surveyTime = new GregorianCalendar();
        surveyTime.setTime(surveyResponse.getDate());
        survey.setSurveyTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(surveyTime));

        CodedField surveyType = new CodedField();
        surveyType.setCode(type);
        surveyType.setCodingStandard("SURVEY");
        survey.setSurveyType(surveyType);

        CodedField enteredBy = new CodedField();
        enteredBy.setCodingStandard("PV_USERS");

        enteredBy.setCode(surveyResponse.getUser().getUsername());
        enteredBy.setDescription(surveyResponse.getUser().getForename() + " " + surveyResponse.getUser().getSurname());
        survey.setEnteredBy(enteredBy);

        Location enteredAt = new Location();
        enteredAt.setCode("PatientView");
        survey.setEnteredAt(enteredAt);

        List<uk.org.rixg.Question> ukrdcQuestions = new ArrayList<>();

        for (QuestionAnswer questionAnswer : surveyResponse.getQuestionAnswers()) {

            uk.org.rixg.Question ukrdcQuestion = new uk.org.rixg.Question();

            CodedField questionType = new CodedField();
            questionType.setCodingStandard(YOUR_HEALTH);
            questionType.setCode(questionAnswer.getQuestion().getType());
            ukrdcQuestion.setQuestionType(questionType);

            if (questionAnswer.getQuestionOption() != null) {

                ukrdcQuestion.setResponse(questionAnswer.getQuestionOption().getType());
            }

            if (questionAnswer.getValue() != null) {

                ukrdcQuestion.setResponse(questionAnswer.getValue());
            }

            if (questionAnswer.getQuestionText() != null) {

                ukrdcQuestion.setQuestionText(questionAnswer.getQuestionText());
            }

            ukrdcQuestions.add(ukrdcQuestion);
        }

        uk.org.rixg.Survey.Questions questions = new uk.org.rixg.Survey.Questions();
        questions.getQuestion().addAll(ukrdcQuestions);
        survey.setQuestions(questions);

        PatientRecord.Surveys surveys = new PatientRecord.Surveys();
        surveys.getSurvey().add(survey);

        patientRecord.setSurveys(surveys);

        JAXBContext jaxbContext = JAXBContext.newInstance(PatientRecord.class);
        StringWriter xml = new StringWriter();
        jaxbContext.createMarshaller().marshal(patientRecord, xml);

        return xml.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String buildInsDiaryXml(User user,
                                   List<InsDiaryRecord> insDiaryRecords,
                                   List<Hospitalisation> hospitalisations,
                                   List<Immunisation> immunisations)
            throws DatatypeConfigurationException, JAXBException {

        PatientRecord patientRecord = new PatientRecord();

        PatientRecord.SendingFacility sendingFacility = new PatientRecord.SendingFacility();
        sendingFacility.setValue("PV");
        patientRecord.setSendingFacility(sendingFacility);
        patientRecord.setSendingExtract(SendingExtract.UKRDC);

        Patient patient = new Patient();

        PatientNumbers patientNumbers = new PatientNumbers();
        Set<Identifier> identifiers = user.getIdentifiers();

        List<PatientNumber> patientNumberList = new ArrayList<>();

        // TODO: needs logic fixing
        // One NHS identifier should be sent as an MRN type. This is the primary identifier and
        // should be picked from the available ones in the order NHS -> CHI -> HSC
        String nhsNumber = null;
        for (Identifier identifier : identifiers) {
            String organization = generateOrganization(identifier.getIdentifierType().getValue());
            if (organization.equals("NHS")) {

                PatientNumber patientNumber = new PatientNumber();
                patientNumber.setNumber(identifier.getIdentifier());
                patientNumber.setOrganization(organization);

                nhsNumber = identifier.getIdentifier();

                patientNumber.setNumberType("NI");
                patientNumberList.add(patientNumber);
            }
        }

        if (nhsNumber != null) {

            PatientNumber MRN = new PatientNumber();
            MRN.setOrganization("NHS");
            MRN.setNumber(nhsNumber);
            MRN.setNumberType("MRN");

            patientNumberList.add(MRN);
        }

        patientNumbers.getPatientNumber().addAll(patientNumberList);
        patient.setPatientNumbers(patientNumbers);
        Patient.Names names = new Patient.Names();
        Name name = new Name();
        name.setUse("L");
        name.setFamily(user.getSurname());
        name.setGiven(user.getForename());
        names.getName().add(name);

        patient.setNames(names);

        // Hardcode to 9 - UNKNOWN
        patient.setGender("9");

        if (user.getDateOfBirth() != null) {
            GregorianCalendar birthTime = new GregorianCalendar();
            birthTime.setTime(user.getDateOfBirth());
            XMLGregorianCalendar birthCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(birthTime);
            birthCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
            patient.setBirthTime(birthCalendar);
        }

        patientRecord.setPatient(patient);

        this.buildHospitalisations(patientRecord, hospitalisations);
        this.buildImmunisation(patientRecord, immunisations);

        this.buildDiaryRecordings(patientRecord, insDiaryRecords);

        Set<Relapse> relapses = new HashSet<>();
        // extract unique Relapse records from InsDiaries
        for (InsDiaryRecord record : insDiaryRecords) {
            if (record.getRelapse() != null && !relapses.contains(record.getRelapse())) {
                relapses.add(record.getRelapse());
            }
        }

        this.buildRelapse(patientRecord, relapses);


        JAXBContext jaxbContext = JAXBContext.newInstance(PatientRecord.class);
        StringWriter xml = new StringWriter();
        jaxbContext.createMarshaller().marshal(patientRecord, xml);

        return xml.toString();
    }

    /**
     * Takes an id from the group a survey was taken under and
     * uses the {@link SurveySendingFacility} mapping to generate
     * a unit code to send to UKRDC.
     *
     * @param surveyGroupId id of the group a survey was taken under.
     * @return Sending facility code.
     */
    private Group getSendingFacilityCode(Long surveyGroupId) {

        SurveySendingFacility surveySendingFacility =
                surveySendingFacilityRepository.findBySurveyGroup_Id(surveyGroupId);

        if (surveySendingFacility == null) {

            return null;
        }

        return surveySendingFacility.getUnit();
    }

    private String buildProgramName(Group group) {

        if (group == null) {

            return null;
        }

        return "PV.HOSPITAL." + group.getCode();
    }

    /**
     * Provide a mapping between PV values and UKRDC.
     */
    private String generateOrganization(String value) {

        switch (value) {

            case "NHS_NUMBER":
                return "NHS";
            case "CHI_NUMBER":
                return "CHI";
            case "HSC_NUMBER":
                return "HSC";
            default:
                return "NHS";
        }
    }

    @Override
    @Transactional
    public void validate(PatientRecord patientRecord) throws ImportResourceException {

        // validate patient
        if (patientRecord.getPatient() == null) {
            throw new ImportResourceException("Patient must be defined");
        }
        if (patientRecord.getPatient().getPatientNumbers() == null) {
            throw new ImportResourceException("PatientNumbers must be defined");
        }
        if (CollectionUtils.isEmpty(patientRecord.getPatient().getPatientNumbers().getPatientNumber())) {
            throw new ImportResourceException("PatientNumbers must have at least one Number");
        }

        // check if we have anything against the patient in db
        findIdentifier(patientRecord);

        // validate surveys
        if (patientRecord.getSurveys() != null && !CollectionUtils.isEmpty(patientRecord.getSurveys().getSurvey())) {
            for (uk.org.rixg.Survey survey : patientRecord.getSurveys().getSurvey()) {
                validateSurvey(survey);
            }
        }

        // validate documents
        if (patientRecord.getDocuments() != null
                && !CollectionUtils.isEmpty(patientRecord.getDocuments().getDocument())) {
            // documents will be stored in fhir so must know group
            if (patientRecord.getSendingFacility() == null || StringUtils.isEmpty(patientRecord.getSendingFacility().getValue())) {
                throw new ImportResourceException("SendingFacility must be defined (for Documents)");
            }
            if (groupRepository.findByCode(patientRecord.getSendingFacility().getValue()) == null) {
                throw new ImportResourceException(String.format("SendingFacility PatientView Group not found (%s for " +
                        "Documents)", patientRecord.getSendingFacility().getValue()));
            }

            for (Document document : patientRecord.getDocuments().getDocument()) {
                validateDocument(document);
            }
        }
    }

    @Override
    @Transactional
    public String findIdentifier(PatientRecord patientRecord) throws ImportResourceException {

        String identifier = null;

        if (patientRecord.getPatient() != null
                && patientRecord.getPatient().getPatientNumbers() != null
                && !CollectionUtils.isEmpty(patientRecord.getPatient().getPatientNumbers().getPatientNumber())) {

            /**
             * Go through the list of patient identifiers and check if we have any patients matching any.
             * We should find only one (patient) identifier for given patient numbers
             */
            for (PatientNumber number : patientRecord.getPatient().getPatientNumbers().getPatientNumber()) {
                String patientNumber = number.getNumber();
                if (StringUtils.isNotEmpty(patientNumber)) {

                    List<Identifier> identifiers = identifierRepository.findByValue(patientNumber);

                    // just continue until we find one
                    if (CollectionUtils.isEmpty(identifiers)) {
                        identifier = null;
                        continue;
                    }
                    if (!CollectionUtils.isEmpty(identifiers) && identifiers.size() > 1) {
                        throw new ImportResourceException("Found more then one identifier for patient number "
                                + patientNumber);
                    }

                    identifier = patientNumber;
                    break;
                }
            }
        } else {
            throw new ImportResourceException("Missing patient number in PatientRecord");
        }

        if (StringUtils.isEmpty(identifier)) {
            throw new ImportResourceException("Could not match PatientNumbers to any patient identifier");
        }

        return identifier;
    }

    private void validateDocument(Document document) throws ImportResourceException {
        if (document.getDocumentType() == null) {
            throw new ImportResourceException("Document DocumentType must be defined");
        }
        if (StringUtils.isEmpty(document.getDocumentType().getCode())) {
            throw new ImportResourceException("Document DocumentType Code must be defined");
        }
        if (document.getFileType() == null) {
            throw new ImportResourceException("Document FileType must be defined");
        }
        if (StringUtils.isEmpty(document.getFileType())) {
            throw new ImportResourceException("Document FileType Code must be defined");
        }
        if (document.getStream() == null) {
            throw new ImportResourceException("Document Stream must be defined");
        }
        if (document.getStream().length < 1) {
            throw new ImportResourceException("Document Stream length too short");
        }
        if (document.getDocumentTime() == null) {
            throw new ImportResourceException("Document DocumentTime must be set");
        }
    }

    private void validateSurvey(uk.org.rixg.Survey survey) throws ImportResourceException {
        if (survey.getSurveyType() == null) {
            throw new ImportResourceException("SurveyType must be defined");
        }
        if (StringUtils.isEmpty(survey.getSurveyType().getCode())) {
            throw new ImportResourceException("SurveyType Code must be defined");
        }

        Survey entitySurvey = surveyService.getByType(survey.getSurveyType().getCode());
        if (entitySurvey == null) {
            throw new ImportResourceException("Survey type '" + survey.getSurveyType().getCode() + "' is not defined");
        }
        if (CollectionUtils.isEmpty(entitySurvey.getQuestionGroups())) {
            throw new ImportResourceException("Survey type '" + survey.getSurveyType().getCode()
                    + "' in database does not have any questions");
        }

        if (survey.getSurveyTime() == null) {
            throw new ImportResourceException("Survey Date must be set");
        }
        if (survey.getQuestions() == null) {
            throw new ImportResourceException("Survey must have Questions");
        }
        if (CollectionUtils.isEmpty(survey.getQuestions().getQuestion())) {
            throw new ImportResourceException("Survey must have at least one Question");
        }

        // map of question type to question from survey
        Map<String, Question> questionMap = new HashMap<>();

        for (QuestionGroup questionGroup : entitySurvey.getQuestionGroups()) {
            for (Question question : questionGroup.getQuestions()) {
                questionMap.put(question.getType(), question);
            }
        }

        List<String> includedQuestionTypes = new ArrayList<>();

        for (uk.org.rixg.Question question : survey.getQuestions().getQuestion()) {
            if (question.getQuestionType() == null) {
                throw new ImportResourceException("All Question must have a QuestionType");
            }

            // get question type
            String code = question.getQuestionType().getCode();

            if (StringUtils.isEmpty(code)) {
                throw new ImportResourceException("All Question must have a QuestionType Code");
            }

            Question entityQuestion = questionMap.get(code);

            if (entityQuestion == null) {
                throw new ImportResourceException("Question type '" + code
                        + "' does not match any questions for survey type '" + entitySurvey.getType() + "'");
            }

            // todo: can have empty Response?, don't include if no Response?
            //if (StringUtils.isNotEmpty(question.getResponse())) {

            // check if has options and if matches
            List<QuestionOption> questionOptions = entityQuestion.getQuestionOptions();
            if (CollectionUtils.isEmpty(questionOptions)) {
                // simple value response expected
                if (StringUtils.isEmpty(question.getResponse())) {
                    throw new ImportResourceException("Question type '" + code
                            + "' must have a Response set (is a value based question)");
                }
            } else {
                // option response expected
                if (StringUtils.isEmpty(question.getResponse())) {
                    throw new ImportResourceException("Question type '" + code
                            + "' must have a Response set (is an option based question)");
                }
                // check option in survey question answer is in list of actual question options
                boolean found = false;
                for (QuestionOption questionOption : questionOptions) {
                    if (questionOption.getType().equals(question.getResponse())) {
                        found = true;
                    }
                }
                if (!found) {
                    throw new ImportResourceException("Question type '" + code
                            + "' must have a known option (is an option based question)");
                }
            }

            // check no duplicate questions
            if (includedQuestionTypes.contains(code)) {
                throw new ImportResourceException("Question type '" + code + "' is duplicated");
            }
            includedQuestionTypes.add(code);
            //}
        }

        if (includedQuestionTypes.isEmpty()) {
            throw new ImportResourceException("Must have at least one Question with a Response");
        }

        // scores
        if (survey.getScores() != null && !CollectionUtils.isEmpty(survey.getScores().getScore())) {
            for (uk.org.rixg.Score score : survey.getScores().getScore()) {
                if (score.getScoreType() == null) {
                    throw new ImportResourceException("Score must have ScoreType");
                }

                if (StringUtils.isEmpty(score.getScoreType().getCode())) {
                    throw new ImportResourceException("Score ScoreType must have Code");
                }

                if (StringUtils.isEmpty(score.getValue())) {
                    throw new ImportResourceException("Score must have Value");
                }

                try {
                    Double.parseDouble(score.getValue());
                } catch (NumberFormatException nfe) {
                    throw new ImportResourceException("Score Value must be double");
                }
            }
        }
    }

    /**
     * InsDiaryRecord are daily recordings converted to Observations.
     *
     * @param patientRecord a patient record that holds all the data
     * @param diaryRecords  a list of InsDiaryRecord to build Observations
     */
    private void buildDiaryRecordings(PatientRecord patientRecord, List<InsDiaryRecord> diaryRecords)
            throws DatatypeConfigurationException {
        /*
             <Observations>
               <Observation>
                  <ObservationTime>2012-11-07T00:00:00Z</ObservationTime>
                  <ObservationCode>
                     <CodingStandard>PV</CodingStandard>
                     <Code>weight</Code>
                     <Description>Weight</Description>
                  </ObservationCode>
                  <ObservationValue>92.4</ObservationValue>
                  <EnteredAt>
                     <CodingStandard>PV</CodingStandard>
                     <Code>PV</Code>
                     <Description>PatientView</Description>
                  </EnteredAt>
               </Observation>
            </Observations>
        */

        Location enteredAt = new Location();
        enteredAt.setCode(PV_CODING_STANDARDS);
        enteredAt.setCodingStandard(PV_CODING_STANDARDS);
        enteredAt.setDescription(PV_CODE);

        Observations observations = new Observations();

        for (InsDiaryRecord record : diaryRecords) {

            GregorianCalendar dairyEntryTime = new GregorianCalendar();
            dairyEntryTime.setTime(record.getEntryDate());

            // add Systolic BP result
            if (record.getSystolicBP() != null) {
                Observation observation = buildObservation(record.getSystolicBP().toString(),
                        SYSTOLIC_BP_CODE, dairyEntryTime, enteredAt);
                observations.getObservation().add(observation);
            }

            // add Diastolic BP result
            if (record.getDiastolicBP() != null) {
                Observation observation = buildObservation(record.getDiastolicBP().toString(),
                        DISATOLIC_BP_CODE, dairyEntryTime, enteredAt);
                observations.getObservation().add(observation);
            }

            // add weight
            if (record.getWeight() != null) {
                Observation observation = buildObservation(record.getWeight().toString(),
                        WEIGHT_CODE, dairyEntryTime, enteredAt);
                observations.getObservation().add(observation);
            }

            // urine dipstick
            if (record.getDipstickType() != null) {
                Observation observation = buildObservation(record.getDipstickType().getName(),
                        PROTEIN_DIPSTICK_CODE, dairyEntryTime, enteredAt);
                observations.getObservation().add(observation);
            }

            if (!CollectionUtils.isEmpty(record.getOedema())) {
                for (OedemaTypes oedema : record.getOedema()) {
                    Observation observation = buildObservation(oedema.getName(),
                            ODEMA_CODE, dairyEntryTime, enteredAt);
                    observations.getObservation().add(observation);
                }
            }
        }

        patientRecord.setObservations(observations);
    }

    /**
     * Builds Encounters, Diagnoses, Medications data for PatientRecord from given list of Relapse recordings
     *
     * @param patientRecord a patient record that holds all the data
     * @param relapses      a list of Relapse to build Encounters + Diagnoses + Medications
     */
    private void buildRelapse(PatientRecord patientRecord, Set<Relapse> relapses) throws DatatypeConfigurationException {

        // TODO: will need to refactor once we know where recordings came from App or Web.
        Location enteredAt = new Location();
        enteredAt.setCode(INS_WEB_CODE);
        enteredAt.setCodingStandard(PV_CODING_STANDARDS);
        enteredAt.setDescription(INS_WEB_DESC);

        CodedField admitReason = new CodedField();
        admitReason.setCode(INS_RELAPSE_CODE);
        admitReason.setCodingStandard(PV_CODING_STANDARDS);
        admitReason.setDescription(INS_RELAPSE_CODE_DESC);

        PatientRecord.Diagnoses diagnoses = new PatientRecord.Diagnoses();
        PatientRecord.Medications medications = new PatientRecord.Medications();

        for (Relapse relapse : relapses) {

            // build Encounters

            Encounter encounter = new Encounter();
            encounter.setEncounterNumber(relapse.getId().toString());
            encounter.setEncounterType("N");

            if (relapse.getRelapseDate() != null) {
                GregorianCalendar fromTime = new GregorianCalendar();
                fromTime.setTime(relapse.getRelapseDate());
                encounter.setFromTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(fromTime));
            }

            if (relapse.getRemissionDate() != null) {
                GregorianCalendar toTime = new GregorianCalendar();
                toTime.setTime(relapse.getRemissionDate());
                encounter.setToTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(toTime));
            }
            encounter.setAdmitReason(admitReason);
            encounter.setEnteredAt(enteredAt);

            patientRecord.getEncounters().getEncounter().add(encounter);

            // build Diagnoses

            // Viral Infection
            if (StringUtils.isNotBlank(relapse.getViralInfection())) {
                Diagnosis diagnosis = new Diagnosis();
                diagnosis.setEncounterNumber(relapse.getId().toString());
                diagnosis.setEnteredAt(enteredAt);

                CFSNOMED diagnosisCode = new CFSNOMED();
                diagnosisCode.setCode(RelapseTriggerTypes.VIRAL_INFECTION.getCode()); // SNOMED code;
                diagnosisCode.setCodingStandard("SNOMED");
                diagnosisCode.setDescription(relapse.getViralInfection());
                diagnosis.setDiagnosis(diagnosisCode);
                diagnoses.getDiagnosis().add(diagnosis);
            }

            // Common Cold
            if (relapse.isCommonCold()) {
                Diagnosis diagnosis = buildDiagnosis(relapse.getId().toString(),
                        RelapseTriggerTypes.COMMON_COLD, enteredAt);
                diagnoses.getDiagnosis().add(diagnosis);
            }

            // Hay Fever
            if (relapse.isHayFever()) {
                Diagnosis diagnosis = buildDiagnosis(relapse.getId().toString(),
                        RelapseTriggerTypes.HAY_FEVER, enteredAt);
                diagnoses.getDiagnosis().add(diagnosis);
            }

            // Allergic Reaction
            if (relapse.isAllergicReaction()) {
                Diagnosis diagnosis = buildDiagnosis(relapse.getId().toString(),
                        RelapseTriggerTypes.ALLERGIC_REACTION, enteredAt);
                diagnoses.getDiagnosis().add(diagnosis);
            }

            // Allergic Skin Rash
            if (relapse.isAllergicSkinRash()) {
                Diagnosis diagnosis = buildDiagnosis(relapse.getId().toString(),
                        RelapseTriggerTypes.ALLERGIC_SKIN_RASH, enteredAt);
                diagnoses.getDiagnosis().add(diagnosis);
            }

            // Food intolerance
            if (relapse.isFoodIntolerance()) {
                Diagnosis diagnosis = buildDiagnosis(relapse.getId().toString(),
                        RelapseTriggerTypes.FOOD_INTOLERANCE, enteredAt);
                diagnoses.getDiagnosis().add(diagnosis);
            }

            // build Medications
            if (!CollectionUtils.isEmpty(relapse.getMedications())) {
                List<Medication> medicationList = buildMedications(relapse.getId().toString(),
                        relapse.getMedications());
                medications.getMedication().addAll(medicationList);
            }
        }


        patientRecord.setDiagnoses(diagnoses);
        patientRecord.setMedications(medications);
    }

    /**
     * Builds Procedures data for PatientRecord from given list of Immunisation recordings.
     *
     * @param patientRecord
     * @param immunisations a list of Immunisation recordings for the patient
     */
    private void buildImmunisation(PatientRecord patientRecord, List<Immunisation> immunisations)
            throws DatatypeConfigurationException {

        // TODO: will need to refactor once we know where recordings came from App or Web.
        Location enteredAt = new Location();
        enteredAt.setCode(INS_WEB_CODE);
        enteredAt.setCodingStandard(PV_CODING_STANDARDS);
        enteredAt.setDescription(INS_WEB_DESC);

        PatientRecord.Procedures procedures = new PatientRecord.Procedures();

        for (Immunisation record : immunisations) {
            Procedure procedure = new Procedure();
            CFSNOMED procedureType = new CFSNOMED();
            procedureType.setCode(record.getCodelist().getCode()); // SNOMED code;
            procedureType.setCodingStandard("SNOMED");
            if (record.getCodelist().equals(ImmunisationCodelist.OTHER)) {
                procedureType.setDescription(record.getOther());
            } else {
                procedureType.setDescription(record.getCodelist().getDescription());
            }

            procedure.setProcedureType(procedureType);

            GregorianCalendar immunisationTime = new GregorianCalendar();
            immunisationTime.setTime(record.getImmunisationDate());
            procedure.setProcedureTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(immunisationTime));

            procedure.setEnteredAt(enteredAt);
            procedures.getProcedure().add(procedure);
        }

        patientRecord.setProcedures(procedures);
    }

    /**
     * Builds Encounters data for PatientRecord from given list of Hospitalisation recordings.
     *
     * @param patientRecord
     * @param hospitalisations a list of Hospitalisation recordings for the patient
     */
    private void buildHospitalisations(PatientRecord patientRecord, List<Hospitalisation> hospitalisations)
            throws DatatypeConfigurationException {


        // TODO: will need to refactor once we know where recordings came from App or Web.
        Location enteredAt = new Location();
        enteredAt.setCode(INS_WEB_CODE);
        enteredAt.setCodingStandard(PV_CODING_STANDARDS);
        enteredAt.setDescription(INS_WEB_DESC);

        CodedField admitReason = new CodedField();
        admitReason.setCode(INS_HOSP_CODE);
        admitReason.setCodingStandard(PV_CODING_STANDARDS);
        admitReason.setDescription(INS_HOSP_CODE_DESC);

        PatientRecord.Encounters encounters = null;
        if (patientRecord.getEncounters() == null) {
            encounters = new PatientRecord.Encounters();
        } else {
            encounters = patientRecord.getEncounters();
        }

        List<Encounter> encounterList = new ArrayList<>();
        for (Hospitalisation record : hospitalisations) {
            Encounter encounter = new Encounter();
            encounter.setEncounterNumber(record.getId().toString());
            encounter.setEncounterType("N");

            if (record.getDateAdmitted() != null) {
                GregorianCalendar fromTime = new GregorianCalendar();
                fromTime.setTime(record.getDateAdmitted());
                encounter.setFromTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(fromTime));
            }

            if (record.getDateDischarged() != null) {
                GregorianCalendar toTime = new GregorianCalendar();
                toTime.setTime(record.getDateDischarged());
                encounter.setToTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(toTime));
            }

            encounter.setVisitDescription(record.getReason());
            encounter.setAdmitReason(admitReason);
            encounter.setEnteredAt(enteredAt);

            encounterList.add(encounter);
        }

        encounters.getEncounter().addAll(encounterList);
        patientRecord.setEncounters(encounters);
    }


    /**
     * Builds Observation from given values
     *
     * @param value     a value for this observation
     * @param code      a code for observation heading
     * @param time      a time when observation was recorded
     * @param enteredAt
     * @return an Observation object
     * @throws DatatypeConfigurationException
     */
    private Observation buildObservation(String value, String code,
                                         GregorianCalendar time, Location enteredAt)
            throws DatatypeConfigurationException {
        Observation observation = new Observation();
        observation.setObservationTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(time));

        ObservationHeading observationHeading = observationHeadingRepository.findOneByCode(code);
        if (observationHeading != null) {
            Observation.ObservationCode observationCode = new Observation.ObservationCode();
            observationCode.setCodingStandard(PV_CODING_STANDARDS);
            observationCode.setCode(observationHeading.getCode());
            observationCode.setDescription(observationHeading.getName());
            observation.setObservationCode(observationCode);
        } else {
            LOG.error("Observation Heading not found for code " + code);
        }
        observation.setObservationValue(value);
        observation.setEnteredAt(enteredAt);

        return observation;
    }

    /**
     * Builds Diagnosis from Relapse record
     *
     * @param encounterId an id of the Relapse record
     * @param trigger     a type of the Relapse trigger
     * @param enteredAt
     * @return a Diagnosis
     */
    private Diagnosis buildDiagnosis(String encounterId,
                                     RelapseTriggerTypes trigger,
                                     Location enteredAt) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setEncounterNumber(encounterId);
        diagnosis.setEnteredAt(enteredAt);

        CFSNOMED diagnosisCode = new CFSNOMED();
        diagnosisCode.setCode(trigger.getCode()); // SNOMED code;
        diagnosisCode.setCodingStandard("SNOMED");
        diagnosisCode.setDescription(trigger.getDescription());
        diagnosis.setDiagnosis(diagnosisCode);

        return diagnosis;
    }

    /**
     * Builds Medications from Relapse record
     *
     * @param encounterId        an id of the Relapse record
     * @param relapseMedications a lust of relapse medication records
     * @return a list of Medication
     */
    private List<Medication> buildMedications(String encounterId,
                                              List<RelapseMedication> relapseMedications)
            throws DatatypeConfigurationException {
        List<Medication> medications = new ArrayList<>();

        Location enteringOrganization = new Location();
        enteringOrganization.setCode(INS_WEB_CODE);
        enteringOrganization.setCodingStandard(PV_CODING_STANDARDS);
        enteringOrganization.setDescription(INS_WEB_DESC);

        for (RelapseMedication relapseMedication : relapseMedications) {
            Medication medicationToAdd = new Medication();
            medicationToAdd.setEnteringOrganization(enteringOrganization);
            medicationToAdd.setEncounterNumber(encounterId);

            if (relapseMedication.getStarted() != null) {
                GregorianCalendar fromTime = new GregorianCalendar();
                fromTime.setTime(relapseMedication.getStarted());
                medicationToAdd.setFromTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(fromTime));
            }
            if (relapseMedication.getStopped() != null) {
                GregorianCalendar toTime = new GregorianCalendar();
                toTime.setTime(relapseMedication.getStopped());
                medicationToAdd.setToTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(toTime));
            }

            // Medication Route
            if (relapseMedication.getRoute() != null) {
                Medication.Route route = new Medication.Route();
                route.setCode(relapseMedication.getRoute().getCode());
                route.setCodingStandard("SNOMED");
                route.setDescription(relapseMedication.getRoute().getDescription());
                medicationToAdd.setRoute(route);
            }

            // Drug Name
            if (relapseMedication.getName() != null) {
                DrugProduct product = new DrugProduct();
                if (relapseMedication.getName().equals(RelapseMedicationTypes.OTHER)) {
                    product.setGeneric(relapseMedication.getOther());
                } else {
                    product.setGeneric(relapseMedication.getName().getName());
                }
                medicationToAdd.setDrugProduct(product);
            }

            if (relapseMedication.getDoseFrequency() != null) {
                medicationToAdd.setFrequency(relapseMedication.getDoseFrequency().getValue());
            }

            if (relapseMedication.getDoseQuantity() != null) {
                medicationToAdd.setDoseQuantity(new BigDecimal(relapseMedication.getDoseQuantity()));
            }

            if (relapseMedication.getDoseUnits() != null) {
                CodedField doseUntit = new CodedField();
                doseUntit.setCode(relapseMedication.getDoseUnits().getCode());
                doseUntit.setCodingStandard("SNOMED");
                doseUntit.setDescription(relapseMedication.getDoseUnits().getDescription());
                medicationToAdd.setDoseUoM(doseUntit);
            }

            medications.add(medicationToAdd);
        }

        return medications;
    }
}
