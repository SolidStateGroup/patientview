package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.service.ObservationService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.AlertObservationHeading;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.FhirLink;
import org.patientview.api.model.FhirObservation;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.patientview.persistence.model.ResultCluster;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserObservationHeading;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.AlertObservationHeadingRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.ObservationHeadingGroupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.ResultClusterRepository;
import org.patientview.persistence.repository.UserObservationHeadingRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.persistence.EntityExistsException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
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
    private ObservationService observationService;

    @Inject
    private EmailService emailService;

    @Inject
    private ObservationHeadingRepository observationHeadingRepository;

    @Inject
    private UserObservationHeadingRepository userObservationHeadingRepository;

    @Inject
    private ObservationHeadingGroupRepository observationHeadingGroupRepository;

    @Inject
    private AlertObservationHeadingRepository alertObservationHeadingRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private ResultClusterRepository resultClusterRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    @Inject
    private Properties properties;

    private static final Long FIRST_PANEL = 1L;
    private static final Long DEFAULT_COUNT = 3L;

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
                        String code = results.getString(1).replace("\"", "").toLowerCase();
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

    @Override
    public void saveObservationHeadingSelection(Long userId, String[] codes) throws ResourceNotFoundException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

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
            userObservationHeadingRepository.delete(toDelete.getId());
        }
        user.getUserObservationHeadings().removeAll(userObservationHeadingsToDelete);

        // save updated user
        userRepository.save(user);
    }

    @Override
    public List<ObservationHeading> getAvailableAlertObservationHeadings(Long userId)
            throws ResourceNotFoundException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<Group> userGroups = Util.convertIterable(groupRepository.findGroupByUser(user));

        List<ObservationHeading> observationHeadings = findAll();

        Set<ObservationHeading> availableObservationHeadings = new HashSet<>();

        for (ObservationHeading observationHeading : observationHeadings) {
            for (ObservationHeadingGroup observationHeadingGroup : observationHeading.getObservationHeadingGroups()) {
                if (userGroups.contains(observationHeadingGroup.getGroup())) {
                    observationHeading.setObservationHeadingGroups(new HashSet<ObservationHeadingGroup>());
                    availableObservationHeadings.add(observationHeading);
                }
            }
        }

        return new ArrayList<>(availableObservationHeadings);
    }

    @Override
    public List<org.patientview.api.model.AlertObservationHeading> getAlertObservationHeadings(Long userId)
            throws ResourceNotFoundException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<AlertObservationHeading> alertObservationHeadings = alertObservationHeadingRepository.findByUser(user);
        List<org.patientview.api.model.AlertObservationHeading> transportAlertObservationHeadings = new ArrayList<>();

        for (AlertObservationHeading alertObservationHeading : alertObservationHeadings) {
            transportAlertObservationHeadings.add(
                    new org.patientview.api.model.AlertObservationHeading(alertObservationHeading, user));
        }

        return transportAlertObservationHeadings;
    }

    @Override
    public void addAlertObservationHeading(Long userId,
                                           org.patientview.api.model.AlertObservationHeading alertObservationHeading)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        ObservationHeading observationHeading
                = observationHeadingRepository.findOne(alertObservationHeading.getObservationHeading().getId());
        if (observationHeading == null) {
            throw new ResourceNotFoundException("Could not find result type");
        }

        List<FhirObservation> fhirObservations
                = observationService.get(user.getId(), observationHeading.getCode(), "appliesDateTime", "DESC", 1L);

        AlertObservationHeading newAlertObservationHeading = new AlertObservationHeading();

        if (!CollectionUtils.isEmpty(fhirObservations)) {
            newAlertObservationHeading.setLatestObservationValue(fhirObservations.get(0).getValue());
            newAlertObservationHeading.setLatestObservationDate(fhirObservations.get(0).getApplies());
        }

        newAlertObservationHeading.setUser(user);
        newAlertObservationHeading.setObservationHeading(observationHeading);
        newAlertObservationHeading.setWebAlert(alertObservationHeading.isWebAlert());
        newAlertObservationHeading.setWebAlertViewed(true);
        newAlertObservationHeading.setEmailAlert(alertObservationHeading.isEmailAlert());
        newAlertObservationHeading.setEmailAlertSent(true);
        newAlertObservationHeading.setCreated(new Date());
        newAlertObservationHeading.setCreator(user);

        alertObservationHeadingRepository.save(newAlertObservationHeading);
    }

    @Override
    public void removeAlertObservationHeading(Long userId, Long alertObservationHeadingId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        AlertObservationHeading alertObservationHeading
                = alertObservationHeadingRepository.findOne(alertObservationHeadingId);
        if (alertObservationHeading == null) {
            throw new ResourceNotFoundException("Could not find alert");
        }

        if (!user.getId().equals(alertObservationHeading.getUser().getId())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        alertObservationHeadingRepository.delete(alertObservationHeading);
    }

    @Override
    public void updateAlertObservationHeading(Long userId,
                                              org.patientview.api.model.AlertObservationHeading alertObservationHeading)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        AlertObservationHeading entityAlertObservationHeading
                = alertObservationHeadingRepository.findOne(alertObservationHeading.getId());
        if (entityAlertObservationHeading == null) {
            throw new ResourceNotFoundException("Could not find alert");
        }

        if (!user.getId().equals(entityAlertObservationHeading.getUser().getId())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        entityAlertObservationHeading.setWebAlert(alertObservationHeading.isWebAlert());
        entityAlertObservationHeading.setWebAlertViewed(alertObservationHeading.isWebAlertViewed());
        entityAlertObservationHeading.setEmailAlert(alertObservationHeading.isEmailAlert());

        alertObservationHeadingRepository.save(entityAlertObservationHeading);
    }

    @Override
    public void sendAlertObservationHeadingEmails() {

        List<AlertObservationHeading> alertObservationHeadings
                = alertObservationHeadingRepository.findByEmailAlertSetAndNotSent();

        Set<String> emailAddresses = new HashSet<>();

        for (AlertObservationHeading alertObservationHeading : alertObservationHeadings) {
            String email =
                    alertObservationHeadingRepository.findOne(alertObservationHeading.getId()).getUser().getEmail();
            if (StringUtils.isNotEmpty(email)) {
                emailAddresses.add(email);
            }
        }

        if (!CollectionUtils.isEmpty(emailAddresses)) {
            Email email = new Email();
            email.setBcc(true);
            email.setSenderEmail(properties.getProperty("smtp.sender.email"));
            email.setSenderName(properties.getProperty("smtp.sender.name"));
            email.setRecipients(emailAddresses.toArray(new String[emailAddresses.size()]));
            email.setSubject("PatientView - You have new results");

            StringBuilder sb = new StringBuilder();
            sb.append("Dear user");
            sb.append(", <br/><br/>New test results have arrived on <a href=\"");
            sb.append(properties.getProperty("site.url"));
            sb.append("\">PatientView</a>");
            sb.append("<br/><br/>Please login to see them.<br/>");
            email.setBody(sb.toString());

            try {
                emailService.sendEmail(email);
                for (AlertObservationHeading alertObservationHeading : alertObservationHeadings) {
                    alertObservationHeading.setEmailAlertSent(true);
                    alertObservationHeadingRepository.save(alertObservationHeading);
                }
            } catch (MessagingException | MailException me) {
                LOG.error("Could not bulk send alert emails: ", me);
            }
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
        entity.setDecimalPlaces(input.getDecimalPlaces());

        return observationHeadingRepository.save(entity);
    }

    private boolean observationHeadingExists(ObservationHeading observationHeading) {
        return !observationHeadingRepository.findByCode(observationHeading.getCode()).isEmpty();
    }
}
