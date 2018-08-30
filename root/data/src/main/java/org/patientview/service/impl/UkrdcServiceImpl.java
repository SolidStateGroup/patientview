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
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionGroup;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.AuditService;
import org.patientview.service.FhirLinkService;
import org.patientview.service.SurveyService;
import org.patientview.service.UkrdcService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.org.rixg.Document;
import uk.org.rixg.PatientNumber;
import uk.org.rixg.PatientRecord;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/06/2016
 */
@Service
public class UkrdcServiceImpl extends AbstractServiceImpl<UkrdcServiceImpl> implements UkrdcService {

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
    SurveyService surveyService;

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
            Group group = groupRepository.findByCode(patientRecord.getSendingFacility().toString());

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
            if (patientRecord.getSendingFacility() == null || StringUtils.isEmpty(patientRecord.getSendingFacility().toString())) {
                throw new ImportResourceException("SendingFacility must be defined (for Documents)");
            }
            if (groupRepository.findByCode(patientRecord.getSendingFacility().toString()) == null) {
                throw new ImportResourceException("SendingFacility PatientView Group not found (for Documents)");
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
}
