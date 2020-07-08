package org.patientview.test.service;

import org.hl7.fhir.instance.model.Attachment;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.ResourceType;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirDocumentReference;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.DocumentReferenceService;
import org.patientview.service.impl.DocumentReferenceServiceImpl;
import org.patientview.test.util.TestUtils;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class DocumentReferenceServiceTest extends BaseTest {

    @Mock
    AlertRepository alertRepository;

    @InjectMocks
    DocumentReferenceService documentReferenceService = new DocumentReferenceServiceImpl();

    @Mock
    FhirResource fhirResource;

    @Mock
    FileDataRepository fileDataRepository;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testAdd_apiExisting() throws Exception {
        Group sourceGroup = TestUtils.createGroup("sourceGroup");
        Date now = new Date();
        Date weekAgo = new DateTime(now).minusWeeks(1).toDate();

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

        // existing fhirlink
        FhirLink fhirLink = TestUtils.createFhirLink(patientUser, identifier, sourceGroup);

        // FhirDocumentReference to be imported
        FhirDocumentReference fhirDocumentReference = new FhirDocumentReference();
        fhirDocumentReference.setType("someType");
        fhirDocumentReference.setFileBase64("ABCD123EFG456");
        fhirDocumentReference.setContent("someContent");
        fhirDocumentReference.setDate(now);

        // FileData saved when importing binary data
        FileData fileData = new FileData();
        fileData.setId(1L);

        // Media FhirDatabaseEntry returned when Media is created in FHIR
        FhirDatabaseEntity mediaFhirDataBaseEntity = new FhirDatabaseEntity();
        mediaFhirDataBaseEntity.setLogicalId(UUID.randomUUID());

        // Map of existing UUID to type+content, note cleaned up type+content in actual service
        Map<String, String> existingDocumentReferenceMap = new HashMap<>();
        existingDocumentReferenceMap.put(UUID.randomUUID().toString(),
                fhirDocumentReference.getType() + fhirDocumentReference.getContent());

        // location UUID, used to point from DocumentReference to Media
        UUID locationUuid = UUID.randomUUID();

        // existing Media, points to patientview filedata in content -> url
        Media media = new Media();
        media.setContent(new Attachment()).getContent().setUrlSimple("1");

        // Alert set up by patient for new letters
        Alert alert = new Alert();
        alert.setAlertType(AlertTypes.LETTER);
        alert.setLatestDate(weekAgo);
        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);

        when(alertRepository.findByUserAndAlertType(eq(fhirLink.getUser()), eq(AlertTypes.LETTER)))
                .thenReturn(alerts);
        when(alertRepository.findById(eq(alert.getId()))).thenReturn(Optional.of(alert));
        when(fhirResource.createEntity(any(Media.class), eq(ResourceType.Media.name()), eq("media")))
                .thenReturn(mediaFhirDataBaseEntity);
        when(fhirResource.get(any(UUID.class), eq(ResourceType.Media))).thenReturn(media);
        when(fhirResource.getExistingLetterDocumentReferenceTypeAndContentBySubjectId(eq(fhirLink.getResourceId())))
                .thenReturn(existingDocumentReferenceMap);
        when(fhirResource.getLocationUuidFromLogicalUuid(
                eq(UUID.fromString(existingDocumentReferenceMap.keySet().iterator().next())),
                eq("documentreference")))
                .thenReturn(locationUuid.toString());
        when(fileDataRepository.existsById(eq(Long.valueOf(media.getContent().getUrlSimple())))).thenReturn(true);
        when(fileDataRepository.save(any(FileData.class))).thenReturn(fileData);

        documentReferenceService.add(fhirDocumentReference, fhirLink);

        verify(alertRepository, times(1)).findByUserAndAlertType(eq(fhirLink.getUser()), eq(AlertTypes.LETTER));
        verify(alertRepository, times(1)).save(any(Alert.class));
        verify(fhirResource, times(1)).createEntity(any(DocumentReference.class),
                eq(ResourceType.DocumentReference.name()), eq("documentreference"));
        verify(fhirResource, times(1)).createEntity(any(Media.class),
                eq(ResourceType.Media.name()), eq("media"));
        verify(fhirResource, times(1)).deleteEntity(any(UUID.class), eq("documentreference"));
        verify(fhirResource, times(1)).deleteEntity(any(UUID.class), eq("media"));
        verify(fhirResource, times(1)).get(any(UUID.class), eq(ResourceType.Media));
        verify(fhirResource, times(1)).getExistingLetterDocumentReferenceTypeAndContentBySubjectId(any(UUID.class));
        verify(fhirResource, times(1)).getLocationUuidFromLogicalUuid(any(UUID.class), eq("documentreference"));
        verify(fileDataRepository, times(1)).existsById(any(Long.class));
        verify(fileDataRepository, times(1)).deleteById(any(Long.class));
        verify(fileDataRepository, times(1)).save(any(FileData.class));
    }

    @Test
    public void testAdd_apiNew() throws Exception {
        Group sourceGroup = TestUtils.createGroup("sourceGroup");
        Date now = new Date();
        Date weekAgo = new DateTime(now).minusWeeks(1).toDate();

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

        // existing fhirlink
        FhirLink fhirLink = TestUtils.createFhirLink(patientUser, identifier, sourceGroup);

        // FhirDocumentReference to be imported
        FhirDocumentReference fhirDocumentReference = new FhirDocumentReference();
        fhirDocumentReference.setType("someType");
        fhirDocumentReference.setFileBase64("ABCD123EFG456");
        fhirDocumentReference.setDate(now);

        // FileData saved when importing binary data
        FileData fileData = new FileData();
        fileData.setId(1L);

        // Media FhirDatabaseEntry returned when Media is created in FHIR
        FhirDatabaseEntity mediaFhirDataBaseEntity = new FhirDatabaseEntity();
        mediaFhirDataBaseEntity.setLogicalId(UUID.randomUUID());

        // Alert set up by patient for new letters
        Alert alert = new Alert();
        alert.setAlertType(AlertTypes.LETTER);
        alert.setLatestDate(weekAgo);
        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);

        when(alertRepository.findByUserAndAlertType(eq(fhirLink.getUser()), eq(AlertTypes.LETTER)))
                .thenReturn(alerts);
        when(alertRepository.findById(eq(alert.getId()))).thenReturn(Optional.of(alert));
        when(fhirResource.createEntity(any(Media.class), eq(ResourceType.Media.name()), eq("media")))
                .thenReturn(mediaFhirDataBaseEntity);
        when(fileDataRepository.save(any(FileData.class))).thenReturn(fileData);

        documentReferenceService.add(fhirDocumentReference, fhirLink);

        verify(alertRepository, times(1)).findByUserAndAlertType(eq(fhirLink.getUser()), eq(AlertTypes.LETTER));
        verify(alertRepository, times(1)).save(any(Alert.class));
        verify(fhirResource, times(1)).createEntity(any(DocumentReference.class),
                eq(ResourceType.DocumentReference.name()), eq("documentreference"));
        verify(fhirResource, times(1)).createEntity(any(Media.class),
                eq(ResourceType.Media.name()), eq("media"));
        verify(fhirResource, times(0)).deleteEntity(any(UUID.class), eq("documentreference"));
        verify(fhirResource, times(0)).deleteEntity(any(UUID.class), eq("media"));
        verify(fhirResource, times(0)).get(any(UUID.class), eq(ResourceType.Media));
        verify(fhirResource, times(1)).getExistingLetterDocumentReferenceTypeAndContentBySubjectId(any(UUID.class));
        verify(fhirResource, times(0)).getLocationUuidFromLogicalUuid(any(UUID.class), eq("documentreference"));
        verify(fileDataRepository, times(0)).existsById(any(Long.class));
        verify(fileDataRepository, times(0)).deleteById(any(Long.class));
        verify(fileDataRepository, times(1)).save(any(FileData.class));
    }
}
