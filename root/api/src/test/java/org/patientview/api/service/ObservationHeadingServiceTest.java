package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.ObservationHeadingServiceImpl;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
public class ObservationHeadingServiceTest {

    User creator;

    @Mock
    ObservationHeadingRepository observationHeadingRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @InjectMocks
    ObservationHeadingService observationHeadingService = new ObservationHeadingServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    /**
     * Test: To see if the observation headings are returned
     * Fail: The calls to the repository are not made, not the right number
     */
    @Test
    public void testFindAll() {
        Pageable pageableAll = new PageRequest(0, Integer.MAX_VALUE);

        List<ObservationHeading> observationHeadings = new ArrayList<>();
        ObservationHeading observationHeading1 = new ObservationHeading();
        observationHeading1.setCode("OBS1");
        observationHeadings.add(observationHeading1);
        ObservationHeading observationHeading2 = new ObservationHeading();
        observationHeading2.setCode("OBS2");
        observationHeadings.add(observationHeading2);

        Page<ObservationHeading> observationHeadingsPage =
                new PageImpl<>(observationHeadings, pageableAll, observationHeadings.size());

        when(observationHeadingRepository.findAll(eq(pageableAll))).thenReturn(observationHeadingsPage);

        Page<ObservationHeading> result = observationHeadingService.findAll(new GetParameters());
        Assert.assertEquals("Should have 2 observation headings", 2, result.getNumberOfElements());
        verify(observationHeadingRepository, Mockito.times(1)).findAll(Matchers.eq(pageableAll));
    }

    @Test
    public void testAdd() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        when(observationHeadingRepository.save(eq(observationHeading))).thenReturn(observationHeading);
        ObservationHeading savedObservationHeading = observationHeadingService.add(observationHeading);

        Assert.assertNotNull("The returned observation heading should not be null", savedObservationHeading);
        verify(observationHeadingRepository, Mockito.times(1)).save(eq(savedObservationHeading));
    }

    @Test
    public void testSave() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        when(observationHeadingRepository.save(eq(observationHeading))).thenReturn(observationHeading);

        try {
            observationHeadingService.save(observationHeading);
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException thrown");
        }

        verify(observationHeadingRepository, Mockito.times(1)).save(eq(observationHeading));
    }
}
