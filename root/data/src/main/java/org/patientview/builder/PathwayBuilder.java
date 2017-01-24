package org.patientview.builder;

import org.patientview.persistence.model.DonorStageData;
import org.patientview.persistence.model.Pathway;
import org.patientview.persistence.model.Stage;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.PathwayTypes;
import org.patientview.persistence.model.enums.StageStatuses;
import org.patientview.persistence.model.enums.StageTypes;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Initialises Donor Pathway for user.
 * <p/>
 * Ideally would be better to store template in db and set up when needed
 */
public class PathwayBuilder {

    private PathwayBuilder() {
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Pathway result = new Pathway();

        private Builder() {
        }

        public Builder setUser(User user) {
            result.setUser(user);
            return this;
        }

        public Builder setCreator(User user) {
            result.setCreator(user);
            return this;
        }

        public Builder setLastUpdater(User user) {
            result.setLastUpdater(user);
            return this;
        }

        public Builder setType(PathwayTypes pathwayTypes) {
            result.setPathwayType(pathwayTypes);
            return this;
        }

        /**
         * Currently there is defined set of stages, hard coding for now
         * Phase 1
         * CONSULTATION("Consultation")
         * TESTING("Testing")
         * REVIEW("Review")
         * <p>
         * Phase 2
         * FURTHER_TESTING("Further Testing")
         * PLANNING("Planning")
         * OPERATION("Operation")
         * POST_OPERATION("Post Operation")
         */
        private void initStages() {

            Set<Stage> stages = new HashSet<>();

            // Stage 1
            {
                Stage stage = new Stage();
                stage.setName("Consultation");
                stage.setVersion(0);
                stage.setStageType(StageTypes.CONSULTATION);
                stage.setStageStatus(StageStatuses.PENDING);
                stage.setStarted(new Date());
                stage.setFurtherInvestigation(false);

                DonorStageData data = new DonorStageData();
                data.setStage(stage);
                data.setCreator(result.getCreator());
                data.setLastUpdater(result.getLastUpdater());
                stage.setStageData(data);

                stage.setPathway(result);
                stages.add(stage);
            }

            // Stage 2
            {
                Stage stage = new Stage();
                stage.setName("Testing");
                stage.setVersion(0);
                stage.setStageType(StageTypes.TESTING);
                stage.setStageStatus(StageStatuses.PENDING);
                stage.setStarted(new Date());
                stage.setFurtherInvestigation(false);

                DonorStageData data = new DonorStageData();
                data.setStage(stage);
                data.setCreator(result.getCreator());
                data.setLastUpdater(result.getLastUpdater());
                stage.setStageData(data);

                stage.setPathway(result);
                stages.add(stage);
            }

            // Stage 3
            {
                Stage stage = new Stage();
                stage.setName("Review");
                stage.setVersion(0);
                stage.setStageType(StageTypes.REVIEW);
                stage.setStageStatus(StageStatuses.PENDING);
                stage.setStarted(new Date());
                stage.setFurtherInvestigation(false);

                DonorStageData data = new DonorStageData();
                data.setStage(stage);
                data.setCreator(result.getCreator());
                data.setLastUpdater(result.getLastUpdater());
                stage.setStageData(data);

                stage.setPathway(result);
                stages.add(stage);
            }

            // Stage 4
            {
                Stage stage = new Stage();
                stage.setName("Further Testing");
                stage.setVersion(0);
                stage.setStageType(StageTypes.FURTHER_TESTING);
                stage.setStageStatus(StageStatuses.PENDING);
                stage.setStarted(new Date());
                stage.setFurtherInvestigation(false);

                DonorStageData data = new DonorStageData();
                data.setStage(stage);
                data.setCreator(result.getCreator());
                data.setLastUpdater(result.getLastUpdater());
                stage.setStageData(data);

                stage.setPathway(result);
                stages.add(stage);
            }

            // Stage 5
            {
                Stage stage = new Stage();
                stage.setName("Planning");
                stage.setVersion(0);
                stage.setStageType(StageTypes.PLANNING);
                stage.setStageStatus(StageStatuses.PENDING);
                stage.setStarted(new Date());
                stage.setFurtherInvestigation(false);

                DonorStageData data = new DonorStageData();
                data.setStage(stage);
                data.setCreator(result.getCreator());
                data.setLastUpdater(result.getLastUpdater());
                stage.setStageData(data);

                stage.setPathway(result);
                stages.add(stage);
            }

            // Stage 6
            {
                Stage stage = new Stage();
                stage.setName("Operation");
                stage.setVersion(0);
                stage.setStageType(StageTypes.OPERATION);
                stage.setStageStatus(StageStatuses.PENDING);
                stage.setStarted(new Date());
                stage.setFurtherInvestigation(false);

                DonorStageData data = new DonorStageData();
                data.setStage(stage);
                data.setCreator(result.getCreator());
                data.setLastUpdater(result.getLastUpdater());
                stage.setStageData(data);

                stage.setPathway(result);
                stages.add(stage);
            }

            // Stage 7
            {
                Stage stage = new Stage();
                stage.setName("Post Operation");
                stage.setVersion(0);
                stage.setStageType(StageTypes.POST_OPERATION);
                stage.setStageStatus(StageStatuses.PENDING);
                stage.setStarted(new Date());
                stage.setFurtherInvestigation(false);

                DonorStageData data = new DonorStageData();
                data.setStage(stage);
                data.setCreator(result.getCreator());
                data.setLastUpdater(result.getLastUpdater());
                stage.setStageData(data);

                stage.setPathway(result);
                stages.add(stage);
            }

            result.setStages(stages);
        }

        public Pathway build() {

            initStages();
            return result;
        }
    }
}
