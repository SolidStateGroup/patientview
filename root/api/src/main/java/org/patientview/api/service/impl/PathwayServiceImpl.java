package org.patientview.api.service.impl;

import org.patientview.api.service.PathwayService;
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

        LOG.info("Initializing pathway for user {}", user.getId());
        User currentUser = getCurrentUser();
        User find = userRepository.findOne(user.getId());

        if (find == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Pathway pathway = PathwayBuilder.newBuilder()
                .setUser(find)
                .setCreator(currentUser)
                .setLastUpdater(currentUser)
                .setType(PathwayTypes.DONORPATHWAY)
                .build();

        pathwayRepository.save(pathway);
    }
}
