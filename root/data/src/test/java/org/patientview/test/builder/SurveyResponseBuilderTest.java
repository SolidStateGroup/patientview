package org.patientview.test.builder;

import generated.SurveyResponse;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.SurveyResponseBuilder;

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

        org.patientview.persistence.model.SurveyResponse built = new SurveyResponseBuilder(surveyResponse).build();

        assertNotNull("Should create SurveyResponse", built);
    }
}
