package org.patientview.api.service.impl;

import org.patientview.api.service.GroupService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
@Service
public class GroupServiceImpl implements GroupService {

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private LookupRepository lookupRepository;

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

    public List<Group> findGroupByUser(User user) {

        List<Group> groups = Util.iterableToList(groupRepository.findGroupByUser(user));

        return addParentAndChildGroups(groups);

    }

    public List<Group> findGroupByType(Long lookupId) {

        Lookup groupType = lookupRepository.findOne(lookupId);

        List<Group> groups = Util.iterableToList(groupRepository.findGroupByType(groupType));

        return addParentAndChildGroups(groups);

    }


    private List<Group> addParentAndChildGroups(List<Group> groups) {
        Lookup parentRelationshipType = lookupRepository.getByLookupTypeAndValue("RELATIONSHIP_TYPE", "PARENT");
        Lookup childRelationshipType = lookupRepository.getByLookupTypeAndValue("RELATIONSHIP_TYPE", "CHILD");
        for (Group group : groups) {

            Set<Group> parentGroups = new HashSet<Group>();
            Set<Group> childGroups = new HashSet<Group>();

            for (GroupRelationship groupRelationship : group.getGroupRelationships()) {

                if (groupRelationship.getLookup().equals(parentRelationshipType)) {
                    Group detachedParentGroup = groupRelationship.getObjectGroup();
                    entityManager.detach(detachedParentGroup);
                    parentGroups.add(detachedParentGroup);
                }

                if (groupRelationship.getLookup().equals(childRelationshipType)) {
                    Group detachedChildGroup = groupRelationship.getObjectGroup();
                    entityManager.detach(detachedChildGroup);
                    childGroups.add(detachedChildGroup);
                }
            }

            group.setParentGroups(parentGroups);
            group.setChildGroups(childGroups);
        }

        return groups;

    }



}
