package org.patientview.api.client;

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
 * Api Client implementation for MedlinePlus Connect web services
 * <p>
 * See https://medlineplus.gov/connect/service.html for more information
 */
public final class MedlineplusApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(MedlineplusApiClient.class);

    private CodeSystem codeSystem;
    private String apiUrl;

    private String contentType;
    private CloseableHttpClient client;
    private static final String BASE_URL = "https://connect.medlineplus.gov/service";

    private static final String CODE_SYSTEM_PARAM = "mainSearchCriteria.v.cs";
    private static final String RESPONSE_TYPE_PARAM = "knowledgeResponseType";
    private static final String CODE_PARAM = "mainSearchCriteria.v.c";


    private MedlineplusApiClient() {
    }

    public static MedlineplusApiClient.Builder newBuilder() {
        return new MedlineplusApiClient.Builder();
    }

    /**
     * Identifies the problem code system that will be used in request
     * ICD-10-CM  mainSearchCriteria.v.cs=2.16.840.1.113883.6.90
     * ICD-9-CM   mainSearchCriteria.v.cs=2.16.840.1.113883.6.103
     * SNOMED CT  mainSearchCriteria.v.cs=2.16.840.1.113883.6.96
     */
    public enum CodeSystem {
        ICD_10_CM {
            @Override
            public String code() {
                return "2.16.840.1.113883.6.90";
            }

            @Override
            public String nameCode() {
                return "ICD-10";
            }
        },
        ICD_9_CM {
            @Override
            public String code() {
                return "2.16.840.1.113883.6.103";
            }

            @Override
            public String nameCode() {
                return "ICD-9";
            }
        },
        SNOMED_CT {
            @Override
            public String code() {
                return "2.16.840.1.113883.6.96";
            }

            @Override
            public String nameCode() {
                return "SNOMED-CT";
            }
        };

        public abstract String code();

        public abstract String nameCode();
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
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(CODE_PARAM, code));

        // Build the server url together with the parameters you wish to pass
        URIBuilder urlBuilder = new URIBuilder(buildFullUrl());
        urlBuilder.addParameters(parameters);

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
    private String buildFullUrl() {
        return apiUrl + "?" + CODE_SYSTEM_PARAM + "=" + codeSystem.code()
                + "&" + RESPONSE_TYPE_PARAM + "=" + contentType;
    }

    public static final class Builder {
        private MedlineplusApiClient result;

        private Builder() {
            result = new MedlineplusApiClient();
        }

        public Builder setCodeSystem(CodeSystem codeSystem) {
            if (null != codeSystem) {
                result.codeSystem = codeSystem;
            }
            return this;
        }

        public MedlineplusApiClient build() {

            // default to ICD-10-CM code system if nothing provided
            if (result.codeSystem == null) {
                result.codeSystem = CodeSystem.ICD_10_CM;
            }
            result.apiUrl = BASE_URL;
            result.contentType = "application/json";
            result.client = HttpClients.custom().build();
            return result;
        }
    }
}
