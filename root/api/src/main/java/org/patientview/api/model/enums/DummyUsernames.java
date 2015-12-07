package org.patientview.api.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/11/2015
 */
public enum DummyUsernames {
    PATIENTVIEW_NOTIFICATIONS("patientviewnotifications");

    private String name;
    DummyUsernames(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
