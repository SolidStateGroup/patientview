package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.GpCountries;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Entity
@Table(name = "pv_gp_master")
public class GpMaster extends AuditModel {

    @Column(name = "practice_code", nullable = false)
    private String practiceCode;

    @Column(name = "practice_name", nullable = false)
    private String practiceName;

    @Column(name = "address_1")
    private String address1;

    @Column(name = "address_2")
    private String address2;

    @Column(name = "address_3")
    private String address3;

    @Column(name = "address_4")
    private String address4;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "country", nullable = false)
    @Enumerated(EnumType.STRING)
    private GpCountries country;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "status_code")
    private String statusCode;

    public GpMaster() {
    }

    public String getPracticeCode() {
        return practiceCode;
    }

    public void setPracticeCode(String practiceCode) {
        this.practiceCode = practiceCode;
    }

    public String getPracticeName() {
        return practiceName;
    }

    public void setPracticeName(String practiceName) {
        this.practiceName = practiceName;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getAddress4() {
        return address4;
    }

    public void setAddress4(String address4) {
        this.address4 = address4;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public GpCountries getCountry() {
        return country;
    }

    public void setCountry(GpCountries country) {
        this.country = country;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}
