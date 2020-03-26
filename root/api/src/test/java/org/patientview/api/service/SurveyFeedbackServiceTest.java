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
import org.patientview.api.service.impl.SurveyFeedbackServiceImpl;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyFeedback;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.persistence.repository.SurveyFeedbackRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/05/2016
 */
public class SurveyFeedbackServiceTest {

    @Mock
    SurveyFeedbackRepository surveyFeedbackRepository;

    @InjectMocks
    SurveyFeedbackService surveyFeedbackService = new SurveyFeedbackServiceImpl();

    @Mock
    SurveyRepository surveyRepository;

    @Mock
    UserRepository userRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testAdd() throws ResourceNotFoundException, VerificationException {
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
        survey.setId(1L);

        SurveyFeedback surveyFeedback = new SurveyFeedback();
        surveyFeedback.setUser(user);
        surveyFeedback.setSurvey(survey);
        surveyFeedback.setFeedback("feedback");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(surveyRepository.findById(Matchers.eq(survey.getId()))).thenReturn(Optional.of(survey));

        surveyFeedbackService.add(user.getId(), surveyFeedback);

        verify(surveyFeedbackRepository, Mockito.times(1)).save(any(SurveyFeedback.class));
    }

    @Test
    public void testGetByUserIdAndSurveyId() throws ResourceNotFoundException {
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

        SurveyFeedback surveyFeedback = new SurveyFeedback();
        surveyFeedback.setUser(user);
        surveyFeedback.setSurvey(survey);
        surveyFeedback.setFeedback("feedback");

        List<SurveyFeedback> surveyFeedbacks = new ArrayList<>();
        surveyFeedbacks.add(surveyFeedback);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(surveyRepository.findById(Matchers.eq(survey.getId()))).thenReturn(Optional.of(survey));
        when(surveyFeedbackRepository.findBySurveyAndUser(eq(survey), eq(user))).thenReturn(surveyFeedbacks);

        List<org.patientview.api.model.SurveyFeedback> returned
                = surveyFeedbackService.getByUserIdAndSurveyId(user.getId(), survey.getId());

        verify(surveyFeedbackRepository, Mockito.times(1)).findBySurveyAndUser(eq(survey), eq(user));
        Assert.assertEquals("Should return 1 SurveyFeedback", 1, returned.size());
        Assert.assertEquals("Should return correct SurveyFeedback",
                surveyFeedback.getFeedback(), returned.get(0).getFeedback());
    }
}
