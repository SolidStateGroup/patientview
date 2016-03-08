package org.patientview.test.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.patientview.persistence.model.FhirDocumentReference;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.AuditService;
import org.patientview.service.DocumentReferenceService;
import org.patientview.service.impl.DocumentReferenceServiceImpl;
import org.patientview.test.util.TestUtils;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(PowerMockRunner.class)
public class DocumentReferenceServiceTest extends BaseTest {

    @Mock
    AlertRepository alertRepository;

    @Mock
    AuditService auditService;

    @Mock
    FhirResource fhirResource;

    @InjectMocks
    DocumentReferenceService documentReferenceService = new DocumentReferenceServiceImpl();

    @Mock
    GroupRepository groupRepository;

    @Mock
    GroupRoleRepository groupRoleRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    UserRepository userRepository;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testAdd() throws Exception {
        Group sourceGroup = TestUtils.createGroup("sourceGroup");

        // patient
        User patientUser = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, sourceGroup, patientUser);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patientUser.setGroupRoles(groupRolesPatient);

        // identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patientUser, "1111111111");

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // existing fhirlink
        FhirLink fhirLink = TestUtils.createFhirLink(patientUser, identifier, sourceGroup);

        FhirDocumentReference fhirDocumentReference = new FhirDocumentReference();

        documentReferenceService.add(fhirDocumentReference, fhirLink);

        //verify(gpLetterRepository, times(1)).save(any(GpLetter.class));
    }
}
