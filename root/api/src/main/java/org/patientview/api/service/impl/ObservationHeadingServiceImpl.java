package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.patientview.persistence.model.ResultCluster;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserObservationHeading;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.ObservationHeadingGroupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.ResultClusterRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    @Inject
    private UserRepository userRepository;

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    private static final Long FIRST_PANEL = 1l;
    private static final Long DEFAULT_COUNT = 3l;

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
            throws ResourceNotFoundException, ResourceForbiddenException {
        ObservationHeading observationHeading = observationHeadingRepository.findOne(observationHeadingId);

        if (observationHeading == null) {
            throw new ResourceNotFoundException("Observation Heading does not exist");
        }

        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException("Group does not exist");
        }

        // only global admin or specialty admin with correct group role can remove
        if (!Util.doesContainRoles(RoleName.GLOBAL_ADMIN)
                && !Util.doesContainGroupAndRole(group.getId(), RoleName.SPECIALTY_ADMIN)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        observationHeading.getObservationHeadingGroups().add(
                new ObservationHeadingGroup(observationHeading, group, panel, panelOrder));

        observationHeadingRepository.save(observationHeading);
    }

    public void updateObservationHeadingGroup(org.patientview.api.model.ObservationHeadingGroup observationHeadingGroup)
            throws ResourceNotFoundException, ResourceForbiddenException {
        ObservationHeading observationHeading
                = observationHeadingRepository.findOne(observationHeadingGroup.getObservationHeadingId());

        if (observationHeading == null) {
            throw new ResourceNotFoundException("Observation Heading does not exist");
        }

        Group group = groupRepository.findOne(observationHeadingGroup.getGroupId());
        if (group == null) {
            throw new ResourceNotFoundException("Group does not exist");
        }

        // only global admin or specialty admin with correct group role can remove
        if (!Util.doesContainRoles(RoleName.GLOBAL_ADMIN)
                && !Util.doesContainGroupAndRole(group.getId(), RoleName.SPECIALTY_ADMIN)) {
            throw new ResourceForbiddenException("Forbidden");
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

    public void removeObservationHeadingGroup(Long observationHeadingGroupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        ObservationHeadingGroup observationHeadingGroup
                = observationHeadingGroupRepository.findOne(observationHeadingGroupId);
        if (observationHeadingGroup == null) {
            throw new ResourceNotFoundException("Observation Heading Group does not exist");
        }

        // only global admin or specialty admin with correct group role can remove
        if (!Util.doesContainRoles(RoleName.GLOBAL_ADMIN)
            && !Util.doesContainGroupAndRole(observationHeadingGroup.getGroup().getId(), RoleName.SPECIALTY_ADMIN)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        observationHeadingGroupRepository.delete(observationHeadingGroup);
    }

    public List<ResultCluster> getResultClusters() {
        return Util.convertIterable(resultClusterRepository.findAll());
    }

    @Override
    public List<ObservationHeading> getAvailableObservationHeadings(Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        List<ObservationHeading> observationHeadings = new ArrayList<>();

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT DISTINCT ON (1) ");
                query.append("CONTENT -> 'name' -> 'text' ");
                query.append("FROM   observation ");
                query.append("WHERE  CONTENT -> 'subject' ->> 'display' = '");
                query.append(fhirLink.getResourceId().toString());
                query.append("' ");

                Connection connection = null;
                try {
                    connection = dataSource.getConnection();
                    java.sql.Statement statement = connection.createStatement();
                    ResultSet results = statement.executeQuery(query.toString());

                    while ((results.next())) {
                        String code = results.getString(1).replace("\"","").toLowerCase();
                        List<ObservationHeading> observationHeadingsByCode
                                = observationHeadingRepository.findByCode(code);
                        if (!CollectionUtils.isEmpty(observationHeadingsByCode)
                                && !observationHeadings.contains(observationHeadingsByCode.get(0))) {
                            observationHeadings.add(observationHeadingsByCode.get(0));
                        }
                    }

                    connection.close();
                } catch (SQLException e) {
                    try {
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (SQLException e2) {
                        throw new FhirResourceException(e2);
                    }

                    throw new FhirResourceException(e);
                }
            }
        }

        return observationHeadings;
    }

    @Override
    public Set<ObservationHeading> getSavedObservationHeadings(Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<ObservationHeading> availableObservationHeadings = getAvailableObservationHeadings(userId);
        Set<ObservationHeading> observationHeadings = new HashSet<>();

        if (CollectionUtils.isEmpty(user.getUserObservationHeadings())) {
            // get list of visible specialties for user and get top 3 observation headings
            for (Group group : Util.convertIterable(groupRepository.findGroupByUser(user))) {
                if (group.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                    List<ObservationHeadingGroup> observationHeadingGroups
                            = observationHeadingGroupRepository.findByGroup(group);

                    // sort by panel order
                    Collections.sort(observationHeadingGroups, new Comparator<ObservationHeadingGroup>() {
                        @Override
                        public int compare(ObservationHeadingGroup ohg1, ObservationHeadingGroup ohg2) {
                            return ohg1.getPanelOrder().compareTo(ohg2.getPanelOrder());
                        }
                    });

                    for (ObservationHeadingGroup observationHeadingGroup : observationHeadingGroups) {
                        if (observationHeadingGroup.getPanel().equals(FIRST_PANEL)
                                && observationHeadings.size() < DEFAULT_COUNT) {
                            ObservationHeading observationHeading = observationHeadingGroup.getObservationHeading();
                            if (availableObservationHeadings.contains(observationHeading)) {
                                observationHeadings.add(observationHeading);
                            }
                        }
                    }
                }
            }
        } else {
            for (UserObservationHeading userObservationHeading : user.getUserObservationHeadings()) {
                observationHeadings.add(userObservationHeading.getObservationHeading());
            }
        }

        return observationHeadings;
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
