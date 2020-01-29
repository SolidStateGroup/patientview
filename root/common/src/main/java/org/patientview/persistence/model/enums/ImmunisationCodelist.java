package org.patientview.persistence.model.enums;

/**
 * Enum Codelist types for Immunisation records
 */
public enum ImmunisationCodelist {
    OTHER("Other", "127785005", "other"),
    MMR("MMR", "38598009", "Measles-mumps-rubella vaccination"),
    PNEUMOCCAL("Pneumoccal", "12866006", "Pneumococcal vaccination"),
    ROTAVIRUS("Rotavirus", "415354003", "Rotavirus vaccination"),
    MEN_B("MenB", "720537002", "Meningitis B vaccination"),
    MEN_ASWY("MenACWY", "390892002", "Meningitis ACW & Y vaccination"),
    VERICELLA("Varicella", "571611000119101", "Administration of varicella live vaccine"),
    HIB_MENC("Hib/MenC", "428975001", "Vaccination with Haemophilus influenzae type B and Neisseria meningitidis serotype C combination vaccine"),
    FLU("Flu", "86198006", "Influenza vaccination"),
    HPV("HPV", "428570002", "Vaccination for human papillomavirus");

    private String name;
    private String code;
    private String description;

    ImmunisationCodelist(String name, String code, String description) {
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.name();
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
