package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 17/07/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class RoleRepositoryTest {

    @Inject
    RoleRepository roleRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;
    LookupType lookupType;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
        lookupType = new LookupType();
        lookupType.setType(LookupTypes.ROLE);
    }

    @Test
    public void testGetRoleByTypeAndName() {
        Role role = dataTestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        Role entityRole = roleRepository.findByRoleTypeAndName(RoleType.PATIENT, RoleName.PATIENT);

        Assert.assertNotNull("Should return Role", entityRole);
        Assert.assertEquals("Should return same Role", role, entityRole);
        Assert.assertEquals("Correct Role should be returned", RoleName.PATIENT, entityRole.getName());
    }

    @Test
    public void testGetRoleByTypeAndNameIncludeHidden() {
        Role role = dataTestUtils.createRole(RoleName.IMPORTER, RoleType.STAFF, false);
        Role entityRole = roleRepository.findByRoleTypeAndName(RoleType.STAFF, RoleName.IMPORTER, false);

        Assert.assertNotNull("Should return Role", entityRole);
        Assert.assertEquals("Should return same Role", role, entityRole);
        Assert.assertEquals("Correct Role should be returned", RoleName.IMPORTER, entityRole.getName());
    }
}
