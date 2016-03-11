package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.ApiKey;
import org.patientview.persistence.model.enums.ApiKeyTypes;
import org.patientview.persistence.repository.ApiKeyRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/03/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class ApiKeyRepositoryTest {

    @Inject
    private ApiKeyRepository apiKeyRepository;

    @Before
    public void setup() {

    }

    @Test
    public void testFindByApiKeyAndType() {
        ApiKey apiKey = new ApiKey();
        apiKey.setKey("abc123");
        apiKey.setExpiryDate(new Date());
        apiKey.setType(ApiKeyTypes.CKD);
        apiKeyRepository.save(apiKey);

        List<ApiKey> apiKeys = apiKeyRepository.findByKeyAndType(apiKey.getKey(), apiKey.getType());
        Assert.assertEquals("There should be 1 api key", 1, apiKeys.size());
        Assert.assertTrue("The api key should be the one created", apiKeys.get(0).equals(apiKey));
    }
}
