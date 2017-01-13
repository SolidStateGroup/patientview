package org.patientview.builder;

import org.patientview.persistence.model.Pathway;
import org.patientview.persistence.model.Stage;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.PathwayTypes;
import org.patientview.persistence.model.enums.StageTypes;

import java.util.HashSet;
import java.util.Set;

/**
 * Initialises Donor Pathway for user.
 * <p/>
 * Ideally would be better to store template in db and set up when needed
 */
public class PathwayBuilder {

    private Pathway pathway;

    private PathwayBuilder() {
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private User user;
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

        public Builder setType(PathwayTypes pathwayTypes) {
            result.setPathwayType(pathwayTypes);
            return this;
        }

        private void buildStages(){

            Set<Stage> stages = new HashSet<>();
            {
                Stage stage = createStage("Point 1 - Consultation", StageTypes.CONSULTATION);
                stage.setPathway(pathway);
                stages.add(stage);
            }
            {
                Stage stage = createStage("Point 2 - Testing", StageTypes.TESTING);
                stage.setPathway(pathway);
                stages.add(stage);
            }
            result.setStages(stages);
        }

        public Pathway build() {

            buildStages();
            return result;
        }


    }
}
