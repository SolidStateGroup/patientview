package org.patientview.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.patientview.builder.SurveyResponseBuilder;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionGroup;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.service.AuditService;
import org.patientview.service.SurveyService;
import org.patientview.service.UkrdcService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.org.rixg.PatientRecord;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/06/2016
 */
@Service
public class UkrdcServiceImpl extends AbstractServiceImpl<UkrdcServiceImpl> implements UkrdcService {

    @Inject
    AuditService auditService;

    @Inject
    IdentifierRepository identifierRepository;

    @Inject
    SurveyResponseRepository surveyResponseRepository;

    @Inject
    SurveyService surveyService;

    @Override
    public void process(PatientRecord patientRecord, String xml, Long importerUserId) throws Exception {
        // user
        List<Identifier> identifiers = identifierRepository.findByValue(
                patientRecord.getPatient().getPatientNumbers().getPatientNumber().get(0).getNumber());
        User user = identifiers.get(0).getUser();

        // if surveys, then process surveys
        if (patientRecord.getSurveys() != null
                && !CollectionUtils.isEmpty(patientRecord.getSurveys().getSurvey())) {
            for (uk.org.rixg.Survey survey : patientRecord.getSurveys().getSurvey()) {
                try {
                    processSurvey(survey, user);
                    LOG.info(identifiers.get(0).getIdentifier() + ": survey response type '"
                            + survey.getSurveyType().getCode() + "' added");
                    // audit
                    auditService.createAudit(AuditActions.SURVEY_RESPONSE_SUCCESS, identifiers.get(0).getIdentifier(),
                            null, null, xml, importerUserId);
                } catch (Exception e) {
                    // audit
                    auditService.createAudit(AuditActions.SURVEY_RESPONSE_FAIL, identifiers.get(0).getIdentifier(),
                            null, e.getMessage(), xml, importerUserId);
                    throw(e);
                }
            }
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

        String patientNumber = patientRecord.getPatient().getPatientNumbers().getPatientNumber().get(0).getNumber();

        if (StringUtils.isEmpty(patientNumber)) {
            throw new ImportResourceException("PatientNumbers Number must not be empty");
        }

        List<Identifier> identifiers = identifierRepository.findByValue(patientNumber);
        if (CollectionUtils.isEmpty(identifiers)) {
            throw new ImportResourceException("No patient found with identifier '" + patientNumber + "'");
        }
        if (identifiers.size() != 1) {
            throw new ImportResourceException("Multiple identifiers found with value '" + patientNumber + "'");
        }

        if (patientRecord.getSurveys() != null && !CollectionUtils.isEmpty(patientRecord.getSurveys().getSurvey())) {
            for (uk.org.rixg.Survey survey : patientRecord.getSurveys().getSurvey()) {
                validateSurvey(survey);
            }
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

        for (uk.org.rixg.Survey.Questions.Question question : survey.getQuestions().getQuestion()) {
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
            for (uk.org.rixg.Survey.Scores.Score score : survey.getScores().getScore()) {
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
