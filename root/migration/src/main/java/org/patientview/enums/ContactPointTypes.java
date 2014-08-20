package org.patientview.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/07/2014
 */
public enum ContactPointTypes {

    UNIT_WEB_ADDRESS("Unit Web Address"),
    TRUST_WEB_ADDRESS("Trust Web Address"),
    PV_ADMIN_NAME("PatientView Admin Name"),
    PV_ADMIN_PHONE("PatientView Admin Phone Number"),
    PV_ADMIN_EMAIL("PatientView Admin Email"),
    UNIT_ENQUIRIES_PHONE("Unit Enquiries Phone Number"),
    UNIT_ENQUIRIES_EMAIL("Unit Enquiries Email"),
    APPOINTMENT_PHONE("Appointment Phone Number"),
    APPOINTMENT_EMAIL("Appointment Email"),
    OUT_OF_HOURS_INFO("Out of Hours Information");

    private String name;
    ContactPointTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }


}
