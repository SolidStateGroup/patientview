package org.patientview.importer.service.impl;

import generated.Patientview;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.patientview.importer.BaseTest;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.PatientService;
import org.patientview.importer.util.Util;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class PatientServiceImplTest extends BaseTest {

    @Mock
    FhirResource fhirResource;

    @Mock
    LookupRepository lookupRepository;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    FhirLinkRepository fhirLinkRepository;

    @InjectMocks
    PatientService patientService = new PatientServiceImpl();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        PowerMockito.mockStatic(Util.class);
    }

    /**
     * Test: Create a patient with a UUId and make sure this UUID is deleted and a new record created.
     *
     * @throws Exception
     */
    @Test
    public void testAdd() throws Exception {

        when(Util.getVersionId(any(JSONObject.class))).thenReturn(UUID.randomUUID());

        Patientview patient = unmarshallPatientRecord(super.getTestFile());
        String nhsNumber = patient.getPatient().getPersonaldetails().getNhsno();
        User user = TestUtils.createUser("NewPatient");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER), "NHS_NUMBER");
        Identifier identifier = TestUtils.createIdentifier(lookup, user, nhsNumber);
        TestUtils.createFhirLink(user, identifier);

        when(lookupRepository.findByTypeAndValue(eq(LookupTypes.IDENTIFIER), eq("NHS_NUMBER"))).thenReturn(lookup);
        when(identifierRepository.findByTypeAndValue(eq(nhsNumber), eq(lookup))).thenReturn(TestUtils.createIdentifier(lookup, user, nhsNumber));

        patientService.add(patient);

        verify(this.fhirResource, Mockito.times(1)).create(any(org.hl7.fhir.instance.model.Resource.class));
    }


    private static Patientview unmarshallPatientRecord(String content) throws ImportResourceException {
        try {
            JAXBContext jc = JAXBContext.newInstance(Patientview.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return (Patientview) unmarshaller.unmarshal(new StringReader(content));
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall patientview record");
        }

    }
}
