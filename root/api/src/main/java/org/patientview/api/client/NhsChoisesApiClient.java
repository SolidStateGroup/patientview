package org.patientview.api.client;

import org.apache.geronimo.mail.util.StringBufferOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Api Client implementation for NhsChoises API v2
 *
 */
public final class NhsChoisesApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(NhsChoisesApiClient.class);

    private String apiUrl;

    private String contentType;
    private CloseableHttpClient client;
    private static final String BASE_URL = "http://v1.syndication.nhschoices.nhs.uk/organisations/gppractices/postcode/W67HY.xml?range=1";

    private String apiKey;
    private String primaryApiKey;
    private String secondaryApiKey;


    private NhsChoisesApiClient() {
    }

    public static NhsChoisesApiClient.Builder newBuilder() {
        return new NhsChoisesApiClient.Builder();
    }


    /**
     * Retrieves link for given code from MedlinePlus web service
     *
     * @param code
     */
    public MedlineplusResponseJson getLink(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Missing problem code");
        }

        try {
            return doGet(code);
        } catch (Exception e) {
            LOG.error("Exception in MedlineplusApiClient ", e);
        }
        return null;

    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing client");
        }
    }

    private MedlineplusResponseJson doGet(String code) throws IOException, URISyntaxException {

        // add code parameter
//        List<NameValuePair> parameters = new ArrayList<>();
//        parameters.add(new BasicNameValuePair(CODE_PARAM, code));

        // Build the server url together with the parameters you wish to pass
        URIBuilder urlBuilder = new URIBuilder(apiUrl);
        //urlBuilder.addParameters(parameters);

        HttpGet get = new HttpGet(urlBuilder.build());


        CloseableHttpResponse response = client.execute(get);
        String body = getBody(response);
        response.close();
        LOG.debug("GET response body {}", body);
        int httpCode = response.getStatusLine().getStatusCode();

        MedlineplusResponseJson responseJson = new MedlineplusResponseJson();

        // only parse on 200 reponse
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
     * Helper method to build full url baseUrl + endpoint uri + params
     */
//    private String buildFullUrl() {
//        return apiUrl + "?" + CODE_SYSTEM_PARAM + "=" + codeSystem.code()
//                + "&" + RESPONSE_TYPE_PARAM + "=" + contentType;
//    }

    public static final class Builder {
        private NhsChoisesApiClient result;

        private Builder() {
            result = new NhsChoisesApiClient();
        }

        public Builder setApiKey(String apiKey) {
            if (null != apiKey) {
                result.apiKey = apiKey;
            }
            return this;
        }

        public Builder setPrimaryApiKey(String primaryApiKey) {
            if (null != primaryApiKey) {
                result.primaryApiKey = primaryApiKey;
            }
            return this;
        }

        public Builder setSecpndaryApiKey(String secondaryApiKey) {
            if (null != secondaryApiKey) {
                result.secondaryApiKey = secondaryApiKey;
            }
            return this;
        }

        public NhsChoisesApiClient build() {

            result.apiUrl = BASE_URL;
            // default to ICD-10-CM code system if nothing provided
            if (result.apiKey == null) {
                result.apiUrl = result.apiUrl + "&apikey=" + result.apiKey;
            }

            result.contentType = "application/json";
            result.client = HttpClients.custom().build();
            return result;
        }
    }
}
