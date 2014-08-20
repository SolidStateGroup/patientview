package org.patientview.api.service;

import org.patientview.persistence.model.Link;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 15/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface LinkService extends CrudService<Link> {

}