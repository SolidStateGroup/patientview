package org.patientview.test.service;

import com.itextpdf.text.DocumentException;
import generated.Patientview;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.service.GpLetterService;
import org.patientview.service.impl.GpLetterServiceImpl;
import org.patientview.test.util.TestUtils;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class GpLetterServiceTest extends BaseTest {

    @Mock
    GpLetterRepository gpLetterRepository;

    @Mock
    GpMasterRepository gpMasterRepository;

    @InjectMocks
    GpLetterService gpLetterService = new GpLetterServiceImpl();

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

        verify(gpLetterRepository, Mockito.times(1)).save(any(GpLetter.class));
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

        verify(gpLetterRepository, Mockito.times(1)).save(any(GpLetter.class));
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
