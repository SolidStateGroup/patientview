package org.patientview.builder;

import generated.Survey;
import org.apache.commons.lang.StringUtils;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionGroup;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.enums.QuestionElementTypes;
import org.patientview.persistence.model.enums.QuestionHtmlTypes;
import org.springframework.util.CollectionUtils;

/**
 * Build a PatientView survey given XML based generated Survey object.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 18/04/2016
 */
public class SurveyBuilder {

    private Survey survey;

    public SurveyBuilder(Survey survey) {
        this.survey = survey;
    }

    public org.patientview.persistence.model.Survey build() throws Exception {
        // base survey
        org.patientview.persistence.model.Survey newSurvey = new org.patientview.persistence.model.Survey();
        newSurvey.setDescription(StringUtils.isNotEmpty(survey.getDescription()) ? survey.getDescription() : null);
        newSurvey.setType(StringUtils.isNotEmpty(survey.getType()) ? survey.getType() : null);

        // question groups
        for (generated.Survey.QuestionGroups.QuestionGroup questionGroup :
                survey.getQuestionGroups().getQuestionGroup()) {
            // new question group
            QuestionGroup newQuestionGroup = new QuestionGroup();
            newQuestionGroup.setDescription(StringUtils.isNotEmpty(questionGroup.getDescription())
                    ? questionGroup.getDescription() : null);
            if (questionGroup.getDisplayOrder() != null) {
                newQuestionGroup.setDisplayOrder(questionGroup.getDisplayOrder().intValue());
            }
            newQuestionGroup.setNumber(StringUtils.isNotEmpty(questionGroup.getNumber())
                    ? questionGroup.getNumber() : null);
            newQuestionGroup.setSurvey(newSurvey);
            newQuestionGroup.setText(StringUtils.isNotEmpty(questionGroup.getText())
                    ? questionGroup.getText() : null);

            // questions
            for (generated.Survey.QuestionGroups.QuestionGroup.Questions.Question question
                    : questionGroup.getQuestions().getQuestion()) {
                // new question
                Question newQuestion = new Question();
                newQuestion.setDescription(StringUtils.isNotEmpty(question.getDescription())
                        ? question.getDescription() : null);
                if (question.getDisplayOrder() != null) {
                    newQuestion.setDisplayOrder(question.getDisplayOrder().intValue());
                }
                newQuestion.setElementType(QuestionElementTypes.valueOf(question.getElementType().toString()));
                newQuestion.setHelpLink(StringUtils.isNotEmpty(question.getHelpLink()) ? question.getHelpLink() : null);
                newQuestion.setHtmlType(QuestionHtmlTypes.valueOf(question.getHtmlType().toString()));
                newQuestion.setNumber(StringUtils.isNotEmpty(question.getNumber()) ? question.getNumber() : null);
                newQuestion.setQuestionGroup(newQuestionGroup);

                // set custom question to false when imported
                newQuestion.setCustomQuestion(false);

                if (question.getRangeEnd() != null) {
                    newQuestion.setRangeEnd(question.getRangeEnd().intValue());
                }
                if (question.getRangeStart() != null) {
                    newQuestion.setRangeStart(question.getRangeStart().intValue());
                }
                newQuestion.setText(StringUtils.isNotEmpty(question.getText()) ? question.getText() : null);
                newQuestion.setType(StringUtils.isNotEmpty(question.getType()) ? question.getType() : null);

                // question options
                if (question.getQuestionOptions() != null
                        && !CollectionUtils.isEmpty(question.getQuestionOptions().getQuestionOption())) {
                    for (generated.Survey.QuestionGroups.QuestionGroup.Questions.Question.QuestionOptions.QuestionOption
                            questionOption : question.getQuestionOptions().getQuestionOption()) {
                        // new question option
                        QuestionOption newQuestionOption = new QuestionOption();
                        newQuestionOption.setDescription(StringUtils.isNotEmpty(questionOption.getDescription())
                                ? questionOption.getDescription() : null);
                        if (questionOption.getDisplayOrder() != null) {
                            newQuestionOption.setDisplayOrder(questionOption.getDisplayOrder().intValue());
                        }
                        newQuestionOption.setQuestion(newQuestion);
                        newQuestionOption.setText(StringUtils.isNotEmpty(questionOption.getText())
                                ? questionOption.getText() : null);
                        newQuestionOption.setType(StringUtils.isNotEmpty(questionOption.getType())
                                ? questionOption.getType() : null);
                        if (questionOption.getScore() != null) {
                            newQuestionOption.setScore(questionOption.getScore().intValue());
                        }

                        // add question option to question
                        newQuestion.getQuestionOptions().add(newQuestionOption);
                    }
                }

                // add question to question group
                newQuestionGroup.getQuestions().add(newQuestion);
            }

            // add question group to survey
            newSurvey.getQuestionGroups().add(newQuestionGroup);
        }

        return newSurvey;
    }
}
