package org.patientview.importer.controller;

import generated.Patientview;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.importer.service.QueueService;
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
public class ImportControllerTest {

    @Mock
    UriComponentsBuilder uriComponentsBuilder;

    MockMvc mockMvc;

    @Mock
    QueueService queueService;

    @InjectMocks
    ImportController importController;

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
    //@Ignore("IntegrationTest")
    public void importIntegrationTest() throws Exception {
        post(getTestFile());
    }


    String getTestFile() throws IOException, URISyntaxException {

        URL xmlPath =
                Thread.currentThread().getContextClassLoader().getResource("data/xml/SAC02_01436_21626578408.xml");
        File file = new File(xmlPath.toURI());
        return  new String(Files.readAllBytes(Paths.get(file.getPath())));
    }

    private static org.apache.http.HttpResponse post(String json) throws Exception {

        org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();

        String postUrl="http://diabetes-pv.dev.solidstategroup.com/importer/import";// put in your url
        HttpPost post = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(json);

        post.setEntity(postingString);
        post.setHeader("Content-type", "application/xml");
        return httpClient.execute(post);

    }


}
