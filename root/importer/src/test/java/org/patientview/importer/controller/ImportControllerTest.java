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
    @Ignore
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

    // used to test the service once running (CKD files)
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD01() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD/RQHC7_01_1234567345.xml");
        files.add("data/xml/CKD/RQHC7_01_4325437666.xml");
        files.add("data/xml/CKD/RQHC7_01_4444443333.xml");
        files.add("data/xml/CKD/RQHC7_01_5432134678.xml");
        files.add("data/xml/CKD/RQHC7_01_5566778833.xml");
        files.add("data/xml/CKD/RQHC7_01_9879879876.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD02() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD/RQHC7_02_4325437666.xml");
        files.add("data/xml/CKD/RQHC7_02_9879879876.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD03() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD/RQHC7_03_4325437666.xml");
        files.add("data/xml/CKD/RQHC7_03_9879879876.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD04() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD/RQHC7_04_4325437666.xml");
        files.add("data/xml/CKD/RQHC7_04_9879879876.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD_2_01() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD_2/RQHC7_01_1234567345.xml");
        files.add("data/xml/CKD_2/RQHC7_01_5432134678.xml");
        files.add("data/xml/CKD_2/RQHC7_01_5566778833.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD_2_02() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD_2/RQHC7_02_1234567345.xml");
        files.add("data/xml/CKD_2/RQHC7_02_5432134678.xml");
        files.add("data/xml/CKD_2/RQHC7_02_5566778833.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    //@Ignore("IntegrationTest")
    public void importIntegrationTestCKD_3_01() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD_3/RQR00_01_667788990.xml");
        files.add("data/xml/CKD_3/RQR00_01_5556667778.xml");
        files.add("data/xml/CKD_3/RQR00_01_6655443322.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    // hfdemo
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestHfdemo() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/hfdemo/EDINHF1_5745634581_01.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
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

    // used to test the service once running (IBD files)
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestIbd() throws Exception {
        List<String> files = new ArrayList<>();
        // contain real data, must be added to folder manually
        files.add("data/xml/ibd/SALIBD_3466151139.xml");
        //files.add("data/xml/ibd/SALIBD_4426012465.xml");
        //files.add("data/xml/ibd/SALIBD_4500666672.xml");
        //files.add("data/xml/ibd/SALIBD_6186660419.xml");
        //files.add("data/xml/ibd/SALIBD_6304291914.xml");

        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    String getTestFile() throws IOException, URISyntaxException {
        // local testing
        //String fileName = "data/xml/SAC02_01436_1111111111.xml";
        String fileName = "data/xml/EDINHF1_01436_1111111111.xml";
        //String fileName = "data/xml/2.0.6tests/SAC02_01436_1111111111_PDF.xml";

        // IBD
        //String fileName = "data/xml/ibd/1111111111_ibd.xml";

        // partial migration
        //String fileName = "data/xml/partialmigration/test1.xml";

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
        //String postUrl="https://test.patientview.org/importer/import";
        //String postUrl="https://production.patientview.org/importer/import";
        String postUrl="http://localhost:8081/importer/import";

        HttpPost post = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(json);
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/xml");
        HttpResponse httpResponse = httpClient.execute(post);
        return httpResponse;
    }
}
