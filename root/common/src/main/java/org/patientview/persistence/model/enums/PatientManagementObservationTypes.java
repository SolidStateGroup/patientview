package org.patientview.persistence.model.enums;

/**
 * added for IBD patient management, must be included in NonTestObservationTypes
 * Created by jamesr@solidstategroup.com
 * Created on 22/03/2016
 */
public enum PatientManagementObservationTypes {
    IBD_SURGERYMAINPROCEDURE("Surgery Main Procedure"),
    SURGERY_HOSPITAL_CODE("Surgery Hospital Code"),
    SURGERY_OTHER_DETAILS("Surgery Other Details"),
    GENDER("Gender"),
    IBD_CROHNSLOCATION("Crohns Location"),
    IBD_CROHNSPROXIMALTERMINALILEUM("Crohns Proximal Terminal Ileum"),
    IBD_CROHNSPERIANAL("Crohns Perianal"),
    IBD_CROHNSBEHAVIOUR("Crohns Behaviour"),
    IBD_UCEXTENT("Ulcerative Colitis Extent"),
    IBD_EGIMCOMPLICATION("EGIM Complication"),
    IBD_EGIMCOMPLICATIONSOTHER("Other EGIM Complications"),
    IBD_SMOKINGSTATUS("Smoking Status"),
    IBD_FAMILYHISTORY("Family History"),
    IBD_ALLERGYSUBSTANCE("Allergy Substance"),
    IBD_VACCINATIONRECORD("Vaccination Record"),
    IBD_COLONOSCOPYSURVEILLANCE("Year For Colonoscopy Surveillance");

    private String name;
    PatientManagementObservationTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
