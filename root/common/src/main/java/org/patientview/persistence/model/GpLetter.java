package org.patientview.persistence.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Entity
@Table(name = "pv_gp_letter")
public class GpLetter  extends BaseModel {

    @Column(name = "claimed_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date claimedDate;

    @Column(name = "claimed_email")
    private String claimedEmail;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "claimed_group")
    private Group claimedGroup;

    @Column(name = "claimed_practice_code")
    private String claimedPracticeCode;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @Column(name = "gp_address1")
    private String gpAddress1;

    @Column(name = "gp_address2")
    private String gpAddress2;

    @Column(name = "gp_address3")
    private String gpAddress3;

    @Column(name = "gp_address4")
    private String gpAddress4;

    @Column(name = "gp_name")
    private String gpName;

    @Column(name = "gp_postcode", nullable = false)
    private String gpPostcode;

    @Column(name = "letter_content")
    private String letterContent;

    @Column(name = "patient_identifier")
    private String patientIdentifier;

    @Column(name = "patient_forename")
    private String patientForename;

    @Column(name = "patient_surname")
    private String patientSurname;

    @Column(name = "patient_date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date patientDateOfBirth;

    @Column(name = "signup_key")
    private String signupKey;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "source_group")
    private Group sourceGroup;

    public GpLetter() { }

    public Date getClaimedDate() {
        return claimedDate;
    }

    public void setClaimedDate(Date claimedDate) {
        this.claimedDate = claimedDate;
    }

    public String getClaimedEmail() {
        return claimedEmail;
    }

    public void setClaimedEmail(String claimedEmail) {
        this.claimedEmail = claimedEmail;
    }

    public Group getClaimedGroup() {
        return claimedGroup;
    }

    public void setClaimedGroup(Group claimedGroup) {
        this.claimedGroup = claimedGroup;
    }

    public String getClaimedPracticeCode() {
        return claimedPracticeCode;
    }

    public void setClaimedPracticeCode(String claimedPracticeCode) {
        this.claimedPracticeCode = claimedPracticeCode;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getGpAddress1() {
        return gpAddress1;
    }

    public void setGpAddress1(String gpAddress1) {
        this.gpAddress1 = gpAddress1;
    }

    public String getGpAddress2() {
        return gpAddress2;
    }

    public void setGpAddress2(String gpAddress2) {
        this.gpAddress2 = gpAddress2;
    }

    public String getGpAddress3() {
        return gpAddress3;
    }

    public void setGpAddress3(String gpAddress3) {
        this.gpAddress3 = gpAddress3;
    }

    public String getGpAddress4() {
        return gpAddress4;
    }

    public void setGpAddress4(String gpAddress4) {
        this.gpAddress4 = gpAddress4;
    }

    public String getGpName() {
        return gpName;
    }

    public void setGpName(String gpName) {
        this.gpName = gpName;
    }

    public String getGpPostcode() {
        return gpPostcode;
    }

    public void setGpPostcode(String gpPostcode) {
        this.gpPostcode = gpPostcode;
    }

    public String getLetterContent() {
        return letterContent;
    }

    public void setLetterContent(String letterContent) {
        this.letterContent = letterContent;
    }

    public String getPatientForename() {
        return patientForename;
    }

    public void setPatientForename(String patientForename) {
        this.patientForename = patientForename;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getPatientSurname() {
        return patientSurname;
    }

    public void setPatientSurname(String patientSurname) {
        this.patientSurname = patientSurname;
    }

    public Date getPatientDateOfBirth() {
        return patientDateOfBirth;
    }

    public void setPatientDateOfBirth(Date patientDateOfBirth) {
        this.patientDateOfBirth = patientDateOfBirth;
    }

    public String getSignupKey() {
        return signupKey;
    }

    public void setSignupKey(String signupKey) {
        this.signupKey = signupKey;
    }

    public Group getSourceGroup() {
        return sourceGroup;
    }

    public void setSourceGroup(Group sourceGroup) {
        this.sourceGroup = sourceGroup;
    }
}
