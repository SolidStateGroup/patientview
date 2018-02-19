package org.patientview.persistence.model;

import generated.Sex;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Models a research study that is available to a user
 */
public class ResearchStudyCriteria {

    private Sex gender;
    private Integer fromAge;
    private Integer toAge;
    private String diagnosis_id;
    private String group_id;
    private String treatment_id;

    public ResearchStudyCriteria() {
    }


    public Sex getGender() {
        return gender;
    }

    public void setGender(Sex gender) {
        this.gender = gender;
    }

    public Integer getFromAge() {
        return fromAge;
    }

    public void setFromAge(Integer fromAge) {
        this.fromAge = fromAge;
    }

    public String getDiagnosis_id() {
        return diagnosis_id;
    }

    public void setDiagnosis_id(String diagnosis_id) {
        this.diagnosis_id = diagnosis_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getTreatment_id() {
        return treatment_id;
    }

    public void setTreatment_id(String treatment_id) {
        this.treatment_id = treatment_id;
    }

    public Integer getToAge() {
        return toAge;
    }

    public void setToAge(Integer toAge) {
        this.toAge = toAge;
    }
}
