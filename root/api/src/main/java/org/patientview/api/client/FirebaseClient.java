package org.patientview.api.client;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

/**
 * Firebase API client implementation for sending out notification.
 */
public class FirebaseClient {
    private static Logger LOG = LoggerFactory.getLogger(FirebaseClient.class);
    private static final String API_URL = "https://fcm.googleapis.com/fcm/send";
    private static String SERVER_KEY;
    private static boolean IS_LIVE = false;

    private FirebaseClient() {
    }

    /**
     * @return a FirebaseClient client builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Send push notification for new results.
     * Only users with setting set to Mobile will be notified.
     * <p>
     * Messages sent to targets.
     * This class interfaces with the FCM server by sending the Notification over HTTP-POST JSON.
     *
     * @param userId an id of the user to send notification for
     * @return FcmResponse object containing HTTP response info.
     */
    public String notifyResult(Long userId) {
        LOG.info("Sending mobile push notification for user {}", userId);
        if (StringUtils.isEmpty(SERVER_KEY)) {
            LOG.error("No Server-Key has been defined for firebase client.");
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

            JSONObject notification = new JSONObject();
            notification.put("body", "New results have arrived");
            notification.put("title", "New data has arrived on PatientView. Please log in to view");

            return push(topic, notification, null, null);
        } catch (Exception e) {
            LOG.error("Failed to send push notification to user {} isLive {}", userId, IS_LIVE, e);
            return null;
        }
    }

    /**
     * Send push notification for new message in conversation.
     * When users installed to mobile app they will be automatically subscribed to new message
     * notification.
     *
     * @param userId an id of the user to send notification for
     * @return FcmResponse object containing HTTP response info.
     */
    public String notifyMessage(Long userId, Long conversationId) {
        LOG.info("Sending message mobile push notification for user {}", userId);
        if (StringUtils.isEmpty(SERVER_KEY)) {
            LOG.error("No Server-Key has been defined for firebase client.");
            return null;
        }

        try {

            /**
             * The same firebase app for Test and Live
             * but different topics per env
             * LIVE: /topics/{userid}-messages
             * TEST:  /topics/{userid}-test-messages
             */
            String topic = "/topics/" + userId + "-test-messages";
            if (IS_LIVE) {
                topic = "/topics/" + userId + "-messages";
            }

            JSONObject notification = new JSONObject();
            notification.put("body", "A new message has arrived on PatientView. Please login to view");
            notification.put("title", "PatientView: New message(s) received");

            JSONObject data = new JSONObject();
            data.put("conversationId", conversationId);

            return push(topic, notification, data, conversationId.toString());
        } catch (Exception e) {
            LOG.error("Failed to send push notification to user {} isLive {}", userId, IS_LIVE, e);
            return null;
        }
    }


    /**
     * Common helper to send message to firebase
     *
     * @param topic        a topic in firebase
     * @param notification a message to be used to send to subscriber as notification
     * @return
     */
    private String push(String topic, JSONObject notification, JSONObject data, String groupBy) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "key=" + SERVER_KEY);
        httpHeaders.set("Content-Type", "application/json;charset=UTF-8");

        JSONObject body = new JSONObject();
        body.put("to", topic);
        body.put("priority", "high");
        //body.put("dry_run", true); // to test without sending the message
        // if we need to group notification by some common key
        if (groupBy != null && !groupBy.isEmpty()) {
            body.put("collapse_key", groupBy);
        }

        if (data != null) {
            body.put("data", data);
        }

        body.put("notification", notification);

        HttpEntity<String> httpEntity = new HttpEntity<>(body.toString(), httpHeaders);
        String response = restTemplate.postForObject(API_URL, httpEntity, String.class);

        LOG.info("Firebase client response " + response);
        return response;
    }


    /**
     * Builder to initialize firebase client.
     */
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

        /**
         * Set live if you want to hit live push notification url
         *
         * @param isLive a boolean to indicate if to use live urls for push notification
         * @return
         */
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



