package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.GpCountries;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Entity
@Table(name = "pv_gp_master")
public class GpMaster implements Serializable, Comparable {

    @Id
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

    @Column(name = "last_update_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by")
    private User lastUpdater;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User creator;

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

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public User getLastUpdater() {
        return lastUpdater;
    }

    public void setLastUpdater(User lastUpdater) {
        this.lastUpdater = lastUpdater;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(this.getClass().isAssignableFrom(o.getClass()))) {
            return false;
        }

        GpMaster model = (GpMaster) o;
        return practiceCode != null && practiceCode.equals(model.practiceCode);
    }

    @Override
    public int hashCode() {
        if (practiceCode != null) {
            return practiceCode.hashCode();
        } else {
            return -1;
        }
    }

    public int compareTo(Object o) {
        GpMaster model = (GpMaster) o;
        return this.practiceCode.compareTo(model.getPracticeCode());
    }
}
