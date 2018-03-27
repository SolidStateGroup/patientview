package org.patientview.api.client.nhschoices;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Tests for NhsChoices v2 api client implementation
 * <p>
 * API keys can be found under https://developer.api.nhs.uk
 */
public class NhsChoicesApiClientTest {

    private Logger log = LoggerFactory.getLogger(getClass());
    //private static String apiKey = "{add-your-key-here}";
    private static String apiKey = "1ef7a9bf9c1c41deaeff37a5bdf9e615";

    // @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_Success_When_Letter_Valid() throws IOException {
        String letter = "A";
        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey(apiKey)
                .build();

        List<ConditionLink> conditionLinks = apiClient.getConditions(letter);

        Assert.assertNotNull("Should get condition links in response", conditionLinks);
        Assert.assertTrue("Should have condition links in response", conditionLinks.size() > 0);

        for (ConditionLink link : conditionLinks) {
            Assert.assertNotNull("Should get condition name", link.getName());
            Assert.assertNotNull("Should get condition url", link.getUrl());
            Assert.assertNotNull("Should get condition description", link.getDescription());
        }
    }

    // @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_All_Conditions_Success() throws IOException {

        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey(apiKey)
                .build();

        List<ConditionLink> conditionLinks = apiClient.getAllConditions();

        Assert.assertNotNull("Should get condition links in response", conditionLinks);
        Assert.assertTrue("Should have condition links in response", conditionLinks.size() > 0);

        for (ConditionLink link : conditionLinks) {
            Assert.assertNotNull("Should get condition name", link.getName());
            Assert.assertNotNull("Should get condition url", link.getUrl());
            Assert.assertNotNull("Should get condition description", link.getDescription());
        }
    }

    // @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_Success_When_Letter_Invalid_No_Conditions() throws IOException {

        // send invalid letter
        String invalidLetter = "6";
        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey(apiKey)
                .build();

        List<ConditionLink> conditionLinks = apiClient.getConditions(invalidLetter);


        /**
         * Will still should get response from nhs api, but without any conditions
         */
        Assert.assertNull("Should not get any conditions in response", conditionLinks);
    }

    // @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_Null_When_Api_Key_Invalid() throws IOException {

        // send invalid letter
        String letter = "A";
        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey("invalid_key")
                .build();

        List<ConditionLink> conditionLinks = apiClient.getConditions(letter);
        Assert.assertNull("Should get feed in response", conditionLinks);
    }

    @Test
    public void testAtoZ() {
        int count = 0;
        for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
            System.out.println(alphabet);
            count++;
        }
        Assert.assertTrue("Should have 26 letters", count == 26);
    }
}
