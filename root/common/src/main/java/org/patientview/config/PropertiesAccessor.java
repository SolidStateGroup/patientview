package org.patientview.config;

import org.springframework.beans.factory.support.AbstractBeanFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO this is currently unused
 *
 * Created by james@solidstategroup.com
 * Created on 11/06/2014
 */
public class PropertiesAccessor {

    private final AbstractBeanFactory beanFactory;

    private final Map<String,String> cache = new ConcurrentHashMap();

    @Inject
    protected PropertiesAccessor(AbstractBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public  String getProperty(String key) {
        if(cache.containsKey(key)){
            return cache.get(key);
        }

        String foundProp = null;
        try {
            foundProp = beanFactory.resolveEmbeddedValue("${" + key.trim() + "}");
            cache.put(key,foundProp);
        } catch (IllegalArgumentException ex) {
            // ok - property was not found
        }

        return foundProp;
    }



}
