package org.patientview.persistence.model.enums;

/**
 * Enum for relapse medication's route types
 */
public enum MedicationRouteTypes {
    ORAL("Oral", "26643006", "Oral"),
    IV("IV", "47625008", "Intravenous (IV)"),
    IM("IM", "78421000", "Intramuscular (IM)");

    private String name;
    private String code;
    private String description;

    MedicationRouteTypes(String name, String code, String description) {
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public String getId() {
        return this.name();
    }
}
