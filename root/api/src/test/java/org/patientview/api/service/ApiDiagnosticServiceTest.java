package org.patientview.api.service;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.FhirDiagnosticReport;
import org.patientview.api.service.impl.ApiDiagnosticServiceImpl;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirDiagnosticReportRange;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.DiagnosticReportObservationTypes;
import org.patientview.persistence.model.enums.DiagnosticReportTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.ObservationHeadingGroupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.ResultClusterRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.DiagnosticService;
import org.patientview.service.FhirLinkService;
import org.patientview.test.util.TestUtils;
import org.patientview.util.Util;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/10/2014
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class ApiDiagnosticServiceTest {

    User creator;

    @InjectMocks
    ApiDiagnosticService apiDiagnosticService = new ApiDiagnosticServiceImpl();

    @Mock
    ApiPatientService apiPatientService;

    @Mock
    DiagnosticService diagnosticService;

    @Mock
    FhirLinkService fhirLinkService;

    @Mock
    FhirResource fhirResource;

    @Mock
    GroupRepository groupRepository;

    @Mock
    GroupService groupService;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    ObservationHeadingGroupRepository observationHeadingGroupRepository;

    @Mock
    ObservationHeadingRepository observationHeadingRepository;

    @Mock
    ResultClusterRepository resultClusterRepository;

    @Mock
    UserService userService;

    private Date now;
    private Date weekAgo;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
        PowerMockito.mockStatic(Util.class);
        this.now = new Date();
        this.weekAgo = new org.joda.time.DateTime(now).minusWeeks(1).toDate();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetByUserId() {

        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);
        fhirLink.setResourceType(ResourceType.Patient.name());
        fhirLink.setGroup(group);

        DiagnosticReport diagnosticReport = new DiagnosticReport();

        DateTime diagnosticDate = new DateTime();
        DateAndTime date = new DateAndTime(new Date());
        diagnosticDate.setValue(date);
        diagnosticReport.setDiagnostic(diagnosticDate);

        CodeableConcept name = new CodeableConcept();
        name.setTextSimple("EXAMPLE_DIAGNOSTIC_NAME");
        diagnosticReport.setName(name);

        CodeableConcept type = new CodeableConcept();
        type.setTextSimple(DiagnosticReportTypes.IMAGING.toString());
        diagnosticReport.setServiceCategory(type);

        diagnosticReport.setStatusSimple(DiagnosticReport.DiagnosticReportStatus.registered);

        ResourceReference resultReference = diagnosticReport.addResult();
        resultReference.setDisplaySimple("12345678-230a-4ce0-879b-443154a4d9e6");

        List<DiagnosticReport> diagnosticReports = new ArrayList<>();
        diagnosticReports.add(diagnosticReport);

        // create JSON object for observation (used for result)
        JSONObject resultJson = new JSONObject();
        String versionId = "31d2f326-230a-4ce0-879b-443154a4d9e6";
        String resourceId = "d52847eb-c2c7-4015-ba6c-952962536287";

        JSONObject link = new JSONObject();
        link.put("href", "http://www.patientview.org/patient/" + versionId);

        JSONArray links = new JSONArray();
        links.put(link);

        JSONObject valueQuantity = new JSONObject();
        valueQuantity.put("value", "99.9");

        JSONObject resultName = new JSONObject();
        JSONArray resultNameCoding = new JSONArray();
        JSONObject resultNameCodingDisplay = new JSONObject();
        resultName.put("text", DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.toString());
        resultNameCodingDisplay.put("display", DiagnosticReportObservationTypes.DIAGNOSTIC_RESULT.getName());
        resultNameCoding.put(0, resultNameCodingDisplay);
        resultName.put("coding", resultNameCoding);

        JSONObject resource = new JSONObject();
        resource.put("link", links);
        resource.put("id", resourceId);
        resource.put("resourceType", "Observation");
        resource.put("valueQuantity", valueQuantity);
        resource.put("name", resultName);

        JSONObject content = new JSONObject();
        content.put("content", resource);

        JSONArray resultArray = new JSONArray();
        resultArray.put(content);

        resultJson.put("entry", resultArray);

        try {
            when(userService.get(Matchers.eq(user.getId()))).thenReturn(user);
            when(fhirResource.findResourceByQuery(any(String.class), eq(DiagnosticReport.class)))
                    .thenReturn(diagnosticReports);
            when(fhirResource.getResource(any(UUID.class), eq(ResourceType.Observation))).thenReturn(resultJson);

            List<FhirDiagnosticReport> fhirDiagnosticReports = apiDiagnosticService.getByUserId(user.getId());
            Assert.assertEquals("Should return 1 FhirDiagnostic", 1, fhirDiagnosticReports.size());
            Assert.assertEquals("Should have correct value", "99.9", fhirDiagnosticReports.get(0).getResult().getValue());

        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException: " + rnf.getMessage());
        } catch (FhirResourceException fre) {
            Assert.fail("FhirResourceException: " + fre.getMessage());
        }
    }

    @Test
    public void testImportDiagnostics() throws Exception {
        // auth
        Group group = TestUtils.createGroup("testGroup");
        Role staffRole = TestUtils.createRole(RoleName.IMPORTER);
        User staff = TestUtils.createUser("testStaff");
        GroupRole groupRole = TestUtils.createGroupRole(staffRole, group, staff);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staff.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(staff, groupRoles);

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        // identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patient, "1111111111");

        TestUtils.createFhirLink(patient, identifier, group);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // FhirDiagnosticReportRange
        FhirDiagnosticReportRange fhirDiagnosticReportRange = new FhirDiagnosticReportRange();
        fhirDiagnosticReportRange.setGroupCode("DSF01");
        fhirDiagnosticReportRange.setIdentifier("1111111111");
        fhirDiagnosticReportRange.setStartDate(weekAgo);
        fhirDiagnosticReportRange.setEndDate(now);
        fhirDiagnosticReportRange.setDiagnostics(
                new ArrayList<org.patientview.persistence.model.FhirDiagnosticReport>());

        // FhirObservation
        FhirObservation fhirObservation = new FhirObservation();
        fhirObservation.setValue("imaging result");

        // FhirDiagnosticReport
        org.patientview.persistence.model.FhirDiagnosticReport fhirDiagnosticReport
                = new org.patientview.persistence.model.FhirDiagnosticReport();
        fhirDiagnosticReport.setDate(now);
        fhirDiagnosticReport.setName("imaging diagnostic");
        fhirDiagnosticReport.setType(DiagnosticReportTypes.IMAGING.toString());
        fhirDiagnosticReport.setResult(fhirObservation);
        fhirDiagnosticReportRange.getDiagnostics().add(fhirDiagnosticReport);

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);
        when(diagnosticService.deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirDiagnosticReportRange.getStartDate()), eq(fhirDiagnosticReportRange.getEndDate())))
                .thenReturn(1);
        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group)))
                .thenReturn(patient.getFhirLinks().iterator().next());
        when(groupRepository.findByCode(eq(fhirDiagnosticReportRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirDiagnosticReportRange.getIdentifier())))
                .thenReturn(identifiers);
        when(Util.isInEnum(eq(fhirDiagnosticReport.getType()), eq(DiagnosticReportTypes.class))).thenReturn(true);

        ServerResponse serverResponse = apiDiagnosticService.importDiagnostics(fhirDiagnosticReportRange);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertTrue("Should have correct added success message, got '"
                + serverResponse.getSuccessMessage() + "'", serverResponse.getSuccessMessage().contains("added 1"));
        Assert.assertTrue("Should have correct deleted success message, got '"
                + serverResponse.getSuccessMessage() + "'", serverResponse.getSuccessMessage().contains("deleted 1"));

        verify(diagnosticService, times(1)).add(eq(fhirDiagnosticReport), any(FhirLink.class));
        verify(diagnosticService, times(1)).deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirDiagnosticReportRange.getStartDate()), eq(fhirDiagnosticReportRange.getEndDate()));
        verify(fhirLinkService, times(1)).createFhirLink(eq(patient), eq(identifier), eq(group));
    }

    @Test
    public void testImportDiagnostics_addOnly() throws Exception {
        // auth
        Group group = TestUtils.createGroup("testGroup");
        Role staffRole = TestUtils.createRole(RoleName.IMPORTER);
        User staff = TestUtils.createUser("testStaff");
        GroupRole groupRole = TestUtils.createGroupRole(staffRole, group, staff);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staff.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(staff, groupRoles);

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        // identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patient, "1111111111");

        TestUtils.createFhirLink(patient, identifier, group);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // FhirDiagnosticReportRange
        FhirDiagnosticReportRange fhirDiagnosticReportRange = new FhirDiagnosticReportRange();
        fhirDiagnosticReportRange.setGroupCode("DSF01");
        fhirDiagnosticReportRange.setIdentifier("1111111111");
        fhirDiagnosticReportRange.setDiagnostics(
                new ArrayList<org.patientview.persistence.model.FhirDiagnosticReport>());

        // FhirObservation
        FhirObservation fhirObservation = new FhirObservation();
        fhirObservation.setValue("imaging result");

        // FhirDiagnosticReport
        org.patientview.persistence.model.FhirDiagnosticReport fhirDiagnosticReport
                = new org.patientview.persistence.model.FhirDiagnosticReport();
        fhirDiagnosticReport.setDate(now);
        fhirDiagnosticReport.setName("imaging diagnostic");
        fhirDiagnosticReport.setType(DiagnosticReportTypes.IMAGING.toString());
        fhirDiagnosticReport.setResult(fhirObservation);
        fhirDiagnosticReportRange.getDiagnostics().add(fhirDiagnosticReport);

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);
        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group)))
                .thenReturn(patient.getFhirLinks().iterator().next());
        when(groupRepository.findByCode(eq(fhirDiagnosticReportRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirDiagnosticReportRange.getIdentifier())))
                .thenReturn(identifiers);
        when(Util.isInEnum(eq(fhirDiagnosticReport.getType()), eq(DiagnosticReportTypes.class))).thenReturn(true);

        ServerResponse serverResponse = apiDiagnosticService.importDiagnostics(fhirDiagnosticReportRange);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertTrue("Should have correct added success message, got '"
                + serverResponse.getSuccessMessage() + "'", serverResponse.getSuccessMessage().contains("added 1"));
        Assert.assertTrue("Should have correct deleted success message, got '"
                + serverResponse.getSuccessMessage() + "'", !serverResponse.getSuccessMessage().contains("deleted 1"));

        verify(diagnosticService, times(1)).add(eq(fhirDiagnosticReport), any(FhirLink.class));
        verify(diagnosticService, times(0)).deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirDiagnosticReportRange.getStartDate()), eq(fhirDiagnosticReportRange.getEndDate()));
        verify(fhirLinkService, times(1)).createFhirLink(eq(patient), eq(identifier), eq(group));
    }

    @Test
    public void testImportDiagnostics_deleteOnly() throws Exception {
        // auth
        Group group = TestUtils.createGroup("testGroup");
        Role staffRole = TestUtils.createRole(RoleName.IMPORTER);
        User staff = TestUtils.createUser("testStaff");
        GroupRole groupRole = TestUtils.createGroupRole(staffRole, group, staff);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staff.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(staff, groupRoles);

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        // identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patient, "1111111111");

        TestUtils.createFhirLink(patient, identifier, group);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // FhirDiagnosticReportRange
        FhirDiagnosticReportRange fhirDiagnosticReportRange = new FhirDiagnosticReportRange();
        fhirDiagnosticReportRange.setGroupCode("DSF01");
        fhirDiagnosticReportRange.setIdentifier("1111111111");
        fhirDiagnosticReportRange.setStartDate(weekAgo);
        fhirDiagnosticReportRange.setEndDate(now);

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);
        when(diagnosticService.deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirDiagnosticReportRange.getStartDate()), eq(fhirDiagnosticReportRange.getEndDate())))
                .thenReturn(1);
        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group)))
                .thenReturn(patient.getFhirLinks().iterator().next());
        when(groupRepository.findByCode(eq(fhirDiagnosticReportRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirDiagnosticReportRange.getIdentifier())))
                .thenReturn(identifiers);
        when(Util.getFhirLink(eq(group), eq(identifier.getIdentifier()), eq(patient.getFhirLinks())))
                .thenReturn(patient.getFhirLinks().iterator().next());

        ServerResponse serverResponse = apiDiagnosticService.importDiagnostics(fhirDiagnosticReportRange);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertTrue("Should have correct added success message, got '"
                + serverResponse.getSuccessMessage() + "'", !serverResponse.getSuccessMessage().contains("added 1"));
        Assert.assertTrue("Should have correct deleted success message, got '"
                + serverResponse.getSuccessMessage() + "'", serverResponse.getSuccessMessage().contains("deleted 1"));

        verify(diagnosticService, times(0)).add(
                any(org.patientview.persistence.model.FhirDiagnosticReport.class), any(FhirLink.class));
        verify(diagnosticService, times(1)).deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirDiagnosticReportRange.getStartDate()), eq(fhirDiagnosticReportRange.getEndDate()));
        verify(fhirLinkService, times(0)).createFhirLink(eq(patient), eq(identifier), eq(group));
    }


    @Test
    public void testImportDiagnostics_patientFromAnotherGroup_shouldFail() throws Exception {
        // auth
        Group group = TestUtils.createGroup("testGroup");
        Role staffRole = TestUtils.createRole(RoleName.IMPORTER);
        User staff = TestUtils.createUser("testStaff");
        GroupRole groupRole = TestUtils.createGroupRole(staffRole, group, staff);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staff.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(staff, groupRoles);

        // patient
        Group patientGroup = TestUtils.createGroup("PatientGroup");
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, patientGroup, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        // identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patient, "1111111111");

        TestUtils.createFhirLink(patient, identifier, patientGroup);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // FhirDiagnosticReportRange
        FhirDiagnosticReportRange fhirDiagnosticReportRange = new FhirDiagnosticReportRange();
        fhirDiagnosticReportRange.setGroupCode("DSF01");
        fhirDiagnosticReportRange.setIdentifier("1111111111");
        fhirDiagnosticReportRange.setStartDate(weekAgo);
        fhirDiagnosticReportRange.setEndDate(now);
        fhirDiagnosticReportRange.setDiagnostics(new ArrayList<>());

        // FhirObservation
        FhirObservation fhirObservation = new FhirObservation();
        fhirObservation.setValue("imaging result");

        // FhirDiagnosticReport
        org.patientview.persistence.model.FhirDiagnosticReport fhirDiagnosticReport
                = new org.patientview.persistence.model.FhirDiagnosticReport();
        fhirDiagnosticReport.setDate(now);
        fhirDiagnosticReport.setName("imaging diagnostic");
        fhirDiagnosticReport.setType(DiagnosticReportTypes.IMAGING.toString());
        fhirDiagnosticReport.setResult(fhirObservation);
        fhirDiagnosticReportRange.getDiagnostics().add(fhirDiagnosticReport);

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);

        when(diagnosticService.deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirDiagnosticReportRange.getStartDate()), eq(fhirDiagnosticReportRange.getEndDate())))
                .thenReturn(1);
        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(patientGroup)))
                .thenReturn(patient.getFhirLinks().iterator().next());
        when(groupRepository.findByCode(eq(fhirDiagnosticReportRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirDiagnosticReportRange.getIdentifier())))
                .thenReturn(identifiers);
        when(Util.isInEnum(eq(fhirDiagnosticReport.getType()), eq(DiagnosticReportTypes.class))).thenReturn(true);

        ServerResponse serverResponse = apiDiagnosticService.importDiagnostics(fhirDiagnosticReportRange);

        Assert.assertTrue(
                "Should fail, got '" + serverResponse.getErrorMessage() + "'", !serverResponse.isSuccess());
        Assert.assertTrue("Should have correct error message, got '"
                        + serverResponse.getErrorMessage() + "'",
                serverResponse.getErrorMessage().contains("patient not a member of imported group"));

        verify(diagnosticService, times(0)).add(eq(fhirDiagnosticReport), any(FhirLink.class));
        verify(diagnosticService, times(0)).deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirDiagnosticReportRange.getStartDate()), eq(fhirDiagnosticReportRange.getEndDate()));
        verify(fhirLinkService, times(0)).createFhirLink(eq(patient), eq(identifier),
                eq(patientGroup));
    }

    @Test
    public void testImportDiagnostics_patientOutsideImporterGroup_shouldFail() throws Exception {
        // auth
        Group group = TestUtils.createGroup("testGroup");
        Role staffRole = TestUtils.createRole(RoleName.IMPORTER);
        User staff = TestUtils.createUser("testStaff");
        GroupRole groupRole = TestUtils.createGroupRole(staffRole, group, staff);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staff.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(staff, groupRoles);

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        // identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patient, "1111111111");

        TestUtils.createFhirLink(patient, identifier, group);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // FhirDiagnosticReportRange
        FhirDiagnosticReportRange fhirDiagnosticReportRange = new FhirDiagnosticReportRange();
        fhirDiagnosticReportRange.setGroupCode("DSF01");
        fhirDiagnosticReportRange.setIdentifier("1111111111");
        fhirDiagnosticReportRange.setStartDate(weekAgo);
        fhirDiagnosticReportRange.setEndDate(now);
        fhirDiagnosticReportRange.setDiagnostics(new ArrayList<>());

        // FhirObservation
        FhirObservation fhirObservation = new FhirObservation();
        fhirObservation.setValue("imaging result");

        // FhirDiagnosticReport
        org.patientview.persistence.model.FhirDiagnosticReport fhirDiagnosticReport
                = new org.patientview.persistence.model.FhirDiagnosticReport();
        fhirDiagnosticReport.setDate(now);
        fhirDiagnosticReport.setName("imaging diagnostic");
        fhirDiagnosticReport.setType(DiagnosticReportTypes.IMAGING.toString());
        fhirDiagnosticReport.setResult(fhirObservation);
        fhirDiagnosticReportRange.getDiagnostics().add(fhirDiagnosticReport);

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(false);

        when(diagnosticService.deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirDiagnosticReportRange.getStartDate()), eq(fhirDiagnosticReportRange.getEndDate())))
                .thenReturn(1);
        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group)))
                .thenReturn(patient.getFhirLinks().iterator().next());
        when(groupRepository.findByCode(eq(fhirDiagnosticReportRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirDiagnosticReportRange.getIdentifier())))
                .thenReturn(identifiers);
        when(Util.isInEnum(eq(fhirDiagnosticReport.getType()), eq(DiagnosticReportTypes.class))).thenReturn(true);

        ServerResponse serverResponse = apiDiagnosticService.importDiagnostics(fhirDiagnosticReportRange);

        Assert.assertTrue(
                "Should fail, got '" + serverResponse.getErrorMessage() + "'", !serverResponse.isSuccess());
        Assert.assertTrue("Should have correct error message, got '"
                + serverResponse.getErrorMessage() + "'", serverResponse.getErrorMessage().contains("Forbidden"));

        verify(diagnosticService, times(0)).add(eq(fhirDiagnosticReport), any(FhirLink.class));
        verify(diagnosticService, times(0)).deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirDiagnosticReportRange.getStartDate()), eq(fhirDiagnosticReportRange.getEndDate()));
        verify(fhirLinkService, times(0)).createFhirLink(eq(patient), eq(identifier), eq(group));
    }
}
