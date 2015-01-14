package org.patientview.api.job;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.Timer;
import org.patientview.api.service.UktService;

import java.util.Properties;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/01/2015
 */
public class UktTaskTest {

    @Mock
    UktService uktService;

    @Mock
    Timer timer;

    @Mock
    Properties properties;

    @InjectMocks
    UktTask uktTask;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUktImport() throws Exception {
        when(properties.getProperty("ukt.import.enabled")).thenReturn("true");
        uktTask.importUktData();
        verify(uktService, Mockito.times(1)).importData();
    }

    @Test
    public void testUktExport() throws Exception {
        when(properties.getProperty("ukt.export.enabled")).thenReturn("true");
        uktTask.exportUktData();
        verify(uktService, Mockito.times(1)).exportData();
    }
}