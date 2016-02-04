package org.patientview.importer.service;

import generated.Patientview;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.patientview.importer.BaseTest;
import org.patientview.importer.Utility.Util;
import org.patientview.importer.service.impl.GpLetterServiceImpl;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.repository.GpMasterRepository;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class GpLetterServiceTest extends BaseTest {

    @Mock
    GpMasterRepository gpMasterRepository;

    @InjectMocks
    GpLetterService gpLetterService = new GpLetterServiceImpl();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        PowerMockito.mockStatic(Util.class);
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

        when(gpMasterRepository.findByPostcode(gpdetails.getGppostcode())).thenReturn(gps);

        Assert.assertTrue("Should be valid practice details",
                gpLetterService.hasValidPracticeDetailsCheckMaster(patientview));
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

        when(gpMasterRepository.findByPostcode(gpdetails.getGppostcode())).thenReturn(gps);

        Assert.assertFalse("Should be invalid practice details",
                gpLetterService.hasValidPracticeDetailsCheckMaster(patientview));
    }

}
