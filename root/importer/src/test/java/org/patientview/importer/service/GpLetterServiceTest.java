package org.patientview.importer.service;

import com.itextpdf.text.DocumentException;
import generated.Patientview;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.patientview.importer.BaseTest;
import org.patientview.importer.Utility.Util;
import org.patientview.importer.service.impl.GpLetterServiceImpl;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.service.GpLetterCreationService;
import org.patientview.test.util.TestUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class GpLetterServiceTest extends BaseTest {

    @Mock
    GpLetterRepository gpLetterRepository;

    @Mock
    GpMasterRepository gpMasterRepository;

    @InjectMocks
    GpLetterService gpLetterService = new GpLetterServiceImpl();

    @Mock
    GpLetterCreationService gpLetterCreationService;

    @Mock
    Properties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        PowerMockito.mockStatic(Util.class);
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

        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpMasters);
        when(properties.getProperty(eq("site.url"))).thenReturn("www.patientview.org");
        when(properties.getProperty(eq("gp.letter.output.directory"))).thenReturn("/opt/patientview/gpletter");

        gpLetterService.add(patientview, sourceGroup);

        verify(gpMasterRepository, Mockito.times(1)).findByPostcode(eq(gpdetails.getGppostcode()));
        verify(gpLetterRepository, Mockito.times(1)).save(any(GpLetter.class));
        verify(gpLetterCreationService, Mockito.times(1)).generateLetter(
                any(GpLetter.class), isNull(GpMaster.class), any(String.class), any(String.class));
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

        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpMasters);
        when(properties.getProperty(eq("site.url"))).thenReturn("www.patientview.org");

        gpLetterService.add(patientview, sourceGroup);

        verify(gpMasterRepository, Mockito.times(2)).findByPostcode(eq(gpdetails.getGppostcode()));
        verify(gpLetterRepository, Mockito.times(1)).save(any(GpLetter.class));
        verify(gpLetterCreationService, Mockito.times(1)).generateLetter(
                any(GpLetter.class), eq(gpMaster), any(String.class), any(String.class));
    }

    @Test
    public void testHasValidPracticeDetails() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGpaddress3("address3");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpdetails.getGppostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpMasters);

        Assert.assertTrue("Should be valid practice details", gpLetterService.hasValidPracticeDetails(patientview));
    }

    @Test
    public void testHasValidPracticeDetails_noPostcode() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGpaddress3("address3");
        patientview.setGpdetails(gpdetails);

        Assert.assertFalse("Should be invalid practice details", gpLetterService.hasValidPracticeDetails(patientview));
    }

    @Test
    public void testHasValidPracticeDetails_2FieldsA() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpdetails.getGppostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpMasters);

        Assert.assertTrue("Should be valid practice details", gpLetterService.hasValidPracticeDetails(patientview));
    }

    @Test
    public void testHasValidPracticeDetails_2FieldsB() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress3("address3");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpdetails.getGppostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpMasters);

        Assert.assertTrue("Should be valid practice details", gpLetterService.hasValidPracticeDetails(patientview));
    }

    @Test
    public void testHasValidPracticeDetails_notEnoughFields() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpaddress1("address1");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        Assert.assertFalse("Should be invalid practice details", gpLetterService.hasValidPracticeDetails(patientview));
    }

    @Test
    public void testHasValidPracticeDetailsCheckMaster() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        GpMaster gp = new GpMaster();
        gp.setPostcode(gpdetails.getGppostcode());
        List<GpMaster> gps = new ArrayList<>();
        gps.add(gp);

        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gps);

        Assert.assertTrue("Should be valid practice details",
                gpLetterService.hasValidPracticeDetailsSingleMaster(patientview));
    }

    @Test
    public void testHasValidPracticeDetailsCheckMaster_tooManyGpMaster() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        GpMaster gp = new GpMaster();
        gp.setPostcode(gpdetails.getGppostcode());
        GpMaster gp2 = new GpMaster();
        gp2.setPostcode("ER45 6TY");
        List<GpMaster> gps = new ArrayList<>();
        gps.add(gp);
        gps.add(gp2);

        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gps);

        Assert.assertFalse("Should be invalid practice details",
                gpLetterService.hasValidPracticeDetailsSingleMaster(patientview));
    }

    @Test
    public void testMatchByGpDetails_checkDetails() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGpaddress3("address3");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode(gpdetails.getGppostcode());
        gpLetter.setGpAddress1(gpdetails.getGpaddress1());
        gpLetter.setGpAddress2(gpdetails.getGpaddress2());
        gpLetter.setGpAddress3(gpdetails.getGpaddress3());
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        when(gpLetterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpLetters);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpdetails.getGppostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpMasters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(patientview);

        Assert.assertEquals("Should have one match", 1, found.size());
    }

    @Test
    public void testMatchByGpDetails_checkDetailsMultiple1() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGpaddress3("address3");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode(gpdetails.getGppostcode());
        gpLetter.setGpAddress1(gpdetails.getGpaddress1());
        gpLetter.setGpAddress2(gpdetails.getGpaddress2());
        gpLetter.setGpAddress3(gpdetails.getGpaddress3());
        GpLetter gpLetter2 = new GpLetter();
        gpLetter2.setGpPostcode(gpdetails.getGppostcode());
        gpLetter2.setGpAddress1(gpdetails.getGpaddress1() + "2");
        gpLetter2.setGpAddress2(gpdetails.getGpaddress2());
        gpLetter2.setGpAddress3(gpdetails.getGpaddress3() + "2");
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);
        gpLetters.add(gpLetter2);

        when(gpLetterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpLetters);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpdetails.getGppostcode());
        gpMaster.setPracticeName("name1");
        gpMaster.setPracticeCode("A1234");
        GpMaster gpMaster2 = new GpMaster();
        gpMaster2.setPostcode(gpdetails.getGppostcode());
        gpMaster2.setPracticeName("name2");
        gpMaster2.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        gpMasters.add(gpMaster2);
        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpMasters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(patientview);

        Assert.assertEquals("Should have one match", 1, found.size());
    }

    @Test
    public void testMatchByGpDetails_checkDetailsMultiple2() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGpaddress3("address3");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode(gpdetails.getGppostcode());
        gpLetter.setGpAddress1(gpdetails.getGpaddress1());
        gpLetter.setGpAddress2(gpdetails.getGpaddress2());
        gpLetter.setGpAddress3(gpdetails.getGpaddress3());
        GpLetter gpLetter2 = new GpLetter();
        gpLetter2.setGpPostcode(gpdetails.getGppostcode());
        gpLetter2.setGpAddress1(gpdetails.getGpaddress1() + "2");
        gpLetter2.setGpAddress2(gpdetails.getGpaddress2());
        gpLetter2.setGpAddress3(gpdetails.getGpaddress3());
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);
        gpLetters.add(gpLetter2);

        when(gpLetterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpLetters);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpdetails.getGppostcode());
        gpMaster.setPracticeName("name1");
        gpMaster.setPracticeCode("A1234");
        GpMaster gpMaster2 = new GpMaster();
        gpMaster2.setPostcode(gpdetails.getGppostcode());
        gpMaster2.setPracticeName("name2");
        gpMaster2.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        gpMasters.add(gpMaster2);
        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpMasters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(patientview);

        Assert.assertEquals("Should have two matches", 2, found.size());
    }

    @Test
    public void testMatchByGpDetails_checkMaster() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpname("Dr Someone");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpdetails.getGppostcode());
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode(gpdetails.getGppostcode());
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpMasters);
        when(gpLetterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpLetters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(patientview);

        Assert.assertEquals("Should have one match", 1, found.size());
    }

    @Test
    public void testMatchByGpDetails_checkMasterMultiple() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpdetails.getGppostcode());
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode(gpdetails.getGppostcode());
        GpLetter gpLetter2 = new GpLetter();
        gpLetter2.setGpPostcode(gpdetails.getGppostcode());
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);
        gpLetters.add(gpLetter2);

        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpMasters);
        when(gpLetterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpLetters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(patientview);

        Assert.assertEquals("Should have two matches", 2, found.size());
    }

    @Test
    public void testMatchByGpDetails_checkBoth() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpdetails.getGppostcode());
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode(gpdetails.getGppostcode());
        gpLetter.setGpAddress1(gpdetails.getGpaddress1());
        gpLetter.setGpAddress2(gpdetails.getGpaddress2());
        gpLetter.setGpAddress3(gpdetails.getGpaddress3());
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);

        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpMasters);
        when(gpLetterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpLetters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(patientview);

        Assert.assertEquals("Should have one match", 1, found.size());
    }

    @Test
    public void testMatchByGpDetails_checkBothMultiple() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpdetails.getGppostcode());
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode(gpdetails.getGppostcode());
        gpLetter.setGpAddress1(gpdetails.getGpaddress1());
        gpLetter.setGpAddress2(gpdetails.getGpaddress2());
        gpLetter.setGpAddress3(gpdetails.getGpaddress3());
        GpLetter gpLetter2 = new GpLetter();
        gpLetter2.setGpPostcode(gpdetails.getGppostcode());
        gpLetter2.setGpAddress1(gpdetails.getGpaddress1() + "2");
        gpLetter2.setGpAddress2(gpdetails.getGpaddress2());
        gpLetter2.setGpAddress3(gpdetails.getGpaddress3());
        List<GpLetter> gpLetters = new ArrayList<>();
        gpLetters.add(gpLetter);
        gpLetters.add(gpLetter2);

        when(gpMasterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpMasters);
        when(gpLetterRepository.findByPostcode(eq(gpdetails.getGppostcode()))).thenReturn(gpLetters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(patientview);

        Assert.assertEquals("Should have two matches", 2, found.size());
    }
}
