package org.patientview.api.service.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.client.FirebaseClient;
import org.patientview.api.model.BaseGroup;
import org.patientview.api.model.BaseUser;
import org.patientview.api.model.ExternalConversation;
import org.patientview.api.model.enums.DummyUsernames;
import org.patientview.api.service.ConversationService;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.RoleService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.ApiKey;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.ConversationUser;
import org.patientview.persistence.model.ConversationUserLabel;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.MessageReadReceipt;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.ApiKeyTypes;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.ConversationLabel;
import org.patientview.persistence.model.enums.ConversationTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.MessageTypes;
import org.patientview.persistence.model.enums.PatientMessagingFeatureType;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.model.enums.StaffMessagingFeatureType;
import org.patientview.persistence.repository.ApiKeyRepository;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.persistence.repository.ConversationUserLabelRepository;
import org.patientview.persistence.repository.ConversationUserRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.MessageReadReceiptRepository;
import org.patientview.persistence.repository.MessageRepository;
import org.patientview.persistence.repository.MyMediaRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.service.AuditService;
import org.patientview.util.Util;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.patientview.api.util.ApiUtil.currentUserHasRole;
import static org.patientview.api.util.ApiUtil.getCurrentUser;
import static org.patientview.api.util.ApiUtil.isInEnum;
import static org.patientview.api.util.ApiUtil.userHasRole;

