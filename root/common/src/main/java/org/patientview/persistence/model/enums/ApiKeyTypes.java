package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/03/2016
 */
public enum ApiKeyTypes {
    CKD("Care Know Do"),
    IMPORTER("Importer");

    private String name;
    ApiKeyTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
