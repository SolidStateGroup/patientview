package org.patientview.test.persistence.repository;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.ExternalServiceTaskQueueItem;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ExternalServiceTaskQueueStatus;
import org.patientview.persistence.model.enums.ExternalServices;
import org.patientview.persistence.repository.ExternalServiceTaskQueueItemRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/04/2015
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class ExternalServiceTaskQueueItemRepositoryTest {

    @Inject
    ExternalServiceTaskQueueItemRepository externalServiceTaskQueueItemRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testFindByStatus() {
        Date now = new Date();

        externalServiceTaskQueueItemRepository.save(
            new ExternalServiceTaskQueueItem("url1", "method1", "content1",
                    ExternalServiceTaskQueueStatus.PENDING,
                    ExternalServices.RDC_GROUP_ROLE_NOTIFICATION,
                    creator, now));
        externalServiceTaskQueueItemRepository.save(
            new ExternalServiceTaskQueueItem("url2", "method2", "content2",
                    ExternalServiceTaskQueueStatus.IN_PROGRESS,
                    ExternalServices.RDC_GROUP_ROLE_NOTIFICATION,
                    creator, now));

        List<ExternalServiceTaskQueueStatus> statuses = new ArrayList<>();
        statuses.add(ExternalServiceTaskQueueStatus.FAILED);
        statuses.add(ExternalServiceTaskQueueStatus.PENDING);

        List<ExternalServiceTaskQueueItem> items = externalServiceTaskQueueItemRepository.findByStatus(statuses);

        Assert.assertEquals("There is 1 task queue item returned", 1, items.size());
        Assert.assertEquals("Should return correct task queue item", "url1", items.get(0).getUrl());
    }
}