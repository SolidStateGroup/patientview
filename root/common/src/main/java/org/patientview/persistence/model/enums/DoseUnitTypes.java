package org.patientview.persistence.model.enums;

/**
 * Enum for relapse medication's dose units types
 */
public enum DoseUnitTypes {
    MG("Mg", "258685003", "microgram"),
    G("g", "258682000", "gram"),
    IU("iU", "258997004", "international unit");

    private String name;
    private String code;
    private String description;

    DoseUnitTypes(String name, String code, String description) {
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return this.name();
    }
}
