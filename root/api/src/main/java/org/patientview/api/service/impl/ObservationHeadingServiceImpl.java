package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.patientview.persistence.model.ResultCluster;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.ObservationHeadingGroupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.ResultClusterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private ObservationHeadingGroupRepository observationHeadingGroupRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private ResultClusterRepository resultClusterRepository;

    public ObservationHeading add(final ObservationHeading observationHeading) {
        if (observationHeadingExists(observationHeading)) {
            LOG.debug("Observation Heading not created, already exists with these details");
            throw new EntityExistsException("Observation Heading already exists with these details");
        }

        // manage observation heading groups (for migration)
        Set<ObservationHeadingGroup> observationHeadingGroups = new HashSet<>();

        for (ObservationHeadingGroup observationHeadingGroup : observationHeading.getObservationHeadingGroups()) {
            ObservationHeadingGroup newObservationHeadingGroup = new ObservationHeadingGroup();
            newObservationHeadingGroup.setObservationHeading(observationHeading);
            newObservationHeadingGroup.setPanelOrder(observationHeadingGroup.getPanelOrder());
            newObservationHeadingGroup.setPanel(observationHeadingGroup.getPanel());
            newObservationHeadingGroup.setGroup(groupRepository.findOne(observationHeadingGroup.getGroup().getId()));
            observationHeadingGroups.add(newObservationHeadingGroup);
        }

        observationHeading.setObservationHeadingGroups(observationHeadingGroups);

        return observationHeadingRepository.save(observationHeading);
    }

    public void addObservationHeadingGroup(Long observationHeadingId, Long groupId, Long panel, Long panelOrder)
            throws ResourceNotFoundException {
        ObservationHeading observationHeading = observationHeadingRepository.findOne(observationHeadingId);

        if (observationHeading == null) {
            throw new ResourceNotFoundException("Observation Heading does not exist");
        }

        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException("Group does not exist");
        }

        observationHeading.getObservationHeadingGroups().add(
                new ObservationHeadingGroup(observationHeading, group, panel, panelOrder));

        observationHeadingRepository.save(observationHeading);
    }

    public void updateObservationHeadingGroup(org.patientview.api.model.ObservationHeadingGroup observationHeadingGroup)
            throws ResourceNotFoundException {
        ObservationHeading observationHeading
                = observationHeadingRepository.findOne(observationHeadingGroup.getObservationHeadingId());

        if (observationHeading == null) {
            throw new ResourceNotFoundException("Observation Heading does not exist");
        }

        Group group = groupRepository.findOne(observationHeadingGroup.getGroupId());
        if (group == null) {
            throw new ResourceNotFoundException("Group does not exist");
        }

        for (ObservationHeadingGroup oldObservationHeadingGroup : observationHeading.getObservationHeadingGroups()) {
            if (oldObservationHeadingGroup.getId().equals(observationHeadingGroup.getId())) {
                oldObservationHeadingGroup.setGroup(group);
                oldObservationHeadingGroup.setPanel(observationHeadingGroup.getPanel());
                oldObservationHeadingGroup.setPanelOrder(observationHeadingGroup.getPanelOrder());
            }
        }

        observationHeadingRepository.save(observationHeading);
    }

    public void removeObservationHeadingGroup(Long observationHeadingGroupId) throws ResourceNotFoundException {
        ObservationHeadingGroup observationHeadingGroup
                = observationHeadingGroupRepository.findOne(observationHeadingGroupId);
        if (observationHeadingGroup == null) {
            throw new ResourceNotFoundException("Observation Heading Group does not exist");
        }
        observationHeadingGroupRepository.delete(observationHeadingGroup);
    }

    public List<ResultCluster> getResultClusters() {
        return Util.convertIterable(resultClusterRepository.findAll());
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

        return observationHeadingRepository.findAllMinimal(pageable);
    }

    public List<ObservationHeading> findByCode(final String code) {
        return observationHeadingRepository.findByCode(code);
    }

    public List<ObservationHeading> findAll() {
        return Util.convertIterable(observationHeadingRepository.findAll());
    }

    public ObservationHeading get(final Long observationHeadingId) {
        return observationHeadingRepository.findOne(observationHeadingId);
    }

    public ObservationHeading save(final ObservationHeading input) throws ResourceNotFoundException {

        ObservationHeading entity = observationHeadingRepository.findOne(input.getId());
        if (entity == null) {
            throw new ResourceNotFoundException("Observation Heading does not exist");
        }

        entity.setCode(input.getCode());
        entity.setHeading(input.getHeading());
        entity.setName(input.getName());
        entity.setNormalRange(input.getNormalRange());
        entity.setUnits(input.getUnits());
        entity.setMinGraph(input.getMinGraph());
        entity.setMaxGraph(input.getMaxGraph());
        entity.setInfoLink(input.getInfoLink());
        entity.setDefaultPanel(input.getDefaultPanel());
        entity.setDefaultPanelOrder(input.getDefaultPanelOrder());

        return observationHeadingRepository.save(entity);
    }

    private boolean observationHeadingExists(ObservationHeading observationHeading) {
        return !observationHeadingRepository.findByCode(observationHeading.getCode()).isEmpty();
    }
}
