package org.patientview.importer.controller;

import generated.Patientview;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.importer.service.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
public class ImportControllerTest {

    @Mock
    UriComponentsBuilder uriComponentsBuilder;

    @Mock
    QueueService queueService;

    @InjectMocks
    ImportController importController;

    MockMvc mockMvc;
    
    private static final Logger LOG = LoggerFactory.getLogger(ImportControllerTest.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(importController, uriComponentsBuilder).build();
    }

    /**
     * Test: Passing an xml in full to the import resource and see if the resource can pass it to the service
     * Fail: The resource is unable to call the resource
     *
     * @throws Exception
     */
    @Test
    public void testFileImport() throws Exception {
        String content = getTestFile();
        assertTrue("The test file is not null", !StringUtils.isEmpty(content));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/import")
                    .contentType(MediaType.APPLICATION_XML)
                    .content(content))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }
        catch (Exception e) {
            fail("The post request all should not fail " + e.getCause());
        }

        Mockito.verify(queueService, Mockito.times(1)).importRecord(Mockito.any(Patientview.class));

    }

    // used to test the service once running
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTest() throws Exception {
        LOG.info(post(getTestFile()).toString());
    }

    // used to test the service once running (milestone 4 files)
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestMilestone4() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/milestone4/SAC02_01439_41737438900.xml"); // 336 observations
        files.add("data/xml/milestone4/SGC04_01436_57703939407.xml"); // big
        files.add("data/xml/milestone4/SGC04_01436_64098149107.xml"); // big, 6372 observations
        files.add("data/xml/milestone4/SGC04_01456_12314191702.xml");
        files.add("data/xml/milestone4/SGC04_01459_14018849809.xml");
        files.add("data/xml/milestone4/SGC04_01459_28039602801.xml");
        files.add("data/xml/milestone4/SGC04_01459_74569958609.xml");

        // 46 seconds local 10/11/14 fhir_delete and native create observations
        // 5 seconds local 11/11/14 native delete and create observations

        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    // used to test the service once running (2.0.6 files)
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTest206() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/2.0.6tests/SAC02_01439_1202913202.xml"); // ok, 290 observations
        files.add("data/xml/2.0.6tests/SAC02_01439_1312455233.xml"); // 400 bad request, wrong xml
        files.add("data/xml/2.0.6tests/SAC02_01439_3103833318.xml"); // ok, 336 observations
        files.add("data/xml/2.0.6tests/SGC02_01439_1312045485.xml"); // ok, 288 observations
        files.add("data/xml/2.0.6tests/SGC02_01439_2609995652.xml"); // ok, 282 observations

        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    String getTestFile() throws IOException, URISyntaxException {

        // 2.0.6 testing
        String fileName = "data/xml/2.0.6tests/SAC02_01439_1202913202.xml"; // ok, 290 observations
        //String fileName = "data/xml/2.0.6tests/SAC02_01439_1312455233.xml"; // 400 bad request, wrong xml
        //String fileName = "data/xml/2.0.6tests/SAC02_01439_3103833318.xml"; // ok, 336 observations
        //String fileName = "data/xml/2.0.6tests/SGC02_01439_1312045485.xml"; // ok, 288 observations
        //String fileName = "data/xml/2.0.6tests/SGC02_01439_2609995652.xml"; // ok, 282 observations

        //String fileName = "data/xml/SAC02_01436_1111111111.xml";
        //String fileName = "data/xml/ECS_1111111111_new.xml";
        //String fileName = "data/xml/PRODUCTION_TEST_1111111118.xml";
        //String fileName = "data/xml/SAC02_01436_1111111111_blankgp.xml";
        //String fileName = "data/xml/SAC02_01436_1111111111_single.xml";
        //String fileName = "data/xml/IMPORTGROUP_1111111111.xml";
        //String fileName = "data/xml/ECS_1111111111.xml";
        //String fileName = "data/xml/DIA01_1111111111.xml";
        //String fileName = "data/xml/errors/1111111111_damaged.xml";
        //String fileName = "data/xml/errors/abc123_unknown_identifier.xml";
        //String fileName = "data/xml/errors/1111111111_unknown_group.xml";

        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource(fileName);
        File file = new File(xmlPath.toURI());
        return new String(Files.readAllBytes(Paths.get(file.getPath())));
    }

    String getFileFromString(String fileLocation) throws IOException, URISyntaxException {
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource(fileLocation);
        File file = new File(xmlPath.toURI());
        return new String(Files.readAllBytes(Paths.get(file.getPath())));
    }

    private static org.apache.http.HttpResponse post(String json) throws Exception {
        org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();

        // will need to allow IP to post to this "sudo vi /etc/nginx/conf.d/patientview-nginx.conf" then
        // restart with "sudo service nginx restart"
        String postUrl="https://test.patientview.org/importer/import";
        //String postUrl="https://production.patientview.org/importer/import";
        //String postUrl="http://localhost:8081/importer/import";

        HttpPost post = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(json);
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/xml");
        HttpResponse httpResponse = httpClient.execute(post);
        return httpResponse;
    }
}
