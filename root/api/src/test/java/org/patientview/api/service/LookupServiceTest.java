package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.LookupServiceImpl;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.test.util.TestUtils;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 15/07/2014
 */
public class LookupServiceTest {

    User creator;

    @Mock
    LookupTypeRepository lookupTypeRepository;

    @InjectMocks
    LookupService lookupService = new LookupServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetPatientManagementLookupTypes() {
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.GENDER);
        lookupType.getLookups().add(TestUtils.createLookup(lookupType, "1", "Male"));
        lookupType.getLookups().add(TestUtils.createLookup(lookupType, "2", "Female"));
        lookupType.getLookups().add(TestUtils.createLookup(lookupType, "3", "Not Known"));

        when(lookupTypeRepository.findByType(eq(LookupTypes.GENDER))).thenReturn(lookupType);

        List<org.patientview.api.model.LookupType> types = lookupService.getPatientManagementLookupTypes();

        Assert.assertNotNull("Should return types", types);
        Assert.assertEquals("Should return one lookup type", 1, types.size());
        Assert.assertEquals("Should return correct lookup type", lookupType.getId(), types.get(0).getId());

        verify(lookupTypeRepository, Mockito.times(1)).findByType(eq(LookupTypes.GENDER));
        verify(lookupTypeRepository, Mockito.times(10)).findByType(any(LookupTypes.class));
    }
}
