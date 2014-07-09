package org.patientview;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Object to link an item of news to Group and Roles.
 *
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
public class NewsLink extends SimpleAuditModel {

    @OneToOne
    @JoinColumn(name = "news_id")
    private NewsItem newsItem;

    @OneToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @JsonIgnore
    public NewsItem getNewsItem() {
        return newsItem;
    }

    public void setNewsItem(final NewsItem newsItem) {
        this.newsItem = newsItem;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(final Role role) {
        this.role = role;
    }
}
