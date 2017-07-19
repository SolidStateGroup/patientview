package org.patientview.api.client;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

/**
 * Firebase API cient implementation for sending out notification
 */
public class FirebaseClient {
    private static Logger LOG = LoggerFactory.getLogger(FirebaseClient.class);
    private static final String API_URL = "https://fcm.googleapis.com/fcm/send";
    private static String SERVER_KEY;
    private static boolean IS_LIVE = false;

    private FirebaseClient() {
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Messages sent to targets.
     * This class interfaces with the FCM server by sending the Notification over HTTP-POST JSON.
     *
     * @param userId an id of the user to send notification for
     * @return FcmResponse object containing HTTP response info.
     */
    public String push(Long userId) {
        if (SERVER_KEY == null) {
            LOG.info("No Server-Key has been defined for firebase client.");
            return null;
        }

        try {

            /**
             * We are using the same firebase app for Test and Live,
             * different topics per env
             * LIVE: /topics/{userid}-results
             * TEST:  /topics/{userid}-test-results
             */
            String topic = "/topics/" + userId + "-test-results";
            if (IS_LIVE) {
                topic = "/topics/" + userId + "-results";
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", "key=" + SERVER_KEY);
            httpHeaders.set("Content-Type", "application/json;charset=UTF-8");


            JSONObject body = new JSONObject();
            body.put("to", topic);
            body.put("priority", "high");
            //body.put("dry_run", true); // to test without sending the message

            JSONObject notification = new JSONObject();
            notification.put("body", "New results has arrived");
            notification.put("title", "New data has arrived on PatientView. Please log in to view");

            body.put("notification", notification);

            HttpEntity<String> httpEntity = new HttpEntity<String>(body.toString(), httpHeaders);
            String response = restTemplate.postForObject(API_URL, httpEntity, String.class);

            LOG.debug("Firebase client response " + response);
            return response;
        } catch (Exception e) {
            LOG.error("Failed to send push notification to user {} isLive {}", userId, IS_LIVE, e);
            return null;
        }

    }

    public static class Builder {

        private FirebaseClient result;

        private Builder() {
            result = new FirebaseClient();
        }

        /**
         * Set the API Server Key.
         *
         * @param key Firebase Server Key (NOT the Web API Key!!!)
         */
        public Builder setKey(String key) {
            if (null == key) {
                throw new IllegalArgumentException("API key can not be null");
            }
            result.SERVER_KEY = key;
            return this;
        }

        public Builder setLive(Boolean isLive) {
            if (null == isLive) {
                result.IS_LIVE = false;
            } else {
                result.IS_LIVE = isLive;
            }
            return this;
        }

        public FirebaseClient build() {
            return result;
        }
    }
}



