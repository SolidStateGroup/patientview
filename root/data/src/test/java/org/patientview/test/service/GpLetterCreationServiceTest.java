package org.patientview.test.service;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.service.GpLetterCreationService;
import org.patientview.service.impl.GpLetterCreationServiceImpl;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class GpLetterCreationServiceTest extends BaseTest {

    @Mock
    GpLetterRepository gpLetterRepository;

    @Mock
    GpMasterRepository gpMasterRepository;

    @InjectMocks
    GpLetterCreationService gpLetterService = new GpLetterCreationServiceImpl();

    @Mock
    Properties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();
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
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);

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
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);

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
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);

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
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);

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

        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gps);

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

        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gps);

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

        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpLetters);

        GpMaster gpMaster = new GpMaster();
        gpMaster.setPostcode(gpLetter.getGpPostcode());
        gpMaster.setPracticeCode("A1234");
        List<GpMaster> gpMasters = new ArrayList<>();
        gpMasters.add(gpMaster);
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);

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

        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpLetters);

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
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);

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

        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpLetters);

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
        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);

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

        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);
        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpLetters);

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

        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);
        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpLetters);

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

        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);
        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpLetters);

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

        when(gpMasterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpMasters);
        when(gpLetterRepository.findByPostcode(eq(gpLetter.getGpPostcode()))).thenReturn(gpLetters);

        List<GpLetter> found = gpLetterService.matchByGpDetails(gpLetter);

        Assert.assertEquals("Should have two matches", 2, found.size());
    }
}
