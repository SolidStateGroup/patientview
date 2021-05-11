package org.patientview.api.service.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.BaseObservationHeading;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
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
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.ObservationHeadingGroupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.ResultClusterRepository;
import org.patientview.persistence.repository.UserObservationHeadingRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.util.Util;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Class to control the crud operations of the Observation Headings.
 * <p/>
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@Transactional(readOnly = true)
@Service
public class ObservationHeadingServiceImpl extends AbstractServiceImpl<ObservationHeadingServiceImpl>
        implements ObservationHeadingService {

    private static final Long FIRST_PANEL = 1L;
    private static final Long DEFAULT_COUNT = 3L;
    private static final Pattern CODE_PATTERN = Pattern.compile("^([a-zA-Z])[a-zA-Z0-9-_]*$");
    @Inject
    private ObservationHeadingRepository observationHeadingRepository;
    @Inject
    private UserObservationHeadingRepository userObservationHeadingRepository;
    @Inject
    private ObservationHeadingGroupRepository observationHeadingGroupRepository;
    @Inject
    private FhirLinkRepository fhirLinkRepository;
    @Inject
    private FhirResource fhirResource;
    @Inject
    private GroupRepository groupRepository;
    @Inject
    private ResultClusterRepository resultClusterRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    @Named("fhir")
    private HikariDataSource dataSource;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public ObservationHeading add(final ObservationHeading observationHeading) throws ResourceInvalidException {
        if (observationHeadingExists(observationHeading)) {
            LOG.debug("Observation Heading not created, already exists with these details");
            throw new EntityExistsException("Observation Heading already exists with these details");
        }

        // validate code
        if (StringUtils.isEmpty(observationHeading.getCode()) ||
                !CODE_PATTERN.matcher(observationHeading.getCode()).find()) {
            throw new ResourceInvalidException("Invalid character in Code");
        }

        // validate panel order number
        long maxPanel = observationHeadingRepository.findMaxPanelNumber();
        if (!validPanel(maxPanel, observationHeading.getDefaultPanel())) {
            throw new ResourceInvalidException("The maximum allowable value for field 'Default Panel' is "
                    + (maxPanel + 1));
        }

        // manage observation heading groups (for migration)
        Set<ObservationHeadingGroup> observationHeadingGroups = new HashSet<>();

        for (ObservationHeadingGroup observationHeadingGroup : observationHeading.getObservationHeadingGroups()) {
            if (!validPanel(maxPanel, observationHeadingGroup.getPanel())) {
                throw new ResourceInvalidException("The maximum allowable value for field 'Panel' is "
                        + (maxPanel + 1));
            }
            ObservationHeadingGroup newObservationHeadingGroup = new ObservationHeadingGroup();
            newObservationHeadingGroup.setObservationHeading(observationHeading);
            newObservationHeadingGroup.setPanelOrder(observationHeadingGroup.getPanelOrder());
            newObservationHeadingGroup.setPanel(observationHeadingGroup.getPanel());
            newObservationHeadingGroup.setGroup(
                    groupRepository.findById(observationHeadingGroup.getGroup().getId()).get());
            observationHeadingGroups.add(newObservationHeadingGroup);
        }

        observationHeading.setObservationHeadingGroups(observationHeadingGroups);

        return observationHeadingRepository.save(observationHeading);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addObservationHeadingGroup(Long observationHeadingId, Long groupId, Long panel, Long panelOrder)
            throws ResourceNotFoundException, ResourceForbiddenException, ResourceInvalidException {
        ObservationHeading observationHeading = observationHeadingRepository.findById(observationHeadingId)
                .orElseThrow(() -> new ResourceNotFoundException("Observation Heading does not exist"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group does not exist"));

        // only global admin or specialty admin with correct group role can remove
        if (!ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                && !ApiUtil.doesContainGroupAndRole(group.getId(), RoleName.SPECIALTY_ADMIN)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        long maxPanel = observationHeadingRepository.findMaxPanelNumber();
        if (!validPanel(maxPanel, panel)) {
            throw new ResourceInvalidException("The maximum allowable value for field 'Panel' is " + (maxPanel + 1));
        }

        observationHeading.getObservationHeadingGroups().add(
                new ObservationHeadingGroup(observationHeading, group, panel, panelOrder));

        observationHeadingRepository.save(observationHeading);
    }

    @Override
    public List<BaseObservationHeading> findAll() {
        List<BaseObservationHeading> headings = new ArrayList<>();
        for (ObservationHeading heading : observationHeadingRepository.findAll()) {
            headings.add(new BaseObservationHeading(heading));
        }
        return headings;
    }

    @Override
    public Page<ObservationHeading> findAll(final GetParameters getParameters) {
        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        PageRequest pageable = createPageRequest(pageConverted, sizeConverted, sortField, sortDirection);

        return observationHeadingRepository.findAllMinimal(pageable);
    }

    @Override
    public List<ObservationHeading> findByCode(final String code) {
        return observationHeadingRepository.findByCode(code);
    }

    @Override
    public List<ResultCluster> getResultClusters() {
        return Util.convertIterable(resultClusterRepository.findAll());
    }

    @Override
    public List<ObservationHeading> getAvailableObservationHeadings(Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        List<ObservationHeading> observationHeadings = new ArrayList<>();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

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
                java.sql.Statement statement = null;
                ResultSet results = null;
                try {
                    connection = dataSource.getConnection();
                    statement = connection.createStatement();
                    results = statement.executeQuery(query.toString());

                    while ((results.next())) {
                        String code = results.getString(1).replace("\"", "").toLowerCase();
                        List<ObservationHeading> observationHeadingsByCode
                                = observationHeadingRepository.findByCode(code);
                        if (!CollectionUtils.isEmpty(observationHeadingsByCode)
                                && !observationHeadings.contains(observationHeadingsByCode.get(0))) {
                            observationHeadings.add(observationHeadingsByCode.get(0));
                        }
                    }

                } catch (SQLException e) {
                    try {
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (SQLException e2) {
                        throw new FhirResourceException(e2);
                    }

                    throw new FhirResourceException(e);
                } finally {
                    DbUtils.closeQuietly(connection, statement, results);
                }
            }
        }

        return observationHeadings;
    }

    @Override
    public List<ObservationHeading> getPatientEnteredObservationHeadings(Long userId)
            throws ResourceNotFoundException, FhirResourceException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        Group group = groupRepository.findByCode(HiddenGroupCodes.PATIENT_ENTERED.toString());
        if (group == null) {
            throw new ResourceNotFoundException("Group for patient entered data does not exist");
        }

        List<FhirLink> fhirLinks = fhirLinkRepository.findByUserAndGroup(user, group);
        if (!fhirLinks.isEmpty()) {
            StringBuilder resourceIds = new StringBuilder();
            for (FhirLink fhirLink : fhirLinks) {
                resourceIds.append("'").append(fhirLink.getResourceId().toString()).append("',");
            }
            resourceIds.deleteCharAt(resourceIds.length() - 1);

            StringBuilder query = new StringBuilder();
            query.append("SELECT DISTINCT ON (1) ");
            query.append("CONTENT -> 'name' -> 'text' ");
            query.append("FROM   observation ");
            query.append("WHERE  CONTENT -> 'subject' ->> 'display' IN (");
            query.append(resourceIds.toString());
            query.append(") ");

            List<String[]> codeArr = fhirResource.findValuesByQueryAndArray(query.toString(), 1);
            if (codeArr.isEmpty()) {
                return new ArrayList<>();
            }

            List<String> codes = new ArrayList<>();
            for (String[] strings : codeArr) {
                codes.add(strings[0].replace("\"", "").toLowerCase());
            }

            return observationHeadingRepository.findAllByCode(codes);
        }

        return new ArrayList<>();
    }

    @Override
    public List<ObservationHeading> getSavedObservationHeadings(Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        List<ObservationHeading> availableObservationHeadings = getAvailableObservationHeadings(userId);
        List<ObservationHeading> observationHeadings = new ArrayList<>();

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
            List<UserObservationHeading> userObservationHeadings = new ArrayList<>(user.getUserObservationHeadings());

            // order by date added descending
            Collections.sort(userObservationHeadings, new Comparator<UserObservationHeading>() {
                public int compare(UserObservationHeading uoh1, UserObservationHeading uoh2) {
                    return uoh1.getCreated().compareTo(uoh2.getCreated());
                }
            });

            for (UserObservationHeading userObservationHeading : userObservationHeadings) {
                observationHeadings.add(userObservationHeading.getObservationHeading());
            }
        }

        return observationHeadings;
    }


    @Override
    public List<ObservationHeading> getAvailableAlertObservationHeadings(Long userId)
            throws ResourceNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        List<Group> userGroups = Util.convertIterable(groupRepository.findGroupByUser(user));
        List<ObservationHeading> observationHeadings = Util.convertIterable(observationHeadingRepository.findAll());
        List<ObservationHeading> availableObservationHeadings = new ArrayList<>();

        // add based on default panel (if not 0)
        for (ObservationHeading observationHeading : observationHeadings) {
            if (observationHeading.getDefaultPanel() != null
                    && !observationHeading.getDefaultPanel().equals(0L)) {
                availableObservationHeadings.add(observationHeading);
            }
        }

        // add if specialty specific exists, remove if panel 0
        for (ObservationHeading observationHeading : observationHeadings) {
            for (ObservationHeadingGroup observationHeadingGroup : observationHeading.getObservationHeadingGroups()) {
                for (Group userGroup : userGroups) {
                    if (userGroup.getId().equals(observationHeadingGroup.getGroup().getId())) {
                        if (observationHeadingGroup.getPanel() != null
                                && !observationHeadingGroup.getPanel().equals(0L)) {
                            // add if panel != 0
                            availableObservationHeadings.add(observationHeading);
                        } else if (observationHeadingGroup.getPanel() != null
                                && observationHeadingGroup.getPanel().equals(0L)) {
                            // remove if panel == 0
                            availableObservationHeadings.remove(observationHeading);
                        }
                    }
                }
            }
        }

        // convert list to set
        Set<ObservationHeading> out = new HashSet<>();
        for (ObservationHeading observationHeading : availableObservationHeadings) {
            observationHeading.setObservationHeadingGroups(new HashSet<ObservationHeadingGroup>());
            out.add(observationHeading);
        }

        return new ArrayList<>(out);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void delete(final Long observationHeadingId) {
        observationHeadingRepository.deleteById(observationHeadingId);
    }

    @Override
    public ObservationHeading get(final Long observationHeadingId) throws ResourceNotFoundException {
        return observationHeadingRepository.findById(observationHeadingId)
                .orElseThrow(() -> new ResourceNotFoundException("Observation Heading Group does not exist"));
    }

    private boolean observationHeadingExists(ObservationHeading observationHeading) {
        return !observationHeadingRepository.findByCode(observationHeading.getCode()).isEmpty();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void removeObservationHeadingGroup(Long observationHeadingGroupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        ObservationHeadingGroup observationHeadingGroup
                = observationHeadingGroupRepository.findById(observationHeadingGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("Observation Heading Group does not exist"));

        // only global admin or specialty admin with correct group role can remove
        if (!ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                && !ApiUtil.doesContainGroupAndRole(
                observationHeadingGroup.getGroup().getId(), RoleName.SPECIALTY_ADMIN)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        observationHeadingGroupRepository.delete(observationHeadingGroup);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public ObservationHeading save(final ObservationHeading input)
            throws ResourceNotFoundException, ResourceInvalidException {

        ObservationHeading entity = observationHeadingRepository.findById(input.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Observation Heading does not exist"));

        long maxPanel = observationHeadingRepository.findMaxPanelNumber();
        if (!validPanel(maxPanel, input.getDefaultPanel())) {
            throw new ResourceInvalidException("The maximum allowable value for field 'Default Panel'  "
                    + (maxPanel + 1));
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
        entity.setDecimalPlaces(input.getDecimalPlaces());

        return observationHeadingRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveObservationHeadingSelection(Long userId, String[] codes) throws ResourceNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        if (user.getUserObservationHeadings() == null) {
            user.setUserObservationHeadings(new HashSet<UserObservationHeading>());
        }

        // create objects from codes passed in
        Set<UserObservationHeading> userObservationHeadingsToAdd = new HashSet<>();
        for (String code : codes) {
            // should only return one
            List<ObservationHeading> observationHeadings = observationHeadingRepository.findByCode(code);
            if (CollectionUtils.isEmpty(observationHeadings)) {
                throw new ResourceNotFoundException("Could not find observation heading");
            }

            UserObservationHeading userObservationHeading
                    = new UserObservationHeading(user, observationHeadings.get(0));
            userObservationHeading.setCreated(new Date());
            userObservationHeading.setCreator(user);
            userObservationHeadingsToAdd.add(userObservationHeading);
        }

        // get to delete (current - ones passed in)
        Set<UserObservationHeading> userObservationHeadingsToDelete
                = new HashSet<>(user.getUserObservationHeadings());
        Set<UserObservationHeading> dontDelete = new HashSet<>();
        for (UserObservationHeading userObservationHeading : userObservationHeadingsToDelete) {
            for (UserObservationHeading userObservationHeading1 : userObservationHeadingsToAdd) {
                if (userObservationHeading.getObservationHeading().getCode().equals(
                        userObservationHeading1.getObservationHeading().getCode())) {
                    dontDelete.add(userObservationHeading);
                }
            }
        }
        userObservationHeadingsToDelete.removeAll(dontDelete);

        // only add if not already present
        for (UserObservationHeading toAdd : userObservationHeadingsToAdd) {
            boolean found = false;
            for (UserObservationHeading existing : user.getUserObservationHeadings()) {
                if (existing.getObservationHeading().getCode().equals(toAdd.getObservationHeading().getCode())) {
                    found = true;
                }
            }
            if (!found) {
                user.getUserObservationHeadings().add(toAdd);
            }
        }

        // manage deletion
        for (UserObservationHeading toDelete : userObservationHeadingsToDelete) {
            userObservationHeadingRepository.deleteById(toDelete.getId());
        }
        user.getUserObservationHeadings().removeAll(userObservationHeadingsToDelete);

        // save updated user
        userRepository.save(user);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateObservationHeadingGroup(org.patientview.api.model.ObservationHeadingGroup observationHeadingGroup)
            throws ResourceNotFoundException, ResourceForbiddenException, ResourceInvalidException {
        ObservationHeading observationHeading
                = observationHeadingRepository.findById(observationHeadingGroup.getObservationHeadingId())
                .orElseThrow(() -> new ResourceNotFoundException("Observation Heading does not exist"));

        Group group = groupRepository.findById(observationHeadingGroup.getGroupId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Group does not exist"));

        // only global admin or specialty admin with correct group role can remove
        if (!ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                && !ApiUtil.doesContainGroupAndRole(group.getId(), RoleName.SPECIALTY_ADMIN)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        long maxPanel = observationHeadingRepository.findMaxPanelNumber();
        if (!validPanel(maxPanel, observationHeadingGroup.getPanel())) {
            throw new ResourceInvalidException("The maximum allowable value for field 'Panel' is " + (maxPanel + 1));
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

    /**
     * Helper to validate the selected panel number is not outside range.
     * The number must be consecutive from the max panel number.
     *
     * @param maxPanel
     * @param selectedPanel
     * @return
     */
    private boolean validPanel(long maxPanel, Long selectedPanel) {
        if (selectedPanel != null && selectedPanel > (maxPanel + 1)) {
            return false;
        }
        return true;
    }
}
