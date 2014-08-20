package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.impl.NewsServiceImpl;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.NewsLink;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.model.User;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/**
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
public class NewsServiceTest {

    User creator;

    @Mock
    NewsItemRepository newsItemRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    RoleRepository roleRepository;

    @InjectMocks
    NewsService newsService = new NewsServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    /**
     * Test: To see if the news is returned for a user
     * Fail: The calls to the repository are not made, not the right number, not in right order
     */
    @Test
    public void testGetNewsByUser() {
        Pageable pageableAll = new PageRequest(0, Integer.MAX_VALUE);
        User testUser = TestUtils.createUser("testUser");
        Group testGroup = TestUtils.createGroup("testGroup");

        List<NewsItem> roleNews = new ArrayList<>();

        for (int i=0;i<10;i++) {
            NewsItem newsItem = new NewsItem();
            newsItem.setId((long)i);
            newsItem.setCreator(testUser);
            newsItem.setHeading(String.valueOf(i));
            newsItem.setStory("ROLE NEWS STORY TEXT " + String.valueOf(i));
            newsItem.setNewsLinks(new HashSet<NewsLink>());
            newsItem.setCreated(new Date(System.currentTimeMillis() + i));

            NewsLink newsLink = new NewsLink();
            newsLink.setId((long)i);
            newsLink.setNewsItem(newsItem);
            newsLink.setGroup(testGroup);
            roleNews.add(newsItem);
        }

        List<NewsItem> groupNews = new ArrayList<>();

        for (int i=0;i<5;i++) {
            NewsItem newsItem = new NewsItem();
            newsItem.setId((long)(i+10));
            newsItem.setCreator(testUser);
            newsItem.setHeading(String.valueOf(i));
            newsItem.setStory("GROUP NEWS STORY TEXT " + String.valueOf(i));
            newsItem.setNewsLinks(new HashSet<NewsLink>());
            newsItem.setCreated(new Date(System.currentTimeMillis() + 10 + i));

            NewsLink newsLink = new NewsLink();
            newsLink.setId((long)(i+10));
            newsLink.setNewsItem(newsItem);
            newsLink.setGroup(testGroup);
            roleNews.add(newsItem);
        }

        when(userRepository.findOne(Matchers.anyLong())).thenReturn(testUser);
        when(newsItemRepository.findGroupNewsByUser(eq(testUser),
                eq(pageableAll))).thenReturn(new PageImpl<>(roleNews));
        when(newsItemRepository.findRoleNewsByUser(eq(testUser),
                eq(pageableAll))).thenReturn(new PageImpl<>(groupNews));

        try {
            Page<NewsItem> newsItems = newsService.findByUserId(testUser.getId(), new PageRequest(0, 10));

            Assert.assertEquals("Should have 10 news items total", 10, newsItems.getNumberOfElements());
            Assert.assertTrue("Should be ordered by creation date descending",
                    newsItems.getContent().get(0).getCreated().after(newsItems.getContent().get(1).getCreated()));

            verify(newsItemRepository, Mockito.times(1)).findGroupNewsByUser(Matchers.eq(testUser),
                    Matchers.eq(pageableAll));
            verify(newsItemRepository, Mockito.times(1)).findRoleNewsByUser(Matchers.eq(testUser),
                    Matchers.eq(pageableAll));
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException thrown");
        }
    }


    @Test
    public void testGetNewsByUserEditOrDelete() {
        Pageable pageableAll = new PageRequest(0, Integer.MAX_VALUE);

        // create UNIT_ADMIN of testGroup
        User testUser = TestUtils.createUser("testUser");
        Group testGroup = TestUtils.createGroup("testGroup");
        Group testGroup2 = TestUtils.createGroup("testGroup2");
        Role unitAdminRole = TestUtils.createRole(RoleName.UNIT_ADMIN);
        org.patientview.persistence.model.RoleType roleType = new org.patientview.persistence.model.RoleType();
        roleType.setValue(RoleType.STAFF);
        unitAdminRole.setRoleType(roleType);
        GroupRole groupRole = TestUtils.createGroupRole(unitAdminRole, testGroup, testUser);
        testUser.getGroupRoles().add(groupRole);

        // create 2 news items, one with link to testUser group exclusively, the other with other newsLinks
        NewsItem newsItem1 = TestUtils.createNewsItem("HEADING1", "STORY1");
        NewsLink newsLink1 = TestUtils.createNewsLink(newsItem1, testGroup, null);
        newsItem1.getNewsLinks().add(newsLink1);
        newsItem1.setCreated(new Date(System.currentTimeMillis() + 1));

        NewsItem newsItem2 = TestUtils.createNewsItem("HEADING2", "STORY2");
        NewsLink newsLink2 = TestUtils.createNewsLink(newsItem2, testGroup, null);
        NewsLink newsLink3 = TestUtils.createNewsLink(newsItem2, testGroup2, null);
        newsItem2.getNewsLinks().add(newsLink2);
        newsItem2.getNewsLinks().add(newsLink3);
        newsItem2.setCreated(new Date(System.currentTimeMillis() + 2));

        List<NewsItem> groupNews = new ArrayList<>();
        groupNews.add(newsItem1);
        groupNews.add(newsItem2);

        when(userRepository.findOne(Matchers.anyLong())).thenReturn(testUser);
        when(newsItemRepository.findGroupNewsByUser(eq(testUser),
                eq(pageableAll))).thenReturn(new PageImpl<>(groupNews));

        try {
            Page<NewsItem> newsItems = newsService.findByUserId(testUser.getId(), new PageRequest(0, 10));

            Assert.assertEquals("Should have 2 news items total", 2, newsItems.getNumberOfElements());
            Assert.assertTrue("Should be ordered by creation date descending",
                    newsItems.getContent().get(0).getCreated().after(newsItems.getContent().get(1).getCreated()));

            NewsItem returnedNewsItem1 = newsItems.getContent().get(0);
            NewsItem returnedNewsItem2 = newsItems.getContent().get(1);

            Assert.assertEquals("1st newsItem should be STORY2", "STORY2", returnedNewsItem1.getStory());
            Assert.assertEquals("STORY2 should have 2 newsLinks", 2, returnedNewsItem1.getNewsLinks().size());
            Assert.assertEquals("STORY2 should have edit = true", true, returnedNewsItem1.isEdit());
            Assert.assertEquals("STORY1 should have edit = true", true, returnedNewsItem2.isEdit());

            verify(newsItemRepository, Mockito.times(1)).findGroupNewsByUser(Matchers.eq(testUser),
                    Matchers.eq(pageableAll));
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException thrown");
        }
    }

    @Test
    public void testAddGroupAndRole() {
        User user = TestUtils.createUser("testUser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);

        NewsItem newsItem = new NewsItem();
        newsItem.setId(3L);
        newsItem.setCreator(user);
        newsItem.setHeading("HEADING TEXT");
        newsItem.setStory("NEWS STORY TEXT");
        newsItem.setNewsLinks(new HashSet<NewsLink>());

        TestUtils.authenticateTest(user, Collections.EMPTY_LIST);

        when(newsItemRepository.save(eq(newsItem))).thenReturn(newsItem);
        newsItem = newsService.add(newsItem);
        verify(newsItemRepository, Mockito.times(1)).save(Matchers.eq(newsItem));

        when(newsItemRepository.findOne(Matchers.anyLong())).thenReturn(newsItem);
        when(groupRepository.findOne(Matchers.anyLong())).thenReturn(group);
        when(roleRepository.findOne(Matchers.anyLong())).thenReturn(role);

        try {
            newsService.addGroupAndRole(newsItem.getId(), 5L, 6L);
            verify(newsItemRepository, Mockito.times(2)).save(Matchers.eq(newsItem));
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException: " + rnf.getMessage());
        }
    }

    @Test
    public void testCreateNewsItem() {
        User user = TestUtils.createUser("testUser");

        NewsItem newsItem = new NewsItem();
        newsItem.setId(3L);
        newsItem.setCreator(user);
        newsItem.setHeading("HEADING TEXT");
        newsItem.setStory("NEWS STORY TEXT");
        newsItem.setNewsLinks(new HashSet<NewsLink>());

        NewsLink newsLink = new NewsLink();
        newsLink.setId(4L);
        newsLink.setNewsItem(newsItem);
        newsLink.setGroup(TestUtils.createGroup("testGroup"));

        TestUtils.authenticateTest(user, Collections.EMPTY_LIST);

        when(newsItemRepository.save(eq(newsItem))).thenReturn(newsItem);
        newsService.add(newsItem);
        verify(newsItemRepository, Mockito.times(1)).save(Matchers.eq(newsItem));
    }

    @Test
    public void testUpdateNewsItem() {
        User user = TestUtils.createUser("testUser");

        NewsItem newsItem = new NewsItem();
        newsItem.setId(3L);
        newsItem.setCreator(user);
        newsItem.setHeading("HEADING TEXT");
        newsItem.setStory("NEWS STORY TEXT");
        newsItem.setNewsLinks(new HashSet<NewsLink>());

        NewsLink newsLink = new NewsLink();
        newsLink.setId(4L);
        newsLink.setNewsItem(newsItem);
        newsLink.setGroup(TestUtils.createGroup("testGroup"));

        TestUtils.authenticateTest(user, Collections.EMPTY_LIST);

        when(newsItemRepository.save(eq(newsItem))).thenReturn(newsItem);
        when(newsItemRepository.findOne(Matchers.anyLong())).thenReturn(newsItem);

        newsService.add(newsItem);

        try {
            newsItem.setHeading("HEADING TEXT UPDATED");
            newsService.save(newsItem);
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException");
        }

        verify(newsItemRepository, Mockito.times(2)).save(Matchers.eq(newsItem));
    }
}
