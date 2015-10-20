package org.patientview.api.service;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.patientview.api.config.TestCommonConfig;
import org.patientview.api.service.impl.LookingLocalServiceImpl;
import org.patientview.test.util.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 20/10/2015
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestCommonConfig.class, LookingLocalServiceImpl.class})
public class LookingLocalServiceTest {

    private static final String token = "1234567890";

    @Autowired
    private LookingLocalServiceImpl lookingLocalService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetHomeXml() throws IOException, TransformerException, ParserConfigurationException {
        String out = lookingLocalService.getHomeXml();

        Assert.assertNotNull("Should return Looking Local home XML", out);
    }

    @Test
    public void testGetAuthXml() throws IOException, TransformerException, ParserConfigurationException {
        String out = lookingLocalService.getLoginSuccessfulXml(token);

        Assert.assertNotNull("Should return Looking Local home XML", out);
        Assert.assertTrue("Should have login successful message", out.contains("Login Successful"));
        Assert.assertTrue("Should contain links with token", out.contains("?token=" + token));
    }

    @Test
    public void testGetAuthErrorXml() throws IOException, TransformerException, ParserConfigurationException {
        String out = lookingLocalService.getAuthErrorXml();

        Assert.assertNotNull("Should return Looking Local auth error XML", out);
        Assert.assertTrue("Should have correct error message",
                out.contains("username/password combination was not recognised"));
    }

    @Test
    public void testGetErrorXml() throws IOException, TransformerException, ParserConfigurationException {
        String errorMessage = "username not found";
        String out = lookingLocalService.getErrorXml(errorMessage);

        Assert.assertNotNull("Should return Looking Local error XML", out);
        Assert.assertTrue("Should have correct error message", out.contains(errorMessage));
    }

    @Test
    public void testGetMainXml() throws IOException, TransformerException, ParserConfigurationException {
        String out = lookingLocalService.getMainXml(token);

        Assert.assertNotNull("Should return Looking Local main XML", out);
        Assert.assertTrue("Should contain links with token", out.contains("?token=" + token));
    }
}

