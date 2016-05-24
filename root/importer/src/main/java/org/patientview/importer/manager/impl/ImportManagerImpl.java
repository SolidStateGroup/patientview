package org.patientview.importer.manager.impl;

import generated.Patientview;
import generated.Survey;
import generated.SurveyResponse;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.manager.ImportManager;
import org.patientview.importer.service.impl.AbstractServiceImpl;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.QuestionGroup;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.SurveyResponseScore;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.QuestionElementTypes;
import org.patientview.persistence.model.enums.QuestionHtmlTypes;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.service.AllergyService;
import org.patientview.service.AuditService;
import org.patientview.service.ConditionService;
import org.patientview.service.DiagnosticService;
import org.patientview.service.DocumentReferenceService;
import org.patientview.service.EncounterService;
import org.patientview.service.GpLetterService;
import org.patientview.service.MedicationService;
import org.patientview.service.ObservationService;
import org.patientview.service.OrganizationService;
import org.patientview.service.PatientService;
import org.patientview.service.PractitionerService;
import org.patientview.service.SurveyService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Service
public class ImportManagerImpl extends AbstractServiceImpl<ImportManager> implements ImportManager {

    @Inject
    private AllergyService allergyService;

    @Inject
    private AuditService auditService;

    @Inject
    private ConditionService conditionService;

    @Inject
    private DiagnosticService diagnosticService;

    @Inject
    private DocumentReferenceService documentReferenceService;

    @Inject
    private EncounterService encounterService;

    @Inject
    private GpLetterService gpLetterService;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private MedicationService medicationService;

    @Inject
    private ObservationService observationService;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private PatientService patientService;

    @Inject
    private PractitionerService practitionerService;

    @Inject
    private SurveyService surveyService;

    @Inject
    private SurveyResponseRepository surveyResponseRepository;

    @Override
    public void process(Patientview patientview, String xml, Long importerUserId) throws ImportResourceException {

        ResourceReference practitionerReference = null;
        ResourceReference organizationReference;

        try {
            Date start = new Date();
            LOG.info(patientview.getPatient().getPersonaldetails().getNhsno() + ": Starting Import");

            // update Organization based on <centrecode> (Unit/centre details)
            UUID organizationUuid = organizationService.add(patientview);
            organizationReference = Util.createResourceReference(organizationUuid);

            // update Practitioner based on <gpname> (GP details)
            UUID practitionerUuid = practitionerService.add(patientview);
            if (practitionerUuid != null) {
                practitionerReference = Util.createResourceReference(practitionerUuid);
            }

            // update core Patient object based on <nhsno>
            FhirLink fhirLink = patientService.add(patientview, practitionerReference);

            try {
                gpLetterService.createGpLetter(fhirLink, patientview);
            } catch (Exception e) {
                LOG.info("Could not create GP letter, continuing: " + e.getMessage());
            }

            // add other practitioners, only used by IBD for named consultant and nurse
            practitionerService.addOtherPractitionersToPatient(patientview, fhirLink);

            // add Observation, deleting existing Observation within <test><daterange> (tests) and existing
            // Observation of type NonTestObservationTypes.BLOOD_GROUP, PTPULSE, DPPULSE
            observationService.add(patientview, fhirLink);

            // add Condition, deleting existing (diagnoses)
            conditionService.add(patientview, fhirLink);

            // add Encounter, deleting existing (used for treatment and transplant status)
            encounterService.add(patientview, fhirLink, organizationReference);

            // add MedicationStatement and associated Medication, deleting existing (drugdetails)
            medicationService.add(patientview, fhirLink);

            // add DiagnosticReport and associated Observation (diagnostics, originally IBD now generic)
            diagnosticService.add(patientview, fhirLink);

            // add DocumentReference, deleting those with the same date (letters)
            documentReferenceService.add(patientview, fhirLink);

            // add AllergyIntolerance, Substance and AdverseReaction (allergy), deleting existing
            allergyService.add(patientview, fhirLink);

            Date end = new Date();
            LOG.info(patientview.getPatient().getPersonaldetails().getNhsno()
                    + ": Finished Import. Took " + getDateDiff(start,end,TimeUnit.SECONDS) + " seconds.");

            auditService.createAudit(AuditActions.PATIENT_DATA_SUCCESS,
                    patientview.getPatient().getPersonaldetails().getNhsno(),
                    patientview.getCentredetails().getCentrecode(), null, xml, importerUserId);

            updateGroupLastImportDate(patientview.getCentredetails().getCentrecode());

        } catch (Exception e) {
            LOG.error(patientview.getPatient().getPersonaldetails().getNhsno()
                    + ": Error importing patient. Message: " + e.getMessage(), e);

            throw new ImportResourceException(patientview.getPatient().getPersonaldetails().getNhsno()
                    + ": Error, " + e.getMessage());
        }
    }

