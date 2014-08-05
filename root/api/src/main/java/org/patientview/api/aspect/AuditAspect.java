package org.patientview.api.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.patientview.api.service.AuditRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
@Aspect
@Component
public class AuditAspect {

    private static AuditAspect instance;

    @Inject
    private AuditRepository auditRepository;

    @Before("@annotation(org.patientview.api.annotation.Audit)")
    public void auditObject() {}



    public static AuditAspect aspectOf(){

        if (instance == null) {
            instance = new AuditAspect();
        }
        return instance;
    }

}
