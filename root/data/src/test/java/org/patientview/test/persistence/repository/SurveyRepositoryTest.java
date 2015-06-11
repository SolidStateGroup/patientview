package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionGroup;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.QuestionElementTypes;
import org.patientview.persistence.model.enums.QuestionHtmlTypes;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class SurveyRepositoryTest {

    @Inject
    SurveyRepository surveyRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testFindAll() {
        Survey survey = new Survey();
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE);
        survey.setDescription("Crohns Survey");

        QuestionGroup questionGroup = new QuestionGroup();
        questionGroup.setText("Stools");
        questionGroup.setDescription("Stool Questions");
        questionGroup.setSurvey(survey);

        Question question = new Question();
        question.setQuestionGroup(questionGroup);
        question.setElementType(QuestionElementTypes.SINGLE_SELECT);
        question.setHtmlType(QuestionHtmlTypes.SELECT);
        question.setText("Number of Stools (Day)");
        question.setNumber("a");
        question.setDisplayOrder(1);

        QuestionOption questionOption = new QuestionOption();
        questionOption.setQuestion(question);
        questionOption.setText("0-3");

        question.getQuestionOptions().add(questionOption);
        questionGroup.getQuestions().add(question);
        survey.getQuestionGroups().add(questionGroup);

        Survey saved = surveyRepository.save(survey);

        Assert.assertTrue("Survey should be created", saved != null);
        Assert.assertEquals("Question Group should be created", 1, saved.getQuestionGroups().size());
        Assert.assertEquals("Questions should be saved", 1, saved.getQuestionGroups().get(0).getQuestions().size());
        Assert.assertEquals("Question options should be saved", 1,
                saved.getQuestionGroups().get(0).getQuestions().get(0).getQuestionOptions().size());
        Assert.assertEquals("Question option should be correct", questionOption.getText(),
                saved.getQuestionGroups().get(0).getQuestions().get(0).getQuestionOptions().get(0).getText());

        List<Survey> all = convertIterable(surveyRepository.findAll());

        Assert.assertTrue("Survey should be found", !all.isEmpty());
        Assert.assertEquals("1 Survey should be found", 1, all.size());
        Assert.assertEquals("Question Groups should be found", 1, all.get(0).getQuestionGroups().size());
        Assert.assertEquals("Questions should be found", 1, all.get(0).getQuestionGroups().get(0).getQuestions().size());
        Assert.assertEquals("Question options should be found", 1,
                all.get(0).getQuestionGroups().get(0).getQuestions().get(0).getQuestionOptions().size());
        Assert.assertEquals("Question option should be correct", questionOption.getText(),
                all.get(0).getQuestionGroups().get(0).getQuestions().get(0).getQuestionOptions().get(0).getText());
    }

    @Test
    public void testFindByType() {
        Survey survey = new Survey();
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE);
        survey.setDescription("Crohns Survey");

        QuestionGroup questionGroup = new QuestionGroup();
        questionGroup.setText("Stools");
        questionGroup.setDescription("Stool Questions");
        questionGroup.setSurvey(survey);

        Question question = new Question();
        question.setQuestionGroup(questionGroup);
        question.setElementType(QuestionElementTypes.SINGLE_SELECT);
        question.setHtmlType(QuestionHtmlTypes.SELECT);
        question.setText("Number of Stools (Day)");
        question.setNumber("a");
        question.setDisplayOrder(1);

        QuestionOption questionOption = new QuestionOption();
        questionOption.setQuestion(question);
        questionOption.setText("0-3");

        question.getQuestionOptions().add(questionOption);
        questionGroup.getQuestions().add(question);
        survey.getQuestionGroups().add(questionGroup);

        Survey saved = surveyRepository.save(survey);

        Assert.assertTrue("Survey should be created", saved != null);
        Assert.assertEquals("Question Group should be created", 1, saved.getQuestionGroups().size());
        Assert.assertEquals("Questions should be saved", 1, saved.getQuestionGroups().get(0).getQuestions().size());
        Assert.assertEquals("Question options should be saved", 1,
                saved.getQuestionGroups().get(0).getQuestions().get(0).getQuestionOptions().size());
        Assert.assertEquals("Question option should be correct", questionOption.getText(),
                saved.getQuestionGroups().get(0).getQuestions().get(0).getQuestionOptions().get(0).getText());

        List<Survey> all = convertIterable(surveyRepository.findByType(SurveyTypes.CROHNS_SYMPTOM_SCORE));

        Assert.assertTrue("Survey should be found", !all.isEmpty());
        Assert.assertEquals("1 Survey should be found", 1, all.size());
        Assert.assertEquals("Question Groups should be found", 1, all.get(0).getQuestionGroups().size());
        Assert.assertEquals("Questions should be found", 1, all.get(0).getQuestionGroups().get(0).getQuestions().size());
        Assert.assertEquals("Question options should be found", 1,
                all.get(0).getQuestionGroups().get(0).getQuestions().get(0).getQuestionOptions().size());
        Assert.assertEquals("Question option should be correct", questionOption.getText(),
                all.get(0).getQuestionGroups().get(0).getQuestions().get(0).getQuestionOptions().get(0).getText());
    }

    private <T> List<T> convertIterable(Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<T>();
        Iterator<T> lookupIterator = iterable.iterator();

        while (lookupIterator.hasNext()) {
            list.add(lookupIterator.next());
        }
        return list;
    }
}
