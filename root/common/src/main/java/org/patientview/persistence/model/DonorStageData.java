package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * DonorStageData entity model represents stage data specific to DonorView pathway
 */
@Entity
@Table(name = "dv_stage_data")
public class DonorStageData extends AuditModel {

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(name = "bloods")
    private Boolean bloods;

    @Column(name = "crossmatching")
    private Boolean crossmatching;

    @Column(name = "xrays")
    private Boolean xrays;

    @Column(name = "ecg")
    private Boolean ecg;

    @Column(name = "caregiver_text")
    private String caregiverText;

    @Column(name = "carelocation_text")
    private String carelocationText;


    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Boolean getBloods() {
        return bloods;
    }

    public void setBloods(Boolean bloods) {
        this.bloods = bloods;
    }

    public Boolean getCrossmatching() {
        return crossmatching;
    }

    public void setCrossmatching(Boolean crossmatching) {
        this.crossmatching = crossmatching;
    }

    public Boolean getXrays() {
        return xrays;
    }

    public void setXrays(Boolean xrays) {
        this.xrays = xrays;
    }

    public Boolean getEcg() {
        return ecg;
    }

    public void setEcg(Boolean ecg) {
        this.ecg = ecg;
    }

    public String getCaregiverText() {
        return caregiverText;
    }

    public void setCaregiverText(String caregiverText) {
        this.caregiverText = caregiverText;
    }

    public String getCarelocationText() {
        return carelocationText;
    }

    public void setCarelocationText(String carelocationText) {
        this.carelocationText = carelocationText;
    }
}
