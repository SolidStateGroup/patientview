package org.patientview.importer.controller;

import generated.Patientview;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.importer.service.ImportService;
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

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
public class TestImportController {

    @Mock
    UriComponentsBuilder uriComponentsBuilder;

    MockMvc mockMvc;

    @Mock
    ImportService importService;

    @InjectMocks
    private ImportController importController;

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
        Assert.assertTrue("The test file is not null", !StringUtils.isEmpty(content));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/import")
                    .content(content).contentType(MediaType.APPLICATION_XML))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }
        catch (Exception e) {
            Assert.fail("The post request all should not fail " + e.getCause());
        }

        Mockito.verify(importService, Mockito.times(1)).importRecord(Mockito.any(Patientview.class));

    }


    String getTestFile() throws IOException, URISyntaxException {

        URL xmlPath =
                Thread.currentThread().getContextClassLoader().getResource("data/xml/SAC02_01436_21626578408.xml");
        File file = new File(xmlPath.toURI());
        return  new String(Files.readAllBytes(Paths.get(file.getPath())));
    }

}
