package org.patientview.persistence.model.enums;

/**
 * PathwayTypes enumerator denotes pathway type
 * for org.patientview.persistence.model.Pathway
 */
public enum PathwayTypes {
    DONORPATHWAY("Donor Pathway");

    private String name;

    PathwayTypes(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.name();
    }
}
