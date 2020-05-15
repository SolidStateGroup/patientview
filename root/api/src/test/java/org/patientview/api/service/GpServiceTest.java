package org.patientview.api.service;

import junit.framework.Assert;
import net.lingala.zip4j.exception.ZipException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.persistence.model.FhirIdentifier;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.GpDetails;
import org.patientview.persistence.model.GpPractice;
import org.patientview.api.service.impl.GpServiceImpl;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.GpPatient;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.ContactPointTypeRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.AuditService;
import org.patientview.service.GpLetterService;
import org.patientview.test.util.TestUtils;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 02/02/2016
 */
public class GpServiceTest {

    User creator;

    @Mock
    AuditService auditService;

    @Mock
    ContactPointTypeRepository contactPointTypeRepository;

    @Mock
    EmailService emailService;

    @Mock
    FeatureRepository featureRepository;

    @Mock
    FhirResource fhirResource;

    @Mock
    GpLetterService gpLetterService;

    @Mock
    GpLetterRepository gpLetterRepository;

    @Mock
    GpMasterRepository gpMasterRepository;

    @Mock
    GroupFeatureRepository groupFeatureRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    GroupRoleRepository groupRoleRepository;

    @Mock
    LookupRepository lookupRepository;

    @Mock
    NhsChoicesService nhsChoicesService;

    @Mock
    Properties properties;

    @Mock
    RoleRepository roleRepository;

    @Mock
    UserFeatureRepository userFeatureRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserService userService;

    @InjectMocks
    GpService gpService = new GpServiceImpl();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    @Ignore("fails on jenkins")
    public void testUpdateMasterTable() throws IOException, ZipException {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        when(properties.getProperty("gp.master.temp.directory")).thenReturn(testFolder.getRoot().getAbsolutePath());
        when(properties.getProperty("gp.master.url.england"))
                .thenReturn("file://" + getClass().getResource("/gp").getPath().concat("/epraccur.zip"));
        when(properties.getProperty("gp.master.filename.england")).thenReturn("epraccur.csv");
        gpService.updateMasterTable();
    }

    @Test
    public void testClaim() throws VerificationException, MessagingException {
        String url = "http://nhswebsite.com/somepractice.aspx";

        GpDetails details = new GpDetails();
        details.setForename("fore");
        details.setSurname("sur");
        details.setSignupKey("ABC123");
        details.setEmail("someone@nhs.uk");
        details.setPatientIdentifier("1234567890");

        // gp letter, only has postcode so will be checking against gp master table when claiming similar
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName("Dr Some GP");
        gpLetter.setGpPostcode("ABC 12DE");
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        // another gp letter, to confirm that all are claimed at once
        GpLetter gpLetter2 = new GpLetter();
        gpLetter2.setGpName("Dr Another GP");
        gpLetter2.setGpPostcode("ABC 12DE");
        gpLetters.add(gpLetter2);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPracticeName("Some Practice");
        gpMaster.setPracticeCode("P123456");
        gpMaster.setPostcode("ABC 12DE");
        gpMaster.setAddress1("address1");
        gpMaster.setAddress2("address2");
        gpMaster.setAddress3("address3");
        gpMaster.setAddress4("address4");
        gpMaster.setTelephone("01234 567890123");
        gpMaster.setUrl(url);
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        User patientUser = TestUtils.createUser("patientUser");

        Role gpAdminRole = TestUtils.createRole(RoleName.GP_ADMIN, RoleType.STAFF);
        Role patientRole = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);

        Group otherGroup = TestUtils.createGroup("someOtherGroup");
        GroupRole otherGroupRole = TestUtils.createGroupRole(patientRole, otherGroup, patientUser);
        patientUser.getGroupRoles().add(otherGroupRole);

