package org.patientview.api.service.impl;

import org.patientview.api.service.LocationService;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LocationRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/07/2014
 */
@Service
public class LocationServiceImpl implements LocationService {

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private GroupRepository groupRepository;

    public Location create(final Location location) {

        if (location.getGroup() != null) {
            location.setGroup(groupRepository.findOne(location.getGroup().getId()));
        }

        return locationRepository.save(location);
    }

    public Location getLocation(final Long locationId) {
        return locationRepository.findOne(locationId);
    }

    public void deleteLocation(final Long locationId) {
        locationRepository.delete(locationId);
    }

    public Location saveLocation(final Location location) {
        Location entityLocation = locationRepository.findOne(location.getId());
        entityLocation.setLabel(location.getLabel());
        entityLocation.setName(location.getName());
        entityLocation.setPhone(location.getPhone());
        entityLocation.setAddress(location.getAddress());
        entityLocation.setWeb(location.getWeb());
        entityLocation.setEmail(location.getEmail());
        return locationRepository.save(entityLocation);
    }
}
