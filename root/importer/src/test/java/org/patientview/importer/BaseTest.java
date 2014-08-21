package org.patientview.importer;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

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
public abstract class BaseTest {


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


    public String getTestFile() throws IOException, URISyntaxException {

        URL xmlPath =
                Thread.currentThread().getContextClassLoader().getResource("data/xml/SAC02_01436_21626578408.xml");
        File file = new File(xmlPath.toURI());
        return  new String(Files.readAllBytes(Paths.get(file.getPath())));
    }

}
