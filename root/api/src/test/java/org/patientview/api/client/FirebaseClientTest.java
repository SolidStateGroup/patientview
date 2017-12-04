package org.patientview.api.client;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class FirebaseClientTest {

    @Ignore("Need to add firebase key to run the test")
    @Test
    public void testPushNotificationsResults() {

        String key = "{add-your-key-here}";
        FirebaseClient client = FirebaseClient.newBuilder()
                .setKey(key)
                .setLive(false)
                .build();

        String response = client.notifyResult(13832855L);
        Assert.assertNotNull("Should get response", response);
    }

    @Ignore("Need to add firebase key to run the test")
    @Test
    public void testPushNotificationsMessage() {

        String key = "{add-your-key-here}";
        FirebaseClient client = FirebaseClient.newBuilder()
                .setKey(key)
                .setLive(false)
                .build();

        String response = client.notifyMessage(19333L, 123L, "Test Title");
        Assert.assertNotNull("Should get response", response);
    }
}

