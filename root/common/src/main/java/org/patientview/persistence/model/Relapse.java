package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity representing Relapse for the INS Diary.
 *
 * Relapse can cover a set of Diary entries.
 */
@Entity
@Table(name = "pv_relapse")
public class Relapse extends AuditModel {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "relapse_date")
    @Temporal(TemporalType.DATE)
    private Date relapseDate;

    @Column(name = "remission_date")
    @Temporal(TemporalType.DATE)
    private Date remissionDate;

    @Column(name = "viral_infection", nullable = false)
    private String viralInfection;

    @Column(name = "common_cold")
    private boolean commonCold = false;

    @Column(name = "hay_fever")
    private boolean hayFever = false;

    @Column(name = "allergic_reaction")
    private boolean allergicReaction = false;

    @Column(name = "allergic_skin_rash")
    private boolean allergicSkinRash = false;

    @Column(name = "food_intolerance")
    private boolean foodIntolerance = false;

    @OneToMany(mappedBy = "relapse", cascade = {CascadeType.ALL})
    private List<RelapseMedication> medications = new ArrayList();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getRelapseDate() {
        return relapseDate;
    }

    public void setRelapseDate(Date relapseDate) {
        this.relapseDate = relapseDate;
    }

    public Date getRemissionDate() {
        return remissionDate;
    }

    public void setRemissionDate(Date remissionDate) {
        this.remissionDate = remissionDate;
    }

    public String getViralInfection() {
        return viralInfection;
    }

    public void setViralInfection(String viralInfection) {
        this.viralInfection = viralInfection;
    }

    public boolean isCommonCold() {
        return commonCold;
    }

    public void setCommonCold(boolean commonCold) {
        this.commonCold = commonCold;
    }

    public boolean isHayFever() {
        return hayFever;
    }

    public void setHayFever(boolean hayFever) {
        this.hayFever = hayFever;
    }

    public boolean isAllergicReaction() {
        return allergicReaction;
    }

    public void setAllergicReaction(boolean allergicReaction) {
        this.allergicReaction = allergicReaction;
    }

    public boolean isAllergicSkinRash() {
        return allergicSkinRash;
    }

    public void setAllergicSkinRash(boolean allergicSkinRash) {
        this.allergicSkinRash = allergicSkinRash;
    }

    public boolean isFoodIntolerance() {
        return foodIntolerance;
    }

    public void setFoodIntolerance(boolean foodIntolerance) {
        this.foodIntolerance = foodIntolerance;
    }

    public List<RelapseMedication> getMedications() {
        return medications;
    }

    public void setMedications(List<RelapseMedication> medications) {
        this.medications = medications;
    }
}
