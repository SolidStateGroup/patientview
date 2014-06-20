package org.patientview.api.service;

import org.patientview.persistence.model.NewsItem;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface NewsService {

    /**
     * Create and item of news but reattach the group/roles/user
     * associated with the news
     *
     * @param newsItem
     * @return
     */
    NewsItem createNewsItem(NewsItem newsItem);

    NewsItem getNewsItem(Long newsItemId);

    NewsItem saveNewsItem(NewsItem newsItem);

    void deleteNewsItem(Long newsItemId);
}
