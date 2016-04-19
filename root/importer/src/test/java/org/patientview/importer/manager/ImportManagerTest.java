package org.patientview.importer.manager;

import generated.Patientview;
import generated.Survey;
import generated.SurveyResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.patientview.importer.BaseTest;
import org.patientview.importer.manager.impl.ImportManagerImpl;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionGroup;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
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
import java.util.ArrayList;
import java.util.List;
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

    @Mock
    IdentifierRepository identifierRepository;

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

    @Test
    public void testValidateSurveyResponse() throws Exception {
        Unmarshaller unmarshaller = JAXBContext.newInstance(SurveyResponse.class).createUnmarshaller();
        SurveyResponse surveyResponse = (SurveyResponse) unmarshaller.unmarshal(new File(
                Thread.currentThread().getContextClassLoader()
                        .getResource("data/xml/survey_response/survey_response_1.xml").toURI()));
        Assert.assertNotNull("Should have SurveyResponse object", surveyResponse);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(new Identifier());

        // set up currently stored survey
        org.patientview.persistence.model.Survey survey = new org.patientview.persistence.model.Survey();
        QuestionGroup questionGroup = new QuestionGroup();
        for (SurveyResponse.QuestionAnswers.QuestionAnswer questionAnswer
                : surveyResponse.getQuestionAnswers().getQuestionAnswer()) {
            Question question = new Question(questionAnswer.getQuestionType());
            if (StringUtils.isNotEmpty(questionAnswer.getQuestionOption())) {
                QuestionOption questionOption = new QuestionOption();
                questionOption.setType(questionAnswer.getQuestionOption());
                question.getQuestionOptions().add(questionOption);
            }
            questionGroup.getQuestions().add(question);
        }
        survey.getQuestionGroups().add(questionGroup);

        when(identifierRepository.findByValue(eq(surveyResponse.getIdentifier()))).thenReturn(identifiers);
        when(surveyService.getByType(eq(surveyResponse.getSurveyType()))).thenReturn(survey);
        importManager.validate(surveyResponse);
    }
}
