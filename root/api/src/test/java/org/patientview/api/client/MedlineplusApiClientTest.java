package org.patientview.api.client;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Tests for MedlinePlus api client implementation
 */
public class MedlineplusApiClientTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void testApiClient_Return_Success_When_Code_Valid() throws IOException {

        // Problem code for acne in ICD-10-CM format
        String code = "L70";
        MedlineplusApiClient apiClient = MedlineplusApiClient.newBuilder().build();

        MedlineplusResponseJson json = apiClient.getLink(code);

        log.info("DATA: " + json.toString());

        Assert.assertNotNull("Should get feed in response", json.getFeed());
        Assert.assertNotNull("Should get language in feed", json.getFeed().getLang());
        Assert.assertNotNull("Should get title in feed", json.getFeed().getTitle());
        Assert.assertNotNull("Should get title in feed", json.getFeed().getTitle().getValue());
        Assert.assertNotNull("Should get subtitle in feed", json.getFeed().getSubtitle().getValue());
        Assert.assertNotNull("Should get entry in feed", json.getFeed().getEntry());
        Assert.assertTrue("Should get entry in feed", json.getFeed().getEntry().length > 0);

        // check entry in Feed json
        EntryJson entryJson = json.getFeed().getEntry()[0];
        Assert.assertNotNull("Should get title in entry", entryJson.getTitle());
        Assert.assertNotNull("Should get title in entry", entryJson.getTitle().getValue());

        // check for Links, the actual data we need
        LinkJson linkJson = entryJson.getLink()[0];
        Assert.assertNotNull("Should get link in entry", linkJson);
        Assert.assertNotNull("Should get rel in link", linkJson.getRel());
        Assert.assertNotNull("Should get href in link", linkJson.getHref());
    }

    @Test
    public void testApiClient_Return_Success_When_Code_Invalid_No_Link() throws IOException {

        // send invalid code
        String code = "invalid";
        MedlineplusApiClient apiClient = MedlineplusApiClient.newBuilder().build();

        MedlineplusResponseJson json = apiClient.getLink(code);

        log.info("DATA: " + json.toString());

        /**
         * Will still should get response however should not have any entry and link data
         */
        Assert.assertNotNull("Should get feed in response", json.getFeed());
        Assert.assertNotNull("Should get entry in feed", json.getFeed().getEntry());
        Assert.assertFalse("Should not return any entries", json.getFeed().getEntry().length > 0);
    }
}
