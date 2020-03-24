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

/**
 * Tests concerned with retrieving the correct news for a user.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class NewsItemRepositoryTest {

    @Inject
    NewsItemRepository newsItemRepository;

    @Inject
    NewsLinkRepository newsLinkRepository;

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
    public void testGetPublicNews() {

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(creator);
        newsItem.setCreated(new Date());

        NewsLink newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setRole(dataTestUtils.createRole(RoleName.PUBLIC, RoleType.STAFF));
        newsLink.setNewsItem(newsItem);

        newsItem.setNewsLinks(new HashSet<NewsLink>());
        newsItem.getNewsLinks().add(newsLink);

        newsItemRepository.save(newsItem);

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);
        Page<NewsItem> newsItems = newsItemRepository.getPublicNews(pageable);

        // Which should get 1 route back and it should be the one that was created
        Assert.assertEquals("There should be 1 news item available", 1, newsItems.getContent().size());
        Assert.assertTrue("The news item should be the one created", newsItems.getContent().get(0).equals(newsItem));
    }

    @Test
    public void testGetGroupNewsByUser() {

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(creator);
        newsItem.setCreated(new Date());
        newsItemRepository.save(newsItem);

        // Create a group for the news to be linked too
        Group group = dataTestUtils.createGroup("TEST_GROUP");
        NewsLink newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setGroup(group);
        newsLink.setNewsItem(newsItem);
        newsLinkRepository.save(newsLink);

        // Create the user and link the group to the usser
        User newsUser = dataTestUtils.createUser("NewsUser");
        GroupRole groupRole = new GroupRole();
        groupRole.setUser(newsUser);
        Role role = dataTestUtils.createRole(RoleName.SPECIALTY_ADMIN, RoleType.STAFF);
        groupRole.setGroup(group);
        groupRole.setCreator(creator);
        groupRole.setStartDate(new Date());
        groupRoleRepository.save(groupRole);

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);

        Page<NewsItem> newsItems = newsItemRepository.findGroupNewsByUser(newsUser, pageable);

        // Which should get 1 route back and it should be the one that was created
        Assert.assertTrue("There should be 1 news item available", newsItems.getContent().size() == 1);
        Assert.assertTrue("The news item should be the one created", newsItems.getContent().get(0).equals(newsItem));
    }

    /**
     * Test: Create a news item link it to a role, link a user to the role and then retrieve the news
     * Fail: The correct news it not retrieved
     */
    @Test
    public void testGetRoleNewsByUser() {

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(creator);
        newsItem.setCreated(new Date());
        newsItemRepository.save(newsItem);

        // Create a role for the news to be linked too
        Role role = dataTestUtils.createRole(RoleName.SPECIALTY_ADMIN, RoleType.STAFF);
        NewsLink newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setRole(role);
        newsLink.setNewsItem(newsItem);
        newsLinkRepository.save(newsLink);

        // Create the user and link the group to the user
        User newsUser = dataTestUtils.createUser("NewsUser");
        GroupRole groupRole = new GroupRole();
        groupRole.setUser(newsUser);
        groupRole.setRole(role);
        groupRole.setGroup(dataTestUtils.createGroup("TEST_GROUP"));
        groupRole.setCreator(creator);
        groupRole.setStartDate(new Date());
        groupRoleRepository.save(groupRole);

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);

        Page<NewsItem> newsItems = newsItemRepository.findRoleNewsByUser(newsUser, pageable);

        // Which should get 1 route back and it should be the one that was created
        Assert.assertTrue("There should be 1 news item available", newsItems.getContent().size() == 1);
        Assert.assertTrue("The news item should be the one created", newsItems.getContent().get(0).equals(newsItem));
    }

    /**
     * Test: Create a news item link it to a group and role, link a user to the grouprole and then retrieve the news
     * Fail: The correct news it not retrieved
     */
    @Test
    public void testGetGroupRoleNewsByUser() {

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(creator);
        newsItem.setCreated(new Date());
        newsItemRepository.save(newsItem);

        // Create group and role for the news to be linked too
        Role role = dataTestUtils.createRole(RoleName.GLOBAL_ADMIN, RoleType.PATIENT);
        Group group = dataTestUtils.createGroup("TEST_GROUP");

        NewsLink newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setRole(role);
        newsLink.setGroup(group);
        newsLink.setNewsItem(newsItem);
        NewsLink entityNewsLink = newsLinkRepository.save(newsLink);

        // Create the user and link the group to the user
        User newsUser = dataTestUtils.createUser("NewsUser");
        GroupRole groupRole = new GroupRole();
        groupRole.setUser(newsUser);
        groupRole.setRole(role);
        groupRole.setGroup(group);
        groupRole.setCreator(creator);
        groupRole.setStartDate(new Date());
        groupRoleRepository.save(groupRole);

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);
        Page<NewsItem> newsItems = newsItemRepository.findGroupRoleNewsByUser(newsUser, pageable);

        // Should get 1 route back and it should be the one that was created
        Assert.assertEquals("There should be 1 news item available", 1, newsItems.getContent().size());
        Assert.assertTrue("The news item should be the one created", newsItems.getContent().get(0).equals(newsItem));

        // set newsItem without group and test
        entityNewsLink.setGroup(null);
        newsLinkRepository.save(entityNewsLink);

        newsItems = newsItemRepository.findGroupRoleNewsByUser(newsUser, pageable);

        // Which should get 1 route back and it should be the one that was created
        Assert.assertEquals("There should be 0 news item available", 0, newsItems.getContent().size());
    }

    /**
     * Test: Create a news item link it to a group, link a user to the group and then retrieve the news
     * Fail: The correct news it not retrieved
     */
    @Test
    public void testGetRoleNewsByUser_deleteNewsItemLink() {

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(creator);
        newsItem.setCreated(new Date());
        newsItemRepository.save(newsItem);

        // Create a role for the news to be linked too
        Role role = dataTestUtils.createRole(RoleName.SPECIALTY_ADMIN, RoleType.STAFF);
        NewsLink newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setRole(role);
        newsLink.setNewsItem(newsItem);

        newsItem.setNewsLinks(new HashSet<NewsLink>());
        newsItem.getNewsLinks().add(newsLink);
        NewsItem entityNewsItem = newsItemRepository.save(newsItem);

        Assert.assertTrue("There should be 1 news link", newsItem.getNewsLinks().size() == 1);

        NewsLink tempNewsLink = null;
        for (NewsLink temp : entityNewsItem.getNewsLinks()) {
            tempNewsLink = temp;
        }

        //entityManager.remove(tempNewsLink); // required in service
        entityNewsItem.getNewsLinks().remove(tempNewsLink);
        entityNewsItem = newsItemRepository.save(entityNewsItem);

        Assert.assertTrue("There should be 0 news links", entityNewsItem.getNewsLinks().size() == 0);
    }

    @Test
    public void testGetGroupNewsByUserMultipleGroups() {

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(creator);
        newsItem.setCreated(new Date());
        newsItemRepository.save(newsItem);

        // Create a role for the news to be linked too
        Role role = dataTestUtils.createRole(RoleName.SPECIALTY_ADMIN, RoleType.STAFF);
        NewsLink newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setRole(role);
        newsLink.setNewsItem(newsItem);
        newsLinkRepository.save(newsLink);

        // Create a group for the news to be linked too
        Group group = dataTestUtils.createGroup("TEST_GROUP");
        newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setGroup(group);
        newsLink.setNewsItem(newsItem);
        newsLinkRepository.save(newsLink);

        // Create a second group for the news to be linked too
        Group group2 = dataTestUtils.createGroup("TEST_GROUP_2");
        NewsLink newsLink2 = new NewsLink();
        newsLink2.setCreator(creator);
        newsLink2.setCreated(new Date());
        newsLink2.setGroup(group2);
        newsLink2.setNewsItem(newsItem);
        newsLinkRepository.save(newsLink2);

        // Create the user and link the groups to the user
        User newsUser = dataTestUtils.createUser("NewsUser");
        GroupRole groupRole = new GroupRole();
        groupRole.setUser(newsUser);
        groupRole.setRole(role);
        groupRole.setGroup(group);
        groupRole.setCreator(creator);
        groupRole.setStartDate(new Date());
        groupRoleRepository.save(groupRole);

        GroupRole groupRole2 = new GroupRole();
        groupRole2.setUser(newsUser);
        groupRole2.setRole(role);
        groupRole2.setGroup(group2);
        groupRole2.setCreator(creator);
        groupRole2.setStartDate(new Date());
        groupRoleRepository.save(groupRole2);

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);

        Page<NewsItem> newsItems = newsItemRepository.findGroupNewsByUser(newsUser, pageable);

        // Which should get 1 route back and it should be the one that was created
        Assert.assertEquals("There should be 1 news item available", 1, newsItems.getContent().size());
        Assert.assertTrue("The news item should be the one created", newsItems.getContent().get(0).equals(newsItem));
    }

    @Test
    public void testGetNewsFromSpecialtyGroup() {

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(creator);
        newsItem.setCreated(new Date());
        newsItem.setHeading("HEADING");
        newsItem.setStory("STORY");

        // Create a group for the news to be linked too
        Group group = dataTestUtils.createGroup("TEST_GROUP");
        NewsLink newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setGroup(group);
        newsLink.setNewsItem(newsItem);

        // Create a second group for the news to be linked too
        Group group2 = dataTestUtils.createGroup("TEST_GROUP_2");
        NewsLink newsLink2 = new NewsLink();
        newsLink2.setCreator(creator);
        newsLink2.setCreated(new Date());
        newsLink2.setGroup(group2);
        newsLink2.setNewsItem(newsItem);

        newsItem.setNewsLinks(new HashSet<NewsLink>());
        newsItem.getNewsLinks().add(newsLink);
        newsItem.getNewsLinks().add(newsLink2);

        newsItemRepository.save(newsItem);

        // Create a specialty
        Group specialty = dataTestUtils.createGroup("SPECIALTY");
        specialty.setGroupType(dataTestUtils.createLookup("SPECIALTY", LookupTypes.GROUP));

        // Create parent relationship to TEST_GROUP
        group.setGroupRelationships(new HashSet<GroupRelationship>());
        specialty.setGroupRelationships(new HashSet<GroupRelationship>());

        GroupRelationship groupRelationship = TestUtils.createGroupRelationship(group, specialty,
                RelationshipTypes.PARENT);
        groupRelationship.setId(null);
        groupRelationship.setCreator(creator);
        group.getGroupRelationships().add(groupRelationship);

        groupRelationship = TestUtils.createGroupRelationship(specialty, group, RelationshipTypes.CHILD);
        groupRelationship.setId(null);
        groupRelationship.setCreator(creator);
        specialty.getGroupRelationships().add(groupRelationship);

        groupRepository.save(group);
        groupRepository.save(specialty);

        group = groupRepository.findById(group.getId()).get();
        specialty = groupRepository.findById(specialty.getId()).get();

        Assert.assertEquals("There should be a 1 relationship for TEST_GROUP", 1, group.getGroupRelationships().size());
        Assert.assertEquals("There should be a 1 relationship for SPECIALTY", 1, specialty.getGroupRelationships().size());

        // Create the specialty user and link the specialty to the user
        User specialtyUser = dataTestUtils.createUser("NewsUser");
        GroupRole groupRole = new GroupRole();
        groupRole.setUser(specialtyUser);
        groupRole.setRole(dataTestUtils.createRole(RoleName.SPECIALTY_ADMIN, RoleType.STAFF));
        groupRole.setGroup(specialty);
        groupRole.setCreator(creator);
        groupRole.setStartDate(new Date());
        groupRoleRepository.save(groupRole);

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);
        Page<NewsItem> newsItems = newsItemRepository.findGroupNewsByUser(specialtyUser, pageable);

        // should be no direct news relationship (as news is attached to TEST_GROUP not SPECIALTY)
        Assert.assertEquals("There should be 0 news item available", 0, newsItems.getContent().size());

        // should be a single news item as a result of parent/child relationship between SPECIALTY and TEST_GROUP
        Page<NewsItem> newsItems2 = newsItemRepository.findSpecialtyNewsByUser(specialtyUser, pageable);
        Assert.assertEquals("There should be 1 news item available", 1, newsItems2.getContent().size());
    }

    @Test
    public void testGetCreatorUpdaterNewsByUser() {
        User newsUser = dataTestUtils.createUser("NewsUser");

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(newsUser);
        newsItem.setCreated(new Date());
        newsItemRepository.save(newsItem);

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);

        Page<NewsItem> newsItems = newsItemRepository.findCreatorUpdaterNewsByUser(newsUser, pageable);

        Assert.assertTrue("There should be 1 news item available", newsItems.getContent().size() == 1);
        Assert.assertTrue("The news item should be the one created", newsItems.getContent().get(0).equals(newsItem));
    }

    @Test
    public void testGetCreatorUpdaterNewsByUserAndType() {
        User newsUser = dataTestUtils.createUser("NewsUser");

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(newsUser);
        newsItem.setCreated(new Date());
        newsItem.setNewsType(1);
        newsItemRepository.save(newsItem);

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);

        Page<NewsItem> newsItems = newsItemRepository.findCreatorUpdaterNewsByUserAndType(newsUser, 1, pageable);

        Assert.assertTrue("There should be 1 news item available", newsItems.getContent().size() == 1);
        Assert.assertTrue("The news item should be the one created", newsItems.getContent().get(0).equals(newsItem));
    }
}
