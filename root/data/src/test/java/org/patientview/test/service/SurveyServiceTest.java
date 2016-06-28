package org.patientview.test.service;

import generated.Survey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.service.SurveyService;
import org.patientview.service.impl.SurveyServiceImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class SurveyServiceTest extends BaseTest {

    @Mock
    SurveyRepository surveyRepository;

    @InjectMocks
    SurveyService surveyService = new SurveyServiceImpl();

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testAdd() throws Exception {
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("data/xml/survey/survey_1.xml");
        File file = new File(xmlPath.toURI());
        Assert.assertTrue("Test file not loaded", file.exists());

        JAXBContext jc = JAXBContext.newInstance(Survey.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Survey survey = (Survey) unmarshaller.unmarshal(file);

        when(surveyRepository.save(any(org.patientview.persistence.model.Survey.class)))
                .thenReturn(new org.patientview.persistence.model.Survey());

        org.patientview.persistence.model.Survey saved = surveyService.add(survey);

        assertNotNull("Should create Survey", saved);
    }

    @Test
    public void testValidateSurvey() throws Exception {
        Unmarshaller unmarshaller = JAXBContext.newInstance(Survey.class).createUnmarshaller();
        Survey survey = (Survey) unmarshaller.unmarshal(new File(
                Thread.currentThread().getContextClassLoader().getResource("data/xml/survey/survey_1.xml").toURI()));
        Assert.assertNotNull("Should have Survey object", survey);

        surveyService.validate(survey);
    }
}
