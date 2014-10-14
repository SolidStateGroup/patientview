package org.patientview.api.service.impl;

import org.patientview.api.model.Email;
import org.patientview.api.model.UnitRequest;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.GroupService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.repository.ContactPointRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRelationshipRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.LocationRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        return convertToTransportGroups(
                addParentAndChildGroups(groupRepository.findAllVisibleToJoin()));
    }

    @Override
    public Group findByCode(String code) {
        return groupRepository.findByCode(code);
    }

    public Group get(Long id) throws ResourceForbiddenException {
        return addSingleParentAndChildGroup(groupRepository.findOne(id));
    }

    public List<Group> findGroupByUser(User user) {
        List<Group> groups = Util.convertIterable(groupRepository.findGroupByUser(user));
        return addParentAndChildGroups(groups);
    }

    public Group save(Group group) throws ResourceNotFoundException, EntityExistsException, ResourceForbiddenException {
        Group entityGroup = groupRepository.findOne(group.getId());

        if (entityGroup == null) {
            throw new ResourceNotFoundException(String.format("Could not find group %s", group.getId()));
        }

        // check if another group with this code exists
        Group existingGroup = groupRepository.findByCode(group.getCode());
        if (groupExists(group) && !(entityGroup.getId().equals(existingGroup.getId()))) {
            throw new EntityExistsException("Group already exists with this code");
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
        return groupRepository.save(entityGroup);
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
    public Group add(Group group) throws EntityExistsException {
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
            tempGroupFeature.setCreator(userRepository.findOne(1L));
            tempGroupFeature = groupFeatureRepository.save(tempGroupFeature);
            newGroup.getGroupFeatures().add(tempGroupFeature);
        }

        // save contact points
        for (ContactPoint contactPoint : contactPoints) {
            ContactPoint tempContactPoint = new ContactPoint();
            tempContactPoint.setGroup(newGroup);
            tempContactPoint.setCreator(userRepository.findOne(1L));
            tempContactPoint.setContactPointType(entityManager.find(ContactPointType.class,
                    contactPoint.getContactPointType().getId()));
            tempContactPoint.setContent(contactPoint.getContent());
            tempContactPoint = contactPointRepository.save(tempContactPoint);
            newGroup.getContactPoints().add(tempContactPoint);
        }

        // return new group with parents/children for front end to avoid recursion
        return addSingleParentAndChildGroup(newGroup);
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

    public Link addLink(final Long groupId, final Link link) {
        link.setGroup(groupRepository.findOne(groupId));
        link.setCreator(userRepository.findOne(1L));
        return linkRepository.save(link);
    }

    public ContactPoint addContactPoint(final Long groupId, final ContactPoint contactPoint) {
        contactPoint.setGroup(groupRepository.findOne(groupId));
        contactPoint.setCreator(userRepository.findOne(1L));
        contactPoint.setContactPointType(entityManager.find(ContactPointType.class,
                contactPoint.getContactPointType().getId()));
        return contactPointRepository.save(contactPoint);
    }

    public Location addLocation(final Long groupId, final Location location) {
        location.setGroup(groupRepository.findOne(groupId));
        location.setCreator(userRepository.findOne(1L));
        return locationRepository.save(location);
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

    public void passwordRequest(Long groupId, UnitRequest unitRequest) throws ResourceNotFoundException {
        Group group = findGroup(groupId);

        if (group == null) {
            throw new ResourceNotFoundException("The unit has not been found");
        }

        Email email = createPasswordResetEmail(unitRequest, group);
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

    private Group findGroup(Long groupId) throws ResourceNotFoundException {
        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException(String.format("Could not find group %s", groupId));
        }
        return group;
    }

    //Sprint 3 - velocity templates
    private Email createPasswordResetEmail(UnitRequest unitRequest, Group group) {
        Email email = new Email();
        email.setSubject("PatientView - Request for password reset");

        StringBuilder body = new StringBuilder();
        body.append("The following user would like to request a password reset");
        body.append("Forename: ").append(unitRequest.getForename());
        body.append("Surname: ").append(unitRequest.getSurname());
        body.append("DOB: ").append(unitRequest.getDateOfBirth());
        body.append("NHS Number: ").append(unitRequest.getNhsNumber());
        body.append("Associated Unit: ").append(group.getName());

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
}

