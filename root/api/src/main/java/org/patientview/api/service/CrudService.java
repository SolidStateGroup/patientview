package org.patientview.api.service;

import org.patientview.api.exception.ResourceNotFoundException;

/**
 * Base CRUD service to align services methods
 *
 * Created by james@solidstategroup.com
 * Created on 05/08/2014
 */
public interface CrudService<T> {

    T add(T t);
    T get(Long id) throws ResourceNotFoundException;
    void delete(Long id);
    T save(T t) throws ResourceNotFoundException;

}
