package org.patientview.api.client.nhschoices;

import org.apache.geronimo.mail.util.StringBufferOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Api Client implementation for NhsChoices API v2
 */
public final class NhsChoicesApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(NhsChoicesApiClient.class);
    private static final String AUTH_HEADER = "Subscription-Key";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String BASE_URL = "https://api.nhs.uk/";
    protected static final String CONDITIONS_URI = "conditions/";
    protected static final String ORGANISATION_URI = "data/{Organisation}/all";

    private String apiUrl;
    private String contentType;
    private CloseableHttpClient client;


    private String apiKey;


    // Filters the conditions by A-Z
    private static final String PARAM_CONDITION_CATEGORY = "category";
    // Includes synonyms conditions
    private static final String PARAM_SYNONYMS = "synonyms";


    private NhsChoicesApiClient() {
    }

    public static NhsChoicesApiClient.Builder newBuilder() {
        return new NhsChoicesApiClient.Builder();
    }


    /**
     * Retrieves NHSChoices conditions for give letter
     *
     * @param letter an A-Z letter to filter conditions
     * @return a list of condition, or null if nothing found or cannot contact api
     */
    public List<ConditionLink> getConditions(String letter) {
        if (letter == null || letter.isEmpty()) {
            throw new IllegalArgumentException("Please provide letter");
        }

        try {
            NhsChoicesResponseJson responseJson = doGet(letter, CONDITIONS_URI);
            if (responseJson != null) {
                responseJson.getConditionLinks();
            }
        } catch (Exception e) {
            LOG.error("Exception in NhsChoicesApiClient ", e);
        }
        return null;
    }

    /**
     * Finds all NHSChoices conditions
     *
     * @return a list of all condition, or null if nothing found or cannot contact api
     */
    public List<ConditionLink> getAllConditions() {

        List<ConditionLink> allConditions = new ArrayList<>();

        for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
            try {
                NhsChoicesResponseJson responseJson = doGet(String.valueOf(alphabet), CONDITIONS_URI);
                if (responseJson != null) {
                    allConditions.addAll(responseJson.getConditionLinks());
                } else {
                    LOG.warn("NhsChoicesResponseJson is null for  " + alphabet);
                }
            } catch (Exception e) {
                LOG.error("Exception in NhsChoicesApiClient.getAllConditions() ", e);
            }

            // test system need to throttle as allowed 10 calls/minute up to a maximum of 1000 calls per month
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                LOG.error("InterruptedException in NhsChoicesApiClient ", ie);
            }
        }

        return allConditions;
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing client");
        }
    }

    private NhsChoicesResponseJson doGet(String letter, String uri) throws IOException, URISyntaxException {

        // add code parameter
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(PARAM_CONDITION_CATEGORY, letter));
        parameters.add(new BasicNameValuePair(PARAM_SYNONYMS, "false"));

        // Build the server url together with the parameters you wish to pass
        URIBuilder urlBuilder = new URIBuilder(buildFullUrl(uri));
        urlBuilder.addParameters(parameters);

        HttpGet get = new HttpGet(urlBuilder.build());
        // set headers
        get.setHeader(AUTH_HEADER, apiKey);
        get.setHeader(CONTENT_TYPE_HEADER, contentType);

        CloseableHttpResponse response = client.execute(get);
        String body = getBody(response);
        response.close();
        LOG.debug("GET response body {}", body);
        int httpCode = response.getStatusLine().getStatusCode();

        NhsChoicesResponseJson responseJson = new NhsChoicesResponseJson();

        // only parse on 200 response
        if (httpCode == HttpServletResponse.SC_OK) {
            responseJson.parse(body);
        } else {
            return null;
        }
        return responseJson;
    }

    private String getBody(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        StringBuffer buffer = new StringBuffer();
        StringBufferOutputStream out = new StringBufferOutputStream(buffer);
        entity.writeTo(out);
        out.close();
        return buffer.toString();
    }

    /**
     * Helper method to build full url baseUrl + endpoint uri
     */
    private String buildFullUrl(String uri) {
        return apiUrl + uri;
    }

    public static final class Builder {
        private NhsChoicesApiClient result;

        private Builder() {
            result = new NhsChoicesApiClient();
        }

        public Builder setApiKey(String apiKey) {
            if (null != apiKey) {
                result.apiKey = apiKey;
            }
            return this;
        }

        public NhsChoicesApiClient build() {
            result.apiUrl = BASE_URL;
            result.contentType = "application/json";
            result.client = HttpClients.custom().build();
            return result;
        }
    }
}
