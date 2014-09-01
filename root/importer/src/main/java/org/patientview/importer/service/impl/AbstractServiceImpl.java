package org.patientview.importer.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
public class AbstractServiceImpl<T> {
    protected final Logger LOG = LoggerFactory.getLogger(getServiceClass());

    private Class<T> getServiceClass()  {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) superclass.getActualTypeArguments()[0];
    }
}
