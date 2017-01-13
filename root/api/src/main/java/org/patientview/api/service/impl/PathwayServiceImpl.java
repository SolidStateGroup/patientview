package org.patientview.api.service.impl;

import org.patientview.api.service.PathwayService;
import org.patientview.builder.PathwayBuilder;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Pathway;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.PathwayTypes;
import org.patientview.persistence.repository.PathwayRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Implementation of the NoteService
 */
@Service
public class PathwayServiceImpl extends AbstractServiceImpl<PathwayServiceImpl> implements PathwayService {

    @Inject
    private PathwayRepository pathwayRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public void updatePathway(Long userId, org.patientview.api.model.Pathway pathway)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User currentUser = getCurrentUser();
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Pathway entity = pathwayRepository.findOne(pathway.getId());
        if (entity == null) {
            throw new ResourceNotFoundException("Could not find pathway");
        }

        if (!user.getId().equals(entity.getUser().getId())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        // TODO: copy over values for update
        pathwayRepository.save(entity);
    }

    @Override
    public org.patientview.api.model.Pathway getPathway(Long userId, PathwayTypes pathwayType)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Pathway entity = pathwayRepository.findByUserAndPathwayType(user, pathwayType);
        if (entity == null) {
            throw new ResourceNotFoundException("Could not find Pathway");
        }

        return new org.patientview.api.model.Pathway(entity);
    }


    @Override
    public void setupPathway(User user) throws ResourceNotFoundException {
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Pathway pathway = PathwayBuilder.newBuilder()
                .setUser(user)
                .setCreator(getCurrentUser())
                .setType(PathwayTypes.DONORPATHWAY)
                .build();

        pathwayRepository.save(pathway);
    }

}
