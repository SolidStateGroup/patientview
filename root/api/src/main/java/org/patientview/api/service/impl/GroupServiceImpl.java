package org.patientview.api.service.impl;

import org.patientview.api.service.GroupService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRelationshipRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.LocationRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
@Service
public class GroupServiceImpl implements GroupService {

    private static final Logger LOG = LoggerFactory.getLogger(GroupServiceImpl.class);

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
    private UserRepository userRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupRelationshipRepository groupRelationshipRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Inject
    private EntityManager entityManager;


    /**
     * Get all the groups and put the children and parents into the transient objects
     *
     * @return
     */
    public List<Group> findAll() {

        List<Group> groups = Util.iterableToList(groupRepository.findAll());

        return addParentAndChildGroups(groups);

    }

    public Group findOne(Long id) {
        return addSingleParentAndChildGroup(groupRepository.findOne(id));
    }

    public List<Group> findGroupByUser(User user) {

        List<Group> groups = Util.iterableToList(groupRepository.findGroupByUser(user));

        return addParentAndChildGroups(groups);

    }

    public List<Group> findGroupAndChildGroupsByUser(User user) {

        Lookup childRelationshipType = lookupRepository.findByTypeAndValue(LookupTypes.RELATIONSHIP_TYPE, "CHILD");
        Set<Group> groups = new HashSet<Group>();
        // get list of groups associated with user directly (user is member of group)
        groups.addAll(Util.iterableToList(groupRepository.findGroupByUser(user)));
        // for each group get list of children if present
        for (Group group : groups) {
            for (GroupRelationship groupRelationship : group.getGroupRelationships()) {
                if (groupRelationship.getLookup().equals(childRelationshipType)) {
                    groups.add(groupRelationship.getObjectGroup());
                }
            }
        }

        return addParentAndChildGroups(new ArrayList<Group>(groups));
    }

    public List<Group> findGroupByType(Long lookupId) {

        Lookup groupType = lookupRepository.findOne(lookupId);

        List<Group> groups = Util.iterableToList(groupRepository.findGroupByType(groupType));

        return addParentAndChildGroups(groups);

    }


    /**
     * TODO remove links, relationships, locations, and features SPRINT 2
     * FIXME Hacked to get saves
     *
     * @param group
     * @return
     */
    public Group save(Group group) {

        // get existing group
        Group entityGroup = groupRepository.findOne(group.getId());

        // save group relationships
        saveGroupRelationships(group);

        linkRepository.deleteByGroup(group);
        entityManager.flush();

        // set new group links and persist
        if (!CollectionUtils.isEmpty(group.getLinks())) {

            for (Link link : group.getLinks()) {
                //if (link.getId() < 0) {
                link.setId(null);
                //}
                link.setGroup(entityGroup);
                link.setCreator(userRepository.findOne(1L));
                Link persistedLink = linkRepository.save(link);
                LOG.debug("Save link " + persistedLink.getId());
            }
        }

        entityGroup.setLinks(Collections.EMPTY_SET);


        locationRepository.delete(entityGroup.getLocations());
        entityManager.flush();

        // remove deleted group locations
        if (!CollectionUtils.isEmpty(group.getLocations())) {

            // set new group locations and persist
            if (!CollectionUtils.isEmpty(group.getLocations())) {
                for (Location location : group.getLocations()) {
                    location.setId(null);
                    location.setGroup(entityGroup);
                    location.setCreator(userRepository.findOne(1L));
                    locationRepository.save(location);
                }
            }
        }

        entityGroup.setLocations(Collections.EMPTY_SET);

        groupFeatureRepository.delete(entityGroup.getGroupFeatures());
        entityManager.flush();

        if (!CollectionUtils.isEmpty(group.getGroupFeatures())) {

            // save group features
            for (GroupFeature groupFeature : group.getGroupFeatures()) {
                groupFeature.setFeature(featureRepository.findOne(groupFeature.getFeature().getId()));
                groupFeature.setGroup(groupRepository.findOne(entityGroup.getId()));
                groupFeature.setCreator(userRepository.findOne(1L));
                groupFeatureRepository.save(groupFeature);
            }
        }

        entityGroup.setGroupFeatures(Collections.EMPTY_SET);

        entityGroup.setSftpUser(group.getSftpUser());
        entityGroup.setName(group.getName());
        entityGroup.setCode(group.getCode());
        entityGroup.setGroupType(group.getGroupType());
        entityGroup.setVisibleToJoin(group.getVisibleToJoin());
        entityGroup = groupRepository.save(entityGroup);
        return addSingleParentAndChildGroup(entityGroup);
    }

