package org.patientview.test.builder;

import generated.Survey;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.SurveyBuilder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SurveyBuilderTest {

    @Test
    public void testBuildNew() throws Exception {
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("data/xml/survey/survey_1.xml");
        File file = new File(xmlPath.toURI());
        Assert.assertTrue("Test file not loaded", file.exists());

        JAXBContext jc = JAXBContext.newInstance(Survey.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Survey survey = (Survey) unmarshaller.unmarshal(file);

        org.patientview.persistence.model.Survey built = new SurveyBuilder(survey).build();

        assertNotNull("Should create Survey", built);
        assertEquals("Should have correct description", survey.getDescription(), built.getDescription());
        assertEquals("Should have correct number of question groups",
                survey.getQuestionGroups().getQuestionGroup().size(), built.getQuestionGroups().size());
        assertEquals("Question group should have correct number of questions",
                survey.getQuestionGroups().getQuestionGroup().get(0).getQuestions().getQuestion().size(),
                built.getQuestionGroups().get(0).getQuestions().size());
    }
}
