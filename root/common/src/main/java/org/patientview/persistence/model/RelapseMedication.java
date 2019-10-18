package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.DoseFrequencyTypes;
import org.patientview.persistence.model.enums.DoseUnitTypes;
import org.patientview.persistence.model.enums.MedicationRouteTypes;
import org.patientview.persistence.model.enums.RelapseMedicationTypes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * RelapseMedication entity to store patient's taken Medication for Relapse.
 */
@Entity
@Table(name = "pv_relapse_medication")
public class RelapseMedication extends BaseModel {

    @Column(name = "name", nullable = false)
    @Enumerated(EnumType.STRING)
    private RelapseMedicationTypes name;

    @Column(name = "other")
    private String other;

    @Column(name = "dose_quantity")
    private Integer doseQuantity;

    @Column(name = "dose_units")
    @Enumerated(EnumType.STRING)
    private DoseUnitTypes doseUnits;

    @Column(name = "dose_frequency")
    @Enumerated(EnumType.STRING)
    private DoseFrequencyTypes doseFrequency;

    @Column(name = "route")
    @Enumerated(EnumType.STRING)
    private MedicationRouteTypes route;

    @Column(name = "started")
    @Temporal(TemporalType.DATE)
    private Date started;

    @Column(name = "stopped")
    @Temporal(TemporalType.DATE)
    private Date stopped;

    public RelapseMedicationTypes getName() {
        return name;
    }

    public void setName(RelapseMedicationTypes name) {
        this.name = name;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public Integer getDoseQuantity() {
        return doseQuantity;
    }

    public void setDoseQuantity(Integer doseQuantity) {
        this.doseQuantity = doseQuantity;
    }

    public DoseUnitTypes getDoseUnits() {
        return doseUnits;
    }

    public void setDoseUnits(DoseUnitTypes doseUnits) {
        this.doseUnits = doseUnits;
    }

    public DoseFrequencyTypes getDoseFrequency() {
        return doseFrequency;
    }

    public void setDoseFrequency(DoseFrequencyTypes doseFrequency) {
        this.doseFrequency = doseFrequency;
    }

    public MedicationRouteTypes getRoute() {
        return route;
    }

    public void setRoute(MedicationRouteTypes route) {
        this.route = route;
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
}
