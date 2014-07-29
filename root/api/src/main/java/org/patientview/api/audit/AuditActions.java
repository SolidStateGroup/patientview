package org.patientview.api.audit;

/**
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
public enum AuditActions {

    CREATE_USER("Create User"), EDIT_USER("Edit User"), CHANGE_PASSWORD("Change Password"), DELETE_USER("Delete User");

    private String name;
    AuditActions(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