    @Override
    public void process(Survey survey) throws ImportResourceException {
        try {
            surveyService.add(survey);
            LOG.info(survey.getType() + " added");
        } catch (Exception e) {
            LOG.error("Survey process error", e);
            throw new ImportResourceException(e.getMessage());
        }
    }

    @Override
    public void process(SurveyResponse surveyResponse) throws ImportResourceException {
        org.patientview.persistence.model.SurveyResponse newSurveyResponse
                = new org.patientview.persistence.model.SurveyResponse();

        // date
        newSurveyResponse.setDate(surveyResponse.getDate().toGregorianCalendar().getTime());

        // user
        List<Identifier> identifiers = identifierRepository.findByValue(surveyResponse.getIdentifier());
        newSurveyResponse.setUser(identifiers.get(0).getUser());

        // survey
        org.patientview.persistence.model.Survey survey = surveyService.getByType(surveyResponse.getSurveyType());
        newSurveyResponse.setSurvey(survey);

        // create map of question types to Questions
        Map<String, Question> questionMap = new HashMap<>();
        for (QuestionGroup questionGroup : survey.getQuestionGroups()) {
            for (Question question : questionGroup.getQuestions()) {
                questionMap.put(question.getType(), question);
            }
        }

        // question answers
        for (SurveyResponse.QuestionAnswers.QuestionAnswer questionAnswer
                : surveyResponse.getQuestionAnswers().getQuestionAnswer()) {
            // get question
            Question question = questionMap.get(questionAnswer.getQuestionType());
            QuestionAnswer newQuestionAnswer = new QuestionAnswer();
            newQuestionAnswer.setQuestion(question);
            newQuestionAnswer.setSurveyResponse(newSurveyResponse);

            if (StringUtils.isNotEmpty(questionAnswer.getQuestionOption())) {
                // is a question answer with an option, get question options for this question
                Map<String, QuestionOption> questionOptionMap = new HashMap<>();
                for (QuestionOption questionOption : question.getQuestionOptions()) {
                    questionOptionMap.put(questionOption.getType(), questionOption);
                }
                newQuestionAnswer.setQuestionOption(questionOptionMap.get(questionAnswer.getQuestionOption()));
            } else {
                // is a simple value question answer
                newQuestionAnswer.setValue(questionAnswer.getQuestionValue());
            }

            newSurveyResponse.getQuestionAnswers().add(newQuestionAnswer);
        }

        // scores
        if (surveyResponse.getSurveyResponseScores() != null
                && !CollectionUtils.isEmpty(surveyResponse.getSurveyResponseScores().getSurveyResponseScore())) {
            for (SurveyResponse.SurveyResponseScores.SurveyResponseScore surveyResponseScore :
                  surveyResponse.getSurveyResponseScores().getSurveyResponseScore()) {
                SurveyResponseScore newSurveyResponseScore = new SurveyResponseScore();
                if (surveyResponseScore.getScore() != null) {
                    newSurveyResponseScore.setScore(surveyResponseScore.getScore().intValue());
                }
                if (StringUtils.isNotEmpty(surveyResponseScore.getSeverity().toString())) {
                    if (Util.isInEnum(surveyResponseScore.getSeverity().toString(), ScoreSeverity.class)) {
                        newSurveyResponseScore.setSeverity(
                                ScoreSeverity.valueOf(surveyResponseScore.getSeverity().toString()));
                    }
                }
                newSurveyResponseScore.setSurveyResponse(newSurveyResponse);
                newSurveyResponseScore.setType(surveyResponseScore.getType());
                newSurveyResponse.getSurveyResponseScores().add(newSurveyResponseScore);
            }
        }

        // delete existing by user, type, date
        surveyResponseRepository.delete(surveyResponseRepository.findByUserAndSurveyTypeAndDate(
                identifiers.get(0).getUser(), surveyResponse.getSurveyType(),
                surveyResponse.getDate().toGregorianCalendar().getTime()));

        // save new
        surveyResponseRepository.save(newSurveyResponse);

        LOG.info(surveyResponse.getSurveyType() + " response added");
    }

