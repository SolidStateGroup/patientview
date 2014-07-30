package org.patientview.api.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
@Aspect
@Component
public class AuditAspect {

    @Before("@annotation(org.patientview.api.annotation.Audit)")
    public void auditObject() {}


}
