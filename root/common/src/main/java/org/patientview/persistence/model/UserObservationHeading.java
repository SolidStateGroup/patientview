package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 22/12/2014
 *
 * For per patient result list in table view
 */
@Entity
@Table(name = "pv_user_observation_heading")
public class UserObservationHeading extends SimpleAuditModel {

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "observation_heading_id", nullable = false)
    private ObservationHeading observationHeading;

    public UserObservationHeading() {
    }

    public UserObservationHeading(User user, ObservationHeading observationHeading) {
        this.user = user;
        this.observationHeading = observationHeading;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ObservationHeading getObservationHeading() {
        return observationHeading;
    }

    public void setObservationHeading(ObservationHeading observationHeading) {
        this.observationHeading = observationHeading;
    }
}
