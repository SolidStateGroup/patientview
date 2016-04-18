package org.patientview.importer.manager;

import generated.Patientview;
import generated.Survey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.patientview.importer.BaseTest;
import org.patientview.importer.manager.impl.ImportManagerImpl;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.service.AllergyService;
import org.patientview.service.AuditService;
import org.patientview.service.ConditionService;
import org.patientview.service.DiagnosticService;
import org.patientview.service.DocumentReferenceService;
import org.patientview.service.EncounterService;
import org.patientview.service.GpLetterService;
import org.patientview.service.MedicationService;
import org.patientview.service.ObservationService;
import org.patientview.service.OrganizationService;
import org.patientview.service.PatientService;
import org.patientview.service.PractitionerService;
import org.patientview.service.SurveyService;
import org.patientview.test.util.TestUtils;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.UUID;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class ImportManagerTest extends BaseTest {

    @Mock
    AllergyService allergyService;

    @Mock
    AuditService auditService;

    @Mock
    GpLetterService gpLetterService;

    @Mock
    ConditionService conditionService;

    @Mock
    DiagnosticService diagnosticService;

    @Mock
    DocumentReferenceService documentReferenceService;

    @Mock
    EncounterService encounterService;

    @Mock
    GroupRepository groupRepository;

    @InjectMocks
    ImportManager importManager = new ImportManagerImpl();

    @Mock
    MedicationService medicationService;

    @Mock
    ObservationService observationService;

    @Mock
    OrganizationService organizationService;

    @Mock
    PatientService patientService;

    @Mock
    PractitionerService practitionerService;

    @Mock
    SurveyService surveyService;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testProcess() throws Exception {
        Patientview patientview = org.patientview.util.Util.unmarshallPatientRecord(getTestFile());

        when(organizationService.add(eq(patientview))).thenReturn(UUID.randomUUID());
        when(practitionerService.add(eq(patientview))).thenReturn(UUID.randomUUID());
        when(groupRepository.findByCode(eq(patientview.getCentredetails().getCentrecode())))
                .thenReturn(TestUtils.createGroup("testGroup"));

        importManager.process(patientview, getTestFile(), 1L);
    }

    @Test
    public void testValidateSurvey() throws Exception {
        Unmarshaller unmarshaller = JAXBContext.newInstance(Survey.class).createUnmarshaller();
        Survey survey = (Survey) unmarshaller.unmarshal(new File(
                Thread.currentThread().getContextClassLoader().getResource("data/xml/survey/survey_1.xml").toURI()));

        Assert.assertNotNull("Should have Survey object", survey);

        importManager.validate(survey);
    }
}
