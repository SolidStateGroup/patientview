package org.patientview.api.annotation;

import org.patientview.persistence.model.enums.AuditActions;

/**
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
public @interface Audit {
    AuditActions value();
}
