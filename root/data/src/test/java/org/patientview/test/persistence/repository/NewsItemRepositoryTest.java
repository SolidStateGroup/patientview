package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.NewsLink;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.NewsLinkRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
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
    GroupRoleRepository groupRoleRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    Lookup lookup;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
        lookup = dataTestUtils.createLookup("TOP", "ROUTE_TYPE", creator);
    }

    /**
     * Test: Create a news item link it to a group, link a user to the group and then retrieve the news
     * Fail: The correct news it not retrieved
     *
     */
    @Test
    public void testGetGroupNewsByUser() {

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(creator);
        newsItem.setCreated(new Date());
        newsItemRepository.save(newsItem);

        // Create a group for the news to be linked too
        Group group = dataTestUtils.createGroup("TEST_GROUP", creator);
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
        groupRole.setRole(dataTestUtils.createRole("TestRole", creator));
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
     * Test: Create a news item link it to a group, link a user to the group and then retrieve the news
     * Fail: The correct news it not retrieved
     *
     */
    @Test
    public void testGetRoleNewsByUser() {

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(creator);
        newsItem.setCreated(new Date());
        newsItemRepository.save(newsItem);

        // Create a role for the news to be linked too
        Role role = dataTestUtils.createRole("TestRole", creator);
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
        groupRole.setGroup(dataTestUtils.createGroup("TEST_GROUP", creator));
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
     * Test: Create a news item link it to a group, link a user to the group and then retrieve the news
     * Fail: The correct news it not retrieved
     *
     */
    @Test
    public void testGetRoleNewsByUser_deleteNewsItemLink() {

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(creator);
        newsItem.setCreated(new Date());
        newsItemRepository.save(newsItem);

        // Create a role for the news to be linked too
        Role role = dataTestUtils.createRole("TestRole", creator);
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
        Role role = dataTestUtils.createRole("TestRole", creator);
        NewsLink newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setRole(role);
        newsLink.setNewsItem(newsItem);
        newsLinkRepository.save(newsLink);

        // Create a group for the news to be linked too
        Group group = dataTestUtils.createGroup("TEST_GROUP", creator);
        newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setGroup(group);
        newsLink.setNewsItem(newsItem);
        newsLinkRepository.save(newsLink);

        // Create a second group for the news to be linked too
        Group group2 = dataTestUtils.createGroup("TEST_GROUP_2", creator);
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

    /*@Test
    public void testGetGroupAndRoleNewsByUser() {

        // Create a news item
        NewsItem newsItem = new NewsItem();
        newsItem.setCreator(creator);
        newsItem.setCreated(new Date());
        newsItemRepository.save(newsItem);

        // Create a role for the news to be linked too
        Role role = dataTestUtils.createRole("TestRole", creator);
        NewsLink newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setRole(role);
        newsLink.setNewsItem(newsItem);
        newsLinkRepository.save(newsLink);

        // Create a group for the news to be linked too
        Group group = dataTestUtils.createGroup("TEST_GROUP", creator);
        newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setGroup(group);
        newsLink.setNewsItem(newsItem);
        newsLinkRepository.save(newsLink);

        // Create a second group for the news to be linked too
        Group group2 = dataTestUtils.createGroup("TEST_GROUP_2", creator);
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

        Page<NewsItem> newsItems = newsItemRepository.findGroupAndRoleNewsByUser(newsUser, pageable);

        // Which should get 1 route back and it should be the one that was created
        Assert.assertEquals("There should be 1 news item available", 1, newsItems.getContent().size());
        Assert.assertTrue("The news item should be the one created", newsItems.getContent().get(0).equals(newsItem));
    }*/
}
