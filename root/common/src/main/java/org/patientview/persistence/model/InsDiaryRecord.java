package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.patientview.persistence.model.enums.OedemaTypes;
import org.patientview.persistence.model.enums.ProteinDipstickTypes;
import org.patientview.persistence.model.types.StringArrayJsonUserType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * InsDiaryRecord entity to store Ins diary records.
 */
@Entity
@Table(name = "pv_ins_diary")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class InsDiaryRecord extends AuditModel {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "entry_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date entryDate;

    @Type(type = "jsonb")
    @Column(name = "oedema", columnDefinition = "jsonb")
    private List<OedemaTypes> oedema = new ArrayList<>();

    @Column(name = "urine_protein_dipstick_type")
    @Enumerated(EnumType.STRING)
    private ProteinDipstickTypes dipstickType;

    // fhir result captured in diary record as well
    @Column(name = "systolic_bp")
    private Integer systolicBP;

    @Column(name = "systolic_bp_exclude")
    private Boolean systolicBPExclude;

    @Column(name = "diastolic_bp")
    private Integer diastolicBP;

    @Column(name = "diastolic_bp_exclude")
    private Boolean diastolicBPExclude;

    @Column(name = "weight", columnDefinition = "numeric", precision = 19, scale = 2)
    private Double weight;

    @Column(name = "weight_exclude")
    private Boolean weightExclude;

    @Column(name = "is_relapse", nullable = false)
    private boolean inRelapse = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relapse_id")
    private Relapse relapse;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
    }

    public List<OedemaTypes> getOedema() {
        return oedema;
    }

    public void setOedema(List<OedemaTypes> oedema) {
        this.oedema = oedema;
    }

    public ProteinDipstickTypes getDipstickType() {
        return dipstickType;
    }

    public void setDipstickType(ProteinDipstickTypes dipstickType) {
        this.dipstickType = dipstickType;
    }

    public Integer getSystolicBP() {
        return systolicBP;
    }

    public void setSystolicBP(Integer systolicBP) {
        this.systolicBP = systolicBP;
    }

    public Boolean getSystolicBPExclude() {
        return systolicBPExclude;
    }

    public void setSystolicBPExclude(Boolean systolicBPExclude) {
        this.systolicBPExclude = systolicBPExclude;
    }

    public Integer getDiastolicBP() {
        return diastolicBP;
    }

    public void setDiastolicBP(Integer diastolicBP) {
        this.diastolicBP = diastolicBP;
    }

    public Boolean getDiastolicBPExclude() {
        return diastolicBPExclude;
    }

    public void setDiastolicBPExclude(Boolean diastolicBPExclude) {
        this.diastolicBPExclude = diastolicBPExclude;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Boolean getWeightExclude() {
        return weightExclude;
    }

    public void setWeightExclude(Boolean weightExclude) {
        this.weightExclude = weightExclude;
    }

    public boolean isInRelapse() {
        return inRelapse;
    }

    public void setInRelapse(boolean inRelapse) {
        this.inRelapse = inRelapse;
    }

    public Relapse getRelapse() {
        return relapse;
    }

    public void setRelapse(Relapse relapse) {
        this.relapse = relapse;
    }
}