        GpPatient patient = new GpPatient();
        patient.setId(1L);
        patient.setGpName(gpMaster.getPracticeName());
        patient.setIdentifiers(new HashSet<Identifier>());
        patient.getIdentifiers().add(TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patientUser, "1111111111"));
        List<GpPatient> patients = new ArrayList<>();
        patients.add(patient);

        // selected practice
        GpPractice gpPractice = new GpPractice();
        gpPractice.setUrl(url);
        gpPractice.setName(gpMaster.getPracticeName());
        gpPractice.setCode(gpMaster.getPracticeCode());
        details.getPractices().add(gpPractice);

        // selected patients
        details.getPatients().add(patient);

        // contact point types
        ContactPointType pvAdminEmail = new ContactPointType();
        pvAdminEmail.setValue(ContactPointTypes.PV_ADMIN_EMAIL);
        ContactPointType unitEnquriesTelephone = new ContactPointType();
        unitEnquriesTelephone.setValue(ContactPointTypes.UNIT_ENQUIRIES_PHONE);

        List<ContactPointType> types1 = new ArrayList<>();
        types1.add(pvAdminEmail);
        List<ContactPointType> types2 = new ArrayList<>();
        types2.add(unitEnquriesTelephone);

        Lookup generalPracticeLookup = TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.GENERAL_PRACTICE.toString());
        Lookup specialtyLookup = TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.SPECIALTY.toString());

        // GENERAL_PRACTICE specialty
        Group generalPracticeSpecialty = new Group();
        generalPracticeSpecialty.setCode(HiddenGroupCodes.GENERAL_PRACTICE.toString());
        generalPracticeSpecialty.setGroupType(specialtyLookup);

        // MESSAGING feature
        Feature messagingFeature = TestUtils.createFeature(FeatureType.MESSAGING.toString());

        // DEFAULT_MESSAGING_CONTACT feature
        Feature defaultMessagingContactFeature
                = TestUtils.createFeature(FeatureType.DEFAULT_MESSAGING_CONTACT.toString());

        when(contactPointTypeRepository.findByValue(eq(ContactPointTypes.PV_ADMIN_EMAIL))).thenReturn(types1);
        when(contactPointTypeRepository.findByValue(eq(ContactPointTypes.UNIT_ENQUIRIES_PHONE))).thenReturn(types2);
        when(featureRepository.findByName(eq(FeatureType.MESSAGING.toString()))).thenReturn(messagingFeature);
        when(featureRepository.findByName(eq(FeatureType.DEFAULT_MESSAGING_CONTACT.toString())))
                .thenReturn(defaultMessagingContactFeature);
        when(gpLetterRepository.findBySignupKeyAndIdentifier(
                eq(details.getSignupKey()), eq(details.getPatientIdentifier()))).thenReturn(gpLetters);
        when(gpMasterRepository.findByPracticeCode(eq(details.getPractices().get(0).getCode()))).thenReturn(gpMasters);
        when(lookupRepository.findByTypeAndValue(eq(LookupTypes.GROUP), eq(GroupTypes.GENERAL_PRACTICE.toString())))
                .thenReturn(generalPracticeLookup);
        when(groupRepository.findByCode(eq(generalPracticeSpecialty.getCode()))).thenReturn(generalPracticeSpecialty);
        when(roleRepository.findByRoleTypeAndName(
                eq(gpAdminRole.getRoleType().getValue()), eq(gpAdminRole.getName()))).thenReturn(gpAdminRole);
        when(roleRepository.findByRoleTypeAndName(
                eq(patientRole.getRoleType().getValue()), eq(patientRole.getName()))).thenReturn(patientRole);
        when(userRepository.findOne(eq(patient.getId()))).thenReturn(patientUser);

        when(gpLetterService.matchByGpDetails(any(GpLetter.class))).thenReturn(gpLetters);

        GpDetails out = gpService.claim(details);

        Assert.assertNotNull("should set username", out.getUsername());

        verify(auditService, Mockito.times(6)).save(any(Audit.class));
        verify(emailService, Mockito.times(1)).sendEmail(any(Email.class));
        verify(gpLetterRepository, Mockito.times(1)).save(any(List.class));
        verify(groupFeatureRepository, Mockito.times(1)).save(any(Set.class));
        verify(groupRepository, Mockito.times(1)).save(any(Group.class));
        verify(groupRoleRepository, Mockito.times(2)).save(any(Set.class));
        verify(userFeatureRepository, Mockito.times(1)).save(any(Set.class));
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test (expected = VerificationException.class)
    public void testClaim_failsGroupAlreadyCreated() throws VerificationException, MessagingException {
        String url = "http://nhswebsite.com/somepractice.aspx";

        GpDetails details = new GpDetails();
        details.setForename("fore");
        details.setSurname("sur");
        details.setSignupKey("ABC123");
        details.setEmail("someone@nhs.uk");
        details.setPatientIdentifier("1234567890");

        // gp letter, only has postcode so will be checking against gp master table when claiming similar
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName("Dr Some GP");
        gpLetter.setGpPostcode("ABC 12DE");
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPracticeName("Some Practice");
        gpMaster.setPracticeCode("P123456");
        gpMaster.setPostcode("ABC 12DE");
        gpMaster.setAddress1("address1");
        gpMaster.setAddress2("address2");
        gpMaster.setAddress3("address3");
        gpMaster.setAddress4("address4");
        gpMaster.setTelephone("01234 567890123");
        gpMaster.setUrl(url);
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        User patientUser = TestUtils.createUser("patientUser");

        GpPatient patient = new GpPatient();
        patient.setId(1L);
        patient.setGpName(gpMaster.getPracticeName());
        patient.setIdentifiers(new HashSet<Identifier>());
        patient.getIdentifiers().add(TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patientUser, "1111111111"));
        List<GpPatient> patients = new ArrayList<>();
        patients.add(patient);

        // selected patients
        details.getPatients().add(patient);

        // selected practice
        GpPractice gpPractice = new GpPractice();
        gpPractice.setUrl(url);
        gpPractice.setName(gpMaster.getPracticeName());
        gpPractice.setCode(gpMaster.getPracticeCode());
        details.getPractices().add(gpPractice);

        // GENERAL_PRACTICE specialty
        Lookup specialtyLookup = TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.SPECIALTY.toString());

        Group generalPracticeSpecialty = new Group();
        generalPracticeSpecialty.setCode(HiddenGroupCodes.GENERAL_PRACTICE.toString());
        generalPracticeSpecialty.setGroupType(specialtyLookup);

        // already created group of type GENERAL_PRACTICE
        Lookup unitLookup = TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.GENERAL_PRACTICE.toString());

        Group unit = new Group();
        unit.setCode(gpMaster.getPracticeCode());
        unit.setGroupType(unitLookup);

        // Roles
        Role gpAdminRole = TestUtils.createRole(RoleName.GP_ADMIN, RoleType.STAFF);
        Role patientRole = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);

        // MESSAGING feature
        Feature messagingFeature = TestUtils.createFeature(FeatureType.MESSAGING.toString());

        // DEFAULT_MESSAGING_CONTACT feature
        Feature defaultMessagingContactFeature
                = TestUtils.createFeature(FeatureType.DEFAULT_MESSAGING_CONTACT.toString());

        when(featureRepository.findByName(eq(FeatureType.DEFAULT_MESSAGING_CONTACT.toString())))
                .thenReturn(defaultMessagingContactFeature);
        when(featureRepository.findByName(eq(FeatureType.MESSAGING.toString()))).thenReturn(messagingFeature);
        when(groupRepository.findByCode(eq(generalPracticeSpecialty.getCode()))).thenReturn(generalPracticeSpecialty);
        when(groupRepository.findByCode(eq(gpMaster.getPracticeCode()))).thenReturn(unit);
        when(gpMasterRepository.findByPracticeCode(eq(details.getPractices().get(0).getCode()))).thenReturn(gpMasters);
        when(gpLetterRepository.findBySignupKeyAndIdentifier(
                eq(details.getSignupKey()), eq(details.getPatientIdentifier()))).thenReturn(gpLetters);
        when(roleRepository.findByRoleTypeAndName(
                eq(gpAdminRole.getRoleType().getValue()), eq(gpAdminRole.getName()))).thenReturn(gpAdminRole);
        when(roleRepository.findByRoleTypeAndName(
                eq(patientRole.getRoleType().getValue()), eq(patientRole.getName()))).thenReturn(patientRole);

        gpService.claim(details);
    }

    @Test (expected = VerificationException.class)
    public void testClaim_failsNoPatientsSelected() throws VerificationException, MessagingException {
        String url = "http://nhswebsite.com/somepractice.aspx";

        GpDetails details = new GpDetails();
        details.setForename("fore");
        details.setSurname("sur");
        details.setSignupKey("ABC123");
        details.setEmail("someone@nhs.uk");
        details.setPatientIdentifier("1234567890");

        // gp letter, only has postcode so will be checking against gp master table when claiming similar
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName("Dr Some GP");
        gpLetter.setGpPostcode("ABC 12DE");
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPracticeName("Some Practice");
        gpMaster.setPracticeCode("P123456");
        gpMaster.setPostcode("ABC 12DE");
        gpMaster.setAddress1("address1");
        gpMaster.setAddress2("address2");
        gpMaster.setAddress3("address3");
        gpMaster.setAddress4("address4");
        gpMaster.setTelephone("01234 567890123");
        gpMaster.setUrl(url);
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        // selected practice
        GpPractice gpPractice = new GpPractice();
        gpPractice.setUrl(url);
        gpPractice.setName(gpMaster.getPracticeName());
        gpPractice.setCode(gpMaster.getPracticeCode());
        details.getPractices().add(gpPractice);

        when(gpMasterRepository.findByPracticeCode(eq(details.getPractices().get(0).getCode()))).thenReturn(gpMasters);
        when(gpLetterRepository.findBySignupKeyAndIdentifier(
                eq(details.getSignupKey()), eq(details.getPatientIdentifier()))).thenReturn(gpLetters);

        gpService.claim(details);
    }

    @Test (expected = VerificationException.class)
    public void testClaim_failsPracticeNotInGpMaster() throws VerificationException, MessagingException {
        String url = "http://nhswebsite.com/somepractice.aspx";

        GpDetails details = new GpDetails();
        details.setForename("fore");
        details.setSurname("sur");
        details.setSignupKey("ABC123");
        details.setEmail("someone@nhs.uk");
        details.setPatientIdentifier("1234567890");

        // gp letter, only has postcode so will be checking against gp master table when claiming similar
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName("Dr Some GP");
        gpLetter.setGpPostcode("ABC 12DE");
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPracticeName("Some Practice");
        gpMaster.setPracticeCode("P123456");
        gpMaster.setPostcode("ABC 12DE");
        gpMaster.setAddress1("address1");
        gpMaster.setAddress2("address2");
        gpMaster.setAddress3("address3");
        gpMaster.setAddress4("address4");
        gpMaster.setTelephone("01234 567890123");
        gpMaster.setUrl(url);

        // selected practice
        GpPractice gpPractice = new GpPractice();
        gpPractice.setUrl(url);
        gpPractice.setName(gpMaster.getPracticeName());
        gpPractice.setCode(gpMaster.getPracticeCode());
        details.getPractices().add(gpPractice);

        when(gpLetterRepository.findBySignupKeyAndIdentifier(
                eq(details.getSignupKey()), eq(details.getPatientIdentifier()))).thenReturn(gpLetters);

        gpService.claim(details);
    }

    @Test (expected = VerificationException.class)
    public void testClaim_failsNoPracticeSelected() throws VerificationException, MessagingException {
        String url = "http://nhswebsite.com/somepractice.aspx";

        GpDetails details = new GpDetails();
        details.setForename("fore");
        details.setSurname("sur");
        details.setSignupKey("ABC123");
        details.setEmail("someone@nhs.uk");
        details.setPatientIdentifier("1234567890");

        // gp letter, only has postcode so will be checking against gp master table when claiming similar
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName("Dr Some GP");
        gpLetter.setGpPostcode("ABC 12DE");
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        // another gp letter, to confirm that all are claimed at once
        GpLetter gpLetter2 = new GpLetter();
        gpLetter2.setGpName("Dr Another GP");
        gpLetter2.setGpPostcode("ABC 12DE");
        gpLetters.add(gpLetter2);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPracticeName("Some Practice");
        gpMaster.setPracticeCode("P123456");
        gpMaster.setPostcode("ABC 12DE");
        gpMaster.setAddress1("address1");
        gpMaster.setAddress2("address2");
        gpMaster.setAddress3("address3");
        gpMaster.setAddress4("address4");
        gpMaster.setTelephone("01234 567890123");
        gpMaster.setUrl(url);

        User patientUser = TestUtils.createUser("patientUser");

        GpPatient patient = new GpPatient();
        patient.setId(1L);
        patient.setGpName(gpMaster.getPracticeName());
        patient.setIdentifiers(new HashSet<Identifier>());
        patient.getIdentifiers().add(TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patientUser, "1111111111"));

        // selected patients
        details.getPatients().add(patient);

        when(gpLetterRepository.findBySignupKeyAndIdentifier(
                eq(details.getSignupKey()), eq(details.getPatientIdentifier()))).thenReturn(gpLetters);

        gpService.claim(details);
    }

    @Test
    public void testClaim_fullGpLetterDetails() throws VerificationException, MessagingException {
        String url = "http://nhswebsite.com/somepractice.aspx";

        GpDetails details = new GpDetails();
        details.setForename("fore");
        details.setSurname("sur");
        details.setSignupKey("ABC123");
        details.setEmail("someone@nhs.uk");
        details.setPatientIdentifier("1234567890");

        // gp letter, has address details so will match against others in Gp letter table when claiming similar
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName("Dr Some GP");
        gpLetter.setGpAddress1("address1");
        gpLetter.setGpAddress2("address2");
        gpLetter.setGpAddress3("address3");
        gpLetter.setGpPostcode("ABC 12DE");
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPracticeName("Some Practice");
        gpMaster.setPracticeCode("P123456");
        gpMaster.setPostcode("ABC 12DE");
        gpMaster.setAddress1("address1");
        gpMaster.setAddress2("address2");
        gpMaster.setAddress3("address3");
        gpMaster.setAddress4("address4");
        gpMaster.setTelephone("01234 567890123");
        gpMaster.setUrl(url);
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        User patientUser = TestUtils.createUser("patientUser");

        Role gpAdminRole = TestUtils.createRole(RoleName.GP_ADMIN, RoleType.STAFF);
        Role patientRole = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);

        Group otherGroup = TestUtils.createGroup("someOtherGroup");
        GroupRole otherGroupRole = TestUtils.createGroupRole(patientRole, otherGroup, patientUser);
        patientUser.getGroupRoles().add(otherGroupRole);

        GpPatient patient = new GpPatient();
        patient.setId(1L);
        patient.setGpName(gpMaster.getPracticeName());
        patient.setIdentifiers(new HashSet<Identifier>());
        patient.getIdentifiers().add(TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patientUser, "1111111111"));
        List<GpPatient> patients = new ArrayList<>();
        patients.add(patient);

        // selected practice
        GpPractice gpPractice = new GpPractice();
        gpPractice.setUrl(url);
        gpPractice.setName(gpMaster.getPracticeName());
        gpPractice.setCode(gpMaster.getPracticeCode());
        details.getPractices().add(gpPractice);

        // selected patients
        details.getPatients().add(patient);

        // contact point types
        ContactPointType pvAdminEmail = new ContactPointType();
        pvAdminEmail.setValue(ContactPointTypes.PV_ADMIN_EMAIL);
        ContactPointType unitEnquriesTelephone = new ContactPointType();
        unitEnquriesTelephone.setValue(ContactPointTypes.UNIT_ENQUIRIES_PHONE);

        List<ContactPointType> types1 = new ArrayList<>();
        types1.add(pvAdminEmail);
        List<ContactPointType> types2 = new ArrayList<>();
        types2.add(unitEnquriesTelephone);

        Lookup generalPracticeLookup = TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.GENERAL_PRACTICE.toString());
        Lookup specialtyLookup = TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.SPECIALTY.toString());

        // GENERAL_PRACTICE specialty
        Group generalPracticeSpecialty = new Group();
        generalPracticeSpecialty.setCode(HiddenGroupCodes.GENERAL_PRACTICE.toString());
        generalPracticeSpecialty.setGroupType(specialtyLookup);

        // MESSAGING feature
        Feature messagingFeature = TestUtils.createFeature(FeatureType.MESSAGING.toString());

        // DEFAULT_MESSAGING_CONTACT feature
        Feature defaultMessagingContactFeature
                = TestUtils.createFeature(FeatureType.DEFAULT_MESSAGING_CONTACT.toString());

        when(contactPointTypeRepository.findByValue(eq(ContactPointTypes.PV_ADMIN_EMAIL))).thenReturn(types1);
        when(contactPointTypeRepository.findByValue(eq(ContactPointTypes.UNIT_ENQUIRIES_PHONE))).thenReturn(types2);
        when(featureRepository.findByName(eq(FeatureType.MESSAGING.toString()))).thenReturn(messagingFeature);
        when(featureRepository.findByName(eq(FeatureType.DEFAULT_MESSAGING_CONTACT.toString())))
                .thenReturn(defaultMessagingContactFeature);
        when(gpLetterRepository.findBySignupKeyAndIdentifier(
                eq(details.getSignupKey()), eq(details.getPatientIdentifier()))).thenReturn(gpLetters);
        when(gpMasterRepository.findByPracticeCode(eq(details.getPractices().get(0).getCode()))).thenReturn(gpMasters);
        when(lookupRepository.findByTypeAndValue(eq(LookupTypes.GROUP), eq(GroupTypes.GENERAL_PRACTICE.toString())))
                .thenReturn(generalPracticeLookup);
        when(groupRepository.findByCode(eq(generalPracticeSpecialty.getCode()))).thenReturn(generalPracticeSpecialty);
        when(roleRepository.findByRoleTypeAndName(
                eq(gpAdminRole.getRoleType().getValue()), eq(gpAdminRole.getName()))).thenReturn(gpAdminRole);
        when(roleRepository.findByRoleTypeAndName(
                eq(patientRole.getRoleType().getValue()), eq(patientRole.getName()))).thenReturn(patientRole);
        when(userRepository.findOne(eq(patient.getId()))).thenReturn(patientUser);

        when(gpLetterService.matchByGpDetails(any(GpLetter.class))).thenReturn(gpLetters);

        GpDetails out = gpService.claim(details);

        Assert.assertNotNull("should set username", out.getUsername());

        verify(auditService, Mockito.times(6)).save(any(Audit.class));
        verify(emailService, Mockito.times(1)).sendEmail(any(Email.class));
        verify(gpLetterRepository, Mockito.times(1)).save(any(List.class));
        verify(groupFeatureRepository, Mockito.times(1)).save(any(Set.class));
        verify(groupRepository, Mockito.times(1)).save(any(Group.class));
        verify(groupRoleRepository, Mockito.times(2)).save(any(Set.class));
        verify(userFeatureRepository, Mockito.times(1)).save(any(Set.class));
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    public void testInvite() throws VerificationException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        FhirPractitioner practitioner = new FhirPractitioner();
        practitioner.setName("Practitioner Name");
        practitioner.setPostcode("AB1 23C");

        FhirIdentifier fhirIdentifier = new FhirIdentifier(IdentifierTypes.NHS_NUMBER.toString(), "1111111111");

        FhirPatient patient = new FhirPatient();
        patient.getPractitioners().add(practitioner);
        patient.getIdentifiers().add(fhirIdentifier);
        patient.setGroup(group);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPracticeName(practitioner.getName());
        gpMaster.setPostcode(practitioner.getPostcode());
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        when(gpMasterRepository.findByPostcode(eq(practitioner.getPostcode()))).thenReturn(gpMasters);
        when(gpLetterService.hasValidPracticeDetails(any(GpLetter.class))).thenReturn(true);
        when(groupRepository.findOne(eq(patient.getGroup().getId()))).thenReturn(patient.getGroup());

        gpService.invite(user.getId(), patient);

        verify(gpLetterService, Mockito.times(1)).add(any(GpLetter.class), eq(group));
    }

    @Test
    public void testValidateDetails() throws VerificationException {
        GpDetails details = new GpDetails();
        details.setForename("fore");
        details.setSurname("sur");
        details.setSignupKey("ABC123");
        details.setEmail("someone@nhs.uk");
        details.setPatientIdentifier("1234567890");

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode("ABC 12DE");
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPracticeName("Some Practice");
        gpMaster.setPracticeCode("P123456");
        gpMaster.setPostcode("ABC 12DE");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        User user = TestUtils.createUser("patientUser");

        GpPatient patient = new GpPatient();
        patient.setId(1L);
        patient.setGpName(gpMaster.getPracticeName());
        patient.setIdentifiers(new HashSet<Identifier>());
        patient.getIdentifiers().add(TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), user, "1111111111"));
        List<GpPatient> patients = new ArrayList<>();
        patients.add(patient);

        String url = "http://nhswebsite.com/somepractice.aspx";
        Map<String, String> gpMasterDetails = new HashMap<>();
        gpMasterDetails.put("url", url);

        when(gpLetterRepository.findBySignupKeyAndIdentifier(
                eq(details.getSignupKey()), eq(details.getPatientIdentifier()))).thenReturn(gpLetters);
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpMasters);
        when(nhsChoicesService.getDetailsByPracticeCode(eq(gpMaster.getPracticeCode()))).thenReturn(gpMasterDetails);
        when(fhirResource.getGpPatientsFromPostcode(eq(gpMaster.getPostcode()))).thenReturn(patients);

        GpDetails out = gpService.validateDetails(details);

        Assert.assertEquals("should return one practice", 1, out.getPractices().size());
        Assert.assertEquals("should return correct practice name",
                gpMaster.getPracticeName(), out.getPractices().get(0).getName());
        Assert.assertEquals("should return correct practice url", url, out.getPractices().get(0).getUrl());
    }

    @Test (expected = VerificationException.class)
    public void testValidateDetails_alreadyClaimed() throws VerificationException {
        GpDetails details = new GpDetails();
        details.setForename("fore");
        details.setSurname("sur");
        details.setSignupKey("ABC123");
        details.setEmail("someone@nhs.uk");
        details.setPatientIdentifier("1234567890");

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode("ABC 12DE");
        gpLetter.setClaimedDate(new Date());
        gpLetter.setClaimedEmail("someoneelse@nhs.uk");
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        when(properties.getProperty("central.support.contact.email")).thenReturn("centralsupport@nhs.uk");
        when(gpLetterRepository.findBySignupKeyAndIdentifier(
                eq(details.getSignupKey()), eq(details.getPatientIdentifier()))).thenReturn(gpLetters);

        gpService.validateDetails(details);
    }
}
