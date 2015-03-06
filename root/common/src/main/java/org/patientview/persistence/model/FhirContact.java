package org.patientview.persistence.model;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/11/2014
 *
 * Used for FHIR telecom objects, e.g. mobile/work telephone numbers
 */
public class FhirContact extends BaseModel {

    // phone, fax etc
    private String system;

    // mobile, home, work etc
    private String use;

    private String value;

    public FhirContact() {
    }

    public FhirContact(String system, String use, String value) {
        this.system = system;
        this.use = use;
        this.value = value;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
