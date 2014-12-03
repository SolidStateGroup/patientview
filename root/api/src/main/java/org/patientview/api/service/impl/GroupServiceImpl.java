package org.patientview.api.service.impl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Organization;
import org.patientview.api.model.BaseGroup;
import org.patientview.persistence.model.Email;
import org.patientview.api.model.UnitRequest;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.GroupService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.ContactPointRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRelationshipRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.LocationRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
@Service
public class GroupServiceImpl extends AbstractServiceImpl<GroupServiceImpl> implements GroupService {

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private GroupFeatureRepository groupFeatureRepository;

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private LinkRepository linkRepository;

    @Inject
    private ContactPointRepository contactPointRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupRelationshipRepository groupRelationshipRepository;

    @Inject
    private EmailService emailService;

    @Inject
    private EntityManager entityManager;

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private Properties properties;

    /**
     * Get all the groups and put the children and parents into the transient objects
     *
     * @return list of groups
     */
    public List<Group> findAll() {
        List<Group> groups = Util.convertIterable(groupRepository.findAll());
        return addParentAndChildGroups(groups);
    }

    private List<org.patientview.api.model.Group> convertToTransportGroups(List<Group> groups) {
        List<org.patientview.api.model.Group> transportGroups = new ArrayList<>();
        for (Group group : groups) {
            transportGroups.add(new org.patientview.api.model.Group(group));
        }
        return transportGroups;
    }

    @Override
    public List<org.patientview.api.model.Group> findAllPublic() {
        List<org.patientview.api.model.Group> groups = convertToTransportGroups(
                addParentAndChildGroups(groupRepository.findAllVisibleToJoin()));

        // remove unneeded fields (features etc)
        for (org.patientview.api.model.Group group : groups) {
            group.setVisible(null);
            group.setGroupFeatures(null);
            group.setChildGroups(null);
            group.setLinks(null);
            group.setLocations(null);
        }

        return groups;
    }

    @Override
    public Group findByCode(String code) {
        return groupRepository.findByCode(code);
    }

    public Group get(Long id) throws ResourceForbiddenException {
        return addSingleParentAndChildGroup(groupRepository.findOne(id));
    }

    public List<Group> findGroupsByUser(User user) {
        List<Group> groups = Util.convertIterable(groupRepository.findGroupByUser(user));
        return addParentAndChildGroups(groups);
    }

