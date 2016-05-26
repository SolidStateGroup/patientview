package org.patientview.service.impl;

import generated.SurveyResponse;
import org.apache.commons.lang3.StringUtils;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionGroup;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.service.SurveyResponseService;
import org.patientview.service.SurveyService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
public class SurveyResponseServiceImpl extends AbstractServiceImpl<SurveyResponseServiceImpl>
        implements SurveyResponseService {

    @Inject
    IdentifierRepository identifierRepository;

    @Inject
    SurveyService surveyService;

    void throwImportResourceException(String error) throws ImportResourceException {
        //LOG.error(error);
        throw new ImportResourceException(error);
    }

    @Override
    @Transactional
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
                    + "'");
        }
        if (surveyResponse.getQuestionAnswers() == null) {
            throwImportResourceException("Must have survey question answers");
        }
        if (CollectionUtils.isEmpty(surveyResponse.getQuestionAnswers().getQuestionAnswer())) {
            throwImportResourceException("Must have at least one survey question answer");
        }

        // answers
        Map<String, Question> questionMap = new HashMap<>();
        for (QuestionGroup questionGroup : survey.getQuestionGroups()) {
            for (Question question : questionGroup.getQuestions()) {
                questionMap.put(question.getType(), question);
            }
        }

        List<String> includedQuestionTypes = new ArrayList<>();

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
                            + "' must have a value set (is a value based question)");
                }
            } else {
                // option response expected
                if (StringUtils.isEmpty(questionAnswer.getQuestionOption())) {
                    throwImportResourceException("Question type '" + questionAnswer.getQuestionType()
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
                    throwImportResourceException("Question type '" + questionAnswer.getQuestionType()
                            + "' must have a known option (is an option based question)");
                }
            }

            // check no duplicate questions
            if (includedQuestionTypes.contains(question.getType())) {
                throwImportResourceException("Question type '" + question.getType() + "' is duplicated");
            }
            includedQuestionTypes.add(question.getType());
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
}
