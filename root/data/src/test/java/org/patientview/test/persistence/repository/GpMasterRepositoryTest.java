package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.GpCountries;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class GpMasterRepositoryTest {

    @Inject
    private GpMasterRepository gpMasterRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testFindByPracticeCode() {
        GpMaster gp = new GpMaster();
        gp.setPracticeCode("123456");
        gp.setPracticeName("practice name");
        gp.setAddress1("address 1");
        gp.setAddress2("address 2");
        gp.setAddress3("address 3");
        gp.setAddress4("address 4");
        gp.setPostcode("AB12CDE");
        gp.setCountry(GpCountries.ENG);
        gp.setTelephone("0123456789");
        gp.setStatusCode("active");

        gpMasterRepository.save(gp);

        List<GpMaster> gps = gpMasterRepository.findByPracticeCode(gp.getPracticeCode());
        Assert.assertEquals("There should be 1 gp", 1, gps.size());
        Assert.assertTrue("The GP should be the one created", gps.get(0).equals(gp));
    }

    @Test(expected = PersistenceException.class)
    public void testSave_noPracticeCode() {
        GpMaster gp = new GpMaster();
        gp.setPracticeName("practice name");
        gp.setAddress1("address 1");
        gp.setAddress2("address 2");
        gp.setAddress3("address 3");
        gp.setAddress4("address 4");
        gp.setPostcode("AB12CDE");
        gp.setCountry(GpCountries.ENG);
        gp.setTelephone("0123456789");
        gp.setStatusCode("active");

        gpMasterRepository.save(gp);
    }
}
