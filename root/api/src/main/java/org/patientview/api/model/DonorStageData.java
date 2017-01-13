package org.patientview.api.model;

import java.io.Serializable;
import java.util.Date;

/**
 * DonorStageData entity model represents stage data specific to DonorView pathway
 */
public class DonorStageData implements Serializable {

    private Long id;
    private Boolean bloods;
    private Boolean crossmatching;
    private Boolean xrays;
    private Boolean ecg;
    private String caregiverText;
    private String carelocationText;

    private Date lastUpdate;
    private Date created;
    private BaseUser creator;

    public DonorStageData() {
    }

    public DonorStageData(org.patientview.persistence.model.DonorStageData data) {
        this.id = data.getId();
        this.bloods = data.getBloods();
        this.crossmatching = data.getCrossmatching();
        this.xrays = data.getXrays();
        this.ecg = data.getEcg();
        this.caregiverText = data.getCaregiverText();
        this.carelocationText = data.getCarelocationText();
        this.lastUpdate = data.getLastUpdate();
        this.created = data.getCreated();
        if (data.getCreator() != null) {
            setCreator(new BaseUser(data.getCreator()));
        }
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


    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public BaseUser getCreator() {
        return creator;
    }

    public void setCreator(BaseUser creator) {
        this.creator = creator;
    }
}
