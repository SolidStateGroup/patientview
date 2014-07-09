package org.patientview;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
public class NewsItem extends AuditModel {

    @Column(name = "heading")
    private String heading;

    @Column(name = "story")
    private String story;

    @OneToMany(mappedBy = "newsItem", cascade = CascadeType.REMOVE)
    private Set<NewsLink> newsLinks;

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
}
