package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/11/2014
 */
@Entity
@Table(name = "pv_result_cluster_observation_heading")
public class ResultClusterObservationHeading extends BaseModel {

    @OneToOne
    @JoinColumn(name = "result_cluster_id", nullable = false)
    @JsonIgnore
    private ResultCluster resultCluster;

    @OneToOne
    @JoinColumn(name = "observation_heading_id", nullable = false)
    private ObservationHeading observationHeading;

    @Column(name = "order")
    private Long order;

    public ResultCluster getResultCluster() {
        return resultCluster;
    }

    public void setResultCluster(ResultCluster resultCluster) {
        this.resultCluster = resultCluster;
    }

    public ObservationHeading getObservationHeading() {
        return observationHeading;
    }

    public void setObservationHeading(ObservationHeading observationHeading) {
        this.observationHeading = observationHeading;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }
}
