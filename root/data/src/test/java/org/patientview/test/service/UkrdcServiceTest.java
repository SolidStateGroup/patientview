package org.patientview.test.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Hospitalisation;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Immunisation;
import org.patientview.persistence.model.InsDiaryRecord;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.ImmunisationCodelist;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.persistence.repository.SurveySendingFacilityRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.AuditService;
import org.patientview.service.FhirLinkService;
import org.patientview.service.SurveyService;
import org.patientview.service.UkrdcService;
import org.patientview.service.impl.UkrdcServiceImpl;
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

public class UkrdcServiceTest extends BaseTest {

    @Mock
    AuditService auditService;
    @Mock
    FhirLinkService fhirLinkService;
    @Mock
    FhirResource fhirResource;
    @Mock
    FileDataRepository fileDataRepository;
    @Mock
    GroupRepository groupRepository;
    @Mock
    IdentifierRepository identifierRepository;
    @Mock
    SurveyResponseRepository surveyResponseRepository;
    @Mock
    SurveySendingFacilityRepository surveySendingFacilityRepository;

    @Mock
    SurveyService surveyService;

    @Mock
    ObservationHeadingRepository observationHeadingRepository;


    @InjectMocks
    UkrdcService ukrdcService = new UkrdcServiceImpl();

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testBuildInsDiaryXml() throws Exception {
        Group group = TestUtils.createGroup("testGroup");
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());

        ObservationHeading systolicHeading = TestUtils.createObservationHeading(
                UkrdcServiceImpl.SYSTOLIC_BP_CODE, "Systolic Blood Pressure");
        ObservationHeading diaystolicHeading = TestUtils.createObservationHeading(
                UkrdcServiceImpl.DISATOLIC_BP_CODE, "Diaystolic Blood Pressure");
        ObservationHeading weightHeading = TestUtils.createObservationHeading(
                UkrdcServiceImpl.WEIGHT_CODE, "Weight");
        ObservationHeading dipstickHeading = TestUtils.createObservationHeading(
                UkrdcServiceImpl.PROTEIN_DIPSTICK_CODE, "Urine Protein Dipstick");
        ObservationHeading odemaHeading = TestUtils.createObservationHeading(
                UkrdcServiceImpl.ODEMA_CODE, "Odema");

        // patient
        User patient = TestUtils.createUser("patient");
        patient.setDateOfBirth(new Date());
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        Identifier identifier = TestUtils.createIdentifier(lookup, patient, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        when(groupRepository.findGroupByUser(Matchers.eq(patient))).thenReturn(new ArrayList<Group>());
        // return correct observation heading for the code
        when(observationHeadingRepository.findOneByCode(UkrdcServiceImpl.SYSTOLIC_BP_CODE)).thenReturn(systolicHeading);
        when(observationHeadingRepository.findOneByCode(UkrdcServiceImpl.DISATOLIC_BP_CODE))
                .thenReturn(diaystolicHeading);
        when(observationHeadingRepository.findOneByCode(UkrdcServiceImpl.WEIGHT_CODE)).thenReturn(weightHeading);
        when(observationHeadingRepository.findOneByCode(UkrdcServiceImpl.PROTEIN_DIPSTICK_CODE))
                .thenReturn(dipstickHeading);
        when(observationHeadingRepository.findOneByCode(UkrdcServiceImpl.ODEMA_CODE)).thenReturn(odemaHeading);

        List<InsDiaryRecord> insDiaryRecords = Arrays.asList(TestUtils.createRelapseInsDiaryRecord(patient),
                TestUtils.createNoneRelapseInsDiaryRecord(patient));
        List<Hospitalisation> hospitalisations = Arrays.asList(TestUtils.createHospitalisation(patient, "reason one"),
                TestUtils.createHospitalisation(patient, "reason two"),
                TestUtils.createHospitalisation(patient, "some other reason"));
        List<Immunisation> immunisations = Arrays.asList(TestUtils.createImmunisation(patient, ImmunisationCodelist.FLU),
                TestUtils.createImmunisation(patient, ImmunisationCodelist.MMR),
                TestUtils.createImmunisation(patient, ImmunisationCodelist.MEN_ASWY));

        String xml = ukrdcService.buildInsDiaryXml(patient, insDiaryRecords, hospitalisations, immunisations);

        Assert.assertNotNull(xml);
    }

}
