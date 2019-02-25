package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.SurveySendingFacility;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.SurveySendingFacilityRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class SurveySendingFacilityTest {

    @Inject
    private SurveySendingFacilityRepository surveySendingFacilityRepository;

    @Inject
    private GroupRepository groupRepository;

    @Test
    public void should_find_sending_facility_from_survey_group() {

        SurveySendingFacility sendingFacility = new SurveySendingFacility();
        Group surveyGroup = new Group();
        surveyGroup.setId(2L);

        groupRepository.save(surveyGroup);
        sendingFacility.setSurveyGroup(surveyGroup);

        Group unit = new Group();
        unit.setId(3L);
        groupRepository.save(unit);
        sendingFacility.setUnit(unit);

        sendingFacility.setId(1L);

        surveySendingFacilityRepository.save(sendingFacility);

        SurveySendingFacility result = surveySendingFacilityRepository.findBySurveyGroup_Id(2L);

        Assert.assertEquals(new Long(3L), result.getUnit().getId());
    }
}
