package org.patientview.importer.service.impl;

import generated.Patientview;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.patientview.importer.BaseTest;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.PatientService;
import org.patientview.importer.util.Util;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PatientServiceImplTest extends BaseTest {

    @Mock
    FhirResource fhirResource;

    @Mock
    LookupRepository lookupRepository;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    PatientService patientService = new PatientServiceImpl();

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test: Create a patient with a UUId and make sure this UUID is deleted and a new record created.
     *
     * @throws Exception
     */
    @Test
    public void testAdd() throws Exception {


        Patientview patient = Util.unmarshallPatientRecord(super.getTestFile());
        String nhsNumber = patient.getPatient().getPersonaldetails().getNhsno();
        User user = TestUtils.createUser("NewPatient");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER), "NHS_NUMBER");
        Identifier identifier = TestUtils.createIdentifier(lookup, user, nhsNumber);
        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);

        when(lookupRepository.findByTypeAndValue(eq(LookupTypes.IDENTIFIER), eq("NHS_NUMBER"))).thenReturn(lookup);
        when(identifierRepository.findByTypeAndValue(eq(nhsNumber), eq(lookup))).thenReturn(TestUtils.createIdentifier(lookup, user, nhsNumber));

        patientService.add(patient);

        verify(this.fhirResource, Mockito.times(1)).createResource(any(org.hl7.fhir.instance.model.Resource.class));
    }
}