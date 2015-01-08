package org.patientview.api.annotation;

import org.patientview.persistence.model.enums.RoleName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by james@solidstategroup.com
 * Created on 25/07/2014
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GroupMemberOnly {
    RoleName[] roles() default { };
}
