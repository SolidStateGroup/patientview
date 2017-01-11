package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.DonorStageData;
import org.patientview.persistence.model.Pathway;
import org.patientview.persistence.model.Stage;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.PathwayTypes;
import org.patientview.persistence.model.enums.StageTypes;
import org.patientview.persistence.repository.PathwayRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.patientview.test.util.TestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/12/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class PathwayRepositoryTest {

    @Inject
    DataTestUtils dataTestUtils;
    User creator;
    User donor;
    @Inject
    private PathwayRepository pathwayRepository;
    private Long pathwayId;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");

        donor =  dataTestUtils.createUser("donorUser");

        Pathway pathway = new Pathway();
        pathway.setUser(donor);
        pathway.setPathwayType(PathwayTypes.DONORPATHWAY);
        pathway.setCreator(creator);
        pathway.setLastUpdater(creator);

        // add few stages with stage data
        Set<Stage> stages = new HashSet<>();
        {
            Stage stage = TestUtils.createStage("Point 1 - Consultation", StageTypes.CONSULTATION);
            stage.setPathway(pathway);
            stages.add(stage);
        }
        {
            Stage stage = TestUtils.createStage("Point 2 - Testing", StageTypes.TESTING);
            stage.setPathway(pathway);
            stages.add(stage);
        }
        pathway.setStages(stages);


        Pathway create = pathwayRepository.save(pathway);
        pathwayId = create.getId();
    }

    @Test
    public void givenPathwayRepo_whenSave_thenIdAssigned() {

        Pathway found = pathwayRepository.findOne(pathwayId);
        Assert.assertNotNull("Should have found pathway", found);
        Assert.assertNotNull("Should have created date set", found.getCreated());
        Assert.assertNotNull("Should have creator date set", found.getCreator())
        Assert.assertNotNull("Should have updated date set", found.getLastUpdate());

        // check stages and stage data persisted as well
        for (Stage stage : found.getStages()) {
            Assert.assertNotNull("Should have saved stage", stage);
            Assert.assertNotNull("Should have saved stage id", stage.getId());
            Assert.assertNotNull("Should have saved stage status", stage.getStageStatus());
            Assert.assertNotNull("Should have saved stage type", stage.getStageType());
            Assert.assertNotNull("Should have saved stage started date", stage.getStarted());

            // check stage data
            Assert.assertNotNull("Should have stage data", stage.getStageData());
            for (DonorStageData data : stage.getStageData()) {
                Assert.assertNotNull("Should have creator date set", data.getCreator());
                Assert.assertNotNull("Should have created date set", data.getCreated());
                Assert.assertNotNull("Should have updated date set", data.getLastUpdate());
            }

        }
    }

    @Test
    public void givenPathwayRepo_whenFindByUser_correctFound() {

        List<Pathway> list = pathwayRepository.findByUser(donor);
        Assert.assertTrue("Should have found at least 1 pathway", list.size() > 0);

    }

}
