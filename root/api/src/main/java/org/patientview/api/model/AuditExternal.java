package org.patientview.api.model;

import org.patientview.persistence.model.BaseModel;
import org.patientview.persistence.model.enums.AuditActions;

/**
 * AuditExternal TO to handle creation of the AuditLog by external systems.
 */
public class AuditExternal extends BaseModel {

    private AuditActions auditAction;
    private String groupCode;
    private String identifier;
    private String information;
    private String token;

    public AuditExternal() {
    }

    public AuditActions getAuditAction() {
        return auditAction;
    }

    public void setAuditAction(AuditActions auditAction) {
        this.auditAction = auditAction;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

