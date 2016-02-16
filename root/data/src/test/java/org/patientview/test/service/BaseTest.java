package org.patientview.test.service;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

/**
 * Created by james@solidstategroup.com
 * Created on 16/02/2016
 */
public abstract class BaseTest {
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }
}
