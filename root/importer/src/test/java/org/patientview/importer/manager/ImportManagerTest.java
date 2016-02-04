package org.patientview.importer.manager;

import generated.Patientview;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.BaseTest;
import org.patientview.importer.Utility.Util;
import org.patientview.importer.manager.impl.ImportManagerImpl;
import org.patientview.importer.service.GpLetterService;
import org.patientview.importer.service.GroupRoleService;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.test.util.TestUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class ImportManagerTest extends BaseTest {

    @Mock
    GpLetterService gpLetterService;

    @Mock
    GroupRoleService groupRoleService;

    @Mock
    GpMasterRepository gpMasterRepository;

    @InjectMocks
    ImportManager importManager = new ImportManagerImpl();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        PowerMockito.mockStatic(Util.class);
    }

    @Test
    public void testCreateGpLetter_noneExisting() throws ResourceNotFoundException {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

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

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);
        fhirLink.setIsNew(true);

        when(gpLetterService.hasValidPracticeDetails(eq(patientview))).thenReturn(true);

        importManager.createGpLetter(fhirLink, patientview);

        // will not add group role
        verify(groupRoleService, Mockito.times(0)).add(any(Long.class), any(Long.class), any(RoleType.class));

        // will add GP letter
        verify(gpLetterService, Mockito.times(1)).add(patientview);
    }

    @Test
    public void testCreateGpLetter_notNewFhirPatient() throws ResourceNotFoundException {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

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

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);

        when(gpLetterService.hasValidPracticeDetails(eq(patientview))).thenReturn(true);

        importManager.createGpLetter(fhirLink, patientview);

        // will not add group role
        verify(groupRoleService, Mockito.times(0)).add(any(Long.class), any(Long.class), any(RoleType.class));

        // will not add GP letter
        verify(gpLetterService, Mockito.times(0)).add(patientview);
    }

    @Test
    public void testCreateGpLetter_existsWithNameNotClaimed() throws ResourceNotFoundException {
        Patientview patientview = new Patientview();
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

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);
        fhirLink.setIsNew(true);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName(gpdetails.getGpname());

        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        when(gpLetterService.hasValidPracticeDetails(eq(patientview))).thenReturn(true);
        when(gpLetterService.matchByGpDetails(patientview)).thenReturn(gpLetters);

        importManager.createGpLetter(fhirLink, patientview);

        // will not add group role
        verify(groupRoleService, Mockito.times(0)).add(any(Long.class), any(Long.class), any(RoleType.class));

        // will not add new Gp letter
        verify(gpLetterService, Mockito.times(0)).add(patientview);
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

        User user = TestUtils.createUser("testUser");
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(
                        TestUtils.createLookupType(LookupTypes.IDENTIFIER), IdentifierTypes.NHS_NUMBER.toString()),
                user, "1111111111");

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);
        fhirLink.setIsNew(true);

        Group group = TestUtils.createGroup("testGroup");

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName(gpdetails.getGpname());
        gpLetter.setClaimedGroup(group);
        gpLetter.setClaimedDate(new Date());

        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        when(gpLetterService.hasValidPracticeDetails(eq(patientview))).thenReturn(true);
        when(gpLetterService.matchByGpDetails(patientview)).thenReturn(gpLetters);

        importManager.createGpLetter(fhirLink, patientview);

        // will add group role
        verify(groupRoleService, Mockito.times(1)).add(eq(user.getId()), eq(group.getId()), eq(RoleType.PATIENT));

        // will not add new Gp letter
        verify(gpLetterService, Mockito.times(0)).add(patientview);
    }

    @Test
    public void testCreateGpLetter_existsDifferentNameNotClaimed() throws ResourceNotFoundException {
        Patientview patientview = new Patientview();
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

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);
        fhirLink.setIsNew(true);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName("anotherGpName");

        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        when(gpLetterService.hasValidPracticeDetails(eq(patientview))).thenReturn(true);
        when(gpLetterService.matchByGpDetails(patientview)).thenReturn(gpLetters);

        importManager.createGpLetter(fhirLink, patientview);

        // will not add group role
        verify(groupRoleService, Mockito.times(0)).add(any(Long.class), any(Long.class), any(RoleType.class));

        // will add new Gp letter
        verify(gpLetterService, Mockito.times(1)).add(patientview);
    }
}
