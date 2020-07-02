package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.enums.DummyUsernames;
import org.patientview.api.service.impl.ApiSurveyResponseServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.QuestionGroup;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.model.enums.ExternalServices;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.QuestionElementTypes;
import org.patientview.persistence.model.enums.QuestionTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.model.enums.SurveyResponseScoreTypes;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.QuestionRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.patientview.service.UkrdcService;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.mail.MessagingException;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2015
 */
public class ApiSurveyResponseServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    User creator;

    @Mock
    ConversationRepository conversationRepository;

    @Mock
    EmailService emailService;

    @Mock
    FeatureRepository featureRepository;

    @Mock
    LookupService lookupService;

    @Mock
    Properties properties;

    @Mock
    QuestionRepository questionRepository;

    @Mock
    RoleService roleService;

    @Mock
    SurveyRepository surveyRepository;

    @Mock
    SurveyResponseRepository surveyResponseRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserService userService;

    @Mock
    UserTokenRepository userTokenRepository;

    @Mock
    UkrdcService ukrdcService;

    @Mock
    ExternalServiceService externalServiceService;

    @InjectMocks
    ApiSurveyResponseService apiSurveyResponseService = new ApiSurveyResponseServiceImpl();

    private static Survey buildSurveyFrom(long id) {

        Survey survey = new Survey();
        survey.setType("POS_S");
        survey.setId(id);

        return survey;
    }

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
    public void testAdd() throws ResourceForbiddenException, ResourceNotFoundException,
            MessagingException, JAXBException, DatatypeConfigurationException {

        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);
        user.setGroupRoles(groupRoles);

        // staff user for scoring alerts
        User staffUser = TestUtils.createUser("staffUser");
        staffUser.setEmail("staffUser@patientview.org");
        staffUser.setId(2L);
        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN);
        staffUser.setGroupRoles(new HashSet<GroupRole>());
        staffUser.getGroupRoles().add(TestUtils.createGroupRole(staffRole, group, staffUser));
        staffUser.setUserFeatures(new HashSet<UserFeature>());
        Feature scoringAlertFeature = TestUtils.createFeature(FeatureType.IBD_SCORING_ALERTS.toString());
        staffUser.getUserFeatures().add(TestUtils.createUserFeature(scoringAlertFeature, staffUser));
        staffUser.getUserFeatures().add(TestUtils.createUserFeature(
                TestUtils.createFeature(FeatureType.MESSAGING.toString()), staffUser));

        Lookup specialtyGroupLookup
                = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.SPECIALTY.toString());
        List<Role> staffRoles = new ArrayList<>();
        staffRoles.add(staffRole);

        List<User> staffUsers = new ArrayList<>();
        staffUsers.add(staffUser);

        // PatientView Notifications user, sends secure message (conversation)
        User notificationsUser = TestUtils.createUser(DummyUsernames.PATIENTVIEW_NOTIFICATIONS.getName());
        notificationsUser.setId(3L);

        // survey
        Survey survey = new Survey();
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE.toString());
        survey.setId(1L);

        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setUser(user);
        surveyResponse.setDate(new Date());
        surveyResponse.setSurvey(survey);

        Question question = new Question();
        question.setId(1L);
        question.setType(QuestionTypes.FEELING.toString());
        question.setElementType(QuestionElementTypes.SINGLE_SELECT_RANGE);

        QuestionAnswer questionAnswer = new QuestionAnswer();
        questionAnswer.setQuestion(question);
        questionAnswer.setValue("20");
        surveyResponse.getQuestionAnswers().add(questionAnswer);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        when(questionRepository.findById(eq(question.getId()))).thenReturn(Optional.of(question));
        when(surveyRepository.findById(eq(survey.getId()))).thenReturn(Optional.of(survey));

        // scoring alerts
        when(lookupService.findByTypeAndValue(eq(LookupTypes.GROUP), eq(GroupTypes.SPECIALTY.toString())))
                .thenReturn(specialtyGroupLookup);
        when(roleService.getRolesByType(eq(RoleType.STAFF))).thenReturn(staffRoles);
        when(featureRepository.findByName(eq(FeatureType.IBD_SCORING_ALERTS.toString())))
                .thenReturn(scoringAlertFeature);
        when(userRepository.findStaffByGroupsRolesFeatures(eq("%%"), any(List.class), any(List.class), any(List.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(staffUsers));
        when(properties.getProperty("smtp.sender.email")).thenReturn("no_reply@patientview.org");
        when(properties.getProperty("smtp.sender.name")).thenReturn("PatientView Team");
        when(properties.getProperty("site.url")).thenReturn("http://localhost:9000");
        when(emailService.sendEmail(any(Email.class))).thenReturn(true);
        when(userRepository.findByUsernameCaseInsensitive(eq(notificationsUser.getUsername())))
                .thenReturn(notificationsUser);

        apiSurveyResponseService.add(user.getId(), surveyResponse);

        verify(surveyResponseRepository, times(1)).save(any(SurveyResponse.class));
        verify(conversationRepository, times(1)).save(any(Conversation.class));
        verify(emailService, times(1)).sendEmail(any(Email.class));
    }

    @Test
    public void testAdd_IBD_SELF_MANAGEMENT()
            throws ResourceForbiddenException, ResourceNotFoundException, MessagingException, JAXBException, DatatypeConfigurationException {
        String staffToken = "1234567890";

        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);
        user.setGroupRoles(groupRoles);

        // staff user who has switched to patient and is filling in survey on their behalf
        User staffUser = TestUtils.createUser("staffUser");
        staffUser.setEmail("staffUser@patientview.org");
        staffUser.setId(2L);
        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN);
        staffUser.setGroupRoles(new HashSet<GroupRole>());
        staffUser.getGroupRoles().add(TestUtils.createGroupRole(staffRole, group, staffUser));

        Lookup specialtyGroupLookup
                = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.SPECIALTY.toString());
        List<Role> staffRoles = new ArrayList<>();
        staffRoles.add(staffRole);

        List<User> staffUsers = new ArrayList<>();
        staffUsers.add(staffUser);

        UserToken userToken = new UserToken();
        userToken.setUser(staffUser);
        userToken.setToken(staffToken);

        // survey
        Survey survey = new Survey();
        survey.setType(SurveyTypes.IBD_SELF_MANAGEMENT.toString());
        survey.setId(1L);

        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setUser(user);
        surveyResponse.setDate(new Date());
        surveyResponse.setSurvey(survey);
        surveyResponse.setStaffToken(staffToken);

        Question question = new Question();
        question.setId(1L);
        question.setType(QuestionTypes.IBD_SELF_MANAGEMENT_USUAL_SYMPTOMS.toString());
        question.setElementType(QuestionElementTypes.TEXT);

        QuestionAnswer questionAnswer = new QuestionAnswer();
        questionAnswer.setQuestion(question);
        questionAnswer.setValue("some usual symptoms");
        surveyResponse.getQuestionAnswers().add(questionAnswer);

        when(questionRepository.findById(eq(question.getId()))).thenReturn(Optional.of(question));
        when(surveyRepository.findById(eq(survey.getId()))).thenReturn(Optional.of(survey));
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        when(userTokenRepository.findByToken(eq(staffToken))).thenReturn(userToken);
        when(userService.userCanSwitchToUser(eq(staffUser), eq(user))).thenReturn(true);

        // scoring alerts
        when(lookupService.findByTypeAndValue(eq(LookupTypes.GROUP), eq(GroupTypes.SPECIALTY.toString())))
                .thenReturn(specialtyGroupLookup);
        when(roleService.getRolesByType(eq(RoleType.STAFF))).thenReturn(staffRoles);
        when(userRepository.findStaffByGroupsRolesFeatures(eq("%%"), any(List.class), any(List.class), any(List.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(staffUsers));

        apiSurveyResponseService.add(user.getId(), surveyResponse);

        verify(surveyResponseRepository, times(1)).save(any(SurveyResponse.class));
    }

    @Test
    public void testAdd_notHigh() throws ResourceForbiddenException, ResourceNotFoundException, MessagingException, JAXBException, DatatypeConfigurationException {
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.UNIT.toString()));
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);
        user.setGroupRoles(groupRoles);

        // staff user for scoring alerts
        User staffUser = TestUtils.createUser("staffUser");
        staffUser.setEmail("staffUser@patientview.org");
        staffUser.setId(2L);
        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN);
        staffUser.setGroupRoles(new HashSet<GroupRole>());
        staffUser.getGroupRoles().add(TestUtils.createGroupRole(staffRole, group, staffUser));
        staffUser.setUserFeatures(new HashSet<UserFeature>());
        Feature scoringAlertFeature = TestUtils.createFeature(FeatureType.IBD_SCORING_ALERTS.toString());
        staffUser.getUserFeatures().add(TestUtils.createUserFeature(scoringAlertFeature, staffUser));

        Lookup specialtyGroupLookup
                = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.GROUP), GroupTypes.SPECIALTY.toString());
        List<Role> staffRoles = new ArrayList<>();
        staffRoles.add(staffRole);

        List<User> staffUsers = new ArrayList<>();
        staffUsers.add(staffUser);

        // PatientView Notifications user, sends secure message (conversation)
        User notificationsUser = TestUtils.createUser(DummyUsernames.PATIENTVIEW_NOTIFICATIONS.getName());
        notificationsUser.setId(3L);

        // survey
        Survey survey = new Survey();
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE.toString());
        survey.setId(1L);

        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setUser(user);
        surveyResponse.setDate(new Date());
        surveyResponse.setSurvey(survey);

        Question question = new Question();
        question.setId(1L);
        question.setType(QuestionTypes.FEELING.toString());
        question.setElementType(QuestionElementTypes.SINGLE_SELECT_RANGE);

        QuestionAnswer questionAnswer = new QuestionAnswer();
        questionAnswer.setQuestion(question);
        questionAnswer.setValue("1");
        surveyResponse.getQuestionAnswers().add(questionAnswer);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        when(questionRepository.findById(eq(question.getId()))).thenReturn(Optional.of(question));
        when(surveyRepository.findById(eq(survey.getId()))).thenReturn(Optional.of(survey));

        // scoring alerts
        when(lookupService.findByTypeAndValue(eq(LookupTypes.GROUP), eq(GroupTypes.SPECIALTY.toString())))
                .thenReturn(specialtyGroupLookup);
        when(roleService.getRolesByType(eq(RoleType.STAFF))).thenReturn(staffRoles);
        when(featureRepository.findByName(eq(FeatureType.IBD_SCORING_ALERTS.toString())))
                .thenReturn(scoringAlertFeature);
        when(userRepository.findStaffByGroupsRolesFeatures(eq("%%"), any(List.class), any(List.class), any(List.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(staffUsers));
        when(properties.getProperty("smtp.sender.email")).thenReturn("no_reply@patientview.org");
        when(properties.getProperty("smtp.sender.name")).thenReturn("PatientView Team");
        when(properties.getProperty("site.url")).thenReturn("http://localhost:9000");
        when(emailService.sendEmail(any(Email.class))).thenReturn(true);
        when(userRepository.findByUsernameCaseInsensitive(eq(notificationsUser.getUsername())))
                .thenReturn(notificationsUser);

        apiSurveyResponseService.add(user.getId(), surveyResponse);

        verify(surveyResponseRepository, times(1)).save(any(SurveyResponse.class));
        verify(conversationRepository, times(0)).save(any(Conversation.class));
        verify(emailService, times(0)).sendEmail(any(Email.class));
    }

    @Test
    public void testGetByUserIdAndType() throws ResourceNotFoundException {
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        Survey survey = new Survey();
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE.toString());

        SurveyResponse surveyResponse = new SurveyResponse(
                user, 1.0, ScoreSeverity.LOW, new Date(), SurveyResponseScoreTypes.SYMPTOM_SCORE.toString());
        List<SurveyResponse> surveyResponses = new ArrayList<>();
        surveyResponses.add(surveyResponse);
        surveyResponse.setSurvey(survey);

        when(userRepository.findById(Matchers.eq(user.getId()))).thenReturn(Optional.of(user));
        when(surveyResponseRepository.findByUserAndSurveyType(eq(user), eq(survey.getType())))
                .thenReturn(surveyResponses);
        List<SurveyResponse> returned = apiSurveyResponseService.getByUserIdAndSurveyType(user.getId(), survey.getType());

        verify(surveyResponseRepository, times(1)).findByUserAndSurveyType(eq(user), eq(survey.getType()));
        Assert.assertEquals("Should return 1 symptom score", 1, returned.size());
    }

    @Test
    public void testGetLatestByUserIdAndType() throws ResourceNotFoundException {
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        Survey survey = new Survey();
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE.toString());

        List<String> types = new ArrayList<>();
        types.add(survey.getType());

        SurveyResponse surveyResponse = new SurveyResponse(
                user, 1.0, ScoreSeverity.LOW, new Date(), SurveyResponseScoreTypes.SYMPTOM_SCORE.toString());
        List<SurveyResponse> surveyResponses = new ArrayList<>();
        surveyResponses.add(surveyResponse);
        surveyResponse.setSurvey(survey);


        when(userRepository.findById(Matchers.eq(user.getId()))).thenReturn(Optional.of(user));
        when(surveyResponseRepository.findLatestByUserAndSurveyType(eq(user), eq(survey.getType()),
                any(Pageable.class))).thenReturn(new PageImpl<>(surveyResponses));
        List<SurveyResponse> returned = apiSurveyResponseService.getLatestByUserIdAndSurveyType(user.getId(), types);

        verify(surveyResponseRepository, times(1)).findLatestByUserAndSurveyType(eq(user), eq(survey.getType()),
                any(Pageable.class));
        Assert.assertEquals("Should return 1 symptom score", 1, returned.size());
    }

    @Test
    public void should_Not_Store_QuestionAnswer_When_CustomQuestion_Is_True_And_QuestionText_is_null()
            throws ResourceNotFoundException, ResourceForbiddenException,
            JAXBException, DatatypeConfigurationException {

        // Given

        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        SurveyResponse response = new SurveyResponse();

        long surveyId = 14917703;
        response.setSurvey(buildSurveyFrom(surveyId));

        Question questionWithCustomerQuestionFlag = new Question();
        questionWithCustomerQuestionFlag.setId(2L);
        questionWithCustomerQuestionFlag.setCustomQuestion(true);

        QuestionAnswer answerWithoutQuestionText = new QuestionAnswer();
        answerWithoutQuestionText.setValue("Pain");
        answerWithoutQuestionText.setQuestion(questionWithCustomerQuestionFlag);

        Question question = new Question();
        question.setId(3L);
        question.setCustomQuestion(false);

        QuestionAnswer answer = new QuestionAnswer();
        answer.setValue("Mobility");
        answer.setQuestion(question);

        response.setQuestionAnswers(asList(answerWithoutQuestionText, answer));
        response.setDate(new Date());

        when(userRepository.findById(Matchers.eq(user.getId()))).thenReturn(Optional.of(user));
        when(surveyRepository.findById(surveyId)).thenReturn(Optional.of(buildSurveyFrom(surveyId)));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(questionWithCustomerQuestionFlag));
        when(questionRepository.findById(3L)).thenReturn(Optional.of(question));

        String xml = "<xml>test</xml>";

        when(ukrdcService.buildSurveyXml(any(SurveyResponse.class), eq("PROM")))
                .thenReturn(xml);

        // When

        apiSurveyResponseService.add(user.getId(), response);

        // Then

        surveyResponseRepository.save(Matchers.any(SurveyResponse.class));

        // And survey is queued for external delivery

        verify(externalServiceService, times(1))
                .addToQueue(eq(ExternalServices.SURVEY_NOTIFICATION), eq(xml), eq(user), any(Date.class));
    }

    @Test
    public void should_not_return_PROM_when_not_found() throws ResourceNotFoundException {

        // Given

        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        String posS = "POS_S";

        when(userRepository.findById(Matchers.eq(user.getId()))).thenReturn(Optional.of(user));
        when(surveyRepository.findByType("POS_S")).thenReturn(singletonList(new Survey()));

        // When

        List<SurveyResponse> result = apiSurveyResponseService.getByUserIdAndSurveyType(user.getId(), posS);

        // Then

        Assert.assertTrue("Should have 0 response", result.size() == 0);
    }

    @Test
    public void should_map_PROM_to_POS_S() throws ResourceNotFoundException {

        // Given

        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        String posS = "POS_S";

        when(userRepository.findById(Matchers.eq(user.getId()))).thenReturn(Optional.of(user));
        when(surveyRepository.findByType("POS_S")).thenReturn(singletonList(buildPossSurvey()));
        when(surveyResponseRepository.findByUserAndSurveyType(user, "PROM")).thenReturn(singletonList(buildPromSurveyResponse()));

        // When

        List<SurveyResponse> responses = apiSurveyResponseService.getByUserIdAndSurveyType(user.getId(), posS);

        // Then

        Assert.assertTrue("Should have one response", responses.size() == 1);
    }

    private Survey buildPossSurvey() {

        Survey survey = new Survey();
        survey.setId(1L);

        QuestionGroup questionGroup = new QuestionGroup();
        questionGroup.setDescription("test description");

        Question firstQuestion = new Question();
        firstQuestion.setId(14917730L);
        firstQuestion.setType("YSQ1");
        firstQuestion.setText("Pain");

        QuestionOption firstOption = new QuestionOption();
        firstOption.setDescription("1st option from POS_S");
        firstOption.setText("1st option");
        firstOption.setId(1L);
        QuestionOption secondOption = new QuestionOption();
        secondOption.setDescription("2nd option from POS_S");
        secondOption.setText("2nd option");
        secondOption.setId(2L);

        firstQuestion.setQuestionOptions(asList(firstOption, secondOption));

        List<Question> questions = asList(firstQuestion);

        questionGroup.setQuestions(questions);

        List<QuestionGroup> groups = singletonList(questionGroup);

        survey.setQuestionGroups(groups);
        survey.setType("POS_S");

        return survey;
    }

    private SurveyResponse buildPromSurveyResponse() {

        SurveyResponse response = new SurveyResponse();
        response.setSurvey(new Survey());

        QuestionAnswer questionAnswer = new QuestionAnswer();

        Question firstQuestion = new Question();
        firstQuestion.setId(21135588L);
        firstQuestion.setType("TEST");
        firstQuestion.setText("TEST1");

        QuestionOption firstOption = new QuestionOption();
        firstOption.setDescription("1st option from PROM");
        firstOption.setText("1st option");
        firstOption.setId(3L);
        QuestionOption secondOption = new QuestionOption();
        secondOption.setDescription("2nd option from PROM");
        secondOption.setText("2nd option");
        secondOption.setId(4L);

        firstQuestion.setQuestionOptions(asList(firstOption, secondOption));

        questionAnswer.setQuestion(firstQuestion);
        questionAnswer.setQuestionOption(firstOption);

        List<QuestionAnswer> questionAnswers = asList(questionAnswer);

        response.setQuestionAnswers(questionAnswers);

        return response;
    }
}
