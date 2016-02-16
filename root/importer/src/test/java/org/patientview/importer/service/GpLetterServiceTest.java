package org.patientview.importer.service;

import com.itextpdf.text.DocumentException;
import generated.Patientview;
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
        when(gpLetterCreationService.hasValidPracticeDetails(any(GpLetter.class))).thenReturn(true);

        gpLetterService.add(patientview, sourceGroup);

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
        when(gpLetterCreationService.hasValidPracticeDetails(any(GpLetter.class))).thenReturn(false);
        when(gpLetterCreationService.hasValidPracticeDetailsSingleMaster(any(GpLetter.class))).thenReturn(true);

        gpLetterService.add(patientview, sourceGroup);

        verify(gpLetterRepository, Mockito.times(1)).save(any(GpLetter.class));
        verify(gpLetterCreationService, Mockito.times(1)).generateLetter(
                any(GpLetter.class), eq(gpMaster), any(String.class), any(String.class));
    }
}
