package org.patientview.api.service.impl;

import org.patientview.api.service.PathwayService;
import org.patientview.api.service.UserService;
import org.patientview.builder.PathwayBuilder;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.DonorStageData;
import org.patientview.persistence.model.Pathway;
import org.patientview.persistence.model.Stage;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.PathwayTypes;
import org.patientview.persistence.model.enums.StageTypes;
import org.patientview.persistence.repository.PathwayRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Implementation of the NoteService
 */
@Service
@Transactional
public class PathwayServiceImpl extends AbstractServiceImpl<PathwayServiceImpl> implements PathwayService {

    @Inject
    private PathwayRepository pathwayRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Override
    public void updatePathway(Long userId, org.patientview.api.model.Pathway pathway)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User currentUser = getCurrentUser();
        User patient = userRepository.findOne(userId);
        if (patient == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        if (!userService.currentUserCanGetUser(patient)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        Pathway entity = pathwayRepository.findOne(pathway.getId());
        if (entity == null) {
            throw new ResourceNotFoundException("Could not find pathway");
        }

        if (!patient.getId().equals(entity.getUser().getId())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        // update properties
        entity.setLastUpdater(currentUser);
        for (Stage entityStage : entity.getStages()) {
            StageTypes type = entityStage.getStageType();
            org.patientview.api.model.Stage stage = pathway.getStages().get(type.getName());
            if (stage != null) {

                // Update stage
                entityStage.setStageStatus(stage.getStageStatus());
                entityStage.setVersion(stage.getVersion());
                entityStage.setBackToPreviousPoint(stage.getBackToPreviousPoint());
                entityStage.setFurtherInvestigation(stage.getFurtherInvestigation());

                // update StageData
                if (stage.getData() != null) {
                    DonorStageData entityStageData = entityStage.getStageData();
                    entityStageData.setBloods(stage.getData().getBloods());
                    entityStageData.setCrossmatching(stage.getData().getCrossmatching());
                    entityStageData.setXrays(stage.getData().getXrays());
                    entityStageData.setEcg(stage.getData().getEcg());
                    entityStageData.setCaregiverText(stage.getData().getCaregiverText());
                    entityStageData.setCarelocationText(stage.getData().getCarelocationText());
                    entityStageData.setLastUpdater(currentUser);
                }
            }
        }

        pathwayRepository.save(entity);
    }

    @Override
    public org.patientview.api.model.Pathway getPathway(Long userId, PathwayTypes pathwayType)
            throws ResourceNotFoundException, ResourceForbiddenException {

        // TODO: validate current user can edit user data
        User patient = userRepository.findOne(userId);
        if (patient == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        if (!userService.currentUserCanGetUser(patient)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        Pathway entity = pathwayRepository.findByUserAndPathwayType(patient, pathwayType);
        if (entity == null) {
            return null;
        }

        return new org.patientview.api.model.Pathway(entity);
    }


    @Override
    public void setupPathway(User user) throws ResourceNotFoundException, ResourceForbiddenException {

        LOG.info("Initializing pathway for user {}", user.getId());
        User currentUser = getCurrentUser();

        User patient = userRepository.findOne(user.getId());
        if (patient == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        if (!userService.currentUserCanGetUser(patient)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        Pathway path = pathwayRepository.findByUserAndPathwayType(patient, PathwayTypes.DONORPATHWAY);
        // check if we need to setup pathway for user
        if (path != null) {
            LOG.info("Pathway already exist for user {}", user.getId());
        } else {
            Pathway pathway = PathwayBuilder.newBuilder()
                    .setUser(patient)
                    .setCreator(currentUser)
                    .setLastUpdater(currentUser)
                    .setType(PathwayTypes.DONORPATHWAY)
                    .build();

            pathwayRepository.save(pathway);
        }
    }
}
