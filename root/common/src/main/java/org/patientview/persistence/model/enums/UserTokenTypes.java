package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 31/07/2017
 */
public enum UserTokenTypes {
    WEB("Web"),
    MOBILE("Mobile");

    private String name;
    UserTokenTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
