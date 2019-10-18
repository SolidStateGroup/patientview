package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
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

    @Column(name = "alergic_reaction")
    private boolean alergicReaction = false;

    @Column(name = "alergic_skin_rash")
    private boolean alergicSkinRash = false;

    @Column(name = "food_intolerence")
    private boolean foodIntolerence = false;

    private List<RelapseMedication> midications = new ArrayList();


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

    public boolean isAlergicReaction() {
        return alergicReaction;
    }

    public void setAlergicReaction(boolean alergicReaction) {
        this.alergicReaction = alergicReaction;
    }

    public boolean isAlergicSkinRash() {
        return alergicSkinRash;
    }

    public void setAlergicSkinRash(boolean alergicSkinRash) {
        this.alergicSkinRash = alergicSkinRash;
    }

    public boolean isFoodIntolerence() {
        return foodIntolerence;
    }

    public void setFoodIntolerence(boolean foodIntolerence) {
        this.foodIntolerence = foodIntolerence;
    }

    public List<RelapseMedication> getMidications() {
        return midications;
    }

    public void setMidications(List<RelapseMedication> midications) {
        this.midications = midications;
    }
}
