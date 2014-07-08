package org.patientview.api.service.impl;

import org.patientview.api.service.AdminService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 *
 * TODO name change and refactor
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private GroupFeatureRepository groupFeatureRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private LinkRepository linkRepository;

    @Inject
    private LocationRepository locationRepository;

    public Group createGroup(Group group) throws EntityExistsException {
        Group newGroup;

        // avoid persisting parent/child groups before input group with error: org.hibernate.PersistentObjectException:
        // detached entity passed to persist: org.patientview.persistence.model.Group
        Set<Group> parentGroups = new HashSet<Group>(group.getParentGroups());
        Set<Group> childGroups = new HashSet<Group>(group.getChildGroups());
        group.getParentGroups().clear();
        group.getChildGroups().clear();

        // get links and features, avoid persisting until group created successfully
        Set<Link> links = new HashSet<Link>(group.getLinks());
        group.getLinks().clear();
        Set<Location> locations = new HashSet<Location>(group.getLocations());
        group.getLocations().clear();
        Set<GroupFeature> groupFeatures = new HashSet<GroupFeature>(group.getGroupFeatures());
        group.getGroupFeatures().clear();

        // save basic details
        try {
            newGroup = groupRepository.save(group);
        } catch (DataIntegrityViolationException dve) {
            LOG.debug("Group not created, duplicate: {}", dve.getCause());
            throw new EntityExistsException("Group already exists");
        }

        // save correct relationships to other groups
        for (Group tempGroup : parentGroups) {
            newGroup.getParentGroups().add(groupRepository.findOne(tempGroup.getId()));
        }
        for (Group tempGroup : childGroups) {
            newGroup.getChildGroups().add(groupRepository.findOne(tempGroup.getId()));
        }
        newGroup = groupRepository.save(newGroup);

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
        return addSingleLevelParentsAndChildren(newGroup);
    }

    public Group getGroup(Long groupId) {
        return groupRepository.findOne(groupId);
    }

    public Group saveGroup(final Group group) {

        // get existing group
        Group entityGroup = groupRepository.findOne(group.getId());

        // remove deleted group links
        entityGroup.getLinks().removeAll(group.getLinks());
        linkRepository.delete(entityGroup.getLinks());

        // set new group links and persist
        if (!CollectionUtils.isEmpty(group.getLinks())) {
            for (Link link : group.getLinks()) {
                if (link.getId() < 0) { link.setId(null); }
                link.setGroup(entityGroup);
                link.setCreator(userRepository.findOne(1L));
                linkRepository.save(link);
            }
        }
        
        // remove deleted group locations
        entityGroup.getLocations().removeAll(group.getLocations());
        locationRepository.delete(entityGroup.getLocations());

        // set new group locations and persist
        if (!CollectionUtils.isEmpty(group.getLocations())) {
            for (Location location : group.getLocations()) {
                if (location.getId() < 0) { location.setId(null); }
                location.setGroup(entityGroup);
                location.setCreator(userRepository.findOne(1L));
                locationRepository.save(location);
            }
        }

        // remove deleted group features
        entityGroup.getGroupFeatures().removeAll(group.getGroupFeatures());
        groupFeatureRepository.delete(entityGroup.getGroupFeatures());

        // save group features
        if (!CollectionUtils.isEmpty(group.getGroupFeatures())) {
            for (GroupFeature groupFeature : group.getGroupFeatures()) {
                groupFeature.setFeature(featureRepository.findOne(groupFeature.getFeature().getId()));
                groupFeature.setGroup(groupRepository.findOne(entityGroup.getId()));
                groupFeature.setCreator(userRepository.findOne(1L));
                groupFeatureRepository.save(groupFeature);
            }
        }

        return addSingleLevelParentsAndChildren(groupRepository.save(group));
    }
    
    public GroupFeature addGroupFeature(Long groupId, Long featureId) {

        GroupFeature groupFeature = new GroupFeature();
        groupFeature.setStartDate(new Date());
        groupFeature.setCreator(userRepository.findOne(1L));
        groupFeature.setFeature(featureRepository.findOne(featureId));
        groupFeature.setGroup(groupRepository.findOne(groupId));
        return groupFeatureRepository.save(groupFeature);
    }

    public List<Group> getAllGroups() {
        // manually add list of parents/children (avoid recursion by only going one level deep)
        List<Group> groups = Util.iterableToList(groupRepository.findAll());
        for (Group group : groups) {
            group = addSingleLevelParentsAndChildren(group);
        }

        return groups;
    }

    // TODO: refactor to avoid M:M issues with infinite recursion
    /**
     * Create simple set of parents and children avoiding infinite recursion due to self-ref ManyToMany
     * @param inputGroup
     * @return
     */
    private Group addSingleLevelParentsAndChildren(Group inputGroup) {
        for (Group familyGroup : inputGroup.getParentGroups()) {
            Group newGroup = new Group();
            newGroup.setId(familyGroup.getId());
            newGroup.setName(familyGroup.getName());
            newGroup.setCode(familyGroup.getCode());
            newGroup.setFhirResourceId(familyGroup.getFhirResourceId());
            newGroup.setDescription(familyGroup.getDescription());
            newGroup.setGroupType(familyGroup.getGroupType());
            inputGroup.getParents().add(newGroup);
        }
        for (Group familyGroup : inputGroup.getChildGroups()) {
            Group newGroup = new Group();
            newGroup.setId(familyGroup.getId());
            newGroup.setName(familyGroup.getName());
            newGroup.setCode(familyGroup.getCode());
            newGroup.setFhirResourceId(familyGroup.getFhirResourceId());
            newGroup.setDescription(familyGroup.getDescription());
            newGroup.setGroupType(familyGroup.getGroupType());
            inputGroup.getChildren().add(newGroup);
        }

        return inputGroup;
    }

    public List<Role> getAllRoles() {
        return Util.iterableToList(roleRepository.findAll());
    }

    public List<Role> getRolesByType(String type) {
        List<Lookup> roleLookup = Util.iterableToList(lookupRepository.getByLookupTypeAndValue("ROLE", type));
        if (!roleLookup.isEmpty()) {
            return Util.iterableToList(roleRepository.getByType(roleLookup.get(0)));
        } else return Collections.<Role>emptyList();
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        User entityUser = userRepository.findOne(user.getId());
        entityUser.setFhirResourceId(user.getFhirResourceId());
        return userRepository.save(user);
    }

    public GroupFeature createGroupFeature(GroupFeature groupFeature) {
        return groupFeatureRepository.save(groupFeature);
    }

    public List<User> getGroupUserByRoleStaff(Long groupId) {
        return Util.iterableToList(groupRepository.getGroupStaffByRole(groupId, "STAFF"));
    }

    public List<User> getGroupUserByRolePatient(Long groupId) {
        return Util.iterableToList(groupRepository.getGroupStaffByRole(groupId, "PATIENT"));
    }
}
