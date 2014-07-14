package org.patientview.migration.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONObject;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.model.Resource;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.Feature;
import org.patientview.Group;
import org.patientview.Lookup;
import org.patientview.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Dumping ground for some Json utilities to migrate data
 *
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public final class JsonUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JsonUtil.class);

    public static String pvUrl;

    static {

        if ((pvUrl = System.getProperty("url")) == null) {
            throw new RuntimeException("Please specify an environment by using -Durl=apiUrl");
        }

    }

    private JsonUtil() {}


    public static <T, V extends HttpRequestBase> T jsonRequest(String url, Class<T> responseObject,
                                                               Object requestObject, Class<V> httpMethod)
            throws JsonMigrationException, JsonMigrationExistsException {

        Gson gson = new Gson();

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

            br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            String s;
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
            br.close();

        } catch (Exception e) {
            LOG.error("Exception trying to {} data to {} cause: {}", method.getClass().getName(), url, e.getMessage());
            throw new JsonMigrationException(e);

        } finally {
            //httpClient.getConnectionManager().shutdown();
        }


        return gson.fromJson(output.toString(), responseObject);

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
        return httpClient.execute(post);

    }

    public static HttpResponse gsonPut(String postUrl, Object object) throws Exception {

        Gson gson = new Gson();
        HttpClient httpClient = new DefaultHttpClient();
        HttpPut put = new HttpPut(postUrl);

        if (object != null) {
            String json = gson.toJson(object);
            LOG.debug("Putting the following: " + json);
            StringEntity puttingString = new StringEntity(json);
            put.setEntity(puttingString);
        }

        put.setHeader("Content-type", "application/json");
        return httpClient.execute(put);

    }


    public static  HttpResponse gsonPost(String postUrl, Object object) throws Exception {

        Gson gson = new Gson();

        String json = gson.toJson(object);
        LOG.info("Posting the following: " + json);
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost post = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(json);

        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");
        return httpClient.execute(post);

    }



    public static <T> List<T> getStaticDataList(String url) {
        HttpClient httpClient = new DefaultHttpClient();
        Gson gson = new Gson();

        HttpGet get = new HttpGet(url);
        get.addHeader("accept", "application/json");

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

        List<T> data = gson.fromJson(output.toString(), new TypeToken<List<T>>(){}.getType());

        return data;

    }


    public static List<Lookup> getStaticDataLookups(String getUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        Gson gson = new Gson();

        HttpGet get = new HttpGet(getUrl);
        get.addHeader("accept", "application/json");

        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
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

        List<Lookup> data = gson.fromJson(output.toString(), new TypeToken<List<Lookup>>(){}.getType());

        return data;

    }

    public static List<Feature> getStaticDataFeatures(String getUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        HttpGet get = new HttpGet(getUrl);
        get.addHeader("accept", "application/json");

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

        List<Feature> data = gson.fromJson(output.toString(), new TypeToken<List<Feature>>(){}.getType());

        return data;

    }

    public static List<Group> getGroups(String getUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        HttpGet get = new HttpGet(getUrl);
        get.addHeader("accept", "application/json");

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
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        HttpGet get = new HttpGet(getUrl);
        get.addHeader("accept", "application/json");

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
