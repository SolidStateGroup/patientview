package org.patientview.api.service;

import org.im4java.core.IM4JavaException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.MyMediaServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.MyMediaRepository;
import org.patientview.test.util.TestUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class MyMediaServiceTest1 {

    User creator;

    @Mock
    MyMediaRepository myMediaRepository;

    @InjectMocks
    MyMediaService myMediaService = new MyMediaServiceImpl();


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }


    @Test
    public void testSave() throws ResourceNotFoundException, ResourceForbiddenException, IOException,
            IM4JavaException, InterruptedException {
        MyMedia myMedia = new MyMedia();
        myMedia.setCreator(creator);
        when(myMediaRepository.save(eq(myMedia))).thenReturn(myMedia);

        MyMedia savedMyMedia = myMediaService.save(creator.getId(), myMedia);

        assertNotNull(savedMyMedia);
    }

    @Test
    public void testDelete() {

    }


    @Test
    public void testGetFullImage() {

    }


    @Test
    public void testGetThumbnailImage() {

    }


    @Test
    public void testGetAllThumbnailsForUser() {

    }


    @Test
    public void testGetMyMediaById() {

    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }


}