    @Override
    public List<BaseGroup> findMessagingGroupsByUserId(Long userId) throws ResourceNotFoundException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException("User does not exist");
        }

        List<Group> groups = new ArrayList<>();

        if (doesContainRoles(RoleName.GLOBAL_ADMIN)) {
            // GLOBAL_ADMIN can reach all groups
            groups = Util.convertIterable(groupRepository.findAll());
        } else if (doesContainRoles(RoleName.SPECIALTY_ADMIN)) {
            // SPECIALTY_ADMIN gets groups and child groups if available
            List<Group> parentGroups = Util.convertIterable(groupRepository.findGroupByUser(entityUser));
            parentGroups = addParentAndChildGroups(parentGroups);

            // add child groups
            Set<Group> groupSet = new HashSet<>();
            for (Group parentGroup : parentGroups) {
                groupSet.addAll(findChildren(parentGroup.getId()));
            }

            groups = new ArrayList<>(groupSet);
        } else {
            // UNIT_ADMIN, STAFF_ADMIN, DISEASE_GROUP_ADMIN PATIENT do not add specialty type groups
            List<Group> parentGroups = Util.convertIterable(groupRepository.findGroupByUser(entityUser));
            for (Group parentGroup : parentGroups) {
                if (!parentGroup.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                    groups.add(parentGroup);
                }
            }
        }

        // keep only groups with MESSAGING feature and convert to base groups
        Set<BaseGroup> baseGroups = new HashSet<>();
        for (Group group : groups) {
            for (GroupFeature groupFeature : group.getGroupFeatures()) {
                if (groupFeature.getFeature().getName().equals(FeatureType.MESSAGING.toString())) {
                    baseGroups.add(new BaseGroup(group));
                }
            }
        }
        return new ArrayList<>(baseGroups);
    }

    public void save(Group group) throws ResourceNotFoundException, EntityExistsException, ResourceForbiddenException {
        Group entityGroup = groupRepository.findOne(group.getId());

        if (entityGroup == null) {
            throw new ResourceNotFoundException(String.format("Could not find group %s", group.getId()));
        }

        // check if another group with this code exists
        Group existingGroup = groupRepository.findByCode(group.getCode());
        if (groupExists(group) && !(entityGroup.getId().equals(existingGroup.getId()))) {
            throw new EntityExistsException("Group already exists with this code");
        }

        // unit admin cannot change group type
        if (Util.doesContainGroupAndRole(entityGroup.getId(), RoleName.UNIT_ADMIN)
            && !lookupRepository.findOne(group.getGroupType().getId()).equals(entityGroup.getGroupType())) {
            throw new ResourceForbiddenException("Unit Admin cannot change group type");
        }

        entityGroup.setCode(group.getCode());
        entityGroup.setName(group.getName());
        entityGroup.setShortName(group.getShortName());
        entityGroup.setGroupType(lookupRepository.findOne(group.getGroupType().getId()));
        entityGroup.setSftpUser(group.getSftpUser());
        entityGroup.setAddress1(group.getAddress1());
        entityGroup.setAddress2(group.getAddress2());
        entityGroup.setAddress3(group.getAddress3());
        entityGroup.setPostcode(group.getPostcode());
        entityGroup.setVisibleToJoin(group.getVisibleToJoin());
        groupRepository.save(entityGroup);
    }

    private boolean groupExists(Group group) {
        return groupRepository.findByCode(group.getCode()) != null;
    }

    /**
     * TODO remove links, relationships, locations, and features SPRINT 2
     *
     * @param group
     * @return
     * @throws javax.persistence.EntityExistsException
     */
    public Long add(Group group) throws EntityExistsException {
        Group newGroup;

        Set<Link> links;
        // get links and features, avoid persisting until group created successfully
        if (!CollectionUtils.isEmpty(group.getLinks())) {
            links = new HashSet<>(group.getLinks());
            group.getLinks().clear();
        } else {
            links = new HashSet<>();
        }

        Set<Location> locations;
        if (!CollectionUtils.isEmpty(group.getLocations())) {
            locations = new HashSet<>(group.getLocations());
            group.getLocations().clear();
        } else {
            locations = new HashSet<>();
        }

        Set<GroupFeature> groupFeatures;
        if (!CollectionUtils.isEmpty(group.getGroupFeatures())) {
            groupFeatures = new HashSet<>(group.getGroupFeatures());
            group.getGroupFeatures().clear();
        } else {
            groupFeatures = new HashSet<>();
        }

        Set<ContactPoint> contactPoints;
        if (!CollectionUtils.isEmpty(group.getContactPoints())) {
            contactPoints = new HashSet<>(group.getContactPoints());
            group.getContactPoints().clear();
        } else {
            contactPoints = new HashSet<>();
        }

        // save basic details, checking if identical group already exists
        if (groupExists(group)) {
            LOG.debug("Group not created, Group already exists with these details");
            throw new EntityExistsException("Group already exists with these details");
        }

        group.setVisible(true);
        newGroup = groupRepository.save(group);

        // Group Relationships
        saveGroupRelationships(newGroup);

        // save links
        for (Link link : links) {
            link.setGroup(newGroup);
            link = linkRepository.save(link);
            newGroup.getLinks().add(link);
        }

        // save locations
        for (Location location : locations) {
            location.setGroup(newGroup);
            location = locationRepository.save(location);
            newGroup.getLocations().add(location);
        }

        // save features
        for (GroupFeature groupFeature : groupFeatures) {
            GroupFeature tempGroupFeature = new GroupFeature();
            tempGroupFeature.setFeature(featureRepository.findOne(groupFeature.getFeature().getId()));
            tempGroupFeature.setGroup(newGroup);
            tempGroupFeature.setCreator(userRepository.findOne(getCurrentUser().getId()));
            tempGroupFeature = groupFeatureRepository.save(tempGroupFeature);
            newGroup.getGroupFeatures().add(tempGroupFeature);
        }

        // save contact points
        for (ContactPoint contactPoint : contactPoints) {
            ContactPoint tempContactPoint = new ContactPoint();
            tempContactPoint.setGroup(newGroup);
            tempContactPoint.setCreator(userRepository.findOne(getCurrentUser().getId()));
            tempContactPoint.setContactPointType(entityManager.find(ContactPointType.class,
                    contactPoint.getContactPointType().getId()));
            tempContactPoint.setContent(contactPoint.getContent());
            tempContactPoint = contactPointRepository.save(tempContactPoint);
            newGroup.getContactPoints().add(tempContactPoint);
        }

        return newGroup.getId();
    }


    private void saveGroupRelationships(Group group) {

        // delete existing groups
        groupRelationshipRepository.deleteBySourceGroup(group);

        Group sourceGroup = groupRepository.findOne(group.getId());

        // Create a two way relationship; if a parent is a child, the inverse is also true
        if (!CollectionUtils.isEmpty(group.getParentGroups())) {
            for (Group parentGroup : group.getParentGroups()) {

                Group objectGroup = groupRepository.findOne(parentGroup.getId());
                createRelationship(sourceGroup, objectGroup, RelationshipTypes.PARENT);
                createRelationship(objectGroup, sourceGroup, RelationshipTypes.CHILD);
            }
        }
        if (!CollectionUtils.isEmpty(group.getChildGroups())) {
            for (Group childGroup : group.getChildGroups()) {

                Group objectGroup = groupRepository.findOne(childGroup.getId());
                createRelationship(sourceGroup, objectGroup, RelationshipTypes.CHILD);
                createRelationship(objectGroup, sourceGroup, RelationshipTypes.PARENT);
            }
        }
    }

    private GroupRelationship createRelationship(Group sourceGroup, Group objectGroup,
                                                 RelationshipTypes relationshipType) {
        GroupRelationship groupRelationship = new GroupRelationship();
        groupRelationship.setSourceGroup(sourceGroup);
        groupRelationship.setObjectGroup(objectGroup);
        groupRelationship.setRelationshipType(relationshipType);
        return groupRelationshipRepository.save(groupRelationship);
    }

    private void deleteRelationship(Group sourceGroup, Group objectGroup, RelationshipTypes relationshipType) {
        groupRelationshipRepository.deleteBySourceObjectRelationshipType(sourceGroup, objectGroup, relationshipType);
    }

    private Group addSingleParentAndChildGroup(Group group) {
        // TODO Move this to PostConstruct sort out Transaction scope;

        List<Group> parentGroups = new ArrayList<>();
        List<Group> childGroups = new ArrayList<>();

        if (!CollectionUtils.isEmpty(group.getGroupRelationships())) {
            for (GroupRelationship groupRelationship : group.getGroupRelationships()) {

                if (groupRelationship.getRelationshipType() == RelationshipTypes.PARENT) {
                    Group parentGroup = new Group();
                    BeanUtils.copyProperties(groupRelationship.getObjectGroup(), parentGroup);
                    parentGroups.add(parentGroup);
                }

                if (groupRelationship.getRelationshipType() == RelationshipTypes.CHILD) {
                    Group childGroup = new Group();
                    BeanUtils.copyProperties(groupRelationship.getObjectGroup(), childGroup);
                    childGroups.add(childGroup);
                }
            }
        }

        group.setParentGroups(parentGroups);
        group.setChildGroups(childGroups);
        return group;
    }

    // Attached the relationship of children groups and parents groups onto Transient objects
    public List<Group> addParentAndChildGroups(List<Group> groups) {

        for (Group group : groups) {
            addSingleParentAndChildGroup(group);
        }

        return groups;

    }

    public void addParentGroup(Long groupId, Long parentGroupId) {
        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(parentGroupId);

        createRelationship(sourceGroup, objectGroup, RelationshipTypes.PARENT);
        createRelationship(objectGroup, sourceGroup, RelationshipTypes.CHILD);
    }

    public void deleteParentGroup(Long groupId, Long parentGroupId) {
        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(parentGroupId);

        deleteRelationship(sourceGroup, objectGroup, RelationshipTypes.PARENT);
        deleteRelationship(objectGroup, sourceGroup, RelationshipTypes.CHILD);
    }

    public void addChildGroup(Long groupId, Long childGroupId) {
        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(childGroupId);

        createRelationship(sourceGroup, objectGroup, RelationshipTypes.CHILD);
        createRelationship(objectGroup, sourceGroup, RelationshipTypes.PARENT);
    }

    public void deleteChildGroup(Long groupId, Long childGroupId) {
        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(childGroupId);

        deleteRelationship(sourceGroup, objectGroup, RelationshipTypes.CHILD);
        deleteRelationship(objectGroup, sourceGroup, RelationshipTypes.PARENT);
    }

    public void addFeature(Long groupId, Long featureId) {
        GroupFeature groupFeature = new GroupFeature();
        groupFeature.setFeature(featureRepository.findOne(featureId));
        groupFeature.setGroup(groupRepository.findOne(groupId));
        groupFeature.setCreator(userRepository.findOne(1L));
        groupFeatureRepository.save(groupFeature);
    }

    public void deleteFeature(Long groupId, Long featureId) {
        groupFeatureRepository.delete(groupFeatureRepository.findByGroupAndFeature(
                groupRepository.findOne(groupId), featureRepository.findOne(featureId)));
    }

    public List<Group> findChildren(Long groupId) throws ResourceNotFoundException {
        Group group = groupRepository.findOne(groupId);

        if (group == null) {
            throw new ResourceNotFoundException(String.format("The group id %d is not valid", groupId));
        }

        return Util.convertIterable(groupRepository.findChildren(group));
    }

    // Second stage of forgotten password, if username or email have been forgotten
    public void passwordRequest(Long groupId, UnitRequest unitRequest)
            throws ResourceNotFoundException, MailException, MessagingException {
        Group group = findGroup(groupId);

        if (group == null) {
            throw new ResourceNotFoundException("The unit has not been found");
        }

        Email email = createPasswordRequestEmail(unitRequest, group);
        ContactPoint contactPoint = getContactPoint(group.getContactPoints(), ContactPointTypes.PV_ADMIN_EMAIL);

        if (contactPoint == null) {
            throw new ResourceNotFoundException("Unable to find contact email for this unit");
        } else {
            email.setRecipients(new String[]{contactPoint.getContent()});
        }
        emailService.sendEmail(email);
    }

    public void delete(Long id) {
        LOG.info("Delete " + id + " not Implemented");
    }

    public UUID addOrganization(Group group) throws FhirResourceException {
        Organization organization = new Organization();

        Identifier identifier = organization.addIdentifier();
        identifier.setValueSimple(group.getCode());
        identifier.setLabelSimple("CODE");

        Address address = organization.addAddress();
        address.addLineSimple(group.getAddress1());
        address.setCitySimple(group.getAddress2());
        address.setStateSimple(group.getAddress3());
        address.setZipSimple(group.getPostcode());

        Contact telephone = organization.addTelecom();
        telephone.setSystem(new Enumeration(Contact.ContactSystem.phone));

        Contact email = organization.addTelecom();
        email.setSystem(new Enumeration(Contact.ContactSystem.email));

        return FhirResource.getLogicalId(fhirResource.create(organization));
    }

    public List<UUID> getOrganizationLogicalUuidsByCode(final String code) throws FhirResourceException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id ");
        query.append("FROM organization ");
        query.append("WHERE content #> '{identifier,0}' -> 'value' = '\"");
        query.append(code);
        query.append("\"' ");

        // execute and return UUIDs
        try {
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());

            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

            connection.close();
            return uuids;
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        }
    }

    public Page<org.patientview.api.model.Group> getUserGroups(Long userId, GetParameters getParameters) {
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

        Page<Group> groupPage = getUserGroupsData(userId, getParameters);
        if (groupPage == null) {
            return new PageImpl<>(new ArrayList<org.patientview.api.model.Group>(), pageable, 0L);
        }

        // add parent and child groups
        List<Group> content = addParentAndChildGroups(groupPage.getContent());

        // convert to lightweight transport objects, create Page and return
        List<org.patientview.api.model.Group> transportContent = convertGroupsToTransportGroups(content);
        return new PageImpl<>(transportContent, pageable, groupPage.getTotalElements());
    }

    public Page<Group> getUserGroupsAllDetails(Long userId, GetParameters getParameters) {
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

        Page<Group> groupPage = getUserGroupsData(userId, getParameters);
        if (groupPage == null) {
            return new PageImpl<>(new ArrayList<Group>(), pageable, 0L);
        }

        // add parent and child groups
        List<Group> content = addParentAndChildGroups(groupPage.getContent());
        return new PageImpl<>(content, pageable, groupPage.getTotalElements());
    }

    public List<Group> getAllUserGroupsAllDetails(Long userId) {

        Page<Group> groupPage = getUserGroupsData(userId, new GetParameters());
        if (groupPage == null) {
            return new ArrayList<>();
        }

        // add parent and child groups
        return addParentAndChildGroups(groupPage.getContent());
    }

    // TODO: this behaviour may need to be changed later to support cohorts and other parent type groups
    public Page<org.patientview.api.model.Group> getAllowedRelationshipGroups(Long userId) {
        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);

        if (doesContainRoles(RoleName.GLOBAL_ADMIN, RoleName.SPECIALTY_ADMIN)) {

            Page<Group> groupList = groupRepository.findAll("%%", new PageRequest(0, Integer.MAX_VALUE));

            // convert to lightweight transport objects, create Page and return
            List<org.patientview.api.model.Group> transportContent
                    = convertGroupsToTransportGroups(groupList.getContent());
            return new PageImpl<>(transportContent, pageable, groupList.getTotalElements());
        }

        return new PageImpl<>(new ArrayList<org.patientview.api.model.Group>(), pageable, 0L);
    }

    private Group findGroup(Long groupId) throws ResourceNotFoundException {
        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException(String.format("Could not find group %s", groupId));
        }
        return group;
    }

    // Sprint 3 - velocity templates
    // Second stage of forgotten password, if username or email have been forgotten
    private Email createPasswordRequestEmail(UnitRequest unitRequest, Group group) {
        Email email = new Email();
        email.setSenderEmail(properties.getProperty("smtp.sender.email"));
        email.setSenderName(properties.getProperty("smtp.sender.name"));
        email.setSubject("PatientView - Request for Password Reset");

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        String date = sdf.format(unitRequest.getDateOfBirth());

        StringBuilder body = new StringBuilder();
        body.append("Dear Sir/Madam, <br/><br/>");
        body.append("The following user would like to request a password reset on ");
        body.append("<a href=\"");
        body.append(properties.getProperty("site.url"));
        body.append("\">PatientView</a>: ");
        body.append("<br/><br/>Forename: ").append(unitRequest.getForename());
        body.append("<br/>Surname: ").append(unitRequest.getSurname());
        body.append("<br/>DOB: ");
        body.append(date);
        body.append("<br/>NHS Number: ").append(unitRequest.getNhsNumber());
        body.append("<br/>Associated Unit: ").append(group.getName());

        email.setBody(body.toString());
        return email;
    }

    private static ContactPoint getContactPoint(Collection<ContactPoint> contactPoints,
                                                ContactPointTypes contactPointTypes) {
        for (ContactPoint contactPoint: contactPoints) {
            if (contactPoint.getContactPointType().getValue().equals(contactPointTypes)) {
                return contactPoint;
            }
        }
        return null;
    }

    private List<org.patientview.api.model.Group> convertGroupsToTransportGroups(List<Group> groups) {
        List<org.patientview.api.model.Group> transportGroups = new ArrayList<>();

        for (Group group : groups) {
            // do not add groups that have code in GroupCode enum as these are used for patient entered results etc
            if (!Util.isInEnum(group.getCode(), HiddenGroupCodes.class)) {
                transportGroups.add(new org.patientview.api.model.Group(group));
            }
        }

        return transportGroups;
    }

    private Page<Group> getUserGroupsData(Long userId, GetParameters getParameters) {
        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        String[] groupTypes = getParameters.getGroupTypes();
        String filterText = getParameters.getFilterText();

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

        List<Long> groupTypesList = convertStringArrayToLongs(groupTypes);

        if (StringUtils.isEmpty(filterText)) {
            filterText = "%%";
        } else {
            filterText = "%" + filterText.toUpperCase() + "%";
        }
        Page<Group> groupPage;
        User user = userRepository.findOne(userId);
        boolean groupTypesNotEmpty = ArrayUtils.isNotEmpty(groupTypes);

        if (doesContainRoles(RoleName.GLOBAL_ADMIN)) {
            if (groupTypesNotEmpty) {
                groupPage = groupRepository.findAllByGroupType(filterText, groupTypesList, pageable);
            } else {
                groupPage = groupRepository.findAll(filterText, pageable);
            }
        } else if (doesContainRoles(RoleName.SPECIALTY_ADMIN)) {
            if (groupTypesNotEmpty) {
                groupPage = groupRepository.findGroupAndChildGroupsByUserAndGroupType(filterText, groupTypesList,
                        user, pageable);
            } else {
                groupPage = groupRepository.findGroupAndChildGroupsByUser(filterText, user, pageable);
            }
        } else {
            if (groupTypesNotEmpty) {
                groupPage = groupRepository.findGroupsByUserAndGroupTypeNoSpecialties(filterText, groupTypesList,
                        user, pageable);
            } else {
                groupPage = groupRepository.findGroupsByUserNoSpecialties(filterText, user, pageable);
            }
        }
        return groupPage;
    }
}

