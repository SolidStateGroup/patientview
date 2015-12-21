package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 21/12/2015
 */
@Entity
@Table(name = "pv_food_diary")
public class FoodDiary extends AuditModel {

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "food", nullable = false)
    private String food;

    @Column(name = "comment")
    private String comment;

    @Column(name = "date_nutrition", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateNutrition = new Date();

    public FoodDiary() {}

    public FoodDiary(User user, String food, Date dateNutrition) {
        this.user = user;
        this.food = food;
        this.dateNutrition = dateNutrition;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDateNutrition() {
        return dateNutrition;
    }

    public void setDateNutrition(Date dateNutrition) {
        this.dateNutrition = dateNutrition;
    }
}
