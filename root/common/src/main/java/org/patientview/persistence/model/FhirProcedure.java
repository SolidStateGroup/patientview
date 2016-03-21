package org.patientview.persistence.model;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Procedure;
import org.springframework.util.CollectionUtils;

/**
 * FhirProcedure, used for Encounter (surgery) procedures, initially in IBD patient management
 * Created by jamesr@solidstategroup.com
 * Created on 18/03/2016
 */
public class FhirProcedure extends BaseModel {

    private String bodySite;
    private String type;

    public FhirProcedure() {
    }

    public FhirProcedure(String bodySite, String type) {
        this.bodySite = bodySite;
        this.type = type;
    }

    public FhirProcedure(Procedure procedure) {
        if (CollectionUtils.isEmpty(procedure.getBodySite())
                && StringUtils.isNotEmpty(procedure.getBodySite().get(0).getTextSimple())) {
            this.bodySite = procedure.getBodySite().get(0).getTextSimple();
        }
        if (procedure.getType() != null && StringUtils.isNotEmpty(procedure.getType().getTextSimple())) {
            this.type = procedure.getType().getTextSimple();
        }
    }

    public String getBodySite() {
        return bodySite;
    }

    public void setBodySite(String bodySite) {
        this.bodySite = bodySite;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
