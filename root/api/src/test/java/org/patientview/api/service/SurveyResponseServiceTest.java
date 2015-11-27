package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.enums.DummyUsernames;
import org.patientview.api.service.impl.SurveyResponseServiceImpl;
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
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
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
import org.patientview.persistence.repository.QuestionOptionRepository;
import org.patientview.persistence.repository.QuestionRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2015
 */
public class SurveyResponseServiceTest {

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
    QuestionOptionRepository questionOptionRepository;

    @Mock
    RoleService roleService;

    @Mock
    SurveyRepository surveyRepository;

    @Mock
    SurveyResponseRepository surveyResponseRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    SurveyResponseService surveyResponseService = new SurveyResponseServiceImpl();

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
    public void testAdd() throws ResourceNotFoundException, MessagingException {
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
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE);
        survey.setId(1L);

        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setUser(user);
        surveyResponse.setDate(new Date());
        surveyResponse.setSurvey(survey);

        Question question = new Question();
        question.setId(1L);
        question.setType(QuestionTypes.FEELING);
        question.setElementType(QuestionElementTypes.SINGLE_SELECT_RANGE);

        QuestionAnswer questionAnswer = new QuestionAnswer();
        questionAnswer.setQuestion(question);
        questionAnswer.setValue("20");
        surveyResponse.getQuestionAnswers().add(questionAnswer);

        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);
        when(questionRepository.findOne(eq(question.getId()))).thenReturn(question);
        when(surveyRepository.findOne(eq(survey.getId()))).thenReturn(survey);

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

        surveyResponseService.add(user.getId(), surveyResponse);

        verify(surveyResponseRepository, Mockito.times(1)).save(any(SurveyResponse.class));
        verify(conversationRepository, Mockito.times(1)).save(any(Conversation.class));
        verify(emailService, Mockito.times(1)).sendEmail(any(Email.class));
    }

    @Test
    public void testAdd_notHigh() throws ResourceNotFoundException, MessagingException {

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
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE);
        survey.setId(1L);

        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setUser(user);
        surveyResponse.setDate(new Date());
        surveyResponse.setSurvey(survey);

        Question question = new Question();
        question.setId(1L);
        question.setType(QuestionTypes.FEELING);
        question.setElementType(QuestionElementTypes.SINGLE_SELECT_RANGE);

        QuestionAnswer questionAnswer = new QuestionAnswer();
        questionAnswer.setQuestion(question);
        questionAnswer.setValue("1");
        surveyResponse.getQuestionAnswers().add(questionAnswer);

        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);
        when(questionRepository.findOne(eq(question.getId()))).thenReturn(question);
        when(surveyRepository.findOne(eq(survey.getId()))).thenReturn(survey);

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

        surveyResponseService.add(user.getId(), surveyResponse);

        verify(surveyResponseRepository, Mockito.times(1)).save(any(SurveyResponse.class));
        verify(conversationRepository, Mockito.times(0)).save(any(Conversation.class));
        verify(emailService, Mockito.times(0)).sendEmail(any(Email.class));
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
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE);

        SurveyResponse surveyResponse
                = new SurveyResponse(user, 1, ScoreSeverity.LOW, new Date(), SurveyResponseScoreTypes.SYMPTOM_SCORE);
        List<SurveyResponse> surveyResponses = new ArrayList<>();
        surveyResponses.add(surveyResponse);
        surveyResponse.setSurvey(survey);

        when(userRepository.findOne(Matchers.eq(user.getId()))).thenReturn(user);
        when(surveyResponseRepository.findByUserAndSurveyType(eq(user), eq(survey.getType())))
                .thenReturn(surveyResponses);
        List<SurveyResponse> returned = surveyResponseService.getByUserIdAndSurveyType(user.getId(), survey.getType());

        verify(surveyResponseRepository, Mockito.times(1)).findByUserAndSurveyType(eq(user), eq(survey.getType()));
        Assert.assertEquals("Should return 1 symptom score", 1, returned.size());
    }
}
