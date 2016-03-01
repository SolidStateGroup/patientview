package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.ExternalConversation;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.impl.ConversationServiceImpl;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.ConversationUser;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.MessageReadReceipt;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.ConversationTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.MessageTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.MessageRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/08/2014
 */
public class ConversationServiceTest {

    User creator;

    @Mock
    ConversationRepository conversationRepository;

    @InjectMocks
    ConversationService conversationService = new ConversationServiceImpl();

    @Mock
    EmailService emailService;

    @Mock
    FeatureRepository featureRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    MessageRepository messageRepository;

    @Mock
    Properties properties;

    @Mock
    UserRepository userRepository;

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
    public void testAddExternalConversation_identifier() throws Exception {
        User sender = TestUtils.createUser("sender");
        sender.setUserFeatures(new HashSet<UserFeature>());
        sender.getUserFeatures().add(TestUtils.createUserFeature(
                TestUtils.createFeature(FeatureType.MESSAGING.toString()), sender));

        User recipient = TestUtils.createUser("recipient");
        recipient.setUserFeatures(new HashSet<UserFeature>());
        recipient.getUserFeatures().add(TestUtils.createUserFeature(
                TestUtils.createFeature(FeatureType.MESSAGING.toString()), recipient));

        Group group = TestUtils.createGroup("testGroup");
        group.setGroupFeatures(new HashSet<GroupFeature>());
        group.getGroupFeatures().add(TestUtils.createGroupFeature(
                TestUtils.createFeature(FeatureType.MESSAGING.toString()), group));

        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole staffGroupRole = TestUtils.createGroupRole(staffRole, group, sender);
        sender.setGroupRoles(new HashSet<GroupRole>());
        sender.getGroupRoles().add(staffGroupRole);

        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole patientGroupRole = TestUtils.createGroupRole(patientRole, group, recipient);
        recipient.setGroupRoles(new HashSet<GroupRole>());
        recipient.getGroupRoles().add(patientGroupRole);

        ExternalConversation externalConversation = new ExternalConversation();
        externalConversation.setToken("abc123");
        externalConversation.setMessage("some message");
        externalConversation.setTitle("some title");
        externalConversation.setIdentifier("1234567890");
        externalConversation.setSenderUsername(sender.getUsername());

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), recipient, "1111111111"));

        when(identifierRepository.findByValue(eq(externalConversation.getIdentifier()))).thenReturn(identifiers);
        when(properties.getProperty(eq("external.conversation.token"))).thenReturn(externalConversation.getToken());
        when(userRepository.findByUsernameCaseInsensitive(eq(externalConversation.getSenderUsername())))
                .thenReturn(sender);
        when(userRepository.findOne(recipient.getId())).thenReturn(recipient);
        when(userRepository.findOne(sender.getId())).thenReturn(sender);

        ExternalConversation returned = conversationService.addExternalConversation(externalConversation);

        Assert.assertNotNull("Should return ExternalConversation", returned);
        Assert.assertNull("Should not have an error message, got '" + returned.getErrorMessage() + "'",
                returned.getErrorMessage());
        Assert.assertTrue("Should be successful", returned.isSuccess());

        verify(conversationRepository, times(1)).save(any(Conversation.class));
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    public void testAddExternalConversation_username() throws Exception {
        User sender = TestUtils.createUser("sender");
        sender.setUserFeatures(new HashSet<UserFeature>());
        sender.getUserFeatures().add(TestUtils.createUserFeature(
                TestUtils.createFeature(FeatureType.MESSAGING.toString()), sender));

        User recipient = TestUtils.createUser("recipient");
        recipient.setUserFeatures(new HashSet<UserFeature>());
        recipient.getUserFeatures().add(TestUtils.createUserFeature(
                TestUtils.createFeature(FeatureType.MESSAGING.toString()), recipient));

        Group group = TestUtils.createGroup("testGroup");
        group.setGroupFeatures(new HashSet<GroupFeature>());
        group.getGroupFeatures().add(TestUtils.createGroupFeature(
                TestUtils.createFeature(FeatureType.MESSAGING.toString()), group));

        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole staffGroupRole = TestUtils.createGroupRole(staffRole, group, sender);
        sender.setGroupRoles(new HashSet<GroupRole>());
        sender.getGroupRoles().add(staffGroupRole);

        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole patientGroupRole = TestUtils.createGroupRole(patientRole, group, recipient);
        recipient.setGroupRoles(new HashSet<GroupRole>());
        recipient.getGroupRoles().add(patientGroupRole);

        ExternalConversation externalConversation = new ExternalConversation();
        externalConversation.setToken("abc123");
        externalConversation.setMessage("some message");
        externalConversation.setTitle("some title");
        externalConversation.setRecipientUsername("1234567890");
        externalConversation.setSenderUsername(sender.getUsername());

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), recipient, "1111111111"));

        when(identifierRepository.findByValue(eq(externalConversation.getIdentifier()))).thenReturn(identifiers);
        when(properties.getProperty(eq("external.conversation.token"))).thenReturn(externalConversation.getToken());
        when(userRepository.findByUsernameCaseInsensitive(eq(externalConversation.getRecipientUsername())))
                .thenReturn(recipient);
        when(userRepository.findByUsernameCaseInsensitive(eq(externalConversation.getSenderUsername())))
                .thenReturn(sender);
        when(userRepository.findOne(recipient.getId())).thenReturn(recipient);
        when(userRepository.findOne(sender.getId())).thenReturn(sender);

        ExternalConversation returned = conversationService.addExternalConversation(externalConversation);

        Assert.assertNotNull("Should return ExternalConversation", returned);
        Assert.assertNull("Should not have an error message, got '" + returned.getErrorMessage() + "'",
                returned.getErrorMessage());
        Assert.assertTrue("Should be successful", returned.isSuccess());

        verify(conversationRepository, times(1)).save(any(Conversation.class));
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    public void testAddExternalConversation_groupAndUserFeature() throws Exception {
        User sender = TestUtils.createUser("sender");
        sender.setUserFeatures(new HashSet<UserFeature>());
        sender.getUserFeatures().add(TestUtils.createUserFeature(
                TestUtils.createFeature(FeatureType.MESSAGING.toString()), sender));

        User recipient = TestUtils.createUser("recipient");
        recipient.setUserFeatures(new HashSet<UserFeature>());
        recipient.getUserFeatures().add(TestUtils.createUserFeature(
                TestUtils.createFeature(FeatureType.MESSAGING.toString()), recipient));

        Group group = TestUtils.createGroup("testGroup");
        group.setGroupFeatures(new HashSet<GroupFeature>());
        group.getGroupFeatures().add(TestUtils.createGroupFeature(
                TestUtils.createFeature(FeatureType.MESSAGING.toString()), group));
        group.setCode("A12345");

        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole staffGroupRole = TestUtils.createGroupRole(staffRole, group, sender);
        sender.setGroupRoles(new HashSet<GroupRole>());
        sender.getGroupRoles().add(staffGroupRole);

        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole patientGroupRole = TestUtils.createGroupRole(patientRole, group, recipient);
        recipient.setGroupRoles(new HashSet<GroupRole>());
        recipient.getGroupRoles().add(patientGroupRole);

        List<User> recipients = new ArrayList<>();
        recipients.add(recipient);

        ExternalConversation externalConversation = new ExternalConversation();
        externalConversation.setToken("abc123");
        externalConversation.setMessage("some message");
        externalConversation.setTitle("some title");
        externalConversation.setGroupCode(group.getCode());
        externalConversation.setSenderUsername(sender.getUsername());
        externalConversation.setUserFeature(FeatureType.DEFAULT_MESSAGING_CONTACT.toString());

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), recipient, "1111111111"));

        Feature feature = TestUtils.createFeature(externalConversation.getUserFeature());

        when(featureRepository.findByName(eq(externalConversation.getUserFeature()))).thenReturn(feature);
        when(groupRepository.findByCode(eq(externalConversation.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(externalConversation.getIdentifier()))).thenReturn(identifiers);
        when(properties.getProperty(eq("external.conversation.token"))).thenReturn(externalConversation.getToken());
        when(userRepository.findByUsernameCaseInsensitive(eq(externalConversation.getSenderUsername())))
                .thenReturn(sender);
        when(userRepository.findByGroupAndFeature(eq(group), eq(feature))).thenReturn(recipients);
        when(userRepository.findOne(recipient.getId())).thenReturn(recipient);
        when(userRepository.findOne(sender.getId())).thenReturn(sender);

        ExternalConversation returned = conversationService.addExternalConversation(externalConversation);

        Assert.assertNotNull("Should return ExternalConversation", returned);
        Assert.assertNull("Should not have an error message, got '" + returned.getErrorMessage() + "'",
                returned.getErrorMessage());
        Assert.assertTrue("Should be successful", returned.isSuccess());

        verify(conversationRepository, times(1)).save(any(Conversation.class));
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    public void testCreateConversation() throws ResourceForbiddenException {

        // set up group
        Group testGroup = TestUtils.createGroup("testGroup");
        Feature messagingFeature = TestUtils.createFeature(FeatureType.MESSAGING.toString());
        GroupFeature groupFeature = TestUtils.createGroupFeature(messagingFeature, testGroup);
        testGroup.setGroupFeatures(new HashSet<GroupFeature>());
        testGroup.getGroupFeatures().add(groupFeature);

        // add user1 as specialty admin
        User user1 = TestUtils.createUser("newTestUser1");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, testGroup, user1);
        user1.setGroupRoles(new TreeSet<GroupRole>());
        user1.getGroupRoles().add(groupRole);
        user1.setUserFeatures(new HashSet<UserFeature>());
        UserFeature userFeature1 = TestUtils.createUserFeature(messagingFeature, user1);
        user1.getUserFeatures().add(userFeature1);

        // add user2 as unit admin
        User user2 = TestUtils.createUser("newTestUser2");
        Role role2 = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole groupRole2 = TestUtils.createGroupRole(role2, testGroup, user2);
        user2.setGroupRoles(new TreeSet<GroupRole>());
        user2.getGroupRoles().add(groupRole2);
        user2.setUserFeatures(new HashSet<UserFeature>());
        UserFeature userFeature2 = TestUtils.createUserFeature(messagingFeature, user2);
        user2.getUserFeatures().add(userFeature2);

        // authenticate user1
        TestUtils.authenticateTest(user1, user1.getGroupRoles());

        Conversation conversation = new Conversation();
        conversation.setId(3L);
        conversation.setType(ConversationTypes.MESSAGE);

        ConversationUser conversationUser1 = new ConversationUser();
        conversationUser1.setId(4L);
        conversationUser1.setUser(user1);
        conversationUser1.setConversation(conversation);
        conversationUser1.setAnonymous(false);

        ConversationUser conversationUser2 = new ConversationUser();
        conversationUser2.setId(5L);
        conversationUser2.setUser(user2);
        conversationUser2.setConversation(conversation);
        conversationUser2.setAnonymous(false);

        Set<ConversationUser> conversationUserSet = new HashSet<>();
        conversationUserSet.add(conversationUser1);
        conversationUserSet.add(conversationUser2);
        conversation.setConversationUsers(conversationUserSet);

        Message message = new Message();
        message.setId(6L);
        message.setConversation(conversation);
        message.setUser(user1);
        message.setType(MessageTypes.MESSAGE);

        List<Message> messageList = new ArrayList<>();
        messageList.add(message);
        conversation.setMessages(messageList);

        when(conversationRepository.save(eq(conversation))).thenReturn(conversation);
        when(userRepository.findOne(Matchers.eq(user1.getId()))).thenReturn(user1);
        when(userRepository.findOne(Matchers.eq(user2.getId()))).thenReturn(user2);
        when(properties.getProperty(eq("site.url"))).thenReturn("");

        try {
            conversationService.addConversation(user1.getId(), conversation);
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("resource not found exception");
        }
    }

    @Test
    public void testCreateConversation_PatientToUnitAdmin() throws ResourceForbiddenException {

        // set up group
        Group testGroup = TestUtils.createGroup("testGroup");
        Feature messagingFeature = TestUtils.createFeature(FeatureType.MESSAGING.toString());
        GroupFeature groupFeature = TestUtils.createGroupFeature(messagingFeature, testGroup);
        testGroup.setGroupFeatures(new HashSet<GroupFeature>());
        testGroup.getGroupFeatures().add(groupFeature);

        // add user1 as patient
        User user1 = TestUtils.createUser("newTestUser1");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, testGroup, user1);
        user1.setGroupRoles(new TreeSet<GroupRole>());
        user1.getGroupRoles().add(groupRole);
        user1.setUserFeatures(new HashSet<UserFeature>());

        // add user2 as unit admin
        User user2 = TestUtils.createUser("newTestUser2");
        Role role2 = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole groupRole2 = TestUtils.createGroupRole(role2, testGroup, user2);
        user2.setGroupRoles(new TreeSet<GroupRole>());
        user2.getGroupRoles().add(groupRole2);
        user2.setUserFeatures(new HashSet<UserFeature>());
        UserFeature userFeature2 = TestUtils.createUserFeature(messagingFeature, user2);
        user2.getUserFeatures().add(userFeature2);

        // authenticate user1
        TestUtils.authenticateTest(user1, user1.getGroupRoles());

        Conversation conversation = new Conversation();
        conversation.setId(3L);
        conversation.setType(ConversationTypes.MESSAGE);

        ConversationUser conversationUser1 = new ConversationUser();
        conversationUser1.setId(4L);
        conversationUser1.setUser(user1);
        conversationUser1.setConversation(conversation);
        conversationUser1.setAnonymous(false);

        ConversationUser conversationUser2 = new ConversationUser();
        conversationUser2.setId(5L);
        conversationUser2.setUser(user2);
        conversationUser2.setConversation(conversation);
        conversationUser2.setAnonymous(false);

        Set<ConversationUser> conversationUserSet = new HashSet<>();
        conversationUserSet.add(conversationUser1);
        conversationUserSet.add(conversationUser2);
        conversation.setConversationUsers(conversationUserSet);

        Message message = new Message();
        message.setId(6L);
        message.setConversation(conversation);
        message.setUser(user1);
        message.setType(MessageTypes.MESSAGE);

        List<Message> messageList = new ArrayList<>();
        messageList.add(message);
        conversation.setMessages(messageList);

        when(conversationRepository.save(eq(conversation))).thenReturn(conversation);
        when(userRepository.findOne(Matchers.eq(user1.getId()))).thenReturn(user1);
        when(userRepository.findOne(Matchers.eq(user2.getId()))).thenReturn(user2);
        when(properties.getProperty(eq("site.url"))).thenReturn("");

        try {
            conversationService.addConversation(user1.getId(), conversation);
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("resource not found exception");
        }
    }

    @Test(expected=ResourceForbiddenException.class)
    public void testCreateConversation_noGroupfeature() throws ResourceForbiddenException {

        // set up group (no messaging feature)
        Group testGroup = TestUtils.createGroup("testGroup");
        Feature messagingFeature = TestUtils.createFeature(FeatureType.MESSAGING.toString());
        testGroup.setGroupFeatures(new HashSet<GroupFeature>());

        // add user1 as specialty admin
        User user1 = TestUtils.createUser("newTestUser1");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, testGroup, user1);
        user1.setGroupRoles(new TreeSet<GroupRole>());
        user1.getGroupRoles().add(groupRole);
        user1.setUserFeatures(new HashSet<UserFeature>());
        UserFeature userFeature1 = TestUtils.createUserFeature(messagingFeature, user1);
        user1.getUserFeatures().add(userFeature1);

        // add user2 as unit admin
        User user2 = TestUtils.createUser("newTestUser2");
        Role role2 = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole groupRole2 = TestUtils.createGroupRole(role2, testGroup, user2);
        user2.setGroupRoles(new TreeSet<GroupRole>());
        user2.getGroupRoles().add(groupRole2);
        user2.setUserFeatures(new HashSet<UserFeature>());
        UserFeature userFeature2 = TestUtils.createUserFeature(messagingFeature, user2);
        user2.getUserFeatures().add(userFeature2);

        // authenticate user1
        TestUtils.authenticateTest(user1, user1.getGroupRoles());

        Conversation conversation = new Conversation();
        conversation.setId(3L);
        conversation.setType(ConversationTypes.MESSAGE);

        ConversationUser conversationUser1 = new ConversationUser();
        conversationUser1.setId(4L);
        conversationUser1.setUser(user1);
        conversationUser1.setConversation(conversation);
        conversationUser1.setAnonymous(false);

        ConversationUser conversationUser2 = new ConversationUser();
        conversationUser2.setId(5L);
        conversationUser2.setUser(user2);
        conversationUser2.setConversation(conversation);
        conversationUser2.setAnonymous(false);

        Set<ConversationUser> conversationUserSet = new HashSet<>();
        conversationUserSet.add(conversationUser1);
        conversationUserSet.add(conversationUser2);
        conversation.setConversationUsers(conversationUserSet);

        Message message = new Message();
        message.setId(6L);
        message.setConversation(conversation);
        message.setUser(user1);
        message.setType(MessageTypes.MESSAGE);

        List<Message> messageList = new ArrayList<>();
        messageList.add(message);
        conversation.setMessages(messageList);

        when(conversationRepository.save(eq(conversation))).thenReturn(conversation);
        when(userRepository.findOne(Matchers.eq(user1.getId()))).thenReturn(user1);
        when(userRepository.findOne(Matchers.eq(user2.getId()))).thenReturn(user2);
        when(properties.getProperty(eq("site.url"))).thenReturn("");

        try {
            conversationService.addConversation(user1.getId(), conversation);
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("resource not found exception");
        }
    }

    @Test(expected=ResourceForbiddenException.class)
    public void testCreateConversation_NoUserFeature() throws ResourceForbiddenException {

        // set up group
        Group testGroup = TestUtils.createGroup("testGroup");
        Feature messagingFeature = TestUtils.createFeature(FeatureType.MESSAGING.toString());
        GroupFeature groupFeature = TestUtils.createGroupFeature(messagingFeature, testGroup);
        testGroup.setGroupFeatures(new HashSet<GroupFeature>());
        testGroup.getGroupFeatures().add(groupFeature);

        // add user1 as specialty admin (no user feature)
        User user1 = TestUtils.createUser("newTestUser1");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, testGroup, user1);
        user1.setGroupRoles(new TreeSet<GroupRole>());
        user1.getGroupRoles().add(groupRole);
        user1.setUserFeatures(new HashSet<UserFeature>());

        // add user2 as unit admin
        User user2 = TestUtils.createUser("newTestUser2");
        Role role2 = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole groupRole2 = TestUtils.createGroupRole(role2, testGroup, user2);
        user2.setGroupRoles(new TreeSet<GroupRole>());
        user2.getGroupRoles().add(groupRole2);
        user2.setUserFeatures(new HashSet<UserFeature>());
        UserFeature userFeature2 = TestUtils.createUserFeature(messagingFeature, user2);
        user2.getUserFeatures().add(userFeature2);

        // authenticate user1
        TestUtils.authenticateTest(user1, user1.getGroupRoles());

        Conversation conversation = new Conversation();
        conversation.setId(3L);
        conversation.setType(ConversationTypes.MESSAGE);

        ConversationUser conversationUser1 = new ConversationUser();
        conversationUser1.setId(4L);
        conversationUser1.setUser(user1);
        conversationUser1.setConversation(conversation);
        conversationUser1.setAnonymous(false);

        ConversationUser conversationUser2 = new ConversationUser();
        conversationUser2.setId(5L);
        conversationUser2.setUser(user2);
        conversationUser2.setConversation(conversation);
        conversationUser2.setAnonymous(false);

        Set<ConversationUser> conversationUserSet = new HashSet<>();
        conversationUserSet.add(conversationUser1);
        conversationUserSet.add(conversationUser2);
        conversation.setConversationUsers(conversationUserSet);

        Message message = new Message();
        message.setId(6L);
        message.setConversation(conversation);
        message.setUser(user1);
        message.setType(MessageTypes.MESSAGE);

        List<Message> messageList = new ArrayList<>();
        messageList.add(message);
        conversation.setMessages(messageList);

        when(conversationRepository.save(eq(conversation))).thenReturn(conversation);
        when(userRepository.findOne(Matchers.eq(user1.getId()))).thenReturn(user1);
        when(userRepository.findOne(Matchers.eq(user2.getId()))).thenReturn(user2);
        when(properties.getProperty(eq("site.url"))).thenReturn("");

        try {
            conversationService.addConversation(user1.getId(), conversation);
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("resource not found exception");
        }
    }

    @Test
    public void testGetUnreadConversationCount() {

        User user1 = TestUtils.createUser("newTestUser1");
        User user2 = TestUtils.createUser("newTestUser2");

        Conversation conversation = new Conversation();
        conversation.setId(3L);
        conversation.setType(ConversationTypes.MESSAGE);

        ConversationUser conversationUser1 = new ConversationUser();
        conversationUser1.setId(4L);
        conversationUser1.setUser(user1);
        conversationUser1.setConversation(conversation);
        conversationUser1.setAnonymous(false);

        ConversationUser conversationUser2 = new ConversationUser();
        conversationUser2.setId(5L);
        conversationUser2.setUser(user2);
        conversationUser2.setConversation(conversation);
        conversationUser2.setAnonymous(false);

        Set<ConversationUser> conversationUserSet = new HashSet<>();
        conversationUserSet.add(conversationUser1);
        conversationUserSet.add(conversationUser2);
        conversation.setConversationUsers(conversationUserSet);

        Message message = new Message();
        message.setId(6L);
        message.setConversation(conversation);
        message.setUser(user1);
        message.setType(MessageTypes.MESSAGE);
        message.setReadReceipts(new HashSet<MessageReadReceipt>());

        List<Message> messageList = new ArrayList<>();
        messageList.add(message);
        conversation.setMessages(messageList);

        when(conversationRepository.save(eq(conversation))).thenReturn(conversation);
        when(userRepository.findOne(Matchers.eq(user1.getId()))).thenReturn(user1);
        when(userRepository.findOne(Matchers.eq(user2.getId()))).thenReturn(user2);

        when(userRepository.exists(Matchers.eq(user1.getId()))).thenReturn(true);
        when(userRepository.exists(Matchers.eq(user2.getId()))).thenReturn(true);

        when(conversationRepository.getUnreadConversationCount(eq(user1.getId()))).thenReturn(1L);

        TestUtils.authenticateTest(user1, new ArrayList<GroupRole>());

        try {
            Assert.assertEquals("Should get 1 unread conversation", (Long)1L,
                    conversationService.getUnreadConversationCount(user1.getId()));
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("resource not found exception");
        }
    }
}
