package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Entity
@Table(name = "pv_news_item")
public class NewsItem extends BaseModel implements Editable {

    @Column(name = "heading")
    private String heading;

    @Column(name = "story")
    private String story;

    @OneToMany(mappedBy = "newsItem", cascade = {CascadeType.ALL})
    private Set<NewsLink> newsLinks;

    @Column(name = "last_update_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by")
    private User lastUpdater;

    // need created date for UI unlike AuditModel based objects
    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @OneToOne
    @JoinColumn(name = "created_by")
    private User creator;

    @Transient
    private boolean edit;

    @Transient
    private boolean delete;

    public String getHeading() {
        return heading;
    }

    public void setHeading(final String heading) {
        this.heading = heading;
    }

    public String getStory() {
        return story;
    }

    public void setStory(final String story) {
        this.story = story;
    }

    public Set<NewsLink> getNewsLinks() {
        return newsLinks;
    }

    public void setNewsLinks(final Set<NewsLink> newsLinks) {
        this.newsLinks = newsLinks;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public User getLastUpdater() {
        return lastUpdater;
    }

    public void setLastUpdater(User lastUpdater) {
        this.lastUpdater = lastUpdater;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @JsonIgnore
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public int compareTo(Object object) {
        NewsItem newsItem;

        if (object == null) {
            return 1;
        } else {
            newsItem = (NewsItem) object;
        }

        if (newsItem.getCreated().before(this.getCreated())) {
            return -1;
        } else {
            return 1;
        }
    }
}
