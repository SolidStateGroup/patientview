package org.patientview.persistence.model;

/**
 * Base object for all API import objects
 * Created by jamesr@solidstategroup.com
 * Created on 02/03/2016
 */
public class BaseImport {

    private String identifier;
    private String groupCode;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }
}
