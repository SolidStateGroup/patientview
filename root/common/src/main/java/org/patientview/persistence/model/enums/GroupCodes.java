package org.patientview.persistence.model.enums;

/**
 * Group codes are absolute and stored dynamically in database during creation.
 * <p>
 * This enums reflects all group codes currently for only top level group type SPECIALTY
 * Because of legacy naming some of the code are not following convention e.g. Renal hence
 * cannot be used as enum
 */
public enum GroupCodes {

    IBD("IBD"),
    RD("Renal Donor");

    private String name;

    GroupCodes(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.name();
    }
}
