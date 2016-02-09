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
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.GpDetails;
import org.patientview.api.model.GpPractice;
import org.patientview.api.service.impl.GpServiceImpl;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.GpPatient;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.test.util.TestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 02/02/2016
 */
public class GpServiceTest {

    User creator;

    @Mock
    FhirResource fhirResource;

    @Mock
    GpLetterRepository gpLetterRepository;

    @Mock
    GpMasterRepository gpMasterRepository;

    @Mock
    NhsChoicesService nhsChoicesService;

    @Mock
    Properties properties;

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
    public void testClaim() throws VerificationException {
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

        when(gpLetterRepository.findBySignupKeyAndIdentifier(
                eq(details.getSignupKey()), eq(details.getPatientIdentifier()))).thenReturn(gpLetters);
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);
        when(nhsChoicesService.getUrlByPracticeCode(eq(gpMaster.getPracticeCode()))).thenReturn(url);
        when(fhirResource.getGpPatientsFromPostcode(eq(gpMaster.getPostcode()))).thenReturn(patients);

        // selected practice and patients
        GpPractice gpPractice = new GpPractice();
        gpPractice.setUrl(url);
        gpPractice.setName(gpMaster.getPracticeName());
        gpPractice.setCode(gpMaster.getPracticeCode());
        details.getPractices().add(gpPractice);

        details.getPatients().add(patient);

        GpDetails out = gpService.claim(details);

        Assert.assertEquals("should return one practice", 1, out.getPractices().size());
        Assert.assertEquals("should return correct practice name",
                gpMaster.getPracticeName(), out.getPractices().get(0).getName());
        Assert.assertEquals("should return correct practice url", url, out.getPractices().get(0).getUrl());
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

        when(gpLetterRepository.findBySignupKeyAndIdentifier(
                eq(details.getSignupKey()), eq(details.getPatientIdentifier()))).thenReturn(gpLetters);
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);
        when(nhsChoicesService.getUrlByPracticeCode(eq(gpMaster.getPracticeCode()))).thenReturn(url);
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
