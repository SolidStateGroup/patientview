package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 12/09/2014
 */
@Entity
@Table(name = "pv_observation_heading_group")
public class ObservationHeadingGroup extends SimpleAuditModel {

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "observation_heading_id", nullable = false)
    private ObservationHeading observationHeading;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "panel")
    private Long panel;

    @Column(name = "panelOrder")
    private Long panelOrder;

    public ObservationHeading getObservationHeading() {
        return observationHeading;
    }

    public void setObservationHeading(ObservationHeading observationHeading) {
        this.observationHeading = observationHeading;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Long getPanel() {
        return panel;
    }

    public void setPanel(Long panel) {
        this.panel = panel;
    }

    public Long getPanelOrder() {
        return panelOrder;
    }

    public void setPanelOrder(Long panelOrder) {
        this.panelOrder = panelOrder;
    }
}
