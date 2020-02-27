package org.patientview.api.service.impl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.model.BaseGroup;
import org.patientview.api.service.GroupService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.LookupTypes;
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
import org.patientview.util.Util;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

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
    private EntityManager entityManager;

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    @Inject
    private FhirResource fhirResource;

    @CacheEvict(value = "findAllPublic", allEntries = true)
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

    @CacheEvict(value = "findAllPublic", allEntries = true)
    public void addChildGroup(Long groupId, Long childGroupId) {
        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(childGroupId);

        createRelationship(sourceGroup, objectGroup, RelationshipTypes.CHILD);
        createRelationship(objectGroup, sourceGroup, RelationshipTypes.PARENT);
    }

    @CacheEvict(value = "findAllPublic", allEntries = true)
    public void addFeature(Long groupId, Long featureId) {
        GroupFeature groupFeature = new GroupFeature();
        groupFeature.setFeature(featureRepository.findOne(featureId));
        groupFeature.setGroup(groupRepository.findOne(groupId));
        groupFeature.setCreator(userRepository.findOne(1L));
        groupFeatureRepository.save(groupFeature);
    }

    @CacheEvict(value = "findAllPublic", allEntries = true)
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

        FhirDatabaseEntity entity
                = fhirResource.createEntity(organization, ResourceType.Organization.name(), "organization");

        return entity.getLogicalId();
    }

    // Attached the relationship of children groups and parents groups onto Transient objects
    private List<Group> addParentAndChildGroups(List<Group> groups) {
        for (Group group : groups) {
            addSingleParentAndChildGroup(group);
        }
        return groups;
    }

    @CacheEvict(value = "findAllPublic", allEntries = true)
    public void addParentGroup(Long groupId, Long parentGroupId) {
        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(parentGroupId);

        createRelationship(sourceGroup, objectGroup, RelationshipTypes.PARENT);
        createRelationship(objectGroup, sourceGroup, RelationshipTypes.CHILD);
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

    private List<org.patientview.api.model.Group> convertGroupsToTransportGroups(List<Group> groups) {
        List<org.patientview.api.model.Group> transportGroups = new ArrayList<>();

        for (Group group : groups) {
            // do not add groups that have code in GroupCode enum as these are used for patient entered results etc
            if (!ApiUtil.isInEnum(group.getCode(), HiddenGroupCodes.class)) {
                transportGroups.add(new org.patientview.api.model.Group(group));
            }
        }

        return transportGroups;
    }

    private List<org.patientview.api.model.Group> convertToTransportGroups(List<Group> groups) {
        List<org.patientview.api.model.Group> transportGroups = new ArrayList<>();
        for (Group group : groups) {
            transportGroups.add(new org.patientview.api.model.Group(group));
        }
        return transportGroups;
    }

    private GroupRelationship createRelationship(Group sourceGroup, Group objectGroup,
                                                 RelationshipTypes relationshipType) {
        GroupRelationship groupRelationship = new GroupRelationship();
        groupRelationship.setSourceGroup(sourceGroup);
        groupRelationship.setObjectGroup(objectGroup);
        groupRelationship.setRelationshipType(relationshipType);
        return groupRelationshipRepository.save(groupRelationship);
    }

    public void delete(Long id) {
        LOG.info("Delete " + id + " not Implemented");
    }

    @CacheEvict(value = "findAllPublic", allEntries = true)
    public void deleteChildGroup(Long groupId, Long childGroupId) {
        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(childGroupId);

        deleteRelationship(sourceGroup, objectGroup, RelationshipTypes.CHILD);
        deleteRelationship(objectGroup, sourceGroup, RelationshipTypes.PARENT);
    }

    @CacheEvict(value = "findAllPublic", allEntries = true)
    public void deleteFeature(Long groupId, Long featureId) {
        groupFeatureRepository.delete(groupFeatureRepository.findByGroupAndFeature(
                groupRepository.findOne(groupId), featureRepository.findOne(featureId)));
    }

    @CacheEvict(value = "findAllPublic", allEntries = true)
    public void deleteParentGroup(Long groupId, Long parentGroupId) {
        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(parentGroupId);

        deleteRelationship(sourceGroup, objectGroup, RelationshipTypes.PARENT);
        deleteRelationship(objectGroup, sourceGroup, RelationshipTypes.CHILD);
    }

    private void deleteRelationship(Group sourceGroup, Group objectGroup, RelationshipTypes relationshipType) {
        groupRelationshipRepository.deleteBySourceObjectRelationshipType(sourceGroup, objectGroup, relationshipType);
    }

    public List<Group> findAll() {
        List<Group> groups = Util.convertIterable(groupRepository.findAll());
        return addParentAndChildGroups(groups);
    }

    @Cacheable(value = "findAllPublic")
    @Override
    public List<org.patientview.api.model.Group> findAllPublic() {
        List<org.patientview.api.model.Group> groups = convertToTransportGroups(
                addParentAndChildGroups(groupRepository.findAll()));

        // remove unneeded fields
        for (org.patientview.api.model.Group group : groups) {
            // group features required for creating membership requests
            //group.setGroupFeatures(null);
            group.setChildGroups(null);
            group.setLinks(null);
            group.setLocations(null);
            group.setLastImportDate(null);
        }

        return groups;
    }

    @Override
    public Group findByCode(String code) {
        return groupRepository.findByCode(code);
    }

    @Override
    public List<Group> findChildren(Long groupId) throws ResourceNotFoundException {
        Group group = groupRepository.findOne(groupId);

        if (group == null) {
            throw new ResourceNotFoundException(String.format("The group id %d is not valid", groupId));
        }

        return groupRepository.findChildren(group);
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

        Set<Group> groups = new HashSet<>();

        if (ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)) {
            // GLOBAL_ADMIN can reach all groups
            groups = new HashSet<>(Util.convertIterable(groupRepository.findAll()));
        } else if (ApiUtil.currentUserHasRole(RoleName.SPECIALTY_ADMIN)) {
            // SPECIALTY_ADMIN gets groups and child groups if available
            List<Group> parentGroups = Util.convertIterable(groupRepository.findGroupByUser(entityUser));
            parentGroups = addParentAndChildGroups(parentGroups);

            // add child groups
            for (Group parentGroup : parentGroups) {
                groups.addAll(findChildren(parentGroup.getId()));
            }

            // add CENTRAL_SUPPORT groups (similar to specialties but with no children)
            groups.addAll(getSupportGroups());
        } else if (ApiUtil.currentUserHasRole(RoleName.PATIENT)) {
            // PATIENT do not add specialty type groups
            List<Group> parentGroups = Util.convertIterable(groupRepository.findGroupByUser(entityUser));
            for (Group parentGroup : parentGroups) {
                if (!parentGroup.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                    groups.add(parentGroup);
                }
            }
        } else if (ApiUtil.currentUserHasRole(RoleName.UNIT_ADMIN)) {
            // UNIT_ADMIN get all groups (participant list is secured later)
            groups.addAll(groupRepository.findAll());
        } else {
            // STAFF_ADMIN, DISEASE_GROUP_ADMIN get all group types by user
            groups.addAll(Util.convertIterable(groupRepository.findGroupByUser(entityUser)));

            // add CENTRAL_SUPPORT groups (similar to specialties but with no children)
            groups.addAll(getSupportGroups());
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

    public Group get(Long id) throws ResourceForbiddenException {
        return addSingleParentAndChildGroup(groupRepository.findOne(id));
    }

    // TODO: this behaviour may need to be changed later to support cohorts and other parent type groups
    public Page<org.patientview.api.model.Group> getAllowedRelationshipGroups(Long userId) {
        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);

        if (ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN, RoleName.SPECIALTY_ADMIN)) {

            Page<Group> groupList = groupRepository.findAll("%%", new PageRequest(0, Integer.MAX_VALUE));

            // convert to lightweight transport objects, create Page and return
            List<org.patientview.api.model.Group> transportContent
                    = convertGroupsToTransportGroups(groupList.getContent());
            return new PageImpl<>(transportContent, pageable, groupList.getTotalElements());
        }

        return new PageImpl<>(new ArrayList<org.patientview.api.model.Group>(), pageable, 0L);
    }

    public List<Group> getAllUserGroupsAllDetails(Long userId) {

        Page<Group> groupPage = getUserGroupsData(userId, new GetParameters());
        if (groupPage == null) {
            return new ArrayList<>();
        }

        // add parent and child groups
        return addParentAndChildGroups(groupPage.getContent());
    }

    @Override
    public List<org.patientview.api.model.Group> getByFeature(String featureName)
            throws ResourceNotFoundException, ResourceForbiddenException {

        Feature feature = featureRepository.findByName(featureName);
        if (feature == null) {
            throw new ResourceNotFoundException("Feature not found");
        }

        List<org.patientview.api.model.Group> groups
                = convertGroupsToTransportGroups(groupRepository.findByFeature(feature));

        // remove unneeded fields
        for (org.patientview.api.model.Group group : groups) {
            group.setVisible(null);
            // group features required for creating membership requests
            //group.setGroupFeatures(null);
            group.setChildGroups(null);
            group.setLinks(null);
            group.setLocations(null);
            group.setLastImportDate(null);
        }

        return groups;
    }

    public List<UUID> getOrganizationLogicalUuidsByCode(final String code) throws FhirResourceException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id ");
        query.append("FROM organization ");
        query.append("WHERE content #> '{identifier,0}' -> 'value' = '\"");
        query.append(code);
        query.append("\"' ");

        // execute and return UUIDs
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());

            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

            connection.close();
            return uuids;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e2) {
                    throw new FhirResourceException(e2);
                }
            }
            throw new FhirResourceException(e);
        }
    }

    private List<Group> getSupportGroups() {
        List<Group> groups = new ArrayList<>();
        List<Long> groupTypes = new ArrayList<>();
        Lookup lookup = lookupRepository.findByTypeAndValue(LookupTypes.GROUP, "CENTRAL_SUPPORT");
        if (lookup != null) {
            groupTypes.add(lookup.getId());
            Page<Group> supportGroups
                    = groupRepository.findAllByGroupType("%%", groupTypes, new PageRequest(0, Integer.MAX_VALUE));
            if (!supportGroups.getContent().isEmpty()) {
                groups.addAll(supportGroups.getContent());
            }
        }
        return groups;
    }

    public Page<org.patientview.api.model.Group> getUserGroups(Long userId, GetParameters getParameters) {
        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        PageRequest pageable = createPageRequest(pageConverted, sizeConverted, sortField, sortDirection);

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
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        PageRequest pageable = createPageRequest(pageConverted, sizeConverted, sortField, sortDirection);

        Page<Group> groupPage = getUserGroupsData(userId, getParameters);
        if (groupPage == null) {
            return new PageImpl<>(new ArrayList<Group>(), pageable, 0L);
        }

        // add parent and child groups
        List<Group> content = addParentAndChildGroups(groupPage.getContent());
        return new PageImpl<>(content, pageable, groupPage.getTotalElements());
    }

    private Page<Group> getUserGroupsData(Long userId, GetParameters getParameters) {
        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        String[] groupTypes = getParameters.getGroupTypes();
        String filterText = getParameters.getFilterText();
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        PageRequest pageable = createPageRequest(pageConverted, sizeConverted, sortField, sortDirection);

        List<Long> groupTypesList = convertStringArrayToLongs(groupTypes);

        if (StringUtils.isEmpty(filterText)) {
            filterText = "%%";
        } else {
            filterText = "%" + filterText.trim().toUpperCase() + "%";
        }
        Page<Group> groupPage;
        User user = userRepository.findOne(userId);
        boolean groupTypesNotEmpty = ArrayUtils.isNotEmpty(groupTypes);

        if (ApiUtil.userHasRole(user, RoleName.GLOBAL_ADMIN)) {
            if (groupTypesNotEmpty) {
                groupPage = groupRepository.findAllByGroupType(filterText, groupTypesList, pageable);
            } else {
                groupPage = groupRepository.findAll(filterText, pageable);
            }
        } else if (ApiUtil.userHasRole(user, RoleName.SPECIALTY_ADMIN)) {
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

    private boolean groupExists(Group group) {
        return groupRepository.findByCode(group.getCode()) != null;
    }

    @Override
    public boolean groupIdIsSupportGroup(Long groupId) throws ResourceNotFoundException {
        List<Group> supportGroups = getSupportGroups();
        for (Group group : supportGroups) {
            if (group.getId().equals(groupId)) {
                return true;
            }
        }
        return false;
    }

    @CacheEvict(value = "findAllPublic", allEntries = true)
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
        if (ApiUtil.doesContainGroupAndRole(entityGroup.getId(), RoleName.UNIT_ADMIN)
                && !lookupRepository.findOne(group.getGroupType().getId()).equals(entityGroup.getGroupType())) {
            throw new ResourceForbiddenException("Unit Admin cannot change group type");
        }

        // gp admin cannot change group type
        if (ApiUtil.doesContainGroupAndRole(entityGroup.getId(), RoleName.GP_ADMIN)
                && !lookupRepository.findOne(group.getGroupType().getId()).equals(entityGroup.getGroupType())) {
            throw new ResourceForbiddenException("GP Admin cannot change group type");
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
        entityGroup.setNoDataFeed(group.getNoDataFeed());
        groupRepository.save(entityGroup);
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
}
