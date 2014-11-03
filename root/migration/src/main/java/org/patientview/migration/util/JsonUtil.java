package org.patientview.migration.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONObject;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.model.Resource;
import org.patientview.DateDeserializer;
import org.patientview.DateSerializer;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.model.LoginDetails;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;

/**
 * Dumping ground for some Json utilities to migrate data
 *
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public final class JsonUtil {

    private static Gson gson  = new GsonBuilder()
            .registerTypeAdapter(Date.class, new DateDeserializer())
            .registerTypeAdapter(Date.class, new DateSerializer())
            .create();

    private static final Logger LOG = LoggerFactory.getLogger(JsonUtil.class);

    public static String pvUrl;

    public static String token;

    static {
        if ((pvUrl = System.getProperty("url")) == null) {
            throw new RuntimeException("Please specify an environment by using -Durl=apiUrl");
        }
    }

    private JsonUtil() {}

    public static String authenticate(String username, String password)
            throws JsonMigrationException, JsonMigrationExistsException {
        LOG.info("Authenticating " + username);
        LoginDetails loginDetails = new LoginDetails(username, password);
        return JsonUtil.jsonRequest(JsonUtil.pvUrl + "/auth/login", String.class, loginDetails, HttpPost.class, true);
    }

    public static void logout() throws JsonMigrationException, JsonMigrationExistsException {
        JsonUtil.jsonRequest(JsonUtil.pvUrl + "/auth/logout/" + token, String.class, null, HttpDelete.class, true);
    }

    public static <T, V extends HttpRequestBase> T  jsonRequest(String url, Class<T> responseObject,
                                               Object requestObject, Class<V> httpMethod, boolean expectResponse)
            throws JsonMigrationException, JsonMigrationExistsException {

        HttpClient httpClient = getThreadSafeClient();
        V method;

        try {
            Constructor<V> cons = httpMethod.getConstructor(String.class);
            method = cons.newInstance(url);
        } catch (Exception e) {
            LOG.error("Error creating request type {}", e.getCause());
            throw new JsonMigrationException(e);
        }

        try {
            if (requestObject == null) {
                requestObject = new Object();
            }

            String json = gson.toJson(requestObject);
            LOG.debug("Adding the following to request: " + json);
            StringEntity puttingString = new StringEntity(json);
            if (method instanceof HttpEntityEnclosingRequestBase) {
                ((HttpEntityEnclosingRequestBase) method).setEntity(puttingString);
            }

        } catch (Exception e) {
            LOG.error("Error creating request object {}", e.getCause());
            throw new JsonMigrationException(e);
        }

        method.setHeader("Content-type", "application/json");
        method.setHeader("X-Auth-Token", token);
        BufferedReader br;
        StringBuilder output = new StringBuilder();

        try {

            HttpResponse httpResponse = httpClient.execute(method);

            Integer statusCode = httpResponse.getStatusLine().getStatusCode();

            if (statusCode == 409) {
                throw new JsonMigrationExistsException("A conflict response from server");
            }

            if (statusCode >= 300) {
                LOG.error("Server response " + httpResponse);
                throw new JsonMigrationException("An " + httpResponse.getStatusLine().getStatusCode() + " error response from the server");
            }

            if (expectResponse) {
                br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String s;
                while ((s = br.readLine()) != null) {
                    output.append(s);
                }
                br.close();
            }

            //LOG.info("Status: " + statusCode + " " + output.toString());
        } catch (Exception e) {
            LOG.error("Exception trying to {} data to {} cause: {}", method.getClass().getName(), url, e.getMessage());
            throw new JsonMigrationException(e);
        } finally {
            httpClient.getConnectionManager().closeExpiredConnections();
            httpClient.getConnectionManager().shutdown();
        }

        if (expectResponse) {
            return gson.fromJson(output.toString(), responseObject);
        } else {
            return null;
        }
    }

    public static String getResourceUuid(String json) throws Exception {
        HttpResponse httpResponse = gsonPost(json);
        String source = EntityUtils.toString(httpResponse.getEntity());
        System.out.println(source);
        JSONObject jsonObject = new JSONObject(source);
        return String.valueOf(jsonObject.get("insert_resource"));
    }

    private static HttpResponse gsonPost(String json) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        String postUrl="http://dev.solidstategroup.com:7865/api/resource";// put in your url
        HttpPost post = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(json);
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");
        post.setHeader("X-Auth-Token", token);
        return httpClient.execute(post);
    }

    public static HttpResponse gsonPut(String postUrl, Object object) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPut put = new HttpPut(postUrl);

        if (object != null) {
            String json = gson.toJson(object);
            LOG.debug("Putting the following: " + json);
            StringEntity puttingString = new StringEntity(json);
            put.setEntity(puttingString);
        }

        put.setHeader("Content-type", "application/json");
        put.setHeader("X-Auth-Token", token);
        return httpClient.execute(put);
    }

    // testing asynchronous alternate methods
    public static void gsonPost(String postUrl, Object object) throws Exception {
        String json = gson.toJson(object);

        // see http://hc.apache.org/httpcomponents-client-4.3.x/quickstart.html
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(json);
        httpPost.setEntity(postingString);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("X-Auth-Token", token);
        CloseableHttpResponse response2 = httpclient.execute(httpPost);

        try {
            HttpEntity entity2 = response2.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(entity2);
        } finally {
            response2.close();
        }
        httpclient.close();

        // httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10);
        //post.releaseConnection();
        //httpClient.getConnectionManager().closeExpiredConnections();
        //httpClient.getConnectionManager().closeIdleConnections(1, TimeUnit.SECONDS);

        /*RequestBuilder builder = new RequestBuilder("POST");
        Request request = builder.setUrl(postUrl)
                .addHeader("Content-type", "application/json")
                .addHeader("X-Auth-Token", token)
                .setBody(gson.toJson(object))
                .build();

        AsyncHttpClient client = new AsyncHttpClient();
        client.prepareRequest(request);
        client.executeRequest(request).done();*/
        //client.closeAsynchronously();
        //client.executeRequest(request);
        //return null;
    }

    public static <T> List<T> getStaticDataList(String url) {
        HttpClient httpClient = new DefaultHttpClient();

        HttpGet get = new HttpGet(url);
        get.addHeader("accept", "application/json");
        get.addHeader("X-Auth-Token", token);
        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpResponse.getStatusLine().getStatusCode());
        }

        BufferedReader br;
        StringBuilder output = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));
            String s;
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
        } catch (Exception e) {
            LOG.error("Exception trying to get data from: {} cause: {}", url, e.getCause());
            e.printStackTrace();
        }

        return gson.fromJson(output.toString(), new TypeToken<List<T>>(){}.getType());
    }

    public static List<Lookup> getStaticDataLookups(String getUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(getUrl);

        get.addHeader("accept", "application/json");
        get.addHeader("X-Auth-Token", token);
        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (httpResponse.getStatusLine().getStatusCode() != 200 ) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpResponse.getStatusLine().getStatusCode() + getUrl);
        }

        BufferedReader br;

        StringBuilder output = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));

            String s;
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
        } catch (Exception e) {
            LOG.error("Exception trying to get data from: {} cause: {}", getUrl, e.getCause());
            e.printStackTrace();
        }

        return gson.fromJson(output.toString(), new TypeToken<List<Lookup>>(){}.getType());
    }

    public static List<Feature> getStaticDataFeatures(String getUrl) {
        HttpClient httpClient = new DefaultHttpClient();

        HttpGet get = new HttpGet(getUrl);
        get.addHeader("accept", "application/json");
        get.addHeader("X-Auth-Token", token);
        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpResponse.getStatusLine().getStatusCode());
        }

        BufferedReader br;

        StringBuilder output = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));

            String s;
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
        } catch (Exception e) {
            LOG.error("Exception trying to get data from: {} cause: {}", getUrl, e.getCause());
            e.printStackTrace();
        }

        return gson.fromJson(output.toString(), new TypeToken<List<Feature>>(){}.getType());
    }

    public static List<Group> getGroups(String getUrl) {
        HttpClient httpClient = new DefaultHttpClient();

        HttpGet get = new HttpGet(getUrl);
        get.addHeader("accept", "application/json");
        get.addHeader("X-Auth-Token", token);
        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpResponse.getStatusLine().getStatusCode());
        }

        BufferedReader br;

        StringBuilder output = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));

            String s;
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
        } catch (Exception e) {
            LOG.error("Exception trying to get data from: {} cause: {}", getUrl, e.getCause());
            e.printStackTrace();
        }

        List<Group> data = gson.fromJson(output.toString(), new TypeToken<List<Group>>(){}.getType());

        return data;
    }

    public static List<Role> getRoles(String getUrl) {
        HttpClient httpClient = new DefaultHttpClient();

        HttpGet get = new HttpGet(getUrl);
        get.addHeader("accept", "application/json");
        get.addHeader("X-Auth-Token", token);
        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpResponse.getStatusLine().getStatusCode());
        }

        BufferedReader br;

        StringBuilder output = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));

            String s;
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
        } catch (Exception e) {
            LOG.error("Exception trying to get data from: {} cause: {}", getUrl, e.getCause());
            e.printStackTrace();
        }

        List<Role> data = gson.fromJson(output.toString(), new TypeToken<List<Role>>(){}.getType());

        return data;
    }

    public static String serializeResource(Resource resource) {
        OutputStream out = new ByteArrayOutputStream();

        JsonComposer jsonComposer = new JsonComposer();
        try {
            jsonComposer.compose(out, resource, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toString();
    }

    public static DefaultHttpClient getThreadSafeClient()  {
        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();
        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
        return client;
    }
}
