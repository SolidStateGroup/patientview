package org.patientview.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to restrict retrieving and modifying User information not belonging to the current logged in User.
 * Created by james@solidstategroup.com
 * Created on 19/08/2014
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UserOnly {
}
