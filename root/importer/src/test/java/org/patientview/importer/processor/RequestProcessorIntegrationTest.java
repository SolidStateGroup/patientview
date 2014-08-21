package org.patientview.importer.processor;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.importer.config.ImporterConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
// ApplicationContext will be loaded from the static inner ContextConfiguration class
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
@Ignore
public class RequestProcessorIntegrationTest {

    @Inject
    private RequestProcessor requestProcessor;

    @Configuration
    @Import(ImporterConfig.class)
    static class config {

    }

    @Test
    public void testProcess() {
        requestProcessor.init();
    }


}
