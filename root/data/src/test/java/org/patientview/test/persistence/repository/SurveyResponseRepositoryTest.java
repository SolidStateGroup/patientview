package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2015
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class SurveyResponseRepositoryTest {

    @Inject
    SurveyRepository surveyRepository;

    @Inject
    SurveyResponseRepository surveyResponseRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testFindByUserAndType() {
        User user = dataTestUtils.createUser("TestUser");

        Survey survey = new Survey();
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE);
        surveyRepository.save(survey);

        SurveyResponse surveyResponse = new SurveyResponse(user, 1, ScoreSeverity.LOW, new Date());
        surveyResponse.setSurvey(survey);
        surveyResponseRepository.save(surveyResponse);

        List<SurveyResponse> surveyResponses = surveyResponseRepository.findByUserAndSurveyType(user, survey.getType());
        Assert.assertEquals("There should be 1 symptom score", 1, surveyResponses.size());
        Assert.assertTrue("The symptom score should be the one created", surveyResponses.get(0).equals(surveyResponse));
    }
}
