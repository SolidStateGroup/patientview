package org.patientview.api.model;

import java.util.Date;

/**
 * NewsLink, representing the visibility of a NewsItem to Users in specific Groups and Roles.
 * Created by jamesr@solidstategroup.com
 * Created on 14/10/2014
 */
public class NewsLink {

    private Long id;
    private Date created;
    private BaseGroup group;
    private Role role;
    private boolean edit;
    private boolean delete;

    public NewsLink() {
    }

    public NewsLink(org.patientview.persistence.model.NewsLink newsLink) {
        setId(newsLink.getId());
        setCreated(newsLink.getCreated());
        if (newsLink.getGroup() != null) {
            setGroup(new BaseGroup(newsLink.getGroup()));
        }
        if (newsLink.getRole() != null) {
            setRole(new Role(newsLink.getRole()));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public BaseGroup getGroup() {
        return group;
    }

    public void setGroup(BaseGroup group) {
        this.group = group;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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
}