    void throwImportResourceException(String error) throws ImportResourceException {
        LOG.error(error);
        throw new ImportResourceException(error);
    }

    @Override
    public void validate(Patientview patientview) throws ImportResourceException {
        // Patient exists with this identifier
        try {
            patientService.matchPatientByIdentifierValue(patientview);
        } catch (ResourceNotFoundException rnf) {
            throwImportResourceException("Patient with identifier '"
                    + patientview.getPatient().getPersonaldetails().getNhsno() + "' does not exist in PatientView");
        }

        // Group exists
        if (!organizationService.groupWithCodeExists(patientview.getCentredetails().getCentrecode())) {
            throwImportResourceException("Group with code '" + patientview.getCentredetails().getCentrecode()
                    + "' does not exist in PatientView");
        }
    }

    @Override
    public void validate(Survey survey) throws ImportResourceException {
        // survey validation
        if (StringUtils.isEmpty(survey.getType())) {
            throwImportResourceException("Survey type must be defined");
        }
        if (surveyService.getByType(survey.getType()) != null) {
            throwImportResourceException("Survey type '" + survey.getType() + "' already defined");
        }
        if (survey.getQuestionGroups() == null) {
            throwImportResourceException("Survey must have question groups");
        }
        if (CollectionUtils.isEmpty(survey.getQuestionGroups().getQuestionGroup())) {
            throwImportResourceException("Survey must at least one question group");
        }

        // question group validation
        for (Survey.QuestionGroups.QuestionGroup questionGroup : survey.getQuestionGroups().getQuestionGroup()) {
            if (questionGroup.getQuestions() == null) {
                throwImportResourceException("All question groups must contain questions");
            }
            if (CollectionUtils.isEmpty(questionGroup.getQuestions().getQuestion())) {
                throwImportResourceException("All question groups must contain at least one question");
            }
            if (StringUtils.isEmpty(questionGroup.getText())) {
                throwImportResourceException("All question groups must contain text");
            }

            // question validation
            for (Survey.QuestionGroups.QuestionGroup.Questions.Question question :
                questionGroup.getQuestions().getQuestion()) {
                if (question.getElementType() == null) {
                    throwImportResourceException("All questions must have an element type");
                }
                if (!Util.isInEnum(question.getElementType().toString(), QuestionElementTypes.class)) {
                    throwImportResourceException("All questions must have a valid element type");
                }
                if (question.getHtmlType() == null) {
                    throwImportResourceException("All questions must have an html type");
                }
                if (!Util.isInEnum(question.getHtmlType().toString(), QuestionHtmlTypes.class)) {
                    throwImportResourceException("All questions must have a valid html type");
                }
                if (StringUtils.isEmpty(question.getText())) {
                    throwImportResourceException("All questions must contain text");
                }

                // question option validation
                if (question.getQuestionOptions() != null
                        && !CollectionUtils.isEmpty(question.getQuestionOptions().getQuestionOption())) {
                    for (Survey.QuestionGroups.QuestionGroup.Questions.Question.QuestionOptions.QuestionOption
                            questionOption : question.getQuestionOptions().getQuestionOption()) {
                        if (StringUtils.isEmpty(questionOption.getText())) {
                            throwImportResourceException("All question options must contain text");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void validate(SurveyResponse surveyResponse) throws ImportResourceException {
        if (StringUtils.isEmpty(surveyResponse.getSurveyType())) {
            throwImportResourceException("Survey type must be defined");
        }
        org.patientview.persistence.model.Survey survey = surveyService.getByType(surveyResponse.getSurveyType());
        if (survey == null) {
            throwImportResourceException("Survey type '" + surveyResponse.getSurveyType() + "' is not defined");
        }
        if (CollectionUtils.isEmpty(survey.getQuestionGroups())) {
            throwImportResourceException("Survey type '" + surveyResponse.getSurveyType()
                    + "' does not have any questions");
        }
        if (StringUtils.isEmpty(surveyResponse.getIdentifier())) {
            throwImportResourceException("Identifier must be set");
        }
        if (surveyResponse.getDate() == null) {
            throwImportResourceException("Date must be set");
        }
        List<Identifier> identifiers = identifierRepository.findByValue(surveyResponse.getIdentifier());
        if (CollectionUtils.isEmpty(identifiers)) {
            throwImportResourceException("No patient found with identifier '" + surveyResponse.getIdentifier() + "'");
        }
        if (identifiers.size() != 1) {
            throwImportResourceException("Multiple identifiers found with value '" + surveyResponse.getIdentifier()
                    + "', cannot continue");
        }
        if (surveyResponse.getQuestionAnswers() == null) {
            throwImportResourceException("Must have survey answers");
        }
        if (CollectionUtils.isEmpty(surveyResponse.getQuestionAnswers().getQuestionAnswer())) {
            throwImportResourceException("Must have at least one survey answer");
        }

        // answers
        Map<String, Question> questionMap = new HashMap<>();
        for (QuestionGroup questionGroup : survey.getQuestionGroups()) {
            for (Question question : questionGroup.getQuestions()) {
                questionMap.put(question.getType(), question);
            }
        }

        for (SurveyResponse.QuestionAnswers.QuestionAnswer questionAnswer
                : surveyResponse.getQuestionAnswers().getQuestionAnswer()) {
            if (StringUtils.isEmpty(questionAnswer.getQuestionType())) {
                throwImportResourceException("All answers must have a question type");
            }
            Question question = questionMap.get(questionAnswer.getQuestionType());
            if (question == null) {
                throwImportResourceException("Question type '" + questionAnswer.getQuestionType()
                        + "' does not match any questions for survey type '"
                        + surveyResponse.getSurveyType() + "'");
            }

            // check if has options and if matches
            List<QuestionOption> questionOptions = question.getQuestionOptions();
            if (CollectionUtils.isEmpty(questionOptions)) {
                // simple value response expected
                if (StringUtils.isEmpty(questionAnswer.getQuestionValue())) {
                    throwImportResourceException("Question type '" + questionAnswer.getQuestionType()
                            + "' must have a value set");
                }
            } else {
                // option response expected
                if (StringUtils.isEmpty(questionAnswer.getQuestionOption())) {
                    throwImportResourceException("Question type '" + questionAnswer.getQuestionType()
                            + "' must have an option set");
                }
                // check option in survey question answer is in list of actual question options
                boolean found = false;
                for (QuestionOption questionOption : questionOptions) {
                    if (questionOption.getType().equals(questionAnswer.getQuestionOption())) {
                        found = true;
                    }
                }
                if (!found) {
                    throwImportResourceException("Question type '" + questionAnswer.getQuestionType()
                            + "' must have a known option");
                }
            }
        }

        // scores
        if (surveyResponse.getSurveyResponseScores() != null) {
            if (CollectionUtils.isEmpty(surveyResponse.getSurveyResponseScores().getSurveyResponseScore())) {
                throwImportResourceException("Scores must be defined");
            }
            for (SurveyResponse.SurveyResponseScores.SurveyResponseScore surveyResponseScore
                    : surveyResponse.getSurveyResponseScores().getSurveyResponseScore()) {
                if (StringUtils.isEmpty(surveyResponseScore.getType())) {
                    throwImportResourceException("Score type must be defined");
                }
                if (surveyResponseScore.getScore() == null) {
                    throwImportResourceException("Score for type '" + surveyResponseScore.getType()
                            + "' must be defined");
                }
                if (surveyResponseScore.getSeverity() != null
                        && !Util.isInEnum(surveyResponseScore.getSeverity().toString(), ScoreSeverity.class)) {
                    throwImportResourceException("Score severity must be a known severity type");
                }
            }
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
    
    private void updateGroupLastImportDate(String groupCode) {
        Group group = groupRepository.findByCode(groupCode);
        if (group != null) {
            group.setLastImportDate(new Date());
            groupRepository.save(group);
        }
    }
}
