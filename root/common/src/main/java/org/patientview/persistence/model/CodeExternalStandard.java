package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Link table between Code and ExternalStandard (M:M)
 * Created by jamesr@solidstategroup.com
 * Created on 18/06/2014
 */
@Entity
@Table(name = "pv_code_external_standard")
public class CodeExternalStandard extends BaseModel {

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_id", nullable = false)
    private Code code;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "external_standard_id", nullable = false)
    private ExternalStandard externalStandard;

    public CodeExternalStandard() { }

    public CodeExternalStandard(Code code, ExternalStandard externalStandard) {
        this.code = code;
        this.externalStandard = externalStandard;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public ExternalStandard getExternalStandard() {
        return externalStandard;
    }

    public void setExternalStandard(ExternalStandard externalStandard) {
        this.externalStandard = externalStandard;
    }
}
