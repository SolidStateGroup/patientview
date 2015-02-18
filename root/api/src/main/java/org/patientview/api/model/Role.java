package org.patientview.api.model;

import org.patientview.persistence.model.BaseModel;
import org.patientview.persistence.model.enums.RoleName;

/**
 * Role, representing a single Role, such as PATIENT or UNIT_ADMIN.
 * Created by jamesr@solidstategroup.com
 * Created on 28/08/2014
 */
public class Role extends BaseModel {

    private RoleName name;
    private String description;
    private Boolean visible;

    public Role() {
    }

    public Role(org.patientview.persistence.model.Role role) {
        setId(role.getId());
        setName(role.getName());
        setDescription(role.getDescription());
        setVisible(role.getVisible());
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
