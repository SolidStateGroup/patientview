package org.patientview.test.builder;

import generated.Survey;
import generated.SurveyResponse;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.SurveyBuilder;
import org.patientview.builder.SurveyResponseBuilder;
import org.patientview.persistence.model.User;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

public class SurveyResponseBuilderTest {

    @Test
    public void testBuildNew() throws Exception {
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource(
                "data/xml/survey_response/survey_response_1.xml");
        File file = new File(xmlPath.toURI());
        Assert.assertTrue("Test file not loaded", file.exists());

        JAXBContext jc = JAXBContext.newInstance(SurveyResponse.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        SurveyResponse surveyResponse = (SurveyResponse) unmarshaller.unmarshal(file);

        URL xmlPath2 = Thread.currentThread().getContextClassLoader().getResource("data/xml/survey/survey_1.xml");
        File file2 = new File(xmlPath2.toURI());
        Assert.assertTrue("Test file not loaded", file2.exists());

        JAXBContext jc2 = JAXBContext.newInstance(Survey.class);
        Unmarshaller unmarshaller2 = jc2.createUnmarshaller();
        Survey survey = (Survey) unmarshaller2.unmarshal(file2);

        org.patientview.persistence.model.Survey actualSurvey = new SurveyBuilder(survey).build();

        org.patientview.persistence.model.SurveyResponse built
                = new SurveyResponseBuilder(surveyResponse, actualSurvey, new User()).build();

        assertNotNull("Should create SurveyResponse", built);
    }
}
