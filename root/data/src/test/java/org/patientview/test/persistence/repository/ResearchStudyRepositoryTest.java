package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.NewsLink;
import org.patientview.persistence.model.ResearchStudy;
import org.patientview.persistence.model.ResearchStudyCriteria;
import org.patientview.persistence.model.ResearchStudyCriteriaData;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.GroupRelationshipRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.NewsLinkRepository;
import org.patientview.persistence.repository.ResearchStudyCriteriaRepository;
import org.patientview.persistence.repository.ResearchStudyRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class ResearchStudyRepositoryTest {

    @Inject
    ResearchStudyRepository researchStudyRepository;

    @Inject
    ResearchStudyCriteriaRepository researchStudyCriteriaRepository;

    @Inject
    GroupRepository groupRepository;

    @Inject
    GroupRoleRepository groupRoleRepository;

    @Inject
    GroupRelationshipRepository groupRelationshipRepository;

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


    /**
     * Test: Create a news item link it to a group, link a user to the group and then retrieve the news
     * Fail: The correct news it not retrieved
     */
    @Test
    public void testGetAll() {
        researchStudyRepository.findAll();
    }



    @Test
    public void addStudy(){
        ResearchStudy researchStudy = new ResearchStudy();
//        researchStudy.setCreatedDate(new Date());
//
//        researchStudy.setLastUpdate(researchStudy.getCreatedDate());
        ResearchStudyCriteria criteria = new ResearchStudyCriteria();
        ResearchStudyCriteriaData data = new ResearchStudyCriteriaData();
        data.setGender("M");
        //criteria.setResearchStudyCriterias(data);
        criteria.setResearchStudy(researchStudy);

        researchStudyCriteriaRepository.save(criteria);

        Set<ResearchStudyCriteria> criteriaHashSet = new HashSet<>();
        criteriaHashSet.add(criteria);

        Long study = researchStudyRepository.save(researchStudy).getId();


        assertNotNull(study);
    }
}
