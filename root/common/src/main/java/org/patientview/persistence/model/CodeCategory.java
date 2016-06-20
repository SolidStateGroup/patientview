package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/06/2016
 */
@Entity
@Table(name = "pv_code_category")
public class CodeCategory extends BaseModel {

    @OneToOne
    @JoinColumn(name = "code_id", nullable = false)
    private Code code;

    @OneToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public CodeCategory() {

    }

    public CodeCategory(Code code, Category category) {
        this.code = code;
        this.category = category;
    }

    @JsonIgnore
    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
