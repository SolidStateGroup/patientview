package org.patientview.api.service.impl;

import org.patientview.api.service.SecurityService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.RouteRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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
    private RouteRepository routeRepository;

    public Set<Route> getUserRoutes(User user) {
        Set<Route> routes = new TreeSet<>(Util.convertIterable(routeRepository.findFeatureRoutesByUser(user)));
        routes.addAll(Util.convertIterable(routeRepository.findGroupRoutesByUser(user)));
        routes.addAll(Util.convertIterable(routeRepository.findRoleRoutesByUser(user)));
        return routes;
    }
}
