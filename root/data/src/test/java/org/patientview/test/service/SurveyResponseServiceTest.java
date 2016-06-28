package org.patientview.test.service;

import generated.SurveyResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionGroup;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.service.SurveyResponseService;
import org.patientview.service.SurveyService;
import org.patientview.service.impl.SurveyResponseServiceImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class SurveyResponseServiceTest extends BaseTest {

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    SurveyService surveyService;

    @InjectMocks
    SurveyResponseService surveyResponseService = new SurveyResponseServiceImpl();

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testValidateSurveyResponse() throws Exception {
        Unmarshaller unmarshaller = JAXBContext.newInstance(SurveyResponse.class).createUnmarshaller();
        SurveyResponse surveyResponse = (SurveyResponse) unmarshaller.unmarshal(new File(
                Thread.currentThread().getContextClassLoader()
                        .getResource("data/xml/survey_response/survey_response_1.xml").toURI()));
        Assert.assertNotNull("Should have SurveyResponse object", surveyResponse);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(new Identifier());

        // set up currently stored survey
        org.patientview.persistence.model.Survey survey = new org.patientview.persistence.model.Survey();
        QuestionGroup questionGroup = new QuestionGroup();
        for (SurveyResponse.QuestionAnswers.QuestionAnswer questionAnswer
                : surveyResponse.getQuestionAnswers().getQuestionAnswer()) {
            Question question = new Question(questionAnswer.getQuestionType());
            if (StringUtils.isNotEmpty(questionAnswer.getQuestionOption())) {
                QuestionOption questionOption = new QuestionOption();
                questionOption.setType(questionAnswer.getQuestionOption());
                question.getQuestionOptions().add(questionOption);
            }
            questionGroup.getQuestions().add(question);
        }
        survey.getQuestionGroups().add(questionGroup);

        when(identifierRepository.findByValue(eq(surveyResponse.getIdentifier()))).thenReturn(identifiers);
        when(surveyService.getByType(eq(surveyResponse.getSurveyType()))).thenReturn(survey);
        surveyResponseService.validate(surveyResponse);
    }
}
