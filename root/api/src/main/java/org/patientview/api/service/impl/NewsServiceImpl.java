package org.patientview.api.service.impl;

import org.patientview.api.service.NewsService;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.NewsLink;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.NewsLinkRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.Set;

/**
 * Class to control the crud operations of the News.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Service
public class NewsServiceImpl implements NewsService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private NewsLinkRepository newsLinkRepository;

    @Inject
    private NewsItemRepository newsItemRepository;



    public NewsItem createNewsItem(final NewsItem newsItem) {

        NewsItem persistedNewsItem = newsItemRepository.save(newsItem);

        Set<NewsLink> newsLinks = newsItem.getNewsLinks();

        // Reattach the group or role
        if (!CollectionUtils.isEmpty(newsLinks)) {
            for (NewsLink newsLink : newsLinks) {
                if (newsLink.getGroup() != null) {
                    newsLink.setGroup(groupRepository.findOne(newsLink.getGroup().getId()));
                }

                if (newsLink.getRole() != null) {
                    newsLink.setRole(roleRepository.findOne(newsLink.getRole().getId()));
                }
                newsLink.setNewsItem(persistedNewsItem);
                newsLink.setCreator(userRepository.findOne(1L));
                newsLinkRepository.save(newsLink);
            }
        }

        return persistedNewsItem;
    }

    public NewsItem getNewsItem(final Long newsItemId) {
        return newsItemRepository.findOne(newsItemId);
    }

    public NewsItem saveNewsItem(final NewsItem newsItem) {
        return newsItemRepository.save(newsItem);
    }

    public void deleteNewsItem(final Long newsItemId) {
        newsItemRepository.delete(newsItemId);
    }
}