/**
 * Conversation service, for CRUD operations related to Conversations and Messages.
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@Service
public class ConversationServiceImpl extends AbstractServiceImpl<ConversationServiceImpl>
        implements ConversationService {

    @Inject
    private ApiKeyRepository apiKeyRepository;

    @Inject
    private AuditService auditService;

    @Inject
    private ConversationRepository conversationRepository;

    @Inject
    private ConversationUserRepository conversationUserRepository;

    @Inject
    private ConversationUserLabelRepository conversationUserLabelRepository;

    @Inject
    private EmailService emailService;

    @Inject
    private EntityManager entityManager;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupService groupService;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private MessageReadReceiptRepository messageReadReceiptRepository;

    @Inject
    private MessageRepository messageRepository;

    @Inject
    private MyMediaRepository myMediaRepository;

    @Inject
    private Properties properties;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private RoleService roleService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private FirebaseClient notificationClient;

    /**
     * @inheritDoc
     */
    @Override
    public void addConversation(Long userId, Conversation conversation)
            throws ResourceNotFoundException, ResourceForbiddenException {

        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden (current user features)");
        }

        if (!conversationUsersAndGroupsHaveMessagingFeatures(conversation)) {
            throw new ResourceForbiddenException("Forbidden (conversation user group features)");
        }

        User creator = getCurrentUser();
        User entityUser = findEntityUser(userId);

        // handle comments to central PatientView support (sent via standard contact mechanism in UI but does not
        // create a conversation, simply emails address set in properties
        if (conversation.getStaffFeature() != null
                && conversation.getStaffFeature().equals(FeatureType.CENTRAL_SUPPORT_CONTACT)) {
            sendNewCentralSupportEmail(entityUser, conversation);
        } else {
            // create new conversation
            Conversation newConversation = new Conversation();
            newConversation.setTitle(conversation.getTitle());
            newConversation.setImageData(conversation.getImageData());
            newConversation.setOpen(conversation.getOpen());
            newConversation.setRating(conversation.getRating());
            newConversation.setStatus(conversation.getStatus());
            newConversation.setType(conversation.getType());
            newConversation.setStaffFeature(conversation.getStaffFeature());
            newConversation.setGroupId(conversation.getGroupId());
            newConversation.setCreator(conversation.getCreator() == null ? entityUser : conversation.getCreator());

            // get first message from passed in conversation
            Iterator iter = conversation.getMessages().iterator();
            Message message = (Message) iter.next();

            // set message properties and add to conversation
            Message newMessage = new Message();
            newMessage.setUser(entityUser);
            newMessage.setConversation(newConversation);
            newMessage.setMessage(message.getMessage());
            newMessage.setType(message.getType());
            newMessage.setReadReceipts(new HashSet<MessageReadReceipt>());
            newMessage.getReadReceipts().add(new MessageReadReceipt(newMessage, entityUser));
            newMessage.setCreator(newMessage.getCreator() == null ? entityUser : newMessage.getCreator());

            if (message.getMyMedia() != null) {
                MyMedia myMedia = myMediaRepository.findOne(message.getMyMedia().getId());
                if (myMedia.getCreator().getId().equals(ApiUtil.getCurrentUser().getId())) {
                    newMessage.setMyMedia(myMedia);
                    newMessage.setHasAttachment(true);
                } else {
                    throw new ResourceForbiddenException("Forbidden (MyMedia cannot be attached)");
                }
            }


            List<Message> messageSet = new ArrayList<>();
            messageSet.add(newMessage);
            newConversation.setMessages(messageSet);

            // set conversation users
            Set<ConversationUser> conversationUsers
                    = getConversationUsers(conversation.getConversationUsers(), newConversation, creator);
            newConversation.setConversationUsers(conversationUsers);

            // send email notification to conversation users
            sendNewMessageEmails(conversationUsers, conversation.isAnonymous(), entityUser);

            // set updated, used in UI to order conversations
            newConversation.setLastUpdate(new Date());

            // persist conversation
            conversationRepository.save(newConversation);

            // create audit
            if (conversation.getType().equals(ConversationTypes.MEMBERSHIP_REQUEST)
                    && conversation.getUserId() != null && conversation.getGroupId() != null) {
                entityUser = userRepository.findOne(conversation.getUserId());
                Group entityGroup = groupRepository.findOne(conversation.getGroupId());
                if (entityUser != null && entityGroup != null) {
                    auditService.createAudit(AuditActions.MEMBERSHIP_REQUEST_SENT, entityUser.getUsername(),
                            getCurrentUser(), conversation.getUserId(), AuditObjectTypes.User, entityGroup);
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void addConversationToRecipientsByFeature(Long userId, String featureName, Conversation conversation)
            throws ResourceNotFoundException, ResourceForbiddenException, VerificationException {
        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden (current user features)");
        }

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        if (featureName == null) {
            throw new ResourceNotFoundException("Feature not set");
        }

        Feature feature = featureRepository.findByName(featureName);
        if (feature == null) {
            throw new ResourceNotFoundException("Feature not found");
        }

        if (conversation == null) {
            throw new VerificationException("Conversation not set");
        }

        if (CollectionUtils.isEmpty(conversation.getMessages())) {
            throw new VerificationException("Conversation has no messages");
        }

        if (StringUtils.isEmpty(conversation.getMessages().get(0).getMessage())) {
            throw new VerificationException("Message is empty");
        }

        // get recipients
        List<Long> featureIds = new ArrayList<>();
        featureIds.add(feature.getId());

        // get non specialty UNIT and DISEASE_GROUP groups that a user is in
        List<Long> groupIds = new ArrayList<>();
        for (GroupRole groupRole : user.getGroupRoles()) {
            String groupType = groupRole.getGroup().getGroupType().getValue();
            if (groupType.equals(GroupTypes.UNIT.toString())
                    || groupType.equals(GroupTypes.DISEASE_GROUP.toString())) {
                groupIds.add(groupRole.getGroup().getId());
            }
        }

        // staff roles
        List<Role> staffRoles = roleService.getRolesByType(RoleType.STAFF);
        List<Long> roleIds = new ArrayList<>();
        for (Role role : staffRoles) {
            roleIds.add(role.getId());
        }

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);
        Page<User> page = userRepository.findStaffByGroupsRolesFeatures("%%", groupIds, roleIds, featureIds, pageable);

        if (CollectionUtils.isEmpty(page.getContent())) {
            throw new VerificationException("No staff exist to receive message");
        }

        // add found staff as conversation users
        conversation.setConversationUsers(new HashSet<ConversationUser>());
        for (User staffUser : page.getContent()) {
            conversation.getConversationUsers().add(new ConversationUser(conversation, staffUser));
        }

        // add current user as conversation user
        conversation.getConversationUsers().add(new ConversationUser(conversation, user));

        addConversation(userId, conversation);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void addConversationUser(Long conversationId, Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden");
        }

        Conversation conversation = conversationRepository.findOne(conversationId);
        if (conversation == null) {
            throw new ResourceNotFoundException("Conversation not found");
        }

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        boolean found = false;
        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            if (conversationUser.getUser().getId().equals(userId)) {
                found = true;
            }
        }

        if (!found) {
            ConversationUser conversationUser = new ConversationUser();
            conversationUser.setUser(user);
            conversationUser.setCreator(getCurrentUser());
            conversationUser.setCreated(new Date());
            conversationUser.setConversation(conversation);
            conversationUser.setConversationUserLabels(new HashSet<ConversationUserLabel>());
            conversationUser.setAnonymous(false);

            ConversationUserLabel newConversationUserLabel = new ConversationUserLabel();
            newConversationUserLabel.setConversationUser(conversationUser);
            newConversationUserLabel.setConversationLabel(ConversationLabel.INBOX);
            newConversationUserLabel.setCreated(new Date());
            newConversationUserLabel.setCreator(getCurrentUser());
            conversationUser.getConversationUserLabels().add(newConversationUserLabel);

            conversation.getConversationUsers().add(conversationUser);
            conversationRepository.save(conversation);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void addConversationUserLabel(Long userId, Long conversationId, ConversationLabel conversationLabel)
            throws ResourceNotFoundException, ResourceForbiddenException {
        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden");
        }

        Conversation conversation = conversationRepository.findOne(conversationId);

        if (conversation == null) {
            throw new ResourceNotFoundException("Conversation does not exist");
        }

        if (!loggedInUserIsMemberOfConversation(conversation)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            if (conversationUser.getUser().getId().equals(userId)) {
                boolean found = false;

                // check existing label does not already exist for this user
                if (!CollectionUtils.isEmpty(conversationUser.getConversationUserLabels())) {
                    for (ConversationUserLabel conversationUserLabel : conversationUser.getConversationUserLabels()) {
                        if (conversationUserLabel.getConversationLabel().equals(conversationLabel)) {
                            found = true;
                        }
                    }
                } else {
                    conversationUser.setConversationUserLabels(new HashSet<ConversationUserLabel>());
                }

                // if label doesn't already exist, add it to the ConversationUser
                if (!found) {
                    ConversationUserLabel newConversationUserLabel = new ConversationUserLabel();
                    newConversationUserLabel.setConversationUser(conversationUser);
                    newConversationUserLabel.setConversationLabel(conversationLabel);
                    newConversationUserLabel.setCreated(new Date());
                    newConversationUserLabel.setCreator(getCurrentUser());
                    conversationUser.getConversationUserLabels().add(newConversationUserLabel);
                }
            }
        }

        conversationRepository.save(conversation);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ExternalConversation addExternalConversation(ExternalConversation conversation) {
        // validate essential properties are present and token is correct
        if (StringUtils.isEmpty(conversation.getToken())) {
            return rejectExternalConversation("no token", conversation);
        }

        Date now = new Date();

        // validate api key
        List<ApiKey> apiKeys
                = apiKeyRepository.findByKeyAndType(conversation.getToken(), ApiKeyTypes.EXTERNAL_CONVERSATION);

        if (CollectionUtils.isEmpty(apiKeys)) {
            throw new AuthenticationServiceException("token not found");
        }

        // check not expired
        boolean validApiKey = false;
        if (!CollectionUtils.isEmpty(apiKeys)) {
            for (ApiKey apiKeyEntity : apiKeys) {
                if (apiKeyEntity.getExpiryDate() == null) {
                    validApiKey = true;
                } else if (apiKeyEntity.getExpiryDate().getTime() > now.getTime()) {
                    validApiKey = true;
                }
            }
        }

        if (!validApiKey) {
            throw new AuthenticationServiceException("token has expired");
        }

        if (StringUtils.isEmpty(conversation.getMessage())) {
            return rejectExternalConversation("no message", conversation);
        }
        if (StringUtils.isEmpty(conversation.getTitle())) {
            return rejectExternalConversation("no title", conversation);
        }

        if (StringUtils.isEmpty(conversation.getIdentifier())
                && StringUtils.isEmpty(conversation.getRecipientUsername())
                && StringUtils.isEmpty(conversation.getGroupCode())) {
            return rejectExternalConversation(
                    "must set identifier, recipient username or group code", conversation);
        }

        Set<User> recipients = new HashSet<>();
        User sender = null;

        // if individual user, identifier based, check that other fields not present
        if (StringUtils.isNotEmpty(conversation.getIdentifier())) {
            if (StringUtils.isNotEmpty(conversation.getRecipientUsername())) {
                return rejectExternalConversation(
                        "should not set identifier and recipient username", conversation);
            }
            if (StringUtils.isNotEmpty(conversation.getGroupCode())) {
                return rejectExternalConversation(
                        "should not set identifier and group code", conversation);
            }
            if (StringUtils.isNotEmpty(conversation.getUserFeature())) {
                return rejectExternalConversation(
                        "should not set identifier and user feature", conversation);
            }

            List<Identifier> identifiers = identifierRepository.findByValue(conversation.getIdentifier());
            if (CollectionUtils.isEmpty(identifiers)) {
                return rejectExternalConversation(
                        "identifier not found", conversation);
            }
            recipients.add(identifiers.get(0).getUser());
        }

        // if individual user, username based, check that other fields not present
        if (StringUtils.isNotEmpty(conversation.getRecipientUsername())) {
            if (StringUtils.isNotEmpty(conversation.getIdentifier())) {
                return rejectExternalConversation(
                        "should not set recipient username and identifier", conversation);
            }
            if (StringUtils.isNotEmpty(conversation.getGroupCode())) {
                return rejectExternalConversation(
                        "should not set recipient username and group code", conversation);
            }
            if (StringUtils.isNotEmpty(conversation.getUserFeature())) {
                return rejectExternalConversation(
                        "should not set recipient username and user feature", conversation);
            }

            // find recipient
            User recipient = userRepository.findByUsernameCaseInsensitive(conversation.getRecipientUsername());
            if (recipient == null) {
                return rejectExternalConversation("recipient username not found", conversation);
            }
            recipients.add(recipient);
        }

        // if group code set, check other fields not present and suitable user feature set
        if (StringUtils.isNotEmpty(conversation.getGroupCode())) {
            if (StringUtils.isNotEmpty(conversation.getIdentifier())) {
                return rejectExternalConversation(
                        "should not set group code and identifier", conversation);
            }
            if (StringUtils.isNotEmpty(conversation.getRecipientUsername())) {
                return rejectExternalConversation(
                        "should not set group code and recipient username", conversation);
            }
            if (StringUtils.isEmpty(conversation.getUserFeature())) {
                return rejectExternalConversation(
                        "if group code set, must set user feature", conversation);
            }
            if (!isInEnum(conversation.getUserFeature(), FeatureType.class)) {
                return rejectExternalConversation(
                        "if group code set, must set suitable user feature", conversation);
            }

            // find group
            Group group = groupRepository.findByCode(conversation.getGroupCode());
            if (group == null) {
                return rejectExternalConversation("group not found", conversation);
            }

            // find feature
            Feature feature = featureRepository.findByName(conversation.getUserFeature());
            if (feature == null) {
                return rejectExternalConversation("feature not found", conversation);
            }

            // find users in group with feature
            List<User> users = userRepository.findByGroupAndFeature(group, feature);

            // if MESSAGING feature, all patients have MESSAGING so add them all to recipient list
            if (feature.getName().equals(FeatureType.MESSAGING.toString())) {
                Role patientRole = roleRepository.findByRoleTypeAndName(RoleType.PATIENT, RoleName.PATIENT);
                if (patientRole == null) {
                    return rejectExternalConversation("patient role not found", conversation);
                }
                Page<User> patientPage = userRepository.findPatientByGroupsRolesNoFilter(
                        Arrays.asList(new Long[]{group.getId()}),
                        Arrays.asList(new Long[]{patientRole.getId()}), new PageRequest(0, Integer.MAX_VALUE));

                if (!CollectionUtils.isEmpty(patientPage.getContent())) {
                    users.addAll(patientPage.getContent());
                }
            }

            if (CollectionUtils.isEmpty(users)) {
                return rejectExternalConversation("no users found with feature in group", conversation);
            }
            recipients.addAll(users);
        }

        // check there are recipients
        if (CollectionUtils.isEmpty(recipients)) {
            return rejectExternalConversation("no suitable recipients", conversation);
        }

        // if sender username is set, make sure exists
        if (StringUtils.isNotEmpty(conversation.getSenderUsername())) {
            sender = userRepository.findByUsernameCaseInsensitive(conversation.getSenderUsername());
            if (sender == null) {
                return rejectExternalConversation("sender username not found", conversation);
            }
            recipients.add(sender);
        }

        // create conversation
        Conversation newConversation = new Conversation();
        newConversation.setTitle(conversation.getTitle());
        newConversation.setConversationUsers(new HashSet<ConversationUser>());
        newConversation.setMessages(new ArrayList<Message>());
        newConversation.setLastUpdate(now);
        newConversation.setType(ConversationTypes.MESSAGE);
        newConversation.setOpen(true);

        // add recipients
        for (User recipient : recipients) {
            ConversationUser conversationUser = new ConversationUser(newConversation, recipient);
            conversationUser.setAnonymous(false);
            conversationUser.setConversationUserLabels(new HashSet<ConversationUserLabel>());
            conversationUser.getConversationUserLabels().add(
                    new ConversationUserLabel(conversationUser, ConversationLabel.INBOX));

            newConversation.getConversationUsers().add(conversationUser);
        }

        // check all recipients can receive messages
        if (!conversationUsersAndGroupsHaveMessagingFeatures(newConversation)) {
            String senderString = sender != null ? " or sender" : "";
            return rejectExternalConversation("recipients" + senderString + " are not messaging enabled", conversation);
        }

        // add dummy notification user if no sender
        if (sender == null) {
            User notificationUser = userRepository.findByUsernameCaseInsensitive(
                    DummyUsernames.PATIENTVIEW_NOTIFICATIONS.getName());
            if (notificationUser == null) {
                return rejectExternalConversation(
                        "no sender set, but default notification user not found", conversation);
            }
            ConversationUser conversationUser = new ConversationUser(newConversation, notificationUser);
            conversationUser.setAnonymous(false);
            conversationUser.setConversationUserLabels(new HashSet<ConversationUserLabel>());
            conversationUser.getConversationUserLabels().add(
                    new ConversationUserLabel(conversationUser, ConversationLabel.INBOX));

            newConversation.getConversationUsers().add(conversationUser);

            sender = notificationUser;
        }

        // add message
        Message message = new Message();
        message.setMessage(conversation.getMessage());
        message.setType(MessageTypes.MESSAGE);
        message.setConversation(newConversation);
        message.setUser(sender);
        newConversation.getMessages().add(message);

        // persist
        conversationRepository.save(newConversation);
        messageRepository.save(message);

        // return success
        conversation.setSuccess(true);
        return conversation;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void addMessage(Long conversationId, org.patientview.api.model.Message message)
            throws ResourceNotFoundException, ResourceForbiddenException {
        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden");
        }

        Conversation entityConversation = conversationRepository.findOne(conversationId);
        if (entityConversation == null) {
            throw new ResourceNotFoundException(String.format("Could not find conversation %s", conversationId));
        }

        if (!loggedInUserIsMemberOfConversation(entityConversation)) {
            throw new ResourceForbiddenException("You do not have permission");
        }

        User entityUser = findEntityUser(message.getUser().getId());

        Message newMessage = new Message();
        newMessage.setUser(entityUser);
        newMessage.setConversation(entityConversation);
        newMessage.setMessage(message.getMessage());
        newMessage.setType(message.getType());

        if (message.getMyMedia() != null) {
            MyMedia myMedia = myMediaRepository.findOne(message.getMyMedia().getId());

            //Only allow the owner to attach the media to a conversation
            if (entityUser.getId().equals(myMedia.getCreator().getId())) {
                newMessage.setHasAttachment(true);
                newMessage.setMyMedia(myMedia);
            }
        }
        messageRepository.save(newMessage);

        newMessage.setReadReceipts(new HashSet<MessageReadReceipt>());
        newMessage.getReadReceipts().add(new MessageReadReceipt(newMessage, entityUser));

        entityConversation.getMessages().add(newMessage);
        entityConversation.setLastUpdate(new Date());
        entityConversation = conversationRepository.save(entityConversation);

        // change any ARCHIVE conversation user labels to INBOX
        for (ConversationUser conversationUser : entityConversation.getConversationUsers()) {
            for (ConversationUserLabel conversationUserLabel : conversationUser.getConversationUserLabels()) {
                if (conversationUserLabel.getConversationLabel().equals(ConversationLabel.ARCHIVED)) {
                    conversationUserLabel.setConversationLabel(ConversationLabel.INBOX);
                    conversationUserLabelRepository.save(conversationUserLabel);
                }
            }
        }

        // send email notification to conversation users
        sendNewMessageEmails(entityConversation.getConversationUsers(), false, entityUser);

        // send push notification for mobile users
        sendNewMessageNotification(entityConversation);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void addMessageReadReceipt(Long messageId, Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        User entityUser = findEntityUser(userId);

        Message entityMessage = messageRepository.findOne(messageId);
        if (entityMessage == null) {
            throw new ResourceNotFoundException(String.format("Could not find message %s", messageId));
        }

        // can only add read receipts for own user
        if (!getCurrentUser().equals(entityUser)) {
            throw new ResourceForbiddenException("Cannot add read receipts for other users");
        }

        boolean found = false;
        for (MessageReadReceipt messageReadReceipt : entityMessage.getReadReceipts()) {
            if (messageReadReceipt.getUser().equals(entityUser)) {
                found = true;
            }
        }

        if (!found) {
            entityMessage.getReadReceipts().add(new MessageReadReceipt(entityMessage, entityUser));
            messageRepository.save(entityMessage);
        }
    }

    /**
     * Anonymise Conversation by replacing Users who wish to remain anonymous with a dummy user if required.
     *
     * @param conversation Conversation to anonymise (if required)
     * @return Conversation where Users have been made anonymous (if required)
     */
    private Conversation anonymiseConversation(Conversation conversation) {
        Conversation newConversation = new Conversation();
        newConversation.setConversationUsers(new HashSet<ConversationUser>());
        List<Long> anonUserIds = new ArrayList<>();
        User anonUser = new User();
        anonUser.setForename("Anonymous");
        anonUser.setSurname("User");
        anonUser.setId(-1L);

        // anonymise conversation users
        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            if (conversationUser.getAnonymous()) {
                anonUserIds.add(conversationUser.getUser().getId());

                ConversationUser anonConversationUser = new ConversationUser();
                anonConversationUser.setAnonymous(true);
                anonConversationUser.setUser(anonUser);
                anonConversationUser.getUser().setId(conversationUser.getUser().getId());
                anonConversationUser.setConversation(conversation);
                anonConversationUser.setConversationUserLabels(conversationUser.getConversationUserLabels());
                newConversation.getConversationUsers().add(anonConversationUser);
            } else {
                // validate if current user can switch to this user (used to allow view patient)
                conversationUser.setCanSwitchUser(userService.currentUserCanSwitchToUser(conversationUser.getUser()));
                newConversation.getConversationUsers().add(conversationUser);
            }
        }

        List<Message> newMessages = new ArrayList<>();

        // anonymise messages
        for (Message message : conversation.getMessages()) {
            Message newMessage = new Message();
            newMessage.setId(message.getId());
            newMessage.setConversation(newConversation);
            newMessage.setType(message.getType());
            newMessage.setMessage(message.getMessage());
            newMessage.setCreated(message.getCreated());
            newMessage.setMyMedia(message.getMyMedia());
            newMessage.setHasAttachment(message.getHasAttachment());

            if (message.getUser() != null) {
                if (anonUserIds.contains(message.getUser().getId())) {
                    newMessage.setUser(anonUser);
                } else {
                    message.getUser().setCanSwitchUser(
                            userService.currentUserCanSwitchToUser(message.getUser())
                                    && !getCurrentUser().equals(message.getUser()));
                    newMessage.setUser(message.getUser());
                }
            }

            // anonymise read receipts (include user id for creating read receipts in future)
            Set<MessageReadReceipt> readReceipts = new HashSet<>();
            for (MessageReadReceipt readReceipt : message.getReadReceipts()) {
                Long userId = readReceipt.getUser().getId();
                MessageReadReceipt messageReadReceipt = new MessageReadReceipt();
                if (anonUserIds.contains(userId)) {
                    messageReadReceipt.setUser(anonUser);
                    messageReadReceipt.getUser().setId(userId);
                } else {
                    messageReadReceipt.setUser(readReceipt.getUser());
                }
                readReceipts.add(messageReadReceipt);
            }

            newMessage.setReadReceipts(readReceipts);
            newMessages.add(newMessage);
        }

        // sort messages
        Collections.sort(newMessages, new Comparator<Message>() {
            public int compare(Message m1, Message m2) {
                return m1.getCreated().compareTo(m2.getCreated());
            }
        });

        newConversation.setMessages(newMessages);
        newConversation.setType(conversation.getType());
        newConversation.setStatus(conversation.getStatus());
        newConversation.setImageData(conversation.getImageData());
        newConversation.setOpen(conversation.getOpen());
        newConversation.setTitle(conversation.getTitle());
        newConversation.setRating(conversation.getRating());
        newConversation.setId(conversation.getId());
        return newConversation;
    }

    /**
     * Verify all conversation users have messaging features and member of group with messaging enabled
     *
     * @param conversation Conversation to verify
     * @return true if all conversation users have messaging features and member of group with messaging enabled
     */
    private boolean conversationUsersAndGroupsHaveMessagingFeatures(Conversation conversation) {
        int usersWithMessagingFeaturesCount = 0;

        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            User user = userRepository.findOne(conversationUser.getUser().getId());

            // GLOBAL_ADMIN and PATIENT users always have messaging features
            if (userHasRole(user, RoleName.GLOBAL_ADMIN, RoleName.PATIENT)) {
                usersWithMessagingFeaturesCount++;
            } else if (userHasStaffMessagingFeatures(user)) {
                usersWithMessagingFeaturesCount++;
            }

            // check conversation user member of at least one group with messaging enabled
            if (!userHasRole(user, RoleName.GLOBAL_ADMIN, RoleName.PATIENT)
                    && !userGroupsHaveMessagingFeature(user)) {
                return false;
            }
        }

        return (conversation.getConversationUsers().size() == usersWithMessagingFeaturesCount);
    }

    /**
     * Return true if a set of ConversationUser contains User.
     *
     * @param conversationUserSet Set of ConversationUser to find User in
     * @param user                User to find
     * @return true if User found in Set of ConversationUser
     */
    private boolean conversationUsersContainsUser(Set<ConversationUser> conversationUserSet, User user) {
        for (ConversationUser conversationUser : conversationUserSet) {
            if (conversationUser.getUser().equals(user)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert a List of User to BaseUser.
     *
     * @param users List of Users to convert
     * @return List of BaseUser
     */
    private List<org.patientview.api.model.BaseUser> convertUsersToBaseUsers(List<User> users) {
        List<org.patientview.api.model.BaseUser> transportUsers = new ArrayList<>();

        for (User user : users) {
            // do not allow users to talk to themselves
            if (!getCurrentUser().getId().equals(user.getId())) {
                transportUsers.add(new org.patientview.api.model.BaseUser(user));
            }
        }

        return transportUsers;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void deleteUserFromConversations(User user) {
        // remove from all conversations where user is a member (including messages)
        List<Conversation> conversations
                = conversationRepository.findByUser(user, new PageRequest(0, Integer.MAX_VALUE)).getContent();

        LOG.info("user id: " + user.getId() + " has " + conversations.size() + " conversations");

        // required if previously failed to cleanly delete conversation user labels (RPV-582)
        List<ConversationUserLabel> conversationUserLabels = conversationUserLabelRepository.findByUser(user);
        for (ConversationUserLabel conversationUserLabel : conversationUserLabels) {
            conversationUserLabelRepository.delete(conversationUserLabel);
        }
        conversationUserLabels = conversationUserLabelRepository.findByCreator(user);
        for (ConversationUserLabel conversationUserLabel : conversationUserLabels) {
            conversationUserLabelRepository.delete(conversationUserLabel);
        }

        for (Conversation conversation : conversations) {
            // remove from conversation user list
            LOG.info("conversation id: " + conversation.getId() + ", remove user id: " + user.getId()
                    + " from conversation user list");
            Set<ConversationUser> removedUserConversationUsers = new HashSet<>();
            for (ConversationUser conversationUser : conversation.getConversationUsers()) {
                if (!conversationUser.getUser().getId().equals(user.getId())) {

                    // remove as creator if set
                    if (conversationUser.getCreator() != null
                            && conversationUser.getCreator().getId().equals(user.getId())) {
                        conversationUser.setCreator(null);
                    }
                    if (conversationUser.getLastUpdater() != null
                            && conversationUser.getLastUpdater().getId().equals(user.getId())) {
                        conversationUser.setLastUpdater(null);
                    }

                    removedUserConversationUsers.add(conversationUser);
                } else {
                    // remove conversation user labels
                    for (ConversationUserLabel conversationUserLabel : conversationUser.getConversationUserLabels()) {
                        conversationUserLabelRepository.delete(conversationUserLabel.getId());
                    }
                    conversationUser.setConversationUserLabels(new HashSet<ConversationUserLabel>());
                    conversationUserRepository.save(conversationUser);
                    conversationUserRepository.delete(conversationUser);
                }
            }
            conversation.setConversationUsers(removedUserConversationUsers);

            // remove from messages
            LOG.info("conversation id: " + conversation.getId() + ", remove user id: " + user.getId()
                    + " from messages");
            List<Message> removedUserMessages = new ArrayList<>();
            for (Message message : conversation.getMessages()) {
                if (message.getUser().getId().equals(user.getId())) {
                    LOG.info("message id: " + message.getId() + " set user null");
                    message.setUser(null);
                }
                if (message.getLastUpdater() != null && message.getLastUpdater().getId().equals(user.getId())) {
                    LOG.info("message id: " + message.getId() + " set last updater null");
                    message.setLastUpdater(null);
                }
                if (message.getCreator() != null && message.getCreator().getId().equals(user.getId())) {
                    LOG.info("message id: " + message.getId() + " set creator null");
                    message.setCreator(null);
                }

                // remove read receipts for this user
                Set<MessageReadReceipt> removedUserMessageReadReceipts = new HashSet<>();
                for (MessageReadReceipt messageReadReceipt : message.getReadReceipts()) {
                    if (!messageReadReceipt.getUser().getId().equals(user.getId())) {
                        removedUserMessageReadReceipts.add(messageReadReceipt);
                    } else {
                        messageReadReceiptRepository.delete(messageReadReceipt);
                    }
                }
                message.setReadReceipts(removedUserMessageReadReceipts);

                removedUserMessages.add(message);
            }

            LOG.info("conversation id: " + conversation.getId() + ", save " + removedUserMessages.size() + " messages");

            messageRepository.save(removedUserMessages);
            conversation.setMessages(removedUserMessages);

            if (conversation.getCreator() != null && conversation.getCreator().getId().equals(user.getId())) {
                conversation.setCreator(null);
            }
            conversationRepository.save(conversation);
        }

        user.setConversationUsers(new HashSet<ConversationUser>());
        userRepository.save(user);
    }

    /**
     * @inheritDoc
     */
    @Override
    public org.patientview.api.model.Conversation findByConversationId(Long conversationId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden");
        }

        Conversation conversation = conversationRepository.findOne(conversationId);

        if (conversation == null) {
            throw new ResourceNotFoundException("Conversation does not exist");
        }

        if (!loggedInUserIsMemberOfConversation(conversation)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return new org.patientview.api.model.Conversation(anonymiseConversation(conversation));
    }

    /**
     * @inheritDoc
     */
    @Override
    public Page<org.patientview.api.model.Conversation> findByUserId(Long userId, GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException {
        User entityUser = findEntityUser(userId);

        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden");
        }

        // pagination page number and size
        Integer pageConverted = (StringUtils.isNotEmpty(getParameters.getPage()))
                ? Integer.parseInt(getParameters.getPage()) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(getParameters.getSize()))
                ? Integer.parseInt(getParameters.getSize()) : Integer.MAX_VALUE;
        PageRequest pageable = new PageRequest(pageConverted, sizeConverted);

        // Building String query manually rather than using pre-defined repository method for potential complex queries
        // in future
        boolean labelsSet = getParameters.getConversationLabels() != null
                && getParameters.getConversationLabels().length > 0;
        boolean filterTextSet = StringUtils.isNotEmpty(getParameters.getFilterText());

        StringBuilder sql = new StringBuilder();

        sql.append("FROM Conversation c ");
        sql.append("JOIN c.conversationUsers cu ");
        if (labelsSet) {
            sql.append("JOIN cu.conversationUserLabels cul ");
        }
        if (filterTextSet) {
            sql.append("JOIN c.messages m ");
            sql.append("JOIN c.conversationUsers cu2 ");
        }
        sql.append("WHERE cu.conversation = c ");
        sql.append("AND cu.user = :user ");
        if (labelsSet) {
            sql.append("AND cul.conversationLabel IN :conversationLabels ");
        }

        // search term
        if (filterTextSet) {
            sql.append("AND (UPPER(c.title) LIKE '%");
            sql.append(getParameters.getFilterText().toUpperCase());
            sql.append("%' OR UPPER(m.message) LIKE '%");
            sql.append(getParameters.getFilterText().toUpperCase());
            sql.append("%' OR UPPER(cu2.user.forename) LIKE '%");
            sql.append(getParameters.getFilterText().toUpperCase());
            sql.append("%' OR UPPER(cu2.user.surname) LIKE '%");
            sql.append(getParameters.getFilterText().toUpperCase());
            sql.append("%') ");
        }

        // two queries required, one for content, one for count used in pagination total
        Query listQuery
                = entityManager.createQuery("SELECT distinct(c) " + sql.toString() + "ORDER BY c.lastUpdate DESC ");
        Query countQuery = entityManager.createQuery("SELECT distinct(c.id) " + sql.toString());

        listQuery.setParameter("user", entityUser);
        countQuery.setParameter("user", entityUser);

        if (labelsSet) {
            List<ConversationLabel> conversationLabels = new ArrayList<>();
            for (String conversationLabel : getParameters.getConversationLabels()) {
                conversationLabels.add(ConversationLabel.valueOf(conversationLabel));
            }
            listQuery.setParameter("conversationLabels", conversationLabels);
            countQuery.setParameter("conversationLabels", conversationLabels);
        }

        listQuery.setMaxResults(sizeConverted);

        if (pageConverted == 0) {
            listQuery.setFirstResult(0);
        } else {
            listQuery.setFirstResult((sizeConverted * (pageConverted + 1)) - sizeConverted);
        }

        List<Conversation> conversationList = listQuery.getResultList();
        List<org.patientview.api.model.Conversation> conversations = new ArrayList<>();

        // make anonymous if necessary
        for (Conversation conversation : conversationList) {
            conversations.add(new org.patientview.api.model.Conversation(anonymiseConversation(conversation)));
        }

        return new PageImpl<>(conversations, pageable, countQuery.getResultList().size());
    }

    /**
     * Helper method to get a User given their id.
     *
     * @param userId ID of User to get
     * @return User object
     * @throws ResourceNotFoundException
     */
    private User findEntityUser(Long userId) throws ResourceNotFoundException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException(String.format("Could not find user %s", userId));
        }
        return entityUser;
    }

    /**
     * @inheritDoc
     */
    @Override
    public byte[] getConversationUserPicture(Long conversationId, Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        // anonymous users have id of -1
        if (userId == -1) {
            return null;
        }

        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden");
        }

        Conversation conversation = conversationRepository.findOne(conversationId);
        if (conversation == null) {
            throw new ResourceNotFoundException("Conversation not found");
        }

        if (!loggedInUserIsMemberOfConversation(conversation)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        User entityUser = findEntityUser(userId);
        boolean found = false;

        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            if (conversationUser.getUser().getId().equals(userId)) {
                found = true;
            }
        }

        if (!found) {
            throw new ResourceForbiddenException("User does not belong to conversation");
        }

        if (StringUtils.isNotEmpty(entityUser.getPicture())) {
            return Base64.decodeBase64(entityUser.getPicture());
        } else {
            return null;
        }
    }

    /**
     * Create a new Set of ConversationUser given ConversationUsers and Conversation type (may need to add more Users)
     *
     * @param conversationUsers Set of Conversation Users to add to
     * @param conversation      Conversation with type determining addition of new ConversationUsers
     * @param creator           User creating new ConversationUsers
     * @return Set of ConversationUser
     * @throws ResourceNotFoundException
     */
    private Set<ConversationUser> getConversationUsers(Set<ConversationUser> conversationUsers,
                                                       Conversation conversation, User creator)
            throws ResourceNotFoundException {
        Set<ConversationUser> conversationUserSet = new HashSet<>();

        for (ConversationUser conversationUser : conversationUsers) {
            ConversationUser newConversationUser = new ConversationUser();
            newConversationUser.setConversation(conversation);
            newConversationUser.setUser(userRepository.findOne(conversationUser.getUser().getId()));
            newConversationUser.setAnonymous(conversationUser.getAnonymous() == null
                    ? false : conversationUser.getAnonymous());
            newConversationUser.setCreator(creator);
            newConversationUser.setConversationUserLabels(new HashSet<ConversationUserLabel>());
            conversationUserSet.add(newConversationUser);
        }

        // now handle contacting unit staff
        if (conversation.getType().equals(ConversationTypes.CONTACT_UNIT)) {
            if (conversation.getGroupId() == null) {
                throw new ResourceNotFoundException("Missing Group ID parameter when sending message");
            }
            if (conversation.getStaffFeature() == null) {
                throw new ResourceNotFoundException("Missing Staff feature parameter when sending message");
            }

            Group entityGroup = groupRepository.findOne(conversation.getGroupId());
            Feature entityFeature = featureRepository.findByName(conversation.getStaffFeature().toString());

            if (entityGroup == null) {
                throw new ResourceNotFoundException("Could not find Group when sending message");
            }
            if (entityFeature == null) {
                throw new ResourceNotFoundException("Could not find Staff feature when sending message");
            }

            List<User> staffUsers = new ArrayList<>();

            // if need unit technical contact. if no unit technical contact, try patient support contact
            if (conversation.getStaffFeature().equals(FeatureType.UNIT_TECHNICAL_CONTACT)) {
                staffUsers = userRepository.findByGroupAndFeature(entityGroup, entityFeature);
                if (staffUsers.isEmpty()) {
                    staffUsers = userRepository.findByGroupAndFeature(entityGroup
                            , featureRepository.findByName(FeatureType.PATIENT_SUPPORT_CONTACT.toString()));
                }
            }

            // if need patient support contact
            if (conversation.getStaffFeature().equals(FeatureType.PATIENT_SUPPORT_CONTACT)) {
                staffUsers = userRepository.findByGroupAndFeature(entityGroup, entityFeature);
            }

            // if empty then try default messaging contact
            if (staffUsers.isEmpty()) {
                staffUsers = userRepository.findByGroupAndFeature(entityGroup
                        , featureRepository.findByName(FeatureType.DEFAULT_MESSAGING_CONTACT.toString()));
            }

            if (staffUsers.isEmpty()) {
                throw new ResourceNotFoundException("No support staff available to send message");
            }

            // add found staff to conversation if not already
            for (User user : staffUsers) {
                if (!conversationUsersContainsUser(conversationUserSet, user)) {
                    ConversationUser newConversationUser = new ConversationUser();
                    newConversationUser.setConversation(conversation);
                    newConversationUser.setUser(user);
                    newConversationUser.setAnonymous(false);
                    newConversationUser.setCreator(creator);
                    conversationUserSet.add(newConversationUser);
                }
            }
        }

        // add INBOX conversation user label for all ConversationUser
        for (ConversationUser conversationUser : conversationUserSet) {
            ConversationUserLabel conversationUserLabel = new ConversationUserLabel();
            conversationUserLabel.setConversationUser(conversationUser);
            conversationUserLabel.setCreated(new Date());
            conversationUserLabel.setCreator(creator);
            conversationUserLabel.setConversationLabel(ConversationLabel.INBOX);

            if (CollectionUtils.isEmpty(conversationUser.getConversationUserLabels())) {
                conversationUser.setConversationUserLabels(new HashSet<ConversationUserLabel>());
            }

            conversationUser.getConversationUserLabels().add(conversationUserLabel);
        }

        return conversationUserSet;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<BaseUser> getGroupRecipientsByFeature(Long groupId, String featureName)
            throws ResourceNotFoundException, ResourceForbiddenException {
        if (!groupRepository.exists(groupId)) {
            throw new ResourceNotFoundException("Group not found");
        }

        Feature feature = featureRepository.findByName(featureName);
        if (feature == null) {
            throw new ResourceNotFoundException("Feature not found");
        }

        // only users with certain roles
        if (!(currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || currentUserHasRole(RoleName.UNIT_ADMIN)
                || currentUserHasRole(RoleName.SPECIALTY_ADMIN))) {
            throw new ResourceForbiddenException("Forbidden");
        }

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(groupId);

        List<Long> featureIds = new ArrayList<>();
        featureIds.add(feature.getId());

        List<Role> staffRoles = roleService.getRolesByType(RoleType.STAFF);
        List<Long> roleIds = new ArrayList<>();
        for (Role role : staffRoles) {
            roleIds.add(role.getId());
        }

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);
        Page<User> page = userRepository.findStaffByGroupsRolesFeatures("%%", groupIds, roleIds, featureIds, pageable);
        return convertUsersToBaseUsers(page.getContent());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Long getStaffRecipientCountByFeature(Long userId, String featureName) throws ResourceNotFoundException {
        User user = findEntityUser(userId);

        // feature
        Feature feature = featureRepository.findByName(featureName);
        if (feature == null) {
            throw new ResourceNotFoundException("Feature not found");
        }
        List<Long> featureIds = new ArrayList<>();
        featureIds.add(feature.getId());

        // get non specialty UNIT and DISEASE_GROUP groups that a user is in
        List<Long> groupIds = new ArrayList<>();
        for (GroupRole groupRole : user.getGroupRoles()) {
            String groupType = groupRole.getGroup().getGroupType().getValue();
            if (groupType.equals(GroupTypes.UNIT.toString())
                    || groupType.equals(GroupTypes.DISEASE_GROUP.toString())) {
                groupIds.add(groupRole.getGroup().getId());
            }
        }

        // staff roles
        List<Role> staffRoles = roleService.getRolesByType(RoleType.STAFF);
        List<Long> roleIds = new ArrayList<>();
        for (Role role : staffRoles) {
            roleIds.add(role.getId());
        }

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);
        Page<User> page = userRepository.findStaffByGroupsRolesFeatures("%%", groupIds, roleIds, featureIds, pageable);

        Long count = 0L;

        // validate that users are members of groups with MESSAGING etc
        if (!CollectionUtils.isEmpty(page.getContent())) {
            for (User staffUser : page.getContent()) {
                if (userGroupsHaveMessagingFeature(staffUser)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message getMessageById(Long messageId) throws ResourceNotFoundException, ResourceForbiddenException {
        Message message = messageRepository.findOne(messageId);

        //check if message has permission
        this.findByConversationId(message.getConversation().getId());

        return message;
    }

    /**
     * Get a Map of BaseUsers organised by Role type for global admins, used for potential Conversation recipients.
     *
     * @param groupId ID of Group to get recipients for (optional, will get for all Groups if null)
     * @return Map of BaseUsers organised by Role type
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    private HashMap<String, List<BaseUser>> getGlobalAdminRecipients(Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        HashMap<String, List<BaseUser>> userMap = new HashMap<>();

        GetParameters getParameters = new GetParameters();
        getParameters.setSortField("surname");
        getParameters.setSortDirection("ASC");
        List<String> groupIdList = new ArrayList<>();

        // #310 QA: On selecting a specialty, it should only list users who are in the specialty admin role with the
        // messaging feature assigned.
        boolean isSpecialtyGroup = false;
        if (groupId != null) {
            Group group = groupRepository.findOne(groupId);
            if (group == null) {
                throw new ResourceNotFoundException("Group not found with ID " + groupId);
            }
            if (group.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                isSpecialtyGroup = true;
            }
        }

        List<Role> staffRoles = new ArrayList<>();
        List<Role> patientRoles = new ArrayList<>();

        if (isSpecialtyGroup) {
            staffRoles.add(roleService.findByRoleTypeAndName(RoleType.STAFF, RoleName.SPECIALTY_ADMIN));
        } else {
            staffRoles = roleService.getRolesByType(RoleType.STAFF);
            patientRoles = roleService.getRolesByType(RoleType.PATIENT);
        }

        List<String> featureIds = new ArrayList<>();

        // only retrieve users with features
        for (StaffMessagingFeatureType featureType : StaffMessagingFeatureType.values()) {
            Feature feature = featureRepository.findByName(featureType.toString());
            if (feature != null) {
                featureIds.add(feature.getId().toString());
            }
        }
        getParameters.setFeatureIds(featureIds.toArray(new String[featureIds.size()]));

        if (groupId != null) {
            getParameters.setGroupIds(new String[]{groupId.toString()});
        } else {
            for (Group group : groupService.findAll()) {
                groupIdList.add(group.getId().toString());
            }
            getParameters.setGroupIds(groupIdList.toArray(new String[groupIdList.size()]));
        }

        for (Role role : staffRoles) {
            getParameters.setRoleIds(new String[]{role.getId().toString()});

            List<BaseUser> users = convertUsersToBaseUsers(
                    userService.getUsersByGroupsRolesFeatures(getParameters).getContent());

            userMap.put(role.getName().getName(), users);
        }

        for (Role role : patientRoles) {
            getParameters.setRoleIds(new String[]{role.getId().toString()});

            List<BaseUser> users = convertUsersToBaseUsers(
                    userService.getUsersByGroupsAndRolesNoFilter(getParameters).getContent());

            userMap.put(role.getName().getName(), users);
        }

        return userMap;
    }

    /**
     * Get a Map of BaseUsers organised by Role type for patients, used for potential Conversation recipients.
     *
     * @param groupId ID of Group to get recipients for (optional, will use all a User's Groups if null)
     * @return Map of BaseUsers organised by Role type
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    private HashMap<String, List<BaseUser>> getPatientRecipients(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        User entityUser = findEntityUser(userId);
        HashMap<String, List<BaseUser>> userMap = new HashMap<>();

        GetParameters getParameters = new GetParameters();
        getParameters.setSortField("surname");
        getParameters.setSortDirection("ASC");
        List<String> groupIdList = new ArrayList<>();
        List<Role> staffRoles = roleService.getRolesByType(RoleType.STAFF);

        // patients can only contact staff in their units with PatientMessagingFeatureType
        List<String> featureIdList = new ArrayList<>();

        // restrict features to PatientMessagingFeatureType (subset of Feature Type)
        for (PatientMessagingFeatureType featureType : PatientMessagingFeatureType.class.getEnumConstants()) {
            Feature feat = featureRepository.findByName(featureType.toString());
            if (feat != null) {
                featureIdList.add(feat.getId().toString());
            }
        }

        if (featureIdList.isEmpty()) {
            throw new ResourceNotFoundException("No suitable recipients (by feature)");
        }

        getParameters.setFeatureIds(featureIdList.toArray(new String[featureIdList.size()]));

        // only search for groups patient is in (excluding specialties so only units and disease groups)
        List<Group> patientGroups
                = Util.convertIterable(groupRepository.findGroupsByUserNoSpecialties(
                "%%", entityUser, new PageRequest(0, Integer.MAX_VALUE)));

        for (Group group : patientGroups) {
            groupIdList.add(group.getId().toString());
        }

        getParameters.setGroupIds(groupIdList.toArray(new String[groupIdList.size()]));

        // if restricted to one group
        if (groupId != null) {
            if (groupIdList.contains(groupId.toString())) {
                getParameters.setGroupIds(new String[]{groupId.toString()});
            } else {
                throw new ResourceForbiddenException("Forbidden");
            }
        }

        if (groupIdList.isEmpty()) {
            throw new ResourceNotFoundException("No suitable recipients (by group)");
        }

        for (Role role : staffRoles) {
            getParameters.setRoleIds(new String[]{role.getId().toString()});

            List<BaseUser> users = convertUsersToBaseUsers(
                    userService.getUsersByGroupsRolesFeatures(getParameters).getContent());

            userMap.put(role.getName().getName(), users);
        }

        return userMap;
    }

    /**
     * Get a list of potential message recipients, mapped by User role. Used in UI by user when creating a new
     * Conversation to populate the drop-down select of available recipients after a Group is selected.
     *
     * @param userId  ID of User retrieving available Conversation recipients
     * @param groupId ID of Group to find available Conversation recipients for
     * @return Object containing Lists of BaseUser organised by Role
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    private HashMap<String, List<BaseUser>> getRecipients(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden");
        }

        // to store list of users per role
        HashMap<String, List<BaseUser>> userMap = new HashMap<>();

        if (currentUserHasRole(RoleName.GLOBAL_ADMIN)) {
            userMap = getGlobalAdminRecipients(groupId);
        } else if (currentUserHasRole(RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN,
                RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN)) {
            userMap = getStaffRecipients(userId, groupId);
        } else if (currentUserHasRole(RoleName.PATIENT)) {
            userMap = getPatientRecipients(userId, groupId);
        }

        if (userMap.entrySet().isEmpty()) {
            throw new ResourceNotFoundException("No suitable recipients");
        }

        return userMap;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getRecipientsAsHtml(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        HashMap<String, List<BaseUser>> userMap = getRecipients(userId, groupId);

        StringBuilder sb = new StringBuilder();

        // sort correctly
        List<String> sortOrder = new ArrayList<>();
        sortOrder.add(RoleName.SPECIALTY_ADMIN.getName());
        sortOrder.add(RoleName.UNIT_ADMIN.getName());
        sortOrder.add(RoleName.STAFF_ADMIN.getName());
        sortOrder.add(RoleName.PATIENT.getName());

        List<String> sortedKeys = new ArrayList<>();

        for (String keySorted : sortOrder) {
            for (String key : userMap.keySet()) {
                if (keySorted.equals(key) && !sortedKeys.contains(key)) {
                    sortedKeys.add(key);
                }
            }
        }

        for (String key : userMap.keySet()) {
            if (!sortedKeys.contains(key)) {
                sortedKeys.add(key);
            }
        }

        // For selectize.js
        sb.append("<select id=\"select-recipient\" class=\"demo-default\" placeholder=\"Search for recipient..\">");
        sb.append("<option value=\"\">Search for recipient..</option>");

        for (String userType : sortedKeys) {
            List<BaseUser> users = userMap.get(userType);

            if (!users.isEmpty()) {
                sb.append("<optgroup label=\"");
                sb.append(userType);
                sb.append("\">");

                for (BaseUser baseUser : users) {
                    sb.append("<option value=\"");
                    sb.append(baseUser.getId());
                    sb.append("\">");
                    sb.append(baseUser.getSurname().replace("<", "").replace(">", ""));
                    sb.append(", ");
                    sb.append(baseUser.getForename().replace("<", "").replace(">", ""));

                    if (baseUser.getDateOfBirth() != null) {
                        Date dob = baseUser.getDateOfBirth();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                        String dobString = dateFormat.format(dob);

                        sb.append(" (");
                        sb.append(dobString);
                        sb.append(")");
                    }

                    if (StringUtils.isNotEmpty(baseUser.getRoleDescription())) {
                        sb.append(" (");
                        sb.append(baseUser.getRoleDescription());
                        sb.append(")");
                    }

                    sb.append("</option>");
                }
                sb.append("</optgroup>");
            }
        }

        sb.append("</select>");
        return sb.toString();
    }

    /**
     * TODO: Add more comments to describe logic
     *
     * @inheritDoc
     */
    @Override
    public Map<String, List<BaseUser>> getRecipientsList(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        HashMap<String, List<BaseUser>> userMap = getRecipients(userId, groupId);

        Map<String, List<BaseUser>> recipients = new HashMap<>();

        // sort correctly
        List<String> sortOrder = new ArrayList<>();
        sortOrder.add(RoleName.SPECIALTY_ADMIN.getName());
        sortOrder.add(RoleName.UNIT_ADMIN.getName());
        sortOrder.add(RoleName.STAFF_ADMIN.getName());
        sortOrder.add(RoleName.PATIENT.getName());

        List<String> sortedKeys = new ArrayList<>();

        for (String keySorted : sortOrder) {
            for (String key : userMap.keySet()) {
                if (keySorted.equals(key) && !sortedKeys.contains(key)) {
                    sortedKeys.add(key);
                }
            }
        }

        for (String key : userMap.keySet()) {
            if (!sortedKeys.contains(key)) {
                sortedKeys.add(key);
            }
        }

        for (String userType : sortedKeys) {
            List<BaseUser> users = userMap.get(userType);

            if (!recipients.containsKey(userType)) {
                recipients.put(userType, new ArrayList<BaseUser>());
            }

            if (!users.isEmpty()) {

                for (BaseUser baseUser : users) {
                    recipients.get(userType).add(baseUser);
                }
            }
        }

        return recipients;
    }

    /**
     * Get a Map of BaseUsers organised by Role type for staff members, used for potential Conversation recipients.
     *
     * @param groupId ID of Group to get recipients for (optional, will use all a User's Groups if null)
     * @return Map of BaseUsers organised by Role type
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    private HashMap<String, List<BaseUser>> getStaffRecipients(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        User entityUser = findEntityUser(userId);
        HashMap<String, List<BaseUser>> userMap = new HashMap<>();

        GetParameters getParameters = new GetParameters();
        getParameters.setSortField("surname");
        getParameters.setSortDirection("ASC");
        List<String> groupIdList = new ArrayList<>();

        // #310 QA: On selecting a specialty, it should only list users who are in the specialty admin role with the
        // messaging feature assigned.
        boolean isSpecialtyGroup = false;
        if (groupId != null) {
            Group group = groupRepository.findOne(groupId);
            if (group == null) {
                throw new ResourceNotFoundException("Group not found with ID " + groupId);
            }
            if (group.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                isSpecialtyGroup = true;
            }
        }

        List<Role> staffRoles = new ArrayList<>();
        List<Role> patientRoles = new ArrayList<>();

        if (isSpecialtyGroup) {
            staffRoles.add(roleService.findByRoleTypeAndName(RoleType.STAFF, RoleName.SPECIALTY_ADMIN));
        } else {
            staffRoles = roleService.getRolesByType(RoleType.STAFF);
            patientRoles = roleService.getRolesByType(RoleType.PATIENT);
        }

        // specialty/unit staff and admin can contact all users in specialty/unit
        // staff & patient users can only contact those in their groups
        for (BaseGroup group : groupService.findMessagingGroupsByUserId(entityUser.getId())) {
            groupIdList.add(group.getId().toString());
        }
        getParameters.setGroupIds(groupIdList.toArray(new String[groupIdList.size()]));

        // if restricted to one group
        if (groupId != null) {
            if (groupIdList.contains(groupId.toString()) || groupService.groupIdIsSupportGroup(groupId)) {
                getParameters.setGroupIds(new String[]{groupId.toString()});
            } else {
                throw new ResourceForbiddenException("Forbidden");
            }
        }

        if (groupIdList.isEmpty()) {
            throw new ResourceNotFoundException("No suitable recipients (by group)");
        }

        // restrict features to StaffMessagingFeatureType (subset of Feature Type)
        List<String> featureIdList = new ArrayList<>();
        for (StaffMessagingFeatureType featureType : StaffMessagingFeatureType.class.getEnumConstants()) {
            Feature feat = featureRepository.findByName(featureType.toString());
            if (feat != null) {
                featureIdList.add(feat.getId().toString());
            }
        }

        if (featureIdList.isEmpty()) {
            throw new ResourceNotFoundException("No suitable recipients (by feature)");
        }

        getParameters.setFeatureIds(featureIdList.toArray(new String[featureIdList.size()]));

        for (Role role : staffRoles) {
            getParameters.setRoleIds(new String[]{role.getId().toString()});

            List<BaseUser> users = convertUsersToBaseUsers(
                    userService.getUsersByGroupsRolesFeatures(getParameters).getContent());

            userMap.put(role.getName().getName(), users);
        }

        // when groupId is set, can only get patients if current user is member of group
        if (groupId != null && isUserMemberOfGroup(getCurrentUser(), groupRepository.findOne(groupId))) {
            for (Role role : patientRoles) {
                getParameters.setRoleIds(new String[]{role.getId().toString()});

                List<BaseUser> users = convertUsersToBaseUsers(
                        userService.getUsersByGroupsAndRolesNoFilter(getParameters).getContent());

                userMap.put(role.getName().getName(), users);
            }
        }

        return userMap;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Long getUnreadConversationCount(Long userId) throws ResourceNotFoundException {
        if (!userRepository.exists(userId)) {
            throw new ResourceNotFoundException("User does not exist");
        }
        return conversationRepository.getUnreadConversationCount(userId);
    }

    /**
     * Verify the current logged in User has messaging features, assume all patients and global admins do.
     *
     * @return True if current logged in User has messaging features, false if not
     */
    private boolean loggedInUserHasMessagingFeatures() {
        if (currentUserHasRole(RoleName.PATIENT, RoleName.GLOBAL_ADMIN)) {
            return true;
        }

        return userHasStaffMessagingFeatures(getCurrentUser());
    }

    /**
     * Verify the current logged in User is a member of a Conversation.
     *
     * @param conversation Conversation to verify current logged in User is a member of
     * @return True if current logged in User is a member of the Conversation, false if not
     */
    private boolean loggedInUserIsMemberOfConversation(Conversation conversation) {
        User loggedInUser = getCurrentUser();

        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            if (conversationUser.getUser().getId().equals(loggedInUser.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle errors when creating ExternalConversation.
     *
     * @param message              String reason for rejection
     * @param externalConversation ExternalConversation to reject
     * @return ExternalConversation with success false and error message set
     */
    private ExternalConversation rejectExternalConversation(String message, ExternalConversation externalConversation) {
        externalConversation.setSuccess(false);
        externalConversation.setErrorMessage(message);
        return externalConversation;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void removeConversationUser(Long conversationId, Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden");
        }

        Conversation conversation = conversationRepository.findOne(conversationId);
        if (conversation == null) {
            throw new ResourceNotFoundException("Conversation not found");
        }

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        ConversationUser foundConversationUser = null;

        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            if (conversationUser.getUser().getId().equals(userId)) {
                foundConversationUser = conversationUser;
            }
        }

        if (foundConversationUser != null) {
            conversationUserRepository.delete(foundConversationUser);
            conversation.getConversationUsers().remove(foundConversationUser);
            conversationRepository.save(conversation);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void removeConversationUserLabel(Long userId, Long conversationId, ConversationLabel conversationLabel)
            throws ResourceNotFoundException, ResourceForbiddenException {
        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden");
        }

        Conversation conversation = conversationRepository.findOne(conversationId);

        if (conversation == null) {
            throw new ResourceNotFoundException("Conversation does not exist");
        }

        if (!loggedInUserIsMemberOfConversation(conversation)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            ConversationUserLabel foundConversationUserLabel = null;

            if (conversationUser.getUser().getId().equals(userId)) {
                if (!CollectionUtils.isEmpty(conversationUser.getConversationUserLabels())) {
                    for (ConversationUserLabel conversationUserLabel : conversationUser.getConversationUserLabels()) {
                        if (conversationUserLabel.getConversationLabel().equals(conversationLabel)) {
                            conversationUserLabelRepository.delete(conversationUserLabel);
                            foundConversationUserLabel = conversationUserLabel;
                        }
                    }
                }
            }

            if (foundConversationUserLabel != null) {
                conversationUser.getConversationUserLabels().remove(foundConversationUserLabel);
                conversationUserRepository.save(conversationUser);
            }
        }
    }

    /**
     * Send an email to PatientView central support.
     *
     * @param entityUser   User sending email
     * @param conversation Conversation containing Message for email content
     */
    private void sendNewCentralSupportEmail(User entityUser, Conversation conversation) {
        Email email = new Email();
        email.setSenderEmail(properties.getProperty("smtp.sender.email"));
        email.setSenderName(properties.getProperty("smtp.sender.name"));
        email.setSubject("PatientView - New Central Support Comment");
        email.setRecipients(new String[]{properties.getProperty("central.support.contact.email")});

        StringBuilder sb = new StringBuilder();
        sb.append("Dear PatientView Support, <br/><br/>You have a new comment from ");
        sb.append(entityUser.getForename());
        sb.append(" ");
        sb.append(entityUser.getSurname());
        sb.append(" (<a href=\"mailto:");
        sb.append(entityUser.getEmail());
        sb.append("\">");
        sb.append(entityUser.getEmail());
        sb.append("</a>)<br/><br/>Content: <br/><hr>");
        sb.append(conversation.getMessages().get(0).getMessage());
        sb.append("<hr><br/>");
        email.setBody(sb.toString());

        // try and send but ignore if exception and log
        try {
            emailService.sendEmail(email);
        } catch (MailException | MessagingException me) {
            LOG.error("Cannot send email: {}", me);
        }
    }

    /**
     * Send new Message emails to a Set of Users.
     *
     * @param conversationUsers Set of ConversationUsers to retrieve User details from
     * @param anonymous         boolean set to true if creator of conversation is anonymous, as when sending anonymous
     *                          unit feedback
     * @param sender            User who added message
     */
    private void sendNewMessageEmails(Set<ConversationUser> conversationUsers, boolean anonymous, User sender) {
        for (ConversationUser conversationUser : conversationUsers) {
            User user = conversationUser.getUser();

            // only send messages to other users, not current user and only if user has email address
            if (!user.getId().equals(getCurrentUser().getId())
                    && StringUtils.isNotEmpty(user.getEmail())
                    && !user.getUsername().equals(DummyUsernames.PATIENTVIEW_NOTIFICATIONS.getName())) {

                Email email = new Email();
                email.setSenderEmail(properties.getProperty("smtp.sender.email"));
                email.setSenderName(properties.getProperty("smtp.sender.name"));
                email.setSubject("PatientView - you have a new message");
                email.setRecipients(new String[]{user.getEmail()});

                StringBuilder sb = new StringBuilder();
                sb.append("Dear ").append(user.getForename()).append(" ");
                sb.append(user.getSurname());
                sb.append(", <br/><br/>You have a new message on <a href=\"");
                sb.append(properties.getProperty("site.url"));
                sb.append("\">PatientView</a>");

                // if conversation is not anonymous (anonymous feedback to unit), add user details
                if (!anonymous) {
                    StringBuilder roleSb = new StringBuilder();

                    if (userHasRole(sender, RoleName.GLOBAL_ADMIN)) {
                        // handle global admins
                        roleSb.append(RoleName.GLOBAL_ADMIN.getName());
                    } else {
                        // group roles
                        int count = 0;
                        boolean isSpecialtyAdmin = userHasRole(sender, RoleName.SPECIALTY_ADMIN);
                        // need to have clean list in order to append comma correctly
                        List<GroupRole> groupRoles = new ArrayList<>();

                        // only include visible groups and non specialty groups
                        for (GroupRole groupRole : sender.getGroupRoles()) {
                            if (Boolean.TRUE.equals(groupRole.getGroup().getVisible())
                                    && (!GroupTypes.SPECIALTY.toString().equals(
                                    groupRole.getGroup().getGroupType().getValue()) || isSpecialtyAdmin)) {
                                groupRoles.add(groupRole);
                            }
                        }
                        // append comma
                        for (GroupRole groupRole : groupRoles) {
                            roleSb.append(groupRole.getRole().getName().getName()).append(" at ");
                            roleSb.append(groupRole.getGroup().getName());
                            if (count < groupRoles.size() - 1) {
                                roleSb.append(", ");
                            }
                            count++;
                        }
                    }

                    if (roleSb.length() > 0) {
                        // name
                        sb.append(" from ").append(sender.getName()).append(". <br/><br/>This user is a ");
                        sb.append(roleSb);
                        sb.append(".");
                    }
                }

                sb.append("<br/><br/>Please log in to view your message.<br/>");
                email.setBody(sb.toString());

                // try and send but ignore if exception and log
                try {
                    emailService.sendEmail(email);
                } catch (MailException | MessagingException me) {
                    LOG.error("Cannot send email: {}", me);
                }

                // only send messages to other users, not current user and only if user has email address
                if (!user.getId().equals(getCurrentUser().getId()) && StringUtils.isNotEmpty(user.getEmail())
                        && !user.getUsername().equals(DummyUsernames.PATIENTVIEW_NOTIFICATIONS.getName())) {

                }
            }
        }
    }

    /**
     * Send notification on new message to all users in the conversation
     *
     * @param conversation a  conversation to send notifications for
     */
    private void sendNewMessageNotification(Conversation conversation) {

        // itterate over the list of users for this conversation and notify them of a new message
        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            User user = conversationUser.getUser();

            // only send messages to other users, not current user
            if (!user.getId().equals(getCurrentUser().getId())) {
                notificationClient.notifyMessage(user.getId(), conversation.getId(), conversation.getTitle());
            }
        }
    }

    /**
     * Verify at least one of a User's Groups has the MESSAGING Feature enabled.
     *
     * @param user User to check has at least one Group with MESSAGING Feature enabled
     * @return True if User has at least one Group with MESSAGING Feature enabled, false if not
     */
    private boolean userGroupsHaveMessagingFeature(User user) {
        if (user == null) {
            return false;
        }

        for (GroupRole groupRole : user.getGroupRoles()) {
            if (groupRole.getRole().getName().equals(RoleName.SPECIALTY_ADMIN)) {
                for (GroupRelationship groupRelationship : groupRole.getGroup().getGroupRelationships()) {
                    if (groupRelationship.getRelationshipType().equals(RelationshipTypes.CHILD)) {
                        for (GroupFeature groupFeature : groupRelationship.getObjectGroup().getGroupFeatures()) {
                            if (groupFeature.getFeature().getName().equals(FeatureType.MESSAGING.toString())) {
                                return true;
                            }
                        }
                    }
                }
            }

            for (GroupFeature groupFeature : groupRole.getGroup().getGroupFeatures()) {
                if (groupFeature.getFeature().getName().equals(FeatureType.MESSAGING.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check User has at least one Feature from a restricted list of staff messaging Features.
     *
     * @param user User to check has staff messaging Feature
     * @return True if User has at least one Feature from a restricted list of staff messaging Features, false if not
     */
    private boolean userHasStaffMessagingFeatures(User user) {
        if (user == null) {
            return false;
        }

        if (CollectionUtils.isEmpty(user.getUserFeatures())) {
            return false;
        }

        for (UserFeature userFeature : user.getUserFeatures()) {
            if (isInEnum(userFeature.getFeature().getName(), StaffMessagingFeatureType.class)) {
                return true;
            }
        }
        return false;
    }
}
