package org.patientview.test.service;

import com.itextpdf.text.DocumentException;
import generated.Patientview;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.service.AuditService;
import org.patientview.service.GpLetterService;
import org.patientview.service.impl.GpLetterServiceImpl;
import org.patientview.test.util.TestUtils;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class GpLetterServiceTest extends BaseTest {

    @Mock
    AuditService auditService;

    @Mock
    GpLetterRepository gpLetterRepository;

    @Mock
    GpMasterRepository gpMasterRepository;

    @InjectMocks
    GpLetterService gpLetterService = new GpLetterServiceImpl();

    @Mock
    GroupRepository groupRepository;

    @Mock
    GroupRoleRepository groupRoleRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    Properties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testAdd_fullAddress() throws DocumentException {
        Group sourceGroup = TestUtils.createGroup("sourceGroup");

        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpname("gpName");
        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGpaddress3("address3");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        Patientview.Patient patient = new Patientview.Patient();
        Patientview.Patient.Personaldetails personaldetails = new Patientview.Patient.Personaldetails();
        personaldetails.setForename("forename");
        personaldetails.setSurname("surname");
        personaldetails.setNhsno("1111111111");
        patient.setPersonaldetails(personaldetails);
        patientview.setPatient(patient);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpdetails.getGppostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode().replace(" ", "")))).thenReturn(gpMasters);
        when(properties.getProperty(eq("site.url"))).thenReturn("www.patientview.org");
        when(properties.getProperty(eq("gp.letter.output.directory"))).thenReturn("/opt/patientview/gpletter");

        gpLetterService.add(patientview, sourceGroup);

        verify(gpLetterRepository, times(1)).save(any(GpLetter.class));
    }

    @Test
    public void testAdd_incompleteAddress() throws DocumentException {
        Group sourceGroup = TestUtils.createGroup("sourceGroup");

        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpname("gpName");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        Patientview.Patient patient = new Patientview.Patient();
        Patientview.Patient.Personaldetails personaldetails = new Patientview.Patient.Personaldetails();
        personaldetails.setForename("forename");
        personaldetails.setSurname("surname");
        personaldetails.setNhsno("1111111111");
        patient.setPersonaldetails(personaldetails);
        patientview.setPatient(patient);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPracticeName("practiceName");
        gpMaster.setAddress1("address1");
        gpMaster.setAddress2("address2");
        gpMaster.setAddress3("address3");
        gpMaster.setAddress4("address4");
        gpMaster.setPostcode(gpdetails.getGppostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode().replace(" ", "")))).thenReturn(gpMasters);
        when(properties.getProperty(eq("site.url"))).thenReturn("www.patientview.org");

        gpLetterService.add(patientview, sourceGroup);

        verify(gpLetterRepository, times(1)).save(any(GpLetter.class));
    }

    @Test
    public void testCreateGpLetter_noneExistingCheckMaster() throws ResourceNotFoundException {
        Patientview patientview = new Patientview();

        // patient details
        Patientview.Patient patient = new Patientview.Patient();
        Patientview.Patient.Personaldetails personaldetails = new Patientview.Patient.Personaldetails();
        personaldetails.setForename("forename");
        personaldetails.setSurname("surname");
        personaldetails.setNhsno("1111111111");
        patient.setPersonaldetails(personaldetails);
        patientview.setPatient(patient);

        // gp details
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();
        gpdetails.setGpname("gpName");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        // user
        User user = TestUtils.createUser("testUser");
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(
                        TestUtils.createLookupType(LookupTypes.IDENTIFIER), IdentifierTypes.NHS_NUMBER.toString()),
                user, "1111111111");
        Group sourceGroup = TestUtils.createGroup("testGroup");

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier, sourceGroup);
        fhirLink.setIsNew(true);

        // returned from gp master
        GpMaster gpMaster = new GpMaster();
        gpMaster.setPracticeName("practiceName");
        gpMaster.setAddress1("address1");
        gpMaster.setAddress2("address2");
        gpMaster.setAddress3("address3");
        gpMaster.setAddress4("address4");
        gpMaster.setPostcode(gpdetails.getGppostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode().replace(" ", "")))).thenReturn(gpMasters);
        when(properties.getProperty(eq("site.url"))).thenReturn("www.patientview.org");
        when(properties.getProperty(eq("gp.letter.output.directory"))).thenReturn("/opt/patientview/gpletter");

        gpLetterService.createGpLetter(fhirLink, patientview);

        // will not add group role
        verify(groupRoleRepository, times(0)).save(any(GroupRole.class));

        // will add GP letter
        verify(gpLetterRepository, times(1)).save(any(GpLetter.class));
    }

    @Test
    public void testCreateGpLetter_notNewFhirPatient() throws ResourceNotFoundException {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        // gp details
        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGpaddress3("address3");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        // patient details
        Patientview.Patient patient = new Patientview.Patient();
        Patientview.Patient.Personaldetails personaldetails = new Patientview.Patient.Personaldetails();
        personaldetails.setForename("forename");
        personaldetails.setSurname("surname");
        personaldetails.setNhsno("1111111111");
        patient.setPersonaldetails(personaldetails);
        patientview.setPatient(patient);

        // user
        User user = TestUtils.createUser("testUser");
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(
                        TestUtils.createLookupType(LookupTypes.IDENTIFIER), IdentifierTypes.NHS_NUMBER.toString()),
                user, "1111111111");

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);

        gpLetterService.createGpLetter(fhirLink, patientview);

        // will not add group role
        verify(groupRoleRepository, times(0)).save(any(GroupRole.class));

        // will not add GP letter
        verify(gpLetterRepository, times(0)).save(any(GpLetter.class));
    }

    @Test
    public void testCreateGpLetter_existsWithNameNotClaimed() throws ResourceNotFoundException {
        Patientview patientview = new Patientview();

        // patient details
        Patientview.Patient patient = new Patientview.Patient();
        Patientview.Patient.Personaldetails personaldetails = new Patientview.Patient.Personaldetails();
        personaldetails.setForename("forename");
        personaldetails.setSurname("surname");
        personaldetails.setNhsno("1111111111");
        patient.setPersonaldetails(personaldetails);
        patientview.setPatient(patient);

        // gp details
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();
        gpdetails.setGpname("gpName");
        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGpaddress3("address3");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        User user = TestUtils.createUser("testUser");
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(
                        TestUtils.createLookupType(LookupTypes.IDENTIFIER), IdentifierTypes.NHS_NUMBER.toString()),
                user, "1111111111");
        Group sourceGroup = TestUtils.createGroup("testGroup");

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier, sourceGroup);
        fhirLink.setIsNew(true);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName(gpdetails.getGpname());

        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        // returned from gp master
        GpMaster gpMaster = new GpMaster();
        gpMaster.setPracticeName("practiceName");
        gpMaster.setAddress1("address1");
        gpMaster.setAddress2("address2");
        gpMaster.setAddress3("address3");
        gpMaster.setAddress4("address4");
        gpMaster.setPostcode(gpdetails.getGppostcode());
        gpMaster.setPracticeCode("ABC123C");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        // already exists
        when(gpLetterRepository.findByPostcode(eq(gpMaster.getPostcode()))).thenReturn(gpLetters);
        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode().replace(" ", "")))).thenReturn(gpMasters);
        when(properties.getProperty(eq("site.url"))).thenReturn("www.patientview.org");
        when(properties.getProperty(eq("gp.letter.output.directory"))).thenReturn("/opt/patientview/gpletter");

        gpLetterService.createGpLetter(fhirLink, patientview);

        // will not add group role
        verify(groupRoleRepository, times(0)).save(any(GroupRole.class));

        // will not add GP letter
        verify(gpLetterRepository, times(0)).save(any(GpLetter.class));
    }

    @Test
    public void testCreateGpLetter_existsWithNameClaimed() throws ResourceNotFoundException {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpname("gpName");
        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGpaddress3("address3");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        // patient details
        Patientview.Patient patient = new Patientview.Patient();
        Patientview.Patient.Personaldetails personaldetails = new Patientview.Patient.Personaldetails();
        personaldetails.setForename("forename");
        personaldetails.setSurname("surname");
        personaldetails.setNhsno("1111111111");
        patient.setPersonaldetails(personaldetails);
        patientview.setPatient(patient);

        User user = TestUtils.createUser("testUser");
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(
                        TestUtils.createLookupType(LookupTypes.IDENTIFIER), IdentifierTypes.NHS_NUMBER.toString()),
                user, "1111111111");

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);
        fhirLink.setIsNew(true);

        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        Role role = TestUtils.createRole(RoleName.PATIENT);
        List<Role> roles = new ArrayList<>();
        roles.add(role);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName(gpdetails.getGpname());
        gpLetter.setClaimedGroup(group);
        gpLetter.setClaimedDate(new Date());

        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        // returned from gp master
        GpMaster gpMaster = new GpMaster();
        gpMaster.setPracticeName("practiceName");
        gpMaster.setAddress1("address1");
        gpMaster.setAddress2("address2");
        gpMaster.setAddress3("address3");
        gpMaster.setAddress4("address4");
        gpMaster.setPostcode(gpdetails.getGppostcode());
        gpMaster.setPracticeCode("ABC123C");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        User importerUser = TestUtils.createUser("importerUser");

        when(auditService.getImporterUserId()).thenReturn(importerUser.getId());
        when(gpLetterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpLetters);
        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode().replace(" ", "")))).thenReturn(gpMasters);
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(properties.getProperty(eq("site.url"))).thenReturn("www.patientview.org");
        when(properties.getProperty(eq("gp.letter.output.directory"))).thenReturn("/opt/patientview/gpletter");
        when(roleRepository.findByRoleType(eq(RoleType.PATIENT))).thenReturn(roles);
        when(userRepository.getOne(eq(importerUser.getId()))).thenReturn(importerUser);
        when(userRepository.getOne(eq(user.getId()))).thenReturn(user);

        gpLetterService.createGpLetter(fhirLink, patientview);

        // will add group role
        verify(groupRoleRepository, times(1)).save(any(GroupRole.class));

        // will not add new Gp letter
        verify(gpLetterRepository, times(0)).save(any(GpLetter.class));
    }

    @Test
    public void testCreateGpLetter_existsDifferentNameNotClaimed() throws ResourceNotFoundException {
        Patientview patientview = new Patientview();

        // patient details
        Patientview.Patient patient = new Patientview.Patient();
        Patientview.Patient.Personaldetails personaldetails = new Patientview.Patient.Personaldetails();
        personaldetails.setForename("forename");
        personaldetails.setSurname("surname");
        personaldetails.setNhsno("1111111111");
        patient.setPersonaldetails(personaldetails);
        patientview.setPatient(patient);

        // gp details
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();
        gpdetails.setGpname("gpName");
        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGpaddress3("address3");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        User user = TestUtils.createUser("testUser");
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(
                        TestUtils.createLookupType(LookupTypes.IDENTIFIER), IdentifierTypes.NHS_NUMBER.toString()),
                user, "1111111111");
        Group sourceGroup = TestUtils.createGroup("testGroup");

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier, sourceGroup);
        fhirLink.setIsNew(true);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName("anotherGpName");

        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpdetails.getGppostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        when(gpLetterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpLetters);
        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode().replace(" ", "")))).thenReturn(gpMasters);
        when(properties.getProperty(eq("site.url"))).thenReturn("www.patientview.org");
        when(properties.getProperty(eq("gp.letter.output.directory"))).thenReturn("/opt/patientview/gpletter");

        gpLetterService.createGpLetter(fhirLink, patientview);

        // will not add group role
        verify(groupRoleRepository, times(0)).save(any(GroupRole.class));

        // will add new Gp letter
        verify(gpLetterRepository, times(1)).save(any(GpLetter.class));
    }

    @Test
    public void testHasValidPracticeDetails() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpAddress1("address1");
        gpLetter.setGpAddress2("address2");
        gpLetter.setGpAddress3("address3");
        gpLetter.setGpAddress4("address4");
        gpLetter.setGpPostcode("AB1 23C");

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpLetter.getGpPostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpMasters);

        Assert.assertTrue("Should be valid practice details", gpLetterService.hasValidPracticeDetails(gpLetter));
    }

    @Test
    public void testHasValidPracticeDetails_noPostcode() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpAddress1("address1");
        gpLetter.setGpAddress2("address2");
        gpLetter.setGpAddress3("address3");
        gpLetter.setGpAddress4("address4");

        Assert.assertFalse("Should be invalid practice details", gpLetterService.hasValidPracticeDetails(gpLetter));
    }

    @Test
    public void testHasValidPracticeDetails_2FieldsA() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpAddress1("address1");
        gpLetter.setGpAddress2("address2");
        gpLetter.setGpPostcode("AB1 23C");

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpLetter.getGpPostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpMasters);

        Assert.assertTrue("Should be valid practice details", gpLetterService.hasValidPracticeDetails(gpLetter));
    }

    @Test
    public void testHasValidPracticeDetails_2FieldsB() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpAddress1("address1");
        gpLetter.setGpAddress3("address3");
        gpLetter.setGpPostcode("AB1 23C");

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpLetter.getGpPostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpMasters);

        Assert.assertTrue("Should be valid practice details", gpLetterService.hasValidPracticeDetails(gpLetter));
    }

    @Test
    public void testHasValidPracticeDetails_notEnoughFields() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpAddress1("address1");
        gpLetter.setGpPostcode("AB1 23C");

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpLetter.getGpPostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpMasters);

        Assert.assertFalse("Should be invalid practice details", gpLetterService.hasValidPracticeDetails(gpLetter));
    }

    @Test
    public void testHasValidPracticeDetailsCheckMaster() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpAddress1("address1");
        gpLetter.setGpPostcode("AB1 23C");

        GpMaster gp = new GpMaster();
        gp.setPostcode(gpLetter.getGpPostcode());
        List<GpMaster> gps = new ArrayList<>();
        gps.add(gp);

        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gps);

        Assert.assertTrue("Should be valid practice details",
                gpLetterService.hasValidPracticeDetailsSingleMaster(gpLetter));
    }

    @Test
    public void testHasValidPracticeDetailsCheckMaster_tooManyGpMaster() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode("AB1 23C");

        GpMaster gp = new GpMaster();
        gp.setPostcode(gpLetter.getGpPostcode());
        GpMaster gp2 = new GpMaster();
        gp2.setPostcode("ER45 6TY");
        List<GpMaster> gps = new ArrayList<>();
        gps.add(gp);
        gps.add(gp2);

        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gps);

        Assert.assertFalse("Should be invalid practice details",
                gpLetterService.hasValidPracticeDetailsSingleMaster(gpLetter));
    }

    @Test
    public void testMatchByGpDetails_checkDetails() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpAddress1("address1");
        gpLetter.setGpAddress2("address2");
        gpLetter.setGpAddress3("address3");
        gpLetter.setGpAddress4("address4");
        gpLetter.setGpPostcode("AB1 23C");

        GpLetter gpLetterFound = new GpLetter();
        gpLetterFound.setGpPostcode(gpLetter.getGpPostcode());
        gpLetterFound.setGpAddress1(gpLetter.getGpAddress1());
        gpLetterFound.setGpAddress2(gpLetter.getGpAddress2());
        gpLetterFound.setGpAddress3(gpLetter.getGpAddress3());
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpLetters);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpLetter.getGpPostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpMasters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(gpLetter);

        Assert.assertEquals("Should have one match", 1, found.size());
    }

    @Test
    public void testMatchByGpDetails_checkDetailsMultiple1() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpAddress1("address1");
        gpLetter.setGpAddress2("address2");
        gpLetter.setGpAddress3("address3");
        gpLetter.setGpAddress4("address4");
        gpLetter.setGpPostcode("AB1 23C");

        GpLetter gpLetterFound = new GpLetter();
        gpLetterFound.setGpPostcode(gpLetter.getGpPostcode());
        gpLetterFound.setGpAddress1(gpLetter.getGpAddress1());
        gpLetterFound.setGpAddress2(gpLetter.getGpAddress2());
        gpLetterFound.setGpAddress3(gpLetter.getGpAddress3());
        GpLetter gpLetterFound2 = new GpLetter();
        gpLetterFound2.setGpPostcode(gpLetter.getGpPostcode());
        gpLetterFound2.setGpAddress1(gpLetter.getGpAddress1() + "2");
        gpLetterFound2.setGpAddress2(gpLetter.getGpAddress2());
        gpLetterFound2.setGpAddress3(gpLetter.getGpAddress3() + "2");
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetterFound);
        gpLetters.add(gpLetterFound2);

        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpLetters);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpLetter.getGpPostcode());
        gpMaster.setPracticeName("name1");
        gpMaster.setPracticeCode("A1234");
        GpMaster gpMaster2 = new GpMaster();
        gpMaster2.setPostcode(gpLetter.getGpPostcode());
        gpMaster2.setPracticeName("name2");
        gpMaster2.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        gpMasters.add(gpMaster2);
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpMasters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(gpLetter);

        Assert.assertEquals("Should have one match", 1, found.size());
    }

    @Test
    public void testMatchByGpDetails_checkDetailsMultiple2() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpAddress1("address1");
        gpLetter.setGpAddress2("address2");
        gpLetter.setGpAddress3("address3");
        gpLetter.setGpAddress4("address4");
        gpLetter.setGpPostcode("AB1 23C");

        GpLetter gpLetterFound = new GpLetter();
        gpLetterFound.setGpPostcode(gpLetter.getGpPostcode());
        gpLetterFound.setGpAddress1(gpLetter.getGpAddress1());
        gpLetterFound.setGpAddress2(gpLetter.getGpAddress2());
        gpLetterFound.setGpAddress3(gpLetter.getGpAddress3());
        GpLetter gpLetterFound2 = new GpLetter();
        gpLetterFound2.setGpPostcode(gpLetter.getGpPostcode());
        gpLetterFound2.setGpAddress1(gpLetter.getGpAddress1() + "2");
        gpLetterFound2.setGpAddress2(gpLetter.getGpAddress2());
        gpLetterFound2.setGpAddress3(gpLetter.getGpAddress3());
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetterFound);
        gpLetters.add(gpLetterFound2);

        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpLetters);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpLetter.getGpPostcode());
        gpMaster.setPracticeName("name1");
        gpMaster.setPracticeCode("A1234");
        GpMaster gpMaster2 = new GpMaster();
        gpMaster2.setPostcode(gpLetter.getGpPostcode());
        gpMaster2.setPracticeName("name2");
        gpMaster2.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        gpMasters.add(gpMaster2);
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpMasters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(gpLetter);

        Assert.assertEquals("Should have two matches", 2, found.size());
    }

    @Test
    public void testMatchByGpDetails_checkMaster() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName("name");
        gpLetter.setGpPostcode("AB1 23C");

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpLetter.getGpPostcode());
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        GpLetter gpLetterFound = new GpLetter();
        gpLetterFound.setGpPostcode(gpLetter.getGpPostcode());
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetterFound);

        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpMasters);
        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpLetters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(gpLetter);

        Assert.assertEquals("Should have one match", 1, found.size());
    }

    @Test
    public void testMatchByGpDetails_checkMasterMultiple() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode("AB1 23C");

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpLetter.getGpPostcode());
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        GpLetter gpLetterFound = new GpLetter();
        gpLetterFound.setGpPostcode(gpLetter.getGpPostcode());
        GpLetter gpLetterFound2 = new GpLetter();
        gpLetterFound2.setGpPostcode(gpLetter.getGpPostcode());
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetterFound);
        gpLetters.add(gpLetterFound2);

        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpMasters);
        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpLetters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(gpLetter);

        Assert.assertEquals("Should have two matches", 2, found.size());
    }

    @Test
    public void testMatchByGpDetails_checkBoth() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode("AB1 23C");

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpLetter.getGpPostcode());
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        GpLetter gpLetterFound = new GpLetter();
        gpLetterFound.setGpPostcode(gpLetter.getGpPostcode());
        gpLetterFound.setGpAddress1(gpLetter.getGpPostcode());
        gpLetterFound.setGpAddress2(gpLetter.getGpPostcode());
        gpLetterFound.setGpAddress3(gpLetter.getGpPostcode());
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetterFound);

        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpMasters);
        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpLetters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(gpLetter);

        Assert.assertEquals("Should have one match", 1, found.size());
    }

    @Test
    public void testMatchByGpDetails_checkBothMultiple() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode("AB1 23C");

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpLetter.getGpPostcode());
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        GpLetter gpLetterFound = new GpLetter();
        gpLetterFound.setGpPostcode(gpLetter.getGpPostcode());
        gpLetterFound.setGpAddress1(gpLetter.getGpAddress1());
        gpLetterFound.setGpAddress2(gpLetter.getGpAddress2());
        gpLetterFound.setGpAddress3(gpLetter.getGpAddress3());
        GpLetter gpLetterFound2 = new GpLetter();
        gpLetterFound2.setGpPostcode(gpLetter.getGpPostcode());
        gpLetterFound2.setGpAddress1(gpLetter.getGpAddress1() + "2");
        gpLetterFound2.setGpAddress2(gpLetter.getGpAddress2());
        gpLetterFound2.setGpAddress3(gpLetter.getGpAddress3());
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetterFound);
        gpLetters.add(gpLetterFound2);

        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpMasters);
        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode().replace(" ", "")))).thenReturn(gpLetters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(gpLetter);

        Assert.assertEquals("Should have two matches", 2, found.size());
    }
}
