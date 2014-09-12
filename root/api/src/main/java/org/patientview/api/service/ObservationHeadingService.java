package org.patientview.api.service;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.ObservationHeading;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ObservationHeadingService extends CrudService<ObservationHeading> {

    Page<ObservationHeading> findAll(GetParameters getParameters);

    ObservationHeading add(ObservationHeading observationHeading);

    void addOrUpdateGroup(Long observationHeadingId, Long groupId, Long panel, Long panelOrder)
            throws ResourceNotFoundException;

    void removeGroup(Long observationHeadingId, Long groupId) throws ResourceNotFoundException;
}
