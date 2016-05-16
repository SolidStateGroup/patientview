package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/08/2014
 *
 * Observation types used to reference non test/result Observation records e.g. blood group, smoking history
 * Testcode.java contains other test codes generated from patientview.xsd e.g. ciclosporin, weight
 */
public enum NonTestObservationTypes {
    BLOOD_GROUP("Blood Group"),
    BMD_EXAM("BMD Exam"), // my IBD
    BODY_PARTS_AFFECTED("Body Parts Affected"), // my IBD
    COLONOSCOPY_SURVEILLANCE("Colonoscopy Surveillance"), // my IBD
    DPPULSE("Foot Checkup: dppulse"),
    FAMILY_HISTORY("Family History"), // my IBD
    IBD_DISEASE_COMPLICATIONS("IBD Disease Complications"), // my IBD
    IBD_DISEASE_EXTENT("IBD Disease Extent"), // my IBD
    IBD_EI_MANIFESTATIONS("IBD EI Manifestations"), // my IBD
    MGRADE("Eye Checkup: mgrade"),
    PTPULSE("Foot Checkup: ptpulse"),
    RGRADE("Eye Checkup: rgrade"),
    SMOKING_HISTORY("Smoking History"), // my IBD
    SURGICAL_HISTORY("Surgical History"), // my IBD
    VA("Eye Checkup: va"),
    VACCINATION_RECORD("Vaccination Record"), // my IBD

    // added for IBD patient management, see PatientManagementObservationTypes
    IBD_SURGERYMAINPROCEDURE("Surgery Main Procedure"),
    SURGERY_HOSPITAL_CODE("Surgery Hospital Code"),
    SURGERY_OTHER_DETAILS("Surgery Other Details"),
    GENDER("Gender"),
    IBD_CROHNSLOCATION("Location"),
    IBD_CROHNSPROXIMALTERMINALILEUM("Disease Proximal to Terminal Ileum"),
    IBD_CROHNSPERIANAL("Perianal"),
    IBD_CROHNSBEHAVIOUR("Complications"),
    IBD_UCEXTENT("Location"),
    IBD_EGIMCOMPLICATION("EGIM Complication"),
    IBD_EGIMCOMPLICATIONSOTHER("Other EGIM Complications"),
    IBD_SMOKINGSTATUS("Smoking Status"),
    IBD_FAMILYHISTORY("Family History"),
    IBD_ALLERGYSUBSTANCE("Allergy Substance"),
    IBD_VACCINATIONRECORD("Vaccination Record"),
    IBD_COLONOSCOPYSURVEILLANCE("Year For Colonoscopy Surveillance");

    private String name;
    NonTestObservationTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
