package org.patientview.api.model;

import org.patientview.persistence.model.enums.StageStatuses;
import org.patientview.persistence.model.enums.StageTypes;

import java.io.Serializable;
import java.util.Date;

/**
 * Stage model represents different steps in a Pathway for patient
 */

public class Stage implements Serializable {

    private Long id;
    private String name;
    private StageTypes stageType;
    private StageStatuses stageStatus;
    private Integer version;
    private Date started;
    private Date stopped;
    private DonorStageData data;
    private StageTypes backToPreviousPoint;
    private Boolean furtherInvestigation;

    public Stage() {
    }

    public Stage(org.patientview.persistence.model.Stage stage) {
        this.id = stage.getId();
        this.name = stage.getName();
        this.stageType = stage.getStageType();
        this.stageStatus = stage.getStageStatus();
        this.version = stage.getVersion();
        this.started = stage.getStarted();
        this.stopped = stage.getStopped();
        if (stage.getStageData() != null) {
            setData(new DonorStageData(stage.getStageData()));
        }
        this.backToPreviousPoint = stage.getBackToPreviousPoint();
        this.furtherInvestigation = stage.getFurtherInvestigation();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public DonorStageData getData() {
        return data;
    }

    public void setData(DonorStageData data) {
        this.data = data;
    }

    public StageTypes getBackToPreviousPoint() {
        return backToPreviousPoint;
    }

    public void setBackToPreviousPoint(StageTypes backToPreviousPoint) {
        this.backToPreviousPoint = backToPreviousPoint;
    }

    public Boolean getFurtherInvestigation() {
        return furtherInvestigation;
    }

    public void setFurtherInvestigation(Boolean furtherInvestigation) {
        this.furtherInvestigation = furtherInvestigation;
    }
}
