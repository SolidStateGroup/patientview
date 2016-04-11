package org.patientview.api.model;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 16/03/2016
 */
public class Lookup {
    private Long id;
    private String value;
    private String description;
    private String descriptionFriendly;
    private Long displayOrder;

    public Lookup(org.patientview.persistence.model.Lookup lookup) {
        this.id = lookup.getId();
        this.value = lookup.getValue();
        this.description = lookup.getDescription();
        this.descriptionFriendly = lookup.getDescriptionFriendly();
        this.displayOrder = lookup.getDisplayOrder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionFriendly() {
        return descriptionFriendly;
    }

    public void setDescriptionFriendly(String descriptionFriendly) {
        this.descriptionFriendly = descriptionFriendly;
    }

    public Long getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Long displayOrder) {
        this.displayOrder = displayOrder;
    }
}
