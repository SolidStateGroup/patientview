package org.patientview.api.service.impl;

import org.patientview.api.util.Util;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

/**
 * Created to group the services add utilities
 *
 * Created by james@solidstategroup.com
 * Created on 05/08/2014
 */
public abstract class AbstractServiceImpl<T extends AbstractServiceImpl> {

    protected final Logger LOG = LoggerFactory.getLogger(getServiceClass());

    private Class<T> getServiceClass()  {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) superclass.getActualTypeArguments()[0];
    }

    protected List<Role> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Session is not authenticated");
        }

        if (CollectionUtils.isEmpty(authentication.getAuthorities())) {
            return Collections.EMPTY_LIST;
        }

        return Util.convertAuthorities(authentication.getAuthorities());
    }

    protected User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Session is not authenticated");
        }

        return (User) authentication.getPrincipal();
    }


    @PostConstruct
    public void init() {
        LOG.info("Service started");
    }

    @PreDestroy
    public void close() {
        LOG.info("Service closing");
    }

}