    /**
     * TODO remove links, relationships, locations, and features SPRINT 2
     *
     * @param group
     * @return
     * @throws javax.persistence.EntityExistsException
     */
    public Group create(Group group) throws EntityExistsException {
        Group newGroup;

        Set<Link> links;
        // get links and features, avoid persisting until group created successfully
        if (!CollectionUtils.isEmpty(group.getLinks())) {
            links = new HashSet<Link>(group.getLinks());
            group.getLinks().clear();
        } else {
            links = new HashSet<Link>();
        }

        Set<Location> locations;
        if (!CollectionUtils.isEmpty(group.getLocations())) {
            locations = new HashSet<Location>(group.getLocations());
            group.getLocations().clear();
        } else {
            locations = new HashSet<Location>();
        }

        Set<GroupFeature> groupFeatures;
        if (!CollectionUtils.isEmpty(group.getGroupFeatures())) {
            groupFeatures = new HashSet<GroupFeature>(group.getGroupFeatures());
            group.getGroupFeatures().clear();
        } else {
            groupFeatures = new HashSet<GroupFeature>();
        }


        // save basic details
        try {
            newGroup = groupRepository.save(group);
        } catch (DataIntegrityViolationException dve) {
            LOG.debug("Group not created, duplicate: {}", dve.getCause());
            throw new EntityExistsException("Group already exists");
        }

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

        // return new group with parents/children for front end to avoid recursion
        return addSingleParentAndChildGroup(newGroup);
    }

    public GroupRole addGroupRole(Long userId, Long groupId, Long roleId) {
        GroupRole groupRole = new GroupRole();
        groupRole.setUser(userRepository.findOne(userId));
        groupRole.setGroup(groupRepository.findOne(groupId));
        groupRole.setRole(roleRepository.findOne(roleId));
        groupRole.setCreator(userRepository.findOne(1L));
        return groupRoleRepository.save(groupRole);
    }


    private void saveGroupRelationships(Group group) {

        Lookup parentRelationshipType = lookupRepository.findByTypeAndValue(LookupTypes.RELATIONSHIP_TYPE, "PARENT");
        Lookup childRelationshipType = lookupRepository.findByTypeAndValue(LookupTypes.RELATIONSHIP_TYPE, "CHILD");

        // delete existing groups
        groupRelationshipRepository.deleteBySourceGroup(group);

        Group sourceGroup = groupRepository.findOne(group.getId());

        // Create a two way relationship; if a parent is a child, the inverse is also true
        if (!CollectionUtils.isEmpty(group.getParentGroups())) {
            for (Group parentGroup : group.getParentGroups()) {

                Group objectGroup = groupRepository.findOne(parentGroup.getId());
                createRelationship(sourceGroup, objectGroup, parentRelationshipType);
                createRelationship(objectGroup, sourceGroup, childRelationshipType);
            }
        }
        if (!CollectionUtils.isEmpty(group.getChildGroups())) {
            for (Group childGroup : group.getChildGroups()) {

                Group objectGroup = groupRepository.findOne(childGroup.getId());
                createRelationship(sourceGroup, objectGroup, childRelationshipType);
                createRelationship(objectGroup, sourceGroup, parentRelationshipType);
            }
        }
    }

    private GroupRelationship createRelationship(Group sourceGroup, Group objectGroup, Lookup relationshipType) {
        GroupRelationship groupRelationship = new GroupRelationship();
        groupRelationship.setSourceGroup(sourceGroup);
        groupRelationship.setObjectGroup(objectGroup);
        groupRelationship.setLookup(relationshipType);
        return groupRelationshipRepository.save(groupRelationship);
    }




    private Group addSingleParentAndChildGroup(Group group) {
        // TODO Move this to PostConstruct sort out Transaction scope;
        Lookup parentRelationshipType = lookupRepository.findByTypeAndValue(LookupTypes.RELATIONSHIP_TYPE, "PARENT");
        Lookup childRelationshipType = lookupRepository.findByTypeAndValue(LookupTypes.RELATIONSHIP_TYPE, "CHILD");

        Set<Group> parentGroups = new HashSet<Group>();
        Set<Group> childGroups = new HashSet<Group>();

        if (!CollectionUtils.isEmpty(group.getGroupRelationships())) {
            for (GroupRelationship groupRelationship : group.getGroupRelationships()) {

                if (groupRelationship.getLookup().equals(parentRelationshipType)) {
                    Group detachedParentGroup = groupRelationship.getObjectGroup();
                    entityManager.detach(detachedParentGroup);
                    detachedParentGroup.setParentGroups(Collections.EMPTY_SET);
                    detachedParentGroup.setChildGroups(Collections.EMPTY_SET);
                    parentGroups.add(detachedParentGroup);
                }

                if (groupRelationship.getLookup().equals(childRelationshipType)) {
                    Group detachedChildGroup = groupRelationship.getObjectGroup();
                    entityManager.detach(detachedChildGroup);
                    detachedChildGroup.setParentGroups(Collections.EMPTY_SET);
                    detachedChildGroup.setChildGroups(Collections.EMPTY_SET);
                    childGroups.add(detachedChildGroup);
                }
            }
        }

        group.setParentGroups(parentGroups);
        group.setChildGroups(childGroups);
        return group;
    }

    // Attached the relationship of children groups and parents groups onto Transient objects
    private List<Group> addParentAndChildGroups(List<Group> groups) {

        for (Group group : groups) {
            addSingleParentAndChildGroup(group);
        }

        return groups;

    }

    public void addParentGroup(Long groupId, Long parentGroupId) {

        Lookup parentRelationshipType = lookupRepository.findByTypeAndValue(LookupTypes.RELATIONSHIP_TYPE, "PARENT");
        Lookup childRelationshipType = lookupRepository.findByTypeAndValue(LookupTypes.RELATIONSHIP_TYPE, "CHILD");

        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(parentGroupId);

        createRelationship(sourceGroup, objectGroup, parentRelationshipType);
        createRelationship(objectGroup, sourceGroup, childRelationshipType);

    }


}
