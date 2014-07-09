package org.patientview.api.service.impl;

import org.patientview.api.service.GroupService;
import org.patientview.api.service.SecurityService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service to supply data based on a user's role and group
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Service
public class SecurityServiceImpl implements SecurityService {

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

    @Inject
    private NewsItemRepository newsItemRepository;

    public List<Role> getUserRoles(Long userId) {

        return Util.iterableToList(roleRepository.getValidRolesByUser(userId));

    }

    public Set<Route> getUserRoutes(Long userId) {
        User user = userRepository.findOne(userId);
        Set<Route> routes = new HashSet<Route>(Util.iterableToList(routeRepository.getFeatureRoutesByUser(user)));
        routes.addAll(Util.iterableToList(routeRepository.getGroupRoutesByUser(user)));
        routes.addAll(Util.iterableToList(routeRepository.getRoleRoutesByUser(user)));
        return routes;

    }

    public List<Group> getGroupByUserAndRole(Long userId, Long roleId) {
        User user = userRepository.findOne(userId);
        Role role = roleRepository.findOne(roleId);
        return Util.iterableToList(groupRepository.getGroupByUserAndRole(user, role));
    }

    public List<NewsItem> getNewsByUser(Long userId) {
        User user = userRepository.findOne(userId);
        List<NewsItem> newsItems = Util.iterableToList(newsItemRepository.getGroupNewsByUser(user));
        newsItems.addAll(Util.iterableToList(newsItemRepository.getRoleNewsByUser(user)));
        return newsItems;
    }

    public List<Group> getUserGroups(Long userId) {
        User user = userRepository.findOne(userId);
        //TODO - Refactor to Enum Sprint 2
        if (doesListContainRole(roleRepository.getByUser(user), "SUPER_ADMIN")) {
            return Util.iterableToList(groupService.findAll());
        } else {
            return Util.iterableToList(groupService.findGroupByUser(user));
        }

    }

    //TODO - Refactor to Enum Sprint 2
    private boolean doesListContainRole(List<Role> roles, String roleName) {
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(roleName)) {
                return true;
            }
        }
        return false;
    }



}
