package org.patientview.api.service.impl;

import org.patientview.api.service.LocationService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
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
public class LocationServiceImpl extends AbstractServiceImpl<LocationServiceImpl>
        implements LocationService {

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private GroupRepository groupRepository;

    public Location add(final Long groupId, final Location location)
            throws ResourceNotFoundException, ResourceForbiddenException {

        Group group = groupRepository.findOne(groupId);

        if (group == null) {
            throw new ResourceNotFoundException("Group not found");
        }

        location.setGroup(group);
        location.setCreator(getCurrentUser());

        return locationRepository.save(location);
    }

    public Location get(final Long locationId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        Location location = locationRepository.findOne(locationId);

        if (location == null) {
            throw new ResourceNotFoundException("Contact point does not exist");
        }

        if (!isCurrentUserMemberOfGroup(location.getGroup())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return location;
    }

    public void delete(final Long locationId) throws ResourceNotFoundException, ResourceForbiddenException {

        Location location = locationRepository.findOne(locationId);

        if (location == null) {
            throw new ResourceNotFoundException("Contact point does not exist");
        }

        if (!isCurrentUserMemberOfGroup(location.getGroup())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        location.getGroup().getLocations().remove(location);
        locationRepository.delete(locationId);
    }

    public Location save(final Location location) throws ResourceNotFoundException, ResourceForbiddenException {
        Location entityLocation = locationRepository.findOne(location.getId());

        if (entityLocation == null) {
            throw new ResourceNotFoundException("Contact point does not exist");
        }

        if (!isCurrentUserMemberOfGroup(entityLocation.getGroup())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        entityLocation.setLabel(location.getLabel());
        entityLocation.setName(location.getName());
        entityLocation.setPhone(location.getPhone());
        entityLocation.setAddress(location.getAddress());
        entityLocation.setWeb(location.getWeb());
        entityLocation.setEmail(location.getEmail());
        return locationRepository.save(entityLocation);
    }
}
