package org.patientview.persistence.model.enums;

/**
 * Codelist types for Immunisation
 */
public enum ImmunisationCodelist {
    MMR("MMR"),
    PNEUMOCCAL("Pneumoccal"),
    ROTAVIRUS("Rotavirus"),
    MEN_B("MenB"),
    MEN_ASWY("MenACWY"),
    VERICELLA("Varicella"),
    HIB_MENC("Hib/MenC"),
    FLU("Flu"),
    HPV("HPV");

    private String name;
    ImmunisationCodelist(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
