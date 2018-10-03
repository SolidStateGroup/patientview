package org.patientview.builder;

import generated.SurveyResponse;
import org.apache.commons.lang3.StringUtils;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.QuestionGroup;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponseScore;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.util.Util;
import org.springframework.util.CollectionUtils;
import uk.org.rixg.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Build a PatientView SurveyResponse given XML based generated SurveyResponse object.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 19/04/2016
 */
public class SurveyResponseBuilder {

    private Survey survey;
    private SurveyResponse surveyResponse;
    private uk.org.rixg.Survey surveyResponseUkrdc;
    private User user;

    public SurveyResponseBuilder(SurveyResponse surveyResponse, Survey survey, User user) {
        this.surveyResponse = surveyResponse;
        this.survey = survey;
        this.user = user;
    }

    public SurveyResponseBuilder(uk.org.rixg.Survey surveyResponse, Survey survey, User user) {
        this.surveyResponseUkrdc = surveyResponse;
        this.survey = survey;
        this.user = user;
    }

    // handle both PatientView SurveyResponse and UKRDC Survey as inputs
    public org.patientview.persistence.model.SurveyResponse build() throws Exception {
        if (this.surveyResponse != null) {
            org.patientview.persistence.model.SurveyResponse newSurveyResponse
                    = new org.patientview.persistence.model.SurveyResponse();

            // date
            newSurveyResponse.setDate(this.surveyResponse.getDate().toGregorianCalendar().getTime());

            // user
            newSurveyResponse.setUser(this.user);

            // survey
            newSurveyResponse.setSurvey(this.survey);

            // create map of question types to Questions
            Map<String, Question> questionMap = new HashMap<>();
            for (QuestionGroup questionGroup : this.survey.getQuestionGroups()) {
                for (Question question : questionGroup.getQuestions()) {
                    questionMap.put(question.getType(), question);
                }
            }

            // question answers
            for (generated.SurveyResponse.QuestionAnswers.QuestionAnswer questionAnswer
                    : this.surveyResponse.getQuestionAnswers().getQuestionAnswer()) {
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
            if (this.surveyResponse.getSurveyResponseScores() != null
                    && !CollectionUtils
                    .isEmpty(this.surveyResponse.getSurveyResponseScores().getSurveyResponseScore())) {
                for (generated.SurveyResponse.SurveyResponseScores.SurveyResponseScore surveyResponseScore :
                        this.surveyResponse.getSurveyResponseScores().getSurveyResponseScore()) {
                    SurveyResponseScore newSurveyResponseScore = new SurveyResponseScore();
                    if (surveyResponseScore.getScore() != null) {
                        newSurveyResponseScore.setScore(surveyResponseScore.getScore().doubleValue());
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

            return newSurveyResponse;
        } else if (this.surveyResponseUkrdc != null) {
            org.patientview.persistence.model.SurveyResponse newSurveyResponse
                    = new org.patientview.persistence.model.SurveyResponse();

            // date
            newSurveyResponse.setDate(this.surveyResponseUkrdc.getSurveyTime().toGregorianCalendar().getTime());

            // user
            newSurveyResponse.setUser(this.user);

            // survey
            newSurveyResponse.setSurvey(this.survey);

            // create map of question types to Questions
            Map<String, Question> questionMap = new HashMap<>();
            for (QuestionGroup questionGroup : this.survey.getQuestionGroups()) {
                for (Question question : questionGroup.getQuestions()) {
                    questionMap.put(question.getType(), question);
                }
            }

            // question answers
            for (uk.org.rixg.Question question
                    : this.surveyResponseUkrdc.getQuestions().getQuestion()) {
                // get question
                Question entityQuestion = questionMap.get(question.getQuestionType().getCode());
                QuestionAnswer newQuestionAnswer = new QuestionAnswer();
                newQuestionAnswer.setQuestion(entityQuestion);
                newQuestionAnswer.setSurveyResponse(newSurveyResponse);

                if (!CollectionUtils.isEmpty(entityQuestion.getQuestionOptions())) {
                    // is a question answer with an option, get question options for this question
                    Map<String, QuestionOption> questionOptionMap = new HashMap<>();
                    for (QuestionOption questionOption : entityQuestion.getQuestionOptions()) {
                        questionOptionMap.put(questionOption.getType(), questionOption);
                    }
                    newQuestionAnswer.setQuestionOption(questionOptionMap.get(question.getResponse()));
                } else {
                    // is a simple value question answer
                    newQuestionAnswer.setValue(question.getResponse());
                }

                newSurveyResponse.getQuestionAnswers().add(newQuestionAnswer);
            }

            // scores
            if (this.surveyResponseUkrdc.getScores() != null
                    && !CollectionUtils.isEmpty(this.surveyResponseUkrdc.getScores().getScore())) {
                Level level = null;

                if (this.surveyResponseUkrdc.getLevels() != null &&
                        this.surveyResponseUkrdc.getLevels().getLevel().size() != 0) {
                    level = this.surveyResponseUkrdc.getLevels().getLevel().get(0);
                }
                for (uk.org.rixg.Score surveyResponseScore :
                        this.surveyResponseUkrdc.getScores().getScore()) {

                    SurveyResponseScore newSurveyResponseScore = new SurveyResponseScore();

                    if (surveyResponseScore.getValue() != null) {
                        newSurveyResponseScore.setScore(Double.parseDouble(surveyResponseScore.getValue()));
                    }
                    if (level != null) {
                        newSurveyResponseScore.setLevel(level.getValue());
                    }

                    newSurveyResponseScore.setSurveyResponse(newSurveyResponse);
                    newSurveyResponseScore.setType(surveyResponseScore.getScoreType().getCode());
                    newSurveyResponse.getSurveyResponseScores().add(newSurveyResponseScore);
                }
            }

            return newSurveyResponse;
        }

        return null;
    }
}
