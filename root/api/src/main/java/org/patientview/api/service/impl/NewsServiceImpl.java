package org.patientview.api.service.impl;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.NewsService;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.NewsLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.NewsLinkRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to control the crud operations of the News.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Service
public class NewsServiceImpl extends AbstractServiceImpl<NewsServiceImpl> implements NewsService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private NewsItemRepository newsItemRepository;

    public NewsItem add(final NewsItem newsItem) {
        if (!CollectionUtils.isEmpty(newsItem.getNewsLinks())) {
            for (NewsLink newsLink : newsItem.getNewsLinks()) {
                if (newsLink.getGroup() != null) {
                    newsLink.setGroup(groupRepository.findOne(newsLink.getGroup().getId()));
                }

                if (newsLink.getRole() != null) {
                    newsLink.setRole(roleRepository.findOne(newsLink.getRole().getId()));
                }
                newsLink.setNewsItem(newsItem);
                newsLink.setCreator(userRepository.findOne(1L));
            }
        }
        NewsItem persistedNewsItem = newsItemRepository.save(newsItem);

        return persistedNewsItem;
    }

    public NewsItem get(final Long newsItemId) {
        return newsItemRepository.findOne(newsItemId);
    }

    public NewsItem save(final NewsItem newsItem) {
        return newsItemRepository.save(newsItem);
    }

    public void delete(final Long newsItemId) {
        newsItemRepository.delete(newsItemId);
    }

    public Page<NewsItem> findByUserId(Long userId, Pageable pageable)  throws ResourceNotFoundException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException("Could not find user {}" + userId);
        }

        // get both role and group news
        PageRequest pageableAll = new PageRequest(0, Integer.MAX_VALUE);
        Page<NewsItem> roleNews = newsItemRepository.findRoleNewsByUser(entityUser, pageableAll);
        Page<NewsItem> groupNews = newsItemRepository.findGroupNewsByUser(entityUser, pageableAll);

        // combine results
        Set<NewsItem> newsItemSet = new HashSet<>();
        if (roleNews.getNumberOfElements() > 0) {
            newsItemSet.addAll(roleNews.getContent());
        }
        if (groupNews.getNumberOfElements() > 0) {
            newsItemSet.addAll(groupNews.getContent());
        }
        List<NewsItem> newsItems = new ArrayList<>(newsItemSet);

        // sort combined list
        Collections.sort(newsItems);

        // manually do pagination
        int left = pageable.getOffset();
        int right = (left + pageable.getPageSize()) > newsItems.size() ? newsItems.size() : pageable.getPageSize();
        List<NewsItem> pagedNewsItems = new ArrayList<>();

        if (!newsItems.isEmpty()) {
            pagedNewsItems = newsItems.subList(left, right);
        }

        return new PageImpl<>(pagedNewsItems, pageable, newsItems.size());
    }
}
