package org.patientview.persistence.model.enums;

/**
 * added for IBD patient management surgeries, must be included in PatientManagementObservationTypes
 * Created by jamesr@solidstategroup.com
 * Created on 22/03/2016
 */
public enum SurgeryObservationTypes {
    SURGERY_HOSPITAL_CODE("Surgery Hospital Code"),
    SURGERY_OTHER_DETAILS("Surgery Other Details");

    private String name;
    SurgeryObservationTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
