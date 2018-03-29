package org.patientview.api.client.nhschoices;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Tests for NhsChoices v2 api client implementation
 * <p>
 * API keys can be found under https://developer.api.nhs.uk
 */
public class NhsChoicesApiClientTest {

    private Logger log = LoggerFactory.getLogger(getClass());
    private static String apiKey = "{add-your-key-here}";

    @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_Success_When_Letter_Valid() throws IOException {
        String letter = "A";
        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey(apiKey)
                .build();

        List<ConditionLinkJson> conditionLinks = apiClient.getConditions(letter);

        Assert.assertNotNull("Should get condition links in response", conditionLinks);
        Assert.assertTrue("Should have condition links in response", conditionLinks.size() > 0);

        for (ConditionLinkJson link : conditionLinks) {
            Assert.assertNotNull("Should get condition name", link.getName());
            Assert.assertNotNull("Should get condition url", link.getApiUrl());
            Assert.assertNotNull("Should get condition description", link.getDescription());
        }
    }

    @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_All_Conditions_Success() throws IOException {

        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey(apiKey)
                .build();

        List<ConditionLinkJson> conditionLinks = apiClient.getAllConditions();

        Assert.assertNotNull("Should get condition links in response", conditionLinks);
        Assert.assertTrue("Should have condition links in response", conditionLinks.size() > 0);

        for (ConditionLinkJson link : conditionLinks) {
            Assert.assertNotNull("Should get condition name", link.getName());
            Assert.assertNotNull("Should get condition url", link.getApiUrl());
            Assert.assertNotNull("Should get condition description", link.getDescription());
        }
    }

    @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_Success_When_Letter_Invalid_No_Conditions() throws IOException {

        // send invalid letter
        String invalidLetter = "6";
        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey(apiKey)
                .build();

        List<ConditionLinkJson> conditionLinks = apiClient.getConditions(invalidLetter);


        /**
         * Will still should get response from nhs api, but without any conditions
         */
        Assert.assertNull("Should not get any conditions in response", conditionLinks);
    }

    @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_Null_When_Api_Key_Invalid() throws IOException {

        // send invalid letter
        String letter = "A";
        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey("invalid_key")
                .build();

        List<ConditionLinkJson> conditionLinks = apiClient.getConditions(letter);
        Assert.assertNull("Should get feed in response", conditionLinks);
    }

    @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_GP_Details_When_Practice_Code_Valid() throws IOException {

        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey(apiKey)
                .build();

        String gpPracticeCode = "E85074";

        Map<String, String> gpDetails = apiClient.getGPDetailsByPracticeCode(gpPracticeCode);
        Assert.assertNotNull("Should get GP details in response", gpDetails);
    }

    @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_Null_When_Practice_Code_Invalid() throws IOException {

        // send invalid letter
        String letter = "A";
        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey(apiKey)
                .build();
        String invalidPracticeCode = "123";

        Map<String, String> gpDetails = apiClient.getGPDetailsByPracticeCode(invalidPracticeCode);
        Assert.assertNull("Should Not get any GP details in response", gpDetails);
    }

    @Test
    public void testAtoZ() {
        int count = 0;
        for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
            count++;
        }
        Assert.assertTrue("Should have 26 letters", count == 26);
    }
}
