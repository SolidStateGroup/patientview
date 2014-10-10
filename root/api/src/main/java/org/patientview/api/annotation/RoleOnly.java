package org.patientview.api.annotation;

import org.patientview.persistence.model.enums.RoleName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 10/10/2014
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RoleOnly {
    RoleName[] roles() default { };
}
