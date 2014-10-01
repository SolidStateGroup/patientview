package org.patientview.api.service;

import org.patientview.api.model.ObservationHeadingGroup;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ResultCluster;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ObservationHeadingService extends CrudService<ObservationHeading> {

    List<ObservationHeading> findAll();

    Page<ObservationHeading> findAll(GetParameters getParameters);

    ObservationHeading add(ObservationHeading observationHeading);

    void addObservationHeadingGroup(Long observationHeadingId, Long groupId, Long panel, Long panelOrder)
            throws ResourceNotFoundException;

    void updateObservationHeadingGroup(ObservationHeadingGroup observationHeadingGroup) throws ResourceNotFoundException;

    void removeObservationHeadingGroup(Long observationHeadingGroupId) throws ResourceNotFoundException;

    List<ResultCluster> getResultClusters();
}
