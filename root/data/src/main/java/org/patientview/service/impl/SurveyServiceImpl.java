package org.patientview.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.patientview.builder.SurveyBuilder;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.enums.QuestionElementTypes;
import org.patientview.persistence.model.enums.QuestionHtmlTypes;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.service.SurveyService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
@Service
public class SurveyServiceImpl extends AbstractServiceImpl<SurveyServiceImpl> implements SurveyService {

    @Inject
    private SurveyRepository surveyRepository;

    @Override
    @Transactional
    public Survey add(generated.Survey survey) throws Exception {
        return surveyRepository.save(new SurveyBuilder(survey).build());
    }

    @Override
    @Transactional
    public Survey getByType(final String type) {
        List<Survey> surveys = surveyRepository.findByType(type);
        return !CollectionUtils.isEmpty(surveys) ? surveys.get(0) : null;
    }

    @Override
    public void validate(generated.Survey survey) throws ImportResourceException {
        // survey validation
        if (StringUtils.isEmpty(survey.getType())) {
            throw new ImportResourceException("Survey type must be defined");
        }
        if (getByType(survey.getType()) != null) {
            throw new ImportResourceException("Survey type '" + survey.getType() + "' already defined");
        }
        if (survey.getQuestionGroups() == null) {
            throw new ImportResourceException("Survey must have question groups");
        }
        if (CollectionUtils.isEmpty(survey.getQuestionGroups().getQuestionGroup())) {
            throw new ImportResourceException("Survey must at least one question group");
        }

        // question group validation
        for (generated.Survey.QuestionGroups.QuestionGroup questionGroup : 
                survey.getQuestionGroups().getQuestionGroup()) {
            if (questionGroup.getQuestions() == null) {
                throw new ImportResourceException("All question groups must contain questions");
            }
            if (CollectionUtils.isEmpty(questionGroup.getQuestions().getQuestion())) {
                throw new ImportResourceException("All question groups must contain at least one question");
            }
            if (StringUtils.isEmpty(questionGroup.getText())) {
                throw new ImportResourceException("All question groups must contain text");
            }

            // question validation
            for (generated.Survey.QuestionGroups.QuestionGroup.Questions.Question question :
                    questionGroup.getQuestions().getQuestion()) {
                if (question.getElementType() == null) {
                    throw new ImportResourceException("All questions must have an element type");
                }
                if (!Util.isInEnum(question.getElementType().toString(), QuestionElementTypes.class)) {
                    throw new ImportResourceException("All questions must have a valid element type");
                }
                if (question.getHtmlType() == null) {
                    throw new ImportResourceException("All questions must have an html type");
                }
                if (!Util.isInEnum(question.getHtmlType().toString(), QuestionHtmlTypes.class)) {
                    throw new ImportResourceException("All questions must have a valid html type");
                }
                if (StringUtils.isEmpty(question.getText())) {
                    throw new ImportResourceException("All questions must contain text");
                }

                // question option validation
                if (question.getQuestionOptions() != null
                        && !CollectionUtils.isEmpty(question.getQuestionOptions().getQuestionOption())) {
                    for (generated.Survey.QuestionGroups.QuestionGroup.Questions.Question.QuestionOptions.QuestionOption
                            questionOption : question.getQuestionOptions().getQuestionOption()) {
                        if (StringUtils.isEmpty(questionOption.getText())) {
                            throw new ImportResourceException("All question options must contain text");
                        }
                    }
                }
            }
        }
    }
}
