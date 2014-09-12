package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;

/**
 * Class to control the crud operations of the Observation Headings.
 * <p/>
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@Service
public class ObservationHeadingServiceImpl extends AbstractServiceImpl<ObservationHeadingServiceImpl>
        implements ObservationHeadingService {

    @Inject
    private ObservationHeadingRepository observationHeadingRepository;

    @Inject
    private GroupRepository groupRepository;

    public ObservationHeading add(final ObservationHeading observationHeading) {
        if (observationHeadingExists(observationHeading)) {
            LOG.debug("Observation Heading not created, already exists with these details");
            throw new EntityExistsException("Observation Heading already exists with these details");
        }
        return observationHeadingRepository.save(observationHeading);
    }

    public void addOrUpdateGroup(Long observationHeadingId, Long groupId, Long panel, Long panelOrder)
            throws ResourceNotFoundException {
        ObservationHeading observationHeading = observationHeadingRepository.findOne(observationHeadingId);

        if (observationHeading == null) {
            throw new ResourceNotFoundException("Observation Heading does not exist");
        }

        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException("Group does not exist");
        }

        ObservationHeadingGroup toRemove = null;

        for (ObservationHeadingGroup observationHeadingGroup : observationHeading.getObservationHeadingGroups()) {
            if (observationHeadingGroup.getGroup().equals(group)
                    && observationHeadingGroup.getObservationHeading().equals(observationHeading)) {
                toRemove = observationHeadingGroup;
            }
        }

        if (toRemove != null) {
            observationHeading.getObservationHeadingGroups().remove(toRemove);
        }

        observationHeading.getObservationHeadingGroups().add(
                new ObservationHeadingGroup(observationHeading, group, panel, panelOrder));

        observationHeadingRepository.save(observationHeading);
    }

    public void removeGroup(Long observationHeadingId, Long groupId) throws ResourceNotFoundException {
        ObservationHeading observationHeading = observationHeadingRepository.findOne(observationHeadingId);
        if (observationHeading == null) {
            throw new ResourceNotFoundException("Observation Heading does not exist");
        }
        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException("Group does not exist");
        }

        ObservationHeadingGroup toRemove = null;

        for (ObservationHeadingGroup observationHeadingGroup : observationHeading.getObservationHeadingGroups()) {
            if (observationHeadingGroup.getGroup().equals(group)
                    && observationHeadingGroup.getObservationHeading().equals(observationHeading)) {
                toRemove = observationHeadingGroup;
            }
        }

        if (toRemove != null) {
            observationHeading.getObservationHeadingGroups().remove(toRemove);
            observationHeadingRepository.save(observationHeading);
        }
    }

    public void delete(final Long observationHeadingId) {
        observationHeadingRepository.delete(observationHeadingId);
    }

    public Page<ObservationHeading> findAll(final GetParameters getParameters) {
        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        if (StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortDirection)) {
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection.equals("DESC")) {
                direction = Sort.Direction.DESC;
            }

            pageable = new PageRequest(pageConverted, sizeConverted, new Sort(new Sort.Order(direction, sortField)));
        } else {
            pageable = new PageRequest(pageConverted, sizeConverted);
        }

        return observationHeadingRepository.findAll(pageable);
    }

    public ObservationHeading get(final Long observationHeadingId) {
        return observationHeadingRepository.findOne(observationHeadingId);
    }

    public ObservationHeading save(final ObservationHeading observationHeading) throws ResourceNotFoundException {
        return observationHeadingRepository.save(observationHeading);
    }

    private boolean observationHeadingExists(ObservationHeading observationHeading) {
        return !observationHeadingRepository.findByCode(observationHeading.getCode()).isEmpty();
    }
}
