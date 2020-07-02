package org.patientview.api.service;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.api.service.impl.DocumentServiceImpl;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LetterTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.DocumentReferenceService;
import org.patientview.test.util.TestUtils;
import org.patientview.util.Util;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 21/06/2016
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class DocumentServiceTest {

    User creator;

    @Mock
    ApiPatientService apiPatientService;

    @Mock
    DocumentReferenceService documentReferenceService;

    @Mock
    GroupRepository groupRepository;

    @Mock
    GroupService groupService;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    FhirResource fhirResource;

    @InjectMocks
    DocumentService documentService = new DocumentServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
        PowerMockito.mockStatic(Util.class);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetByUserId() {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        user.setIdentifiers(new HashSet<Identifier>());

        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);
        fhirLink.setResourceType(ResourceType.Patient.name());
        fhirLink.setGroup(group);

        DocumentReference documentReference = new DocumentReference();

        DateTime letterDate = new DateTime();
        DateAndTime date = new DateAndTime(new Date());
        letterDate.setValue(date);
        documentReference.setCreated(letterDate);

        CodeableConcept type = new CodeableConcept();
        type.setTextSimple(LetterTypes.CLINIC_LETTER.toString());
        documentReference.setType(type);

        documentReference.setDescriptionSimple("LETTER_CONTENT");

        List<DocumentReference> documentReferences = new ArrayList<>();
        documentReferences.add(documentReference);

        try {
            when(userRepository.findById(Matchers.eq(user.getId()))).thenReturn(Optional.of(user));
            when(fhirResource.findResourceByQuery(any(String.class), eq(DocumentReference.class)))
                    .thenReturn(documentReferences);

            List<FhirDocumentReference> fhirDocumentReferences
                    = documentService.getByUserIdAndClass(user.getId(), null, null, null);
            Assert.assertEquals("Should return 1 FhirDocumentReference", 1, fhirDocumentReferences.size());
            Assert.assertEquals("Should have correct content", "LETTER_CONTENT",
                    fhirDocumentReferences.get(0).getContent());

        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException: " + rnf.getMessage());
        } catch (FhirResourceException fre) {
            Assert.fail("FhirResourceException: " + fre.getMessage());
        }
    }
}
