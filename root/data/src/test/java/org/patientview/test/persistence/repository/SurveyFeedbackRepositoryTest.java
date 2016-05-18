package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyFeedback;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.persistence.repository.SurveyFeedbackRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/05/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class SurveyFeedbackRepositoryTest {

    @Inject
    SurveyRepository surveyRepository;

    @Inject
    SurveyFeedbackRepository surveyFeedbackRepository;

    @Inject
    UserRepository userRepository;

    @Test
    public void testFindBySurveyAndType() {
        // survey
        Survey survey = new Survey();
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE.toString());
        survey.setDescription("Crohns Survey");
        Survey savedSurvey = surveyRepository.save(survey);
        Assert.assertTrue("Survey should be created", savedSurvey != null);

        // user
        User user = new User();
        User savedUser = userRepository.save(user);
        Assert.assertTrue("User should be created", savedUser != null);

        // survey feedback
        SurveyFeedback surveyFeedback = new SurveyFeedback();
        surveyFeedback.setSurvey(savedSurvey);
        surveyFeedback.setUser(savedUser);
        surveyFeedback.setFeedback("feedback");
        surveyFeedback.setCreated(new Date());
        surveyFeedback.setCreator(savedUser);
        surveyFeedbackRepository.save(surveyFeedback);

        List<SurveyFeedback> surveyFeedbacks = surveyFeedbackRepository.findBySurveyAndUser(savedSurvey, savedUser);

        Assert.assertTrue("SurveyFeedback should be found", !surveyFeedbacks.isEmpty());
        Assert.assertEquals("1 SurveyFeedback should be found", 1, surveyFeedbacks.size());
        Assert.assertEquals("SurveyFeedback should have correct feedback",
                surveyFeedback.getFeedback(), surveyFeedbacks.get(0).getFeedback());
    }
}
