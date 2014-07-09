package org.patientview;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
public class Link extends AuditModel {

    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup linkType;

    @ManyToOne
    @JoinColumn(name = "code_id")
    private Code code;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "link")
    private String link;

    @Column(name = "name")
    private String name;

    @Column(name = "display_order" )
    private Integer displayOrder;

    public Lookup getLinkType() {
        return linkType;
    }

    public void setLinkType(Lookup linkType) {
        this.linkType = linkType;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    @JsonIgnore
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @JsonIgnore
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
