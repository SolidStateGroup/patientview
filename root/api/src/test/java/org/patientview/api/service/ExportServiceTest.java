package org.patientview.api.service;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.ExportServiceImpl;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.QuestionElementTypes;
import org.patientview.persistence.model.enums.QuestionOptionTypes;
import org.patientview.persistence.model.enums.QuestionTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.model.enums.SurveyResponseScoreTypes;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.persistence.repository.QuestionRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.http.HttpEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/12/2015
 */
public class ExportServiceTest {

    @Mock
    QuestionRepository questionRepository;

    @Mock
    SurveyRepository surveyRepository;

    @Mock
    SurveyResponseRepository surveyResponseRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ExportService exportService = new ExportServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testDownloadSurveyResponses() throws Exception {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        user.setIdentifiers(new HashSet<Identifier>());

        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        Survey survey = new Survey();
        survey.setType(SurveyTypes.IBD_CONTROL);

        SurveyResponse surveyResponse
            = new SurveyResponse(user, 1, ScoreSeverity.LOW, new Date(), SurveyResponseScoreTypes.IBD_CONTROL_EIGHT);
        surveyResponse.setSurvey(survey);
        List<SurveyResponse> surveyResponses = new ArrayList<>();
        surveyResponses.add(surveyResponse);

        // questions
        {
            Question question = new Question();
            question.setType(QuestionTypes.IBD_CONTROLLED_TWO_WEEKS);
            question.setElementType(QuestionElementTypes.SINGLE_SELECT);
            question.setText("two weeks");

            QuestionOption questionOption = new QuestionOption();
            questionOption.setType(QuestionOptionTypes.YES);
            questionOption.setQuestion(question);
            questionOption.setText("yes");

            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setQuestion(question);
            questionAnswer.setQuestionOption(questionOption);
            surveyResponse.getQuestionAnswers().add(questionAnswer);

            List<Question> questions = new ArrayList<>();
            questions.add(question);

            when(questionRepository.findByType(QuestionTypes.IBD_CONTROLLED_TWO_WEEKS)).thenReturn(questions);
        }
        {
            Question question = new Question();
            question.setType(QuestionTypes.IBD_CONTROLLED_CURRENT_TREATMENT);
            question.setElementType(QuestionElementTypes.SINGLE_SELECT);
            question.setText("current treatment");

            QuestionOption questionOption = new QuestionOption();
            questionOption.setType(QuestionOptionTypes.NOT_SURE);
            questionOption.setQuestion(question);
            questionOption.setText("not sure");

            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setQuestion(question);
            questionAnswer.setQuestionOption(questionOption);
            surveyResponse.getQuestionAnswers().add(questionAnswer);

            List<Question> questions = new ArrayList<>();
            questions.add(question);

            when(questionRepository.findByType(QuestionTypes.IBD_CONTROLLED_CURRENT_TREATMENT)).thenReturn(questions);
        }
        {
            Question question = new Question();
            question.setType(QuestionTypes.IBD_NO_TREATMENT);
            question.setElementType(QuestionElementTypes.SINGLE_SELECT);
            question.setText("no treatment");

            QuestionOption questionOption = new QuestionOption();
            questionOption.setType(QuestionOptionTypes.NO);
            questionOption.setQuestion(question);
            questionOption.setText("no");

            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setQuestion(question);
            questionAnswer.setQuestionOption(questionOption);
            surveyResponse.getQuestionAnswers().add(questionAnswer);

            List<Question> questions = new ArrayList<>();
            questions.add(question);

            when(questionRepository.findByType(QuestionTypes.IBD_NO_TREATMENT)).thenReturn(questions);
        }
        {
            Question question = new Question();
            question.setType(QuestionTypes.IBD_OVERALL_CONTROL);
            question.setElementType(QuestionElementTypes.SINGLE_SELECT_RANGE);
            question.setText("ranged question");

            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setQuestion(question);
            questionAnswer.setValue("49");
            surveyResponse.getQuestionAnswers().add(questionAnswer);

            List<Question> questions = new ArrayList<>();
            questions.add(question);

            when(questionRepository.findByType(QuestionTypes.IBD_OVERALL_CONTROL)).thenReturn(questions);
        }

        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);
        when(surveyResponseRepository.findByUserAndSurveyType(eq(user), eq(survey.getType())))
                .thenReturn(surveyResponses);

        HttpEntity out = exportService.downloadSurveyResponses(user.getId(), SurveyTypes.IBD_CONTROL.toString());

        Assert.assertNotNull("Should output byte array", out);
        verify(questionRepository, Mockito.times(4)).findByType(any(QuestionTypes.class));
    }
}
