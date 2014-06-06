package org.patientview.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.api.config.ApiConfig;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApiConfig.class)
@WebAppConfiguration
@Transactional
public class TestAdminServiceImpl {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Inject
    private UserRepository userRepository;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    public void testApplicationEntityManager() {
        User user = new User();
        user.setUsername("migration");
        user.setName("Data Migration");
        user.setChangePassword(Boolean.FALSE);
        user.setLocked(Boolean.FALSE);
        user.setEmail("support@patient.org");
        user.setCreator(user);
        user.setPassword("");
        userRepository.save(user);
    }



}
