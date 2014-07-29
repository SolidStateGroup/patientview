package org.patientview.api.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.persistence.model.enums.Roles;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public class Util {

    private Util() {}

    /**
     * This is convert the Iterable<T> type passed for Spring DAO interface into
     * a more useful typed List.
     *
     * @param iterable
     * @param <T>
     * @return
     */
    public static <T> List<T> iterableToList(Iterable<T> iterable) {

        if (iterable == null) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<T>();
        Iterator<T> lookupIterator = iterable.iterator();

        while (lookupIterator.hasNext()) {
            list.add(lookupIterator.next());
        }
        return list;

    }

    // Retrieve the list of Roles from the annotation.
    public static Roles[] getRoles(JoinPoint joinPoint) {
        final org.aspectj.lang.Signature signature = joinPoint.getStaticPart().getSignature();
        if (signature instanceof MethodSignature) {
            final MethodSignature ms = (MethodSignature) signature;

            for (Annotation annotation : ms.getMethod().getDeclaredAnnotations())
                if (annotation.annotationType() == GroupMemberOnly.class) {
                    return Util.getRolesFromAnnotation(annotation);
                }
        }
        return null;
    }

    public static Roles[] getRolesFromAnnotation(Annotation annotation) {
        Method[] methods = annotation.annotationType().getMethods();
        for (Method method : methods) {
            String name = method.getName();
            Class<?> returnType = method.getReturnType();
            Class<?> componentType = returnType.getComponentType();
            if (name.equals("roles") && returnType.isArray()
                    && Roles.class.isAssignableFrom(componentType)) {
                Roles[] features;
                try {
                    features = (Roles[]) (method.invoke(annotation, new Object[] {}));
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Error executing value() method in annotation.getClass().getCanonicalName()", e);
                }
                return features;
            }
        }
        throw new RuntimeException("No value() method returning a Roles[] roles was found in annotation "
                        + annotation.getClass().getCanonicalName());
    }

}

