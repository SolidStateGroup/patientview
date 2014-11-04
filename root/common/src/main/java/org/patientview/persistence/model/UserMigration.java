package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.MigrationStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/11/2014
 */
@Entity
@Table(name = "pv_user_migration")
public class UserMigration extends AuditModel {

    @Column(name = "patientview1_user_id", nullable = false)
    private Long patientview1UserId;

    @Column(name = "patientview2_user_id")
    private Long patientview2UserId;

    @Column(name = "observation_count")
    private Long observationCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MigrationStatus status;

    @Column(name="information")
    private String information;

    public UserMigration () {

    }

    public UserMigration (Long patientview1UserId, MigrationStatus status) {
        this.patientview1UserId = patientview1UserId;
        this.status = status;
    }

    public Long getPatientview1UserId() {
        return patientview1UserId;
    }

    public void setPatientview1UserId(Long patientview1UserId) {
        this.patientview1UserId = patientview1UserId;
    }

    public Long getPatientview2UserId() {
        return patientview2UserId;
    }

    public void setPatientview2UserId(Long patientview2UserId) {
        this.patientview2UserId = patientview2UserId;
    }

    public Long getObservationCount() {
        return observationCount;
    }

    public void setObservationCount(Long observationCount) {
        this.observationCount = observationCount;
    }

    public MigrationStatus getStatus() {
        return status;
    }

    public void setStatus(MigrationStatus status) {
        this.status = status;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }
}
