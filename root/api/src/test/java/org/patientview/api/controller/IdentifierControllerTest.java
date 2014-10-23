package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.IdentifierService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.fail;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
public class IdentifierControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private IdentifierService identifierService;

    @InjectMocks
    private IdentifierController identifierController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(identifierController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testUpdateIdentifier() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);
        Identifier testIdentifier = new Identifier();
        testIdentifier.setId(1L);

        try {
            mockMvc.perform(MockMvcRequestBuilders.put("/identifier")
                    .content(mapper.writeValueAsString(testIdentifier)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testGetIdentifier() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);
        Long identifierId = 1L;
        String url = "/identifier/" + identifierId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.get(url)).andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteIdentifier() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);
        Long identifierId = 1L;
        String url = "/identifier/" + identifierId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testAddIdentifier() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);

        User testUser = TestUtils.createUser("testUser");
        String url = "/user/" + testUser.getId() + "/identifiers";
        Identifier identifier = new Identifier();
        identifier.setId(2L);

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                    .content(mapper.writeValueAsString(identifier)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
        }
        catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testGetIdentifierByValue() throws ResourceNotFoundException  {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);
        String identifierValue = "111111111";

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/identifier/value/" + identifierValue))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }

    @Test
    public void testValidateIdentifier() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);

        User testUser = TestUtils.createUser("testUser");
        String url = "/user/" + testUser.getId() + "/identifier/validate";
        Identifier identifier = new Identifier();

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                    .content(mapper.writeValueAsString(identifier)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }
        catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }
}
