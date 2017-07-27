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
    public void testPushNotifications() {

        String key = "{add-your-key-here}";
        FirebaseClient client = FirebaseClient.newBuilder()
                .setKey(key)
                .setLive(false)
                .build();

        String response = client.push(13832855L);
        Assert.assertNotNull("Should get response", response);
    }
}

