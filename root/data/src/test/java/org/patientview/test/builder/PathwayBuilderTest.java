package org.patientview.test.builder;

import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.PathwayBuilder;
import org.patientview.persistence.model.Pathway;
import org.patientview.persistence.model.Stage;
import org.patientview.persistence.model.enums.PathwayTypes;
import org.patientview.test.util.TestUtils;

/**
 * Unit test for PathwayBuilder
 */
public class PathwayBuilderTest {

    @Test
    public void testPathwayBuilder() throws Exception {
        Pathway pathway = PathwayBuilder.newBuilder()
                .setUser(TestUtils.createUser("newUser"))
                .setCreator(TestUtils.createUser("creatorUser"))
                .setType(PathwayTypes.DONORPATHWAY)
                .build();

        Assert.assertNotNull("Should have created pathway", pathway);
        Assert.assertNotNull("Should have user", pathway.getUser());
        Assert.assertNotNull("Should have creator", pathway.getCreator());
        Assert.assertEquals("Should have created pathway", pathway.getPathwayType(), PathwayTypes.DONORPATHWAY);
        Assert.assertNotNull("Should have stages", pathway.getStages());
        Assert.assertTrue("Should have 3 stages", pathway.getStages().size() == 3);
        for (Stage stage : pathway.getStages()) {
            Assert.assertNotNull("Should have name", stage.getName());
            Assert.assertNotNull("Should have version", stage.getVersion());
            Assert.assertNotNull("Should have type", stage.getStageType());
            Assert.assertNotNull("Should have status", stage.getStageStatus());
            Assert.assertNotNull("Should have pathway for stage", stage.getPathway());
            Assert.assertNotNull("Should have pathway for stage", stage.getStageData());
        }
    }
}
