package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.CodeService;
import org.patientview.persistence.model.Code;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
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

    @Test
    public void testCreateCode() {

        Code testCode = TestUtils.createCode("TestCode");

        try {
            when(codeService.add(eq(testCode))).thenReturn(testCode);
            mockMvc.perform(MockMvcRequestBuilders.post("/code")
                    .content(mapper.writeValueAsString(testCode)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
            verify(codeService, Mockito.times(1)).add(eq(testCode));
        } catch (Exception e) {
            fail("This call should not fail");
        }

    }


}
