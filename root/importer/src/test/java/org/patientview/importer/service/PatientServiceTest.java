package org.patientview.importer.service;

import generated.Patientview;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.patientview.importer.BaseTest;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.importer.service.impl.PatientServiceImpl;
import org.patientview.importer.Utility.Util;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class PatientServiceTest extends BaseTest {

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
     * Test: Create a patient with a UUId.
     *
     * @throws Exception
     */
    // TODO: fix these tests see https://code.google.com/p/powermock/issues/detail?id=504
    @Test
    @Ignore("powermock fail for jdk 1.7.0_67")
    public void testPatientAdd() throws Exception {
        when(Util.getVersionId(any(JSONObject.class))).thenReturn(UUID.randomUUID());

        Patientview patient = unmarshallPatientRecord(super.getTestFile());
        String nhsNumber = patient.getPatient().getPersonaldetails().getNhsno();
        User user = TestUtils.createUser("NewPatient");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER), "NHS_NUMBER");
        Identifier identifier = TestUtils.createIdentifier(lookup, user, nhsNumber);
        TestUtils.createFhirLink(user, identifier);

        when(lookupRepository.findByTypeAndValue(eq(LookupTypes.IDENTIFIER), eq("NHS_NUMBER"))).thenReturn(lookup);
        when(identifierRepository.findByTypeAndValue(eq(nhsNumber), eq(lookup))).thenReturn(TestUtils.createIdentifier(lookup, user, nhsNumber));

        patientService.add(patient, new ResourceReference());

        verify(fhirResource, Mockito.times(1)).createEntity(any(org.hl7.fhir.instance.model.Resource.class), any(String.class), any(String.class));
        verify(fhirLinkRepository, Mockito.times(1)).findByUserAndGroupAndIdentifier(any(User.class), any(Group.class), any(Identifier.class));
        verify(userRepository, Mockito.times(1)).save(eq(user));
    }

    /**
     * Test: Create a patient with a UUId and make sure patient record is updated.
     *
     * @throws Exception
     */
    // TODO: fix these tests see https://code.google.com/p/powermock/issues/detail?id=504
    @Test
    @Ignore("powermock fail for jdk 1.7.0_67")
    public void testPatientAdd_WithUpdate() throws Exception {
        when(Util.getVersionId(any(JSONObject.class))).thenReturn(UUID.randomUUID());

        Patientview patient = unmarshallPatientRecord(super.getTestFile());
        String nhsNumber = patient.getPatient().getPersonaldetails().getNhsno();
        User user = TestUtils.createUser("NewPatient");
        Group group = TestUtils.createGroup("PatientGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER), "NHS_NUMBER");
        Identifier identifier = TestUtils.createIdentifier(lookup, user, nhsNumber);
        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);
        fhirLink.setResourceType(ResourceType.Patient.name());
        List<FhirLink> fhirLinks = new ArrayList<>();
        fhirLinks.add(fhirLink);

        when(groupRepository.findByCode(any(String.class))).thenReturn(group);
        when(lookupRepository.findByTypeAndValue(eq(LookupTypes.IDENTIFIER), eq("NHS_NUMBER"))).thenReturn(lookup);
        when(identifierRepository.findByTypeAndValue(eq(nhsNumber), eq(lookup))).thenReturn(identifier);
        when(fhirLinkRepository.findByUserAndGroupAndIdentifier(eq(user), eq(group), eq(identifier))).thenReturn(fhirLinks);

        patientService.add(patient, new ResourceReference());

        verify(fhirResource, Mockito.times(1)).updateEntity(any(Patient.class), eq(ResourceType.Patient.name()), eq(fhirLink.getResourceId()));
        verify(fhirLinkRepository, Mockito.times(1)).findByUserAndGroupAndIdentifier(any(User.class), any(Group.class), any(Identifier.class));
        verify(userRepository, Mockito.times(1)).save(eq(user));
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
