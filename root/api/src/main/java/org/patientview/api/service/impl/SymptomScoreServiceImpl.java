package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.SymptomScoreService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SymptomScore;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.repository.QuestionOptionRepository;
import org.patientview.persistence.repository.QuestionRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.SymptomScoreRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2015
 */
@Service
public class SymptomScoreServiceImpl extends AbstractServiceImpl<SymptomScoreServiceImpl>
        implements SymptomScoreService {

    @Inject
    private QuestionRepository questionRepository;

    @Inject
    private QuestionOptionRepository questionOptionRepository;

    @Inject
    private SurveyRepository surveyRepository;

    @Inject
    private SymptomScoreRepository symptomScoreRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public void add(Long userId, SymptomScore symptomScore) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Survey survey = surveyRepository.findOne(symptomScore.getSurvey().getId());
        if (survey == null) {
            throw new ResourceNotFoundException("Could not find survey");
        }

        if (symptomScore.getDate() == null) {
            throw new ResourceNotFoundException("Must include symptom score date");
        }

        SymptomScore newSymptomScore = new SymptomScore();
        newSymptomScore.setSurvey(survey);
        newSymptomScore.setUser(user);
        newSymptomScore.setDate(symptomScore.getDate());

        for (QuestionAnswer questionAnswer : symptomScore.getQuestionAnswers()) {
            QuestionAnswer newQuestionAnswer = new QuestionAnswer();
            newQuestionAnswer.setSymptomScore(newSymptomScore);
            boolean answer = false;

            if (questionAnswer.getQuestionOption() != null) {
                // if QuestionTypes.SINGLE_SELECT, will have question option
                QuestionOption questionOption
                        = questionOptionRepository.findOne(questionAnswer.getQuestionOption().getId());

                if (questionOption == null) {
                    throw new ResourceNotFoundException("Question option not found");
                }

                newQuestionAnswer.setQuestionOption(questionAnswer.getQuestionOption());
                answer = true;
            } else if (StringUtils.isNotEmpty(questionAnswer.getValue())) {
                // if QuestionTypes.SINGLE_SELECT_RANGE, will have value
                newQuestionAnswer.setValue(questionAnswer.getValue());
                answer = true;
            }

            if (answer) {
                Question question = questionRepository.findOne(questionAnswer.getQuestion().getId());
                if (question == null) {
                    throw new ResourceNotFoundException("Invalid question");
                }

                newQuestionAnswer.setQuestion(question);
                newSymptomScore.getQuestionAnswers().add(newQuestionAnswer);
            }
        }

        if (newSymptomScore.getQuestionAnswers().isEmpty()) {
            throw new ResourceNotFoundException("No valid answers");
        }

        newSymptomScore.setScore(calculateScore(newSymptomScore));
        newSymptomScore.setSeverity(calculateSeverity(newSymptomScore));

        symptomScoreRepository.save(newSymptomScore);
    }

    private Double calculateScore(SymptomScore newSymptomScore) {
        return Math.random();
    }

    private ScoreSeverity calculateSeverity(SymptomScore newSymptomScore) {
        return ScoreSeverity.HIGH;
    }

    @Override
    public List<SymptomScore> getByUserId(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        return symptomScoreRepository.findByUser(user);
    }

    @Override
    public SymptomScore getSymptomScore(Long userId, Long symptomScoreId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        return symptomScoreRepository.findOne(symptomScoreId);
    }
}
