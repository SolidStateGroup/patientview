package org.patientview;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */

public class Code extends AuditModel {

    @Column(name = "code")
    private String code;

    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup codeType;

    @Column(name = "display_order" )
    private Integer displayOrder;

    @Column(name = "description")
    private String description;

    @OneToOne
    @JoinColumn(name = "standard_type_id")
    private Lookup standardType;

    @OneToMany(mappedBy = "code", cascade = CascadeType.REMOVE)
    private Set<Link> links;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Lookup getCodeType() {
        return codeType;
    }

    public void setCodeType(Lookup codeType) {
        this.codeType = codeType;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Lookup getStandardType() {
        return standardType;
    }

    public void setStandardType(Lookup standardType) {
        this.standardType = standardType;
    }

    public Set<Link> getLinks() {
        return links;
    }

    public void setLinks(Set<Link> links) {
        this.links = links;
    }
}
