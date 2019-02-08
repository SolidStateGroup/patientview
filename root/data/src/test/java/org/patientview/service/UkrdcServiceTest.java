package org.patientview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.patientview.service.impl.UkrdcServiceImpl;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

public class UkrdcServiceTest {

    private static final String POS_S_JSON = "/data/json/survey_response/POS_S.json";
    private static final String USER_JSON = "/data/json/survey_response/User.json";

    private static ObjectMapper MAPPER = new ObjectMapper();
    private UkrdcServiceImpl ukrdcService = new UkrdcServiceImpl();

    private SurveyResponse surveyResponse;

    @Before
    public void setUp() throws Exception {

        surveyResponse = MAPPER.readValue(
                getClass().getResource(POS_S_JSON),
                SurveyResponse.class);

        User user = MAPPER.readValue(getClass().getResource(USER_JSON), User.class);

        surveyResponse.setUser(user);
    }

    @Test
    @Ignore
    public void should_Map_Survey_Response_to_UKRDC_schema()
            throws DatatypeConfigurationException, JAXBException {

        // Given


        // When

        String xml = ukrdcService.buildSurveyXml(surveyResponse);


        System.out.println(xml);


        // Then
    }
}