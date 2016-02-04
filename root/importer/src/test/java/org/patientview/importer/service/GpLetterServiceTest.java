package org.patientview.importer.service;

import generated.Patientview;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.patientview.importer.BaseTest;
import org.patientview.importer.Utility.Util;
import org.patientview.importer.service.impl.GpLetterServiceImpl;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class GpLetterServiceTest extends BaseTest {

    @InjectMocks
    GpLetterService gpLetterService = new GpLetterServiceImpl();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        PowerMockito.mockStatic(Util.class);
    }

    @Test
    public void hasValidPracticeDetails() {
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
    public void hasValidPracticeDetails_noPostcode() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGpaddress3("address3");
        patientview.setGpdetails(gpdetails);

        Assert.assertFalse("Should be invalid practice details", gpLetterService.hasValidPracticeDetails(patientview));
    }

    @Test
    public void hasValidPracticeDetails_2FieldsA() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress2("address2");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        Assert.assertTrue("Should be valid practice details", gpLetterService.hasValidPracticeDetails(patientview));
    }

    @Test
    public void hasValidPracticeDetails_2FieldsB() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpaddress1("address1");
        gpdetails.setGpaddress3("address3");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        Assert.assertTrue("Should be valid practice details", gpLetterService.hasValidPracticeDetails(patientview));
    }

    @Test
    public void hasValidPracticeDetails_notEnoughFields() {
        Patientview patientview = new Patientview();
        Patientview.Gpdetails gpdetails = new Patientview.Gpdetails();

        gpdetails.setGpaddress1("address1");
        gpdetails.setGppostcode("AB1 23C");
        patientview.setGpdetails(gpdetails);

        Assert.assertFalse("Should be invalid practice details", gpLetterService.hasValidPracticeDetails(patientview));
    }

}
