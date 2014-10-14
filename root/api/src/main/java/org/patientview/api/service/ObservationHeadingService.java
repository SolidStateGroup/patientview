package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.model.ObservationHeadingGroup;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ResultCluster;
import org.patientview.persistence.model.enums.RoleName;
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

    // available for all logged in users
    List<ObservationHeading> findAll();

    // available for all logged in users
    Page<ObservationHeading> findAll(GetParameters getParameters);

    // available for all logged in users
    List<ObservationHeading> findByCode(String code);

    // available for all logged in users
    ObservationHeading get(Long observationHeadingId) throws ResourceNotFoundException;

    @RoleOnly
    ObservationHeading add(ObservationHeading observationHeading);

    @RoleOnly
    ObservationHeading save(ObservationHeading observationHeading) throws ResourceNotFoundException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void addObservationHeadingGroup(Long observationHeadingId, Long groupId, Long panel, Long panelOrder)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void updateObservationHeadingGroup(ObservationHeadingGroup observationHeadingGroup)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void removeObservationHeadingGroup(Long observationHeadingGroupId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.PATIENT })
    List<ResultCluster> getResultClusters();
}
