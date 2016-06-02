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
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.service.SurveyResponseService;
import org.patientview.service.SurveyService;
import org.patientview.service.UkrdcService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.org.rixg.PatientRecord;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 26/05/2016
 */
@Service
public class UkrdcServiceImpl extends AbstractServiceImpl<UkrdcServiceImpl>
        implements UkrdcService {

    @Inject
    IdentifierRepository identifierRepository;

    @Inject
    SurveyResponseRepository surveyResponseRepository;

    @Inject
    SurveyService surveyService;

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
            throw new ImportResourceException("Survey type must be defined");
        }
        if (StringUtils.isEmpty(survey.getSurveyType().getCode())) {
            throw new ImportResourceException("Survey type code must be defined");
        }

        Survey entitySurvey = surveyService.getByType(survey.getSurveyType().getCode());
        if (entitySurvey == null) {
            throw new ImportResourceException("Survey type '" + survey.getSurveyType().getCode() + "' is not defined");
        }
        if (CollectionUtils.isEmpty(entitySurvey.getQuestionGroups())) {
            throw new ImportResourceException("Survey type '" + survey.getSurveyType().getCode()
                    + "' does not have any questions");
        }

        if (survey.getUpdatedOn() == null) {
            throw new ImportResourceException("Date must be set");
        }
        if (survey.getQuestions() == null) {
            throw new ImportResourceException("Must have Questions");
        }
        if (CollectionUtils.isEmpty(survey.getQuestions().getQuestion())) {
            throw new ImportResourceException("Must have at least one Question");
        }

        // question type to response
        Map<String, String> typeResponseMap = new HashMap<>();
        for (uk.org.rixg.Survey.Questions.Question question : survey.getQuestions().getQuestion()) {
            if (CollectionUtils.isEmpty(question.getQuestionType())) {
                throw new ImportResourceException("All Question must have at least one QuestionType");
            }
        }

        /*
        List<String> includedQuestionTypes = new ArrayList<>();

        for (generated.SurveyResponse.QuestionAnswers.QuestionAnswer questionAnswer
                : surveyResponse.getQuestionAnswers().getQuestionAnswer()) {
            if (StringUtils.isEmpty(questionAnswer.getQuestionType())) {
                throw new ImportResourceException("All answers must have a question type");
            }
            Question question = questionMap.get(questionAnswer.getQuestionType());
            if (question == null) {
                throw new ImportResourceException("Question type '" + questionAnswer.getQuestionType()
                        + "' does not match any questions for survey type '"
                        + surveyResponse.getSurveyType() + "'");
            }

            // check if has options and if matches
            List<QuestionOption> questionOptions = question.getQuestionOptions();
            if (CollectionUtils.isEmpty(questionOptions)) {
                // simple value response expected
                if (StringUtils.isEmpty(questionAnswer.getQuestionValue())) {
                    throw new ImportResourceException("Question type '" + questionAnswer.getQuestionType()
                            + "' must have a value set (is a value based question)");
                }
            } else {
                // option response expected
                if (StringUtils.isEmpty(questionAnswer.getQuestionOption())) {
                    throw new ImportResourceException("Question type '" + questionAnswer.getQuestionType()
                            + "' must have an option set (is an option based question)");
                }
                // check option in survey question answer is in list of actual question options
                boolean found = false;
                for (QuestionOption questionOption : questionOptions) {
                    if (questionOption.getType().equals(questionAnswer.getQuestionOption())) {
                        found = true;
                    }
                }
                if (!found) {
                    throw new ImportResourceException("Question type '" + questionAnswer.getQuestionType()
                            + "' must have a known option (is an option based question)");
                }
            }

            // check no duplicate questions
            if (includedQuestionTypes.contains(question.getType())) {
                throw new ImportResourceException("Question type '" + question.getType() + "' is duplicated");
            }
            includedQuestionTypes.add(question.getType());
        }

        // scores
        if (surveyResponse.getSurveyResponseScores() != null) {
            if (CollectionUtils.isEmpty(surveyResponse.getSurveyResponseScores().getSurveyResponseScore())) {
                throw new ImportResourceException("Scores must be defined");
            }
            for (generated.SurveyResponse.SurveyResponseScores.SurveyResponseScore surveyResponseScore
                    : surveyResponse.getSurveyResponseScores().getSurveyResponseScore()) {
                if (StringUtils.isEmpty(surveyResponseScore.getType())) {
                    throw new ImportResourceException("Score type must be defined");
                }
                if (surveyResponseScore.getScore() == null) {
                    throw new ImportResourceException("Score for type '" + surveyResponseScore.getType()
                            + "' must be defined");
                }
                if (surveyResponseScore.getSeverity() != null
                        && !Util.isInEnum(surveyResponseScore.getSeverity().toString(), ScoreSeverity.class)) {
                    throw new ImportResourceException("Score severity must be a known severity type");
                }
            }
        }*/
    }
}
