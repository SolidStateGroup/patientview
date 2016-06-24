package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.CodeService;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 15/07/2014
 */
public class CodeControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private CodeService codeService;

    @InjectMocks
    private CodeController codeController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(codeController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testCreateCode() throws Exception {
        Code testCode = TestUtils.createCode("TestCode");
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        when(codeService.add(eq(testCode))).thenReturn(testCode);
        mockMvc.perform(MockMvcRequestBuilders.post("/code")
                .content(mapper.writeValueAsString(testCode)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
        verify(codeService, Mockito.times(1)).add(eq(testCode));
    }

    @Test
    public void testDeleteCode() throws Exception {
        Code testCode = TestUtils.createCode("TestCode");
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        mockMvc.perform(MockMvcRequestBuilders.delete("/code/" + testCode.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(codeService, Mockito.times(1)).delete(eq(testCode.getId()));
    }

    @Test
    public void testGetCodes() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        mockMvc.perform(MockMvcRequestBuilders.get("/code"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(codeService, Mockito.times(1)).getAllCodes(any(GetParameters.class));
    }

    @Test
    public void testGetByCategory() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.PATIENT);
        mockMvc.perform(MockMvcRequestBuilders.get("/codes/category/1"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(codeService, Mockito.times(1)).getByCategory(eq(1L));
    }

    @Test
    public void testSearchDiagnosisCodes() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.PATIENT);
        mockMvc.perform(MockMvcRequestBuilders.get("/codes/diagnosis/searchTerm"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(codeService, Mockito.times(1)).searchDiagnosisCodes(eq("searchTerm"), isNull(String.class));
    }

    @Test
    public void testSearchDiagnosisCodesByStandard() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.PATIENT);
        mockMvc.perform(MockMvcRequestBuilders.get("/codes/diagnosis/searchTerm/standard/standardType"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(codeService, Mockito.times(1)).searchDiagnosisCodes(eq("searchTerm"), eq("standardType"));
    }
}
