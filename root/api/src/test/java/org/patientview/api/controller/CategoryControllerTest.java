package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.CategoryService;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 20/06/2016
 */
public class CategoryControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetAllCategories() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        mockMvc.perform(MockMvcRequestBuilders.get("/categories/all"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(categoryService, Mockito.times(1)).getCategories();
    }

    @Test
    public void testGetCategories() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        mockMvc.perform(MockMvcRequestBuilders.get("/categories"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(categoryService, Mockito.times(1)).getCategories(any(GetParameters.class));
    }

    @Test
    public void testGetCategory() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        mockMvc.perform(MockMvcRequestBuilders.get("/categories/1"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(categoryService, Mockito.times(1)).getCategory(eq(1L));
    }

    @Test
    public void testDeleteCategory() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        mockMvc.perform(MockMvcRequestBuilders.delete("/categories/1"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(categoryService, Mockito.times(1)).delete(eq(1L));
    }
}
