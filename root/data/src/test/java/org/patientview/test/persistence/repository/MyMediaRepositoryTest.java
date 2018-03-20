package org.patientview.test.persistence.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.ResearchStudy;
import org.patientview.persistence.model.ResearchStudyCriteria;
import org.patientview.persistence.model.ResearchStudyCriteriaData;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.GroupRelationshipRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.MyMediaRepository;
import org.patientview.persistence.repository.ResearchStudyCriteriaRepository;
import org.patientview.persistence.repository.ResearchStudyRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

/**
 * Tests concerned with retrieving the correct news for a user.
 *
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
    public void addMyMedia(){
        MyMedia myMedia = new MyMedia();
        myMedia.setCreated(new Date());
        myMedia.setCreator(creator);

        MyMedia returnedItem = myMediaRepository.save(myMedia);
        assertNotNull(returnedItem.getId());
    }
}
