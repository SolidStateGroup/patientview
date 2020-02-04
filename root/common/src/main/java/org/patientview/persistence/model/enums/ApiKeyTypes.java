package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/03/2016
 */
public enum ApiKeyTypes {
    CKD("Care Know Do"),
    EXTERNAL_CONVERSATION("External Conversation"),
    EXTERNAL_AUDIT("External Audit"),
    IMPORTER("Importer"),
    PATIENT("Patient Api Key");

    private String name;
    ApiKeyTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
