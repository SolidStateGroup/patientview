package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.persistence.model.enums.StageStatuses;
import org.patientview.persistence.model.enums.StageTypes;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Stage entity model represents different steps in a Pathway
 */
@Entity
@Table(name = "pv_stage")
public class Stage extends BaseModel {

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_id", nullable = false)
    private Pathway pathway;

    @Column(name = "name")
    private String name;

    @Column(name = "stage_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StageTypes stageType;

    @Column(name = "stage_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StageStatuses stageStatus;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "started_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date started;

    @Column(name = "stopped_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date stopped;

    @OneToOne(mappedBy = "stage", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private DonorStageData stageData;

    @Column(name = "back_to_previous_point")
    @Enumerated(EnumType.STRING)
    private StageTypes backToPreviousPoint;

    public Pathway getPathway() {
        return pathway;
    }

    public void setPathway(Pathway pathway) {
        this.pathway = pathway;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StageTypes getStageType() {
        return stageType;
    }

    public void setStageType(StageTypes stageType) {
        this.stageType = stageType;
    }

    public StageStatuses getStageStatus() {
        return stageStatus;
    }

    public void setStageStatus(StageStatuses stageStatus) {
        this.stageStatus = stageStatus;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getStopped() {
        return stopped;
    }

    public void setStopped(Date stopped) {
        this.stopped = stopped;
    }

    public DonorStageData getStageData() {
        return stageData;
    }

    public void setStageData(DonorStageData stageData) {
        this.stageData = stageData;
    }

    public StageTypes getBackToPreviousPoint() {
        return backToPreviousPoint;
    }

    public void setBackToPreviousPoint(StageTypes backToPreviousPoint) {
        this.backToPreviousPoint = backToPreviousPoint;
    }
}
