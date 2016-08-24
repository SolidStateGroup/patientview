package org.patientview.api.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.MedlinePlusServiceImpl;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.test.util.TestUtils;

/**
 * Unit tests for Medline plus manager
 */
public class MedlinePlusServiceTest {

    @Mock
    CodeRepository codeRepository;

    @InjectMocks
    MedlinePlusService medlinePlusService = new MedlinePlusServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testSynchroniseConditions() throws Exception {


        // medlinePlusService.syncICD10Codes();

        // existing NhschoicesCondition - code1 has been removed so should be marked as removed externally
//        List<NhschoicesCondition> conditions = new ArrayList<>();
//        conditions.add(new NhschoicesCondition("code2", "someName2", "someUri2"));
//
//        // new NhschoicesCondition
//        conditions.add(new NhschoicesCondition("code3", "someName3", "someUri3"));
//
//        when(codeRepository.findAllByStandardType(eq(standardType))).thenReturn(existingCodes);
//        when(lookupRepository.findByTypeAndValue(eq(LookupTypes.CODE_TYPE), eq(codeType.getValue())))
//                .thenReturn(codeType);
//        when(lookupRepository.findByTypeAndValue(eq(LookupTypes.CODE_STANDARD), eq(standardType.getValue())))
//                .thenReturn(standardType);
//        when(nhschoicesConditionRepository.findAll()).thenReturn(conditions);
//
//        nhsChoicesService.synchroniseConditions();
//
//        verify(codeRepository, Mockito.times(1)).findAllByStandardType(eq(standardType));
//        verify(codeRepository, Mockito.times(1)).save(any(List.class));
//        verify(lookupRepository, Mockito.times(1))
//                .findByTypeAndValue(eq(LookupTypes.CODE_TYPE), eq(codeType.getValue()));
//        verify(lookupRepository, Mockito.times(1))
//                .findByTypeAndValue(eq(LookupTypes.CODE_STANDARD), eq(standardType.getValue()));
//        verify(nhschoicesConditionRepository, Mockito.times(1)).save(any(List.class));
    }
}
