package org.patientview.test.persistence.repository;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.MediaTypes;
import org.patientview.persistence.repository.MyMediaRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests concerned with retrieving the correct news for a user.
 * <p>
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class MyMediaRepositoryTest {

    @Inject
    MyMediaRepository myMediaRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    Lookup lookup;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
        lookup = dataTestUtils.createLookup("TOP", LookupTypes.MENU);
    }


    @Test
    public void addMyMedia() {
        MyMedia myMedia = new MyMedia();
        myMedia.setCreated(new Date());
        myMedia.setCreator(creator);
        myMedia.setType(MediaTypes.IMAGE);

        MyMedia returnedItem = myMediaRepository.save(myMedia);
        assertNotNull(returnedItem.getId());
    }


    @Test
    public void getMediaSize() {
        MyMedia myMedia = new MyMedia();
        myMedia.setCreated(new Date());
        myMedia.setCreator(creator);
        myMedia.setType(MediaTypes.IMAGE);
        myMedia.setDeleted(false);

        //Original String
        String string =
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        string = string + string + string + string + string + string + string + string + string + string;
        string = string + string + string + string + string + string + string + string + string + string;
        string = string + string + string + string + string + string + string + string + string + string;
        string = string + string + string + string + string + string + string + string + string + string;

        //Convert to byte[]
        byte[] bytes = string.getBytes();

        myMedia.setContent(bytes);
        myMediaRepository.save(myMedia);
        Long number = myMediaRepository.getUserTotal(creator, false);

        assertTrue(number == 21L);

    }


    @Test
    public void getMediaForUser() {
        MyMedia myMedia = new MyMedia();
        myMedia.setCreated(new Date());
        myMedia.setCreator(creator);
        myMedia.setType(MediaTypes.IMAGE);

        myMediaRepository.save(myMedia);
        PageRequest pageable = new PageRequest(0, 100);

        assertEquals(1, myMediaRepository.getByCreator(creator, false, pageable).getNumberOfElements());
    }



    @Test
    public void getMediaForUserORderCheck() {
        MyMedia myMedia = new MyMedia();
        myMedia.setCreated(new Date());
        myMedia.setCreator(creator);
        myMedia.setLocalPath("MEDIA1");
        myMedia.setType(MediaTypes.IMAGE);

        myMediaRepository.save(myMedia);

        myMedia = new MyMedia();
        myMedia.setCreated(new DateTime().minusDays(10).toDate());
        myMedia.setCreator(creator);
        myMedia.setLocalPath("MEDIA2");
        myMedia.setType(MediaTypes.IMAGE);

        myMediaRepository.save(myMedia);
        PageRequest pageable = new PageRequest(0, 100);

        assertEquals(2, myMediaRepository.getByCreator(creator, false, pageable).getNumberOfElements());
    }


    @Test
    public void getMediaForUse2() {
        MyMedia myMedia = new MyMedia();
        myMedia.setCreated(new Date());
        myMedia.setCreator(creator);
        myMedia.setType(MediaTypes.IMAGE);

        myMediaRepository.save(myMedia);

        myMedia = new MyMedia();
        myMedia.setCreated(new Date());
        myMedia.setCreator(creator);
        myMedia.setDeleted(true);
        myMedia.setType(MediaTypes.IMAGE);

        myMediaRepository.save(myMedia);


        PageRequest pageable = new PageRequest(0, 100);

        assertEquals(1, myMediaRepository.getByCreator(creator, false, pageable).getNumberOfElements());
    }

}
