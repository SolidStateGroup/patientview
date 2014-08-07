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
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.NewsLinkRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.Iterator;

/**
 * Tests concerned with retrieving the correct news for a user.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class NewsRepositoryTest {

    @Inject
    NewsItemRepository newsItemRepository;

    @Inject
    NewsLinkRepository newsLinkRepository;

    @Inject
    GroupRoleRepository groupRoleRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    Lookup lookup;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
        lookup = dataTestUtils.createLookup("TOP", LookupTypes.IDENTIFIER, creator);

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

        Iterable<NewsItem> newsItems = newsItemRepository.findGroupNewsByUser(newsUser);
        Iterator<NewsItem> iterator = newsItems.iterator();
        // Which should get 1 route back and it should be the one that was created
        Assert.assertTrue("There should be 1 new item available", iterator.hasNext());
        Assert.assertTrue("The news item should be the one created", iterator.next().equals(newsItem));

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

        // Create a group for the news to be linked too
        Role role = dataTestUtils.createRole("TestRole", creator);
        NewsLink newsLink = new NewsLink();
        newsLink.setCreator(creator);
        newsLink.setCreated(new Date());
        newsLink.setRole(role);
        newsLink.setNewsItem(newsItem);
        newsLinkRepository.save(newsLink);

        // Create the user and link the group to the usser
        User newsUser = dataTestUtils.createUser("NewsUser");
        GroupRole groupRole = new GroupRole();
        groupRole.setUser(newsUser);
        groupRole.setRole(role);
        groupRole.setGroup(dataTestUtils.createGroup("TEST_GROUP", creator));
        groupRole.setCreator(creator);
        groupRole.setStartDate(new Date());
        groupRoleRepository.save(groupRole);

        Iterable<NewsItem> newsItems = newsItemRepository.findRoleNewsByUser(newsUser);
        Iterator<NewsItem> iterator = newsItems.iterator();
        // Which should get 1 route back and it should be the one that was created
        Assert.assertTrue("There should be 1 new item available", iterator.hasNext());
        Assert.assertTrue("The news item should be the one created", iterator.next().equals(newsItem));

    }


}
