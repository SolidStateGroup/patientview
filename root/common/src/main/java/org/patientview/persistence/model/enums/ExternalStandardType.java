package org.patientview.persistence.model.enums;

/**
 * Enum for direct mapping of the pv_external_standard table
 */
public enum ExternalStandardType {
    ICD_10 {
        @Override
        public long id() {
            return 1;
        }

        @Override
        public String code() {
            return "ICD-10";
        }
    },
    SNOMED_CT {
        @Override
        public long id() {
            return 2;
        }

        @Override
        public String code() {
            return "SNOMED-CT";
        }

    };

    public abstract long id();

    public abstract String code();

}
