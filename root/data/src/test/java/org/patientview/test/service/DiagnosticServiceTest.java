package org.patientview.test.service;

import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Media;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirDiagnosticReport;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.DiagnosticReportTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.DiagnosticService;
import org.patientview.service.impl.DiagnosticServiceImpl;
import org.patientview.test.util.TestUtils;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class DiagnosticServiceTest extends BaseTest {

    @InjectMocks
    DiagnosticService diagnosticService = new DiagnosticServiceImpl();

    @Mock
    FhirResource fhirResource;

    @Mock
    FileDataRepository fileDataRepository;

    private Date now;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        now = new Date();
    }

    @Test
    public void testAdd_api() throws Exception {
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

        // existing fhirlink
        FhirLink fhirLink = TestUtils.createFhirLink(patientUser, identifier, sourceGroup);

        // FhirDiagnosticReport to be imported
        FhirDiagnosticReport fhirDiagnosticReport = new FhirDiagnosticReport();
        fhirDiagnosticReport.setName("an imaging diagnostic");
        fhirDiagnosticReport.setType(DiagnosticReportTypes.IMAGING.toString());
        fhirDiagnosticReport.setDate(now);
        fhirDiagnosticReport.setFilename("diagnostic.pdf");
        fhirDiagnosticReport.setFiletype("application/pdf");
        fhirDiagnosticReport.setFileBase64("ABCD123EFG456");
        fhirDiagnosticReport.setResult(new FhirObservation());
        fhirDiagnosticReport.getResult().setValue("some diagnostic result");

        // FileData saved when importing binary data
        FileData fileData = new FileData();
        fileData.setId(1L);

        // Media FhirDatabaseEntry returned when Media is created in FHIR
        FhirDatabaseEntity mediaFhirDataBaseEntity = new FhirDatabaseEntity();
        mediaFhirDataBaseEntity.setLogicalId(UUID.randomUUID());

        // Observation FhirDatabaseEntry returned when Media is created in FHIR
        FhirDatabaseEntity observationFhirDataBaseEntity = new FhirDatabaseEntity();
        observationFhirDataBaseEntity.setLogicalId(UUID.randomUUID());

        when(fhirResource.createEntity(any(Media.class), eq(ResourceType.Media.name()), eq("media")))
                .thenReturn(mediaFhirDataBaseEntity);
        when(fhirResource.createEntity(any(Observation.class), eq(ResourceType.Observation.name()), eq("observation")))
                .thenReturn(observationFhirDataBaseEntity);
        when(fileDataRepository.save(any(FileData.class))).thenReturn(fileData);

        diagnosticService.add(fhirDiagnosticReport, fhirLink);

        verify(fhirResource, times(1)).createEntity(any(Observation.class),
                eq(ResourceType.Observation.name()), eq("observation"));
        verify(fhirResource, times(1)).createEntity(any(Media.class), eq(ResourceType.Media.name()), eq("media"));
        verify(fhirResource, times(1)).createEntity(
                any(DiagnosticReport.class), eq(ResourceType.DiagnosticReport.name()), eq("diagnosticreport"));
        verify(fileDataRepository, times(1)).save(any(FileData.class));
    }
}
