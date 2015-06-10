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
import org.patientview.persistence.model.enums.QuestionElementTypes;
import org.patientview.persistence.model.enums.QuestionTypes;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.persistence.repository.QuestionOptionRepository;
import org.patientview.persistence.repository.QuestionRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.SymptomScoreRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Integer calculateScore(SymptomScore symptomScore) {
        Map<QuestionTypes, Integer> questionTypeScoreMap = new HashMap<>();
        for (QuestionAnswer questionAnswer : symptomScore.getQuestionAnswers()) {
            if (questionAnswer.getQuestionOption() != null
                    && questionAnswer.getQuestionOption().getScore() != null
                    && questionAnswer.getQuestion() != null
                    && questionAnswer.getQuestion().getType() != null) {
                questionTypeScoreMap.put(
                    questionAnswer.getQuestion().getType(), questionAnswer.getQuestionOption().getScore());
            }

            // add scoring for ranged values
            if (questionAnswer.getQuestion() != null
                    && questionAnswer.getQuestion().getType() != null
                    && questionAnswer.getQuestion().getElementType().equals(QuestionElementTypes.SINGLE_SELECT_RANGE)
                    && questionAnswer.getValue() != null) {
                try {
                    questionTypeScoreMap.put(
                            questionAnswer.getQuestion().getType(), Integer.valueOf(questionAnswer.getValue()));
                } catch (NumberFormatException e) {
                    questionTypeScoreMap.put(
                            questionAnswer.getQuestion().getType(), 0);
                }
            }
        }

        if (symptomScore.getSurvey().getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE)) {
            Integer score = 0;

            if (questionTypeScoreMap.get(QuestionTypes.OPEN_BOWELS) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.OPEN_BOWELS);
            }

            if (questionTypeScoreMap.get(QuestionTypes.ABDOMINAL_PAIN) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.ABDOMINAL_PAIN);
            }

            if (questionTypeScoreMap.get(QuestionTypes.MASS_IN_TUMMY) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.MASS_IN_TUMMY);
            }

            if (questionTypeScoreMap.get(QuestionTypes.COMPLICATION) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.COMPLICATION);
            }

            if (questionTypeScoreMap.get(QuestionTypes.FEELING) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.FEELING);
            }

            return score;
        }

        return 0;
    }

    // note: these are hardcoded
    private ScoreSeverity calculateSeverity(SymptomScore symptomScore) {
        if (symptomScore.getSurvey().getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE)) {
            if (symptomScore.getScore() != null) {
                if (symptomScore.getScore() >= 16) {
                    return ScoreSeverity.HIGH;
                }
                if (symptomScore.getScore() >= 4) {
                    return ScoreSeverity.MEDIUM;
                }
                if (symptomScore.getScore() < 4) {
                    return ScoreSeverity.LOW;
                }
            }
        }

        return ScoreSeverity.UNKNOWN;
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
    public List<SymptomScore> getByUserIdAndSurveyType(Long userId, SurveyTypes surveyType)
            throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }
        if (surveyType == null) {
            throw new ResourceNotFoundException("Must set survey type");
        }

        return symptomScoreRepository.findByUserAndSurveyType(user, surveyType);
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
