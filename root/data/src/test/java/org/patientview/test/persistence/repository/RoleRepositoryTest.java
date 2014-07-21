package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.RoleType;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleTypes;
import org.patientview.persistence.model.enums.Roles;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

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
    LookupRepository lookupRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;
    LookupType lookupType;

    @Inject
    EntityManager entityManager;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
        lookupType = new LookupType();
        lookupType.setType(LookupTypes.ROLE);
    }

    @Test
    @Ignore
    public void testGetRole() {
        Role role = dataTestUtils.createRole("PATIENT", creator);
        entityManager.setFlushMode(FlushModeType.COMMIT);

        RoleType roleType = new RoleType();
        roleType.setLookupType(lookupType);
        roleType.setValue(RoleTypes.PATIENT);
        entityManager.persist(lookupType);
        entityManager.persist(roleType);

        role.setCreator(creator);

        roleRepository.save(role);
        entityManager.flush();
        role = roleRepository.findOne(role.getId());

        Assert.assertEquals("The lookup type should be return",role.getRoleType().getValue() == roleType.getValue());
    }



}
