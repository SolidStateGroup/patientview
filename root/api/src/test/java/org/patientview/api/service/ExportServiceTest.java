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
import java.util.Optional;
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
        survey.setType(SurveyTypes.COLITIS_SYMPTOM_SCORE.toString());

        SurveyResponse surveyResponse = new SurveyResponse(
                user, 1.0, ScoreSeverity.LOW, new Date(), SurveyResponseScoreTypes.SYMPTOM_SCORE.toString());
        surveyResponse.setSurvey(survey);
        List<SurveyResponse> surveyResponses = new ArrayList<>();
        surveyResponses.add(surveyResponse);

        // questions
        {
            Question question = new Question();
            question.setType(QuestionTypes.NUMBER_OF_STOOLS_DAYTIME.toString());
            question.setElementType(QuestionElementTypes.SINGLE_SELECT);
            question.setText("stools daytime");

            QuestionOption questionOption = new QuestionOption();
            questionOption.setType(QuestionOptionTypes.ZERO_TO_THREE.toString());
            questionOption.setQuestion(question);
            questionOption.setText("0-3");

            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setQuestion(question);
            questionAnswer.setQuestionOption(questionOption);
            surveyResponse.getQuestionAnswers().add(questionAnswer);

            List<Question> questions = new ArrayList<>();
            questions.add(question);

            when(questionRepository.findByType(QuestionTypes.NUMBER_OF_STOOLS_DAYTIME.toString()))
                    .thenReturn(questions);
        }
        {
            Question question = new Question();
            question.setType(QuestionTypes.NUMBER_OF_STOOLS_NIGHTTIME.toString());
            question.setElementType(QuestionElementTypes.SINGLE_SELECT);
            question.setText("stools nighttime");

            QuestionOption questionOption = new QuestionOption();
            questionOption.setType(QuestionOptionTypes.ZERO_TO_THREE.toString());
            questionOption.setQuestion(question);
            questionOption.setText("0-3");

            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setQuestion(question);
            questionAnswer.setQuestionOption(questionOption);
            surveyResponse.getQuestionAnswers().add(questionAnswer);

            List<Question> questions = new ArrayList<>();
            questions.add(question);

            when(questionRepository.findByType(QuestionTypes.NUMBER_OF_STOOLS_NIGHTTIME.toString()))
                    .thenReturn(questions);
        }
        {
            Question question = new Question();
            question.setType(QuestionTypes.TOILET_TIMING.toString());
            question.setElementType(QuestionElementTypes.SINGLE_SELECT);
            question.setText("toilet timing");

            QuestionOption questionOption = new QuestionOption();
            questionOption.setType(QuestionOptionTypes.DONT_NEED_TO_RUSH.toString());
            questionOption.setQuestion(question);
            questionOption.setText("no rush");

            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setQuestion(question);
            questionAnswer.setQuestionOption(questionOption);
            surveyResponse.getQuestionAnswers().add(questionAnswer);

            List<Question> questions = new ArrayList<>();
            questions.add(question);

            when(questionRepository.findByType(QuestionTypes.TOILET_TIMING.toString())).thenReturn(questions);
        }
        {
            Question question = new Question();
            question.setType(QuestionTypes.PRESENT_BLOOD.toString());
            question.setElementType(QuestionElementTypes.SINGLE_SELECT);
            question.setText("toilet timing");

            QuestionOption questionOption = new QuestionOption();
            questionOption.setType(QuestionOptionTypes.NONE.toString());
            questionOption.setQuestion(question);
            questionOption.setText("none");

            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setQuestion(question);
            questionAnswer.setQuestionOption(questionOption);
            surveyResponse.getQuestionAnswers().add(questionAnswer);

            List<Question> questions = new ArrayList<>();
            questions.add(question);

            when(questionRepository.findByType(QuestionTypes.PRESENT_BLOOD.toString())).thenReturn(questions);
        }
        {
            Question question = new Question();
            question.setType(QuestionTypes.COMPLICATION.toString());
            question.setElementType(QuestionElementTypes.SINGLE_SELECT);
            question.setText("complication");

            QuestionOption questionOption = new QuestionOption();
            questionOption.setType(QuestionOptionTypes.NONE.toString());
            questionOption.setQuestion(question);
            questionOption.setText("none");

            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setQuestion(question);
            questionAnswer.setQuestionOption(questionOption);
            surveyResponse.getQuestionAnswers().add(questionAnswer);

            List<Question> questions = new ArrayList<>();
            questions.add(question);

            when(questionRepository.findByType(QuestionTypes.COMPLICATION.toString())).thenReturn(questions);
        }
        {
            Question question = new Question();
            question.setType(QuestionTypes.FEELING.toString());
            question.setElementType(QuestionElementTypes.SINGLE_SELECT);
            question.setText("feeling");

            QuestionOption questionOption = new QuestionOption();
            questionOption.setType(QuestionOptionTypes.WELL.toString());
            questionOption.setQuestion(question);
            questionOption.setText("well");

            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setQuestion(question);
            questionAnswer.setQuestionOption(questionOption);
            surveyResponse.getQuestionAnswers().add(questionAnswer);

            List<Question> questions = new ArrayList<>();
            questions.add(question);

            when(questionRepository.findByType(QuestionTypes.FEELING.toString())).thenReturn(questions);
        }

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        when(surveyResponseRepository.findByUserAndSurveyType(eq(user), eq(survey.getType())))
                .thenReturn(surveyResponses);

        HttpEntity out
                = exportService.downloadSurveyResponses(user.getId(), SurveyTypes.COLITIS_SYMPTOM_SCORE.toString());

        Assert.assertNotNull("Should output byte array", out);
        verify(questionRepository, Mockito.times(6)).findByType(any(String.class));
    }
}
