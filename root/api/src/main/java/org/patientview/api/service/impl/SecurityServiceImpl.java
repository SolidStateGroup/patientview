package org.patientview.api.service.impl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.SecurityService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Service to supply data based on a user's role and group
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Service
public class SecurityServiceImpl extends AbstractServiceImpl<SecurityServiceImpl> implements SecurityService {

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private RouteRepository routeRepository;

    // TODO Remove and introduce service
    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupService groupService;

    @Inject
    private UserRepository userRepository;

    public List<Role> getUserRoles(Long userId) {
        return Util.convertIterable(roleRepository.findValidRolesByUser(userId));
    }

    public Set<Route> getUserRoutes(Long userId) {
        User user = userRepository.findOne(userId);
        Set<Route> routes = new TreeSet<>(Util.convertIterable(routeRepository.findFeatureRoutesByUser(user)));
        routes.addAll(Util.convertIterable(routeRepository.findGroupRoutesByUser(user)));
        routes.addAll(Util.convertIterable(routeRepository.findRoleRoutesByUser(user)));
        return routes;
    }

    public List<Group> getGroupByUserAndRole(Long userId, Long roleId) {
        User user = userRepository.findOne(userId);
        Role role = roleRepository.findOne(roleId);
        return Util.convertIterable(groupRepository.findGroupByUserAndRole(user, role));
    }

    private List<org.patientview.api.model.Group> convertGroupsToTransportGroups(List<Group> groups) {
        List<org.patientview.api.model.Group> transportGroups = new ArrayList<>();

        for (Group group : groups) {
            transportGroups.add(new org.patientview.api.model.Group(group));
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
        }
        else {
            if (groupTypesNotEmpty) {
                groupPage = groupRepository.findGroupsByUserAndGroupTypeNoSpecialties(filterText, groupTypesList,
                        user, pageable);
            } else {
                groupPage = groupRepository.findGroupsByUserNoSpecialties(filterText, user, pageable);
            }
        }
        return groupPage;
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
        List<Group> content = groupService.addParentAndChildGroups(groupPage.getContent());

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
        List<Group> content = groupService.addParentAndChildGroups(groupPage.getContent());
        return new PageImpl<>(content, pageable, groupPage.getTotalElements());
    }

    public List<Group> getAllUserGroupsAllDetails(Long userId) {

        Page<Group> groupPage = getUserGroupsData(userId, new GetParameters());
        if (groupPage == null) {
            return new ArrayList<>();
        }

        // add parent and child groups
        List<Group> content = groupService.addParentAndChildGroups(groupPage.getContent());
        return content;
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
}
