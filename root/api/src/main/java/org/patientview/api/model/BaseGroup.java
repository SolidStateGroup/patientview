package org.patientview.api.model;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 27/08/2014
 */
public class BaseGroup {

    private Long id;
    private String name;
    private String shortName;
    private String code;


    public BaseGroup() {

    }

    public BaseGroup(org.patientview.persistence.model.Group group) {
        setCode(group.getCode());
        setId(group.getId());
        setName(group.getName());
        setShortName(group.getShortName());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

}
