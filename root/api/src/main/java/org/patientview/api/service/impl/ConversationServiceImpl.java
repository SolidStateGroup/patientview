package org.patientview.api.service.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.BaseGroup;
import org.patientview.api.model.BaseUser;
import org.patientview.api.service.AuditService;
import org.patientview.persistence.model.ConversationUserLabel;
import org.patientview.persistence.model.Email;
import org.patientview.api.service.ConversationService;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.RoleService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.ConversationUser;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.MessageReadReceipt;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.ConversationLabel;
import org.patientview.persistence.model.enums.ConversationTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.PatientMessagingFeatureType;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.model.enums.StaffMessagingFeatureType;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.persistence.repository.ConversationUserLabelRepository;
import org.patientview.persistence.repository.ConversationUserRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.MessageReadReceiptRepository;
import org.patientview.persistence.repository.MessageRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Conversation service, for CRUD operations related to Conversations and Messages.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@Service
public class ConversationServiceImpl extends AbstractServiceImpl<ConversationServiceImpl>
        implements ConversationService {

    @Inject
    private AuditService auditService;

    @Inject
    private EmailService emailService;

    @Inject
    private ConversationRepository conversationRepository;

    @Inject
    private ConversationUserRepository conversationUserRepository;

    @Inject
    private ConversationUserLabelRepository conversationUserLabelRepository;

    @Inject
    private EntityManager entityManager;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupService groupService;

    @Inject
    private MessageReadReceiptRepository messageReadReceiptRepository;

    @Inject
    private MessageRepository messageRepository;

    @Inject
    private Properties properties;

    @Inject
    private RoleService roleService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    /**
     * Add a new conversation.
     * @param conversation Conversation to add
     * @return Conversation, newly added
     */
    public Conversation add(Conversation conversation) {
        // TODO: add conversation
        return null;
    }

    /**
     * Create a new conversation, including recipients and associated Message.
     * @param userId ID of User creating Conversation
     * @param conversation Conversation object containing all required properties and first Message content
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
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

        User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        creator = userRepository.findOne(creator.getId());
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

            List<Message> messageSet = new ArrayList<>();
            messageSet.add(newMessage);
            newConversation.setMessages(messageSet);

            // set conversation users
            Set<ConversationUser> conversationUsers
                    = createEntityConversationUserSet(conversation.getConversationUsers(), newConversation, creator);
            newConversation.setConversationUsers(conversationUsers);

            // send email notification to conversation users
            sendNewMessageEmails(conversationUsers);

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
     * Add a User to a Conversation by creating a new ConversationUser with ConversationLabel.INBOX.
     * @param conversationId ID of Conversation to add User to
     * @param userId ID of User to be added to Conversation
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
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
     * Add a label to a User's Conversation, e.g. ConversationLabel.ARCHIVED for archived Conversations.
     * @param userId ID of User to add Conversation label to
     * @param conversationId ID of Conversation to add label to
     * @param conversationLabel ConversationLabel label to add to Conversation for this User
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
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
     * Add a Message to an existing Conversation.
     * @param conversationId ID of Conversation to add Message to
     * @param message Message object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
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
        sendNewMessageEmails(entityConversation.getConversationUsers());
    }

    /**
     * Add a read receipt for a Message given the Message and User IDs.
     * @param messageId ID of Message to add read receipt for
     * @param userId ID of User who has read the Message
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
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

        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            if (conversationUser.getAnonymous()) {
                anonUserIds.add(conversationUser.getUser().getId());

                ConversationUser anonConversationUser = new ConversationUser();
                anonConversationUser.setAnonymous(true);
                anonConversationUser.setUser(anonUser);
                anonConversationUser.setConversation(conversation);
                newConversation.getConversationUsers().add(anonConversationUser);
            } else {
                newConversation.getConversationUsers().add(conversationUser);
            }
        }

        List<Message> newMessages = new ArrayList<>();

        for (Message message : conversation.getMessages()) {
            Message newMessage = new Message();
            newMessage.setId(message.getId());
            newMessage.setConversation(newConversation);
            newMessage.setType(message.getType());
            newMessage.setMessage(message.getMessage());
            newMessage.setReadReceipts(message.getReadReceipts());
            newMessage.setCreated(message.getCreated());

            if (message.getUser() != null) {
                if (anonUserIds.contains(message.getUser().getId())) {
                    newMessage.setUser(anonUser);
                } else {
                    newMessage.setUser(message.getUser());
                }
            }
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
     * @param conversation Conversation to verify
     * @return true if all conversation users have messaging features and member of group with messaging enabled
     */
    private boolean conversationUsersAndGroupsHaveMessagingFeatures(Conversation conversation) {
        int usersWithMessagingFeaturesCount = 0;

        for (ConversationUser conversationUser : conversation.getConversationUsers()) {

            User entityUser = userRepository.findOne(conversationUser.getUser().getId());

            // GLOBAL_ADMIN and PATIENT users always have messaging features
            if (userHasRole(entityUser, RoleName.GLOBAL_ADMIN, RoleName.PATIENT)) {
                usersWithMessagingFeaturesCount++;
            } else if (userHasStaffMessagingFeatures(entityUser)) {
                usersWithMessagingFeaturesCount++;
            }

            // check conversation user member of at least one group with messaging enabled
            if (!userHasRole(entityUser, RoleName.GLOBAL_ADMIN, RoleName.PATIENT)
                    && !userGroupsHaveMessagingFeature(entityUser)) {
                return false;
            }
        }

        return (conversation.getConversationUsers().size() == usersWithMessagingFeaturesCount);
    }

    private List<org.patientview.api.model.BaseUser> convertUsersToTransportBaseUsers(List<User> users) {
        List<org.patientview.api.model.BaseUser> transportUsers = new ArrayList<>();

        for (User user : users) {
            // do not allow users to talk to themselves
            if (!getCurrentUser().getId().equals(user.getId())) {
                transportUsers.add(new org.patientview.api.model.BaseUser(user));
            }
        }

        return transportUsers;
    }

    private Set<ConversationUser> createEntityConversationUserSet(Set<ConversationUser> conversationUsers,
                                                                  Conversation conversation, User creator)
            throws ResourceNotFoundException {
        Set<ConversationUser> conversationUserSet = new HashSet<>();

        for (ConversationUser conversationUser : conversationUsers) {
            ConversationUser newConversationUser = new ConversationUser();
            newConversationUser.setConversation(conversation);
            newConversationUser.setUser(userRepository.findOne(conversationUser.getUser().getId()));
            newConversationUser.setAnonymous(conversationUser.getAnonymous());
            newConversationUser.setCreator(creator);
            newConversationUser.setConversationUserLabels(new HashSet<ConversationUserLabel>());
            conversationUserSet.add(newConversationUser);
        }

        // now handle contacting unit staff
        if (conversation.getType().equals(ConversationTypes.CONTACT_UNIT)) {
            Group entityGroup = groupRepository.findOne(conversation.getGroupId());
            Feature entityFeature = featureRepository.findByName(conversation.getStaffFeature().toString());

            if (entityGroup == null || entityFeature == null) {
                throw new ResourceNotFoundException("Missing parameters when sending message");
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

            // add found staff to conversation
            for (User user : staffUsers) {
                ConversationUser newConversationUser = new ConversationUser();
                newConversationUser.setConversation(conversation);
                newConversationUser.setUser(userRepository.findOne(user.getId()));
                newConversationUser.setAnonymous(false);
                newConversationUser.setCreator(creator);
                conversationUserSet.add(newConversationUser);
            }
        }

        // add INBOX conversation user label for all ConversationUser
        for (ConversationUser conversationUser : conversationUserSet) {
            ConversationUserLabel conversationUserLabel = new ConversationUserLabel();
            conversationUserLabel.setConversationUser(conversationUser);
            conversationUserLabel.setCreated(new Date());
            conversationUserLabel.setCreator(creator);
            conversationUserLabel.setConversationLabel(ConversationLabel.INBOX);
            conversationUser.getConversationUserLabels().add(conversationUserLabel);
        }

        return conversationUserSet;
    }

    /**
     * Delete a Conversation given ID.
     * @param conversationId ID of Conversation to delete
     */
    public void delete(Long conversationId) {
        conversationRepository.delete(conversationId);
    }

    /**
     * Delete a User from all Conversations, used during User deletion.
     * @param user User to delete from all Conversations
     */
    @Override
    public void deleteUserFromConversations(User user) {
        // remove from all conversations where user is a member (including messages)
        List<Conversation> conversations
                = conversationRepository.findByUser(user, new PageRequest(0, Integer.MAX_VALUE)).getContent();

        for (Conversation conversation : conversations) {
            // remove from conversation user list
            Set<ConversationUser> removedUserConversationUsers = new HashSet<>();
            for (ConversationUser conversationUser :conversation.getConversationUsers()) {
                if (!conversationUser.getUser().getId().equals(user.getId())) {

                    // remove as creator if set
                    if (conversationUser.getCreator().getId().equals(user.getId())) {
                        conversationUser.setCreator(null);
                    }

                    removedUserConversationUsers.add(conversationUser);
                } else {
                    conversationUserRepository.delete(conversationUser);
                }
            }
            conversation.setConversationUsers(removedUserConversationUsers);

            // remove from messages
            List<Message> removedUserMessages = new ArrayList<>();
            for (Message message : conversation.getMessages()) {
                if (message.getUser().getId().equals(user.getId())) {
                    message.setUser(null);
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
     * Get a Conversation, including Messages given a Conversation ID.
     * @param conversationId ID of Conversation to retrieve
     * @return Conversation object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
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
     * Get a Page of Conversation objects given a User (who is a member of the Conversations).
     * @param userId ID of User to retrieve Conversations for
     * @param getParameters GetParameters object for pagination properties defined in UI, including page number, size
     * @return Page of Conversation objects
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
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
     * Get Conversation by ID.
     * @param conversationId ID of Conversation to get
     * @return Conversation object
     */
    @Override
    public Conversation get(Long conversationId) {
        return anonymiseConversation(conversationRepository.findOne(conversationId));
    }

    /**
     * Get a Conversation User's picture, returned as byte[] to allow direct viewing in browser when set as img source.
     * Will only retrieve picture if current user is a member of conversation.
     * @param conversationId ID of User to retrieve picture for
     * @param userId ID of User to retrieve picture for
     * @return byte[] binary picture data
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
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
     * Get a List of BaseUser (used as Conversation recipients) for a Group based on the feature passed in, currently
     * DEFAULT_MESSAGING_CONTACT. Used when creating a membership request from patients page.
     * @param groupId ID of Group to find available recipients for
     * @param featureName String name of Feature that Users must have to be recipients
     * @return List of BaseUser
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
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
        if (!(Util.doesContainRoles(RoleName.GLOBAL_ADMIN)
                || Util.doesContainRoles(RoleName.UNIT_ADMIN)
                || Util.doesContainRoles(RoleName.SPECIALTY_ADMIN))) {
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
        return convertUsersToTransportBaseUsers(page.getContent());
    }

    /**
     * Get a Map of BaseUsers organised by Role type for global admins, used for potential Conversation recipients.
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
        List<Role> staffRoles = roleService.getRolesByType(RoleType.STAFF);
        List<Role> patientRoles = roleService.getRolesByType(RoleType.PATIENT);
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

            List<BaseUser> users = convertUsersToTransportBaseUsers(
                    userService.getUsersByGroupsRolesFeatures(getParameters).getContent());

            userMap.put(role.getName().getName(), users);
        }

        for (Role role : patientRoles) {
            getParameters.setRoleIds(new String[]{role.getId().toString()});

            List<BaseUser> users = convertUsersToTransportBaseUsers(
                    userService.getUsersByGroupsAndRolesNoFilter(getParameters).getContent());

            userMap.put(role.getName().getName(), users);
        }

        return userMap;
    }

    /**
     * Get a Map of BaseUsers organised by Role type for patients, used for potential Conversation recipients.
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

            List<BaseUser> users = convertUsersToTransportBaseUsers(
                    userService.getUsersByGroupsRolesFeatures(getParameters).getContent());

            userMap.put(role.getName().getName(), users);
        }

        return userMap;
    }

    /**
     * Get a list of potential message recipients, mapped by User role. Used in UI by user when creating a new
     * Conversation to populate the drop-down select of available recipients after a Group is selected.
     * Note: not currently used due to speed concerns when rendering large lists client-side in ie8.
     * @param userId ID of User retrieving available Conversation recipients
     * @param groupId ID of Group to find available Conversation recipients for
     * @return Object containing Lists of BaseUser organised by Role
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @Override
    public HashMap<String, List<BaseUser>> getRecipients(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden");
        }

        // to store list of users per role
        HashMap<String, List<BaseUser>> userMap = new HashMap<>();

        if (doesContainRoles(RoleName.GLOBAL_ADMIN)) {
            userMap = getGlobalAdminRecipients(groupId);
        } else if (doesContainRoles(RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN,
                RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN)) {
            userMap = getStaffRecipients(userId, groupId);
        } else  if (doesContainRoles(RoleName.PATIENT)) {
            userMap = getPatientRecipients(userId, groupId);
        }

        if (userMap.entrySet().isEmpty()) {
            throw new ResourceNotFoundException("No suitable recipients");
        }

        return userMap;
    }

    /**
     * Fast method of returning available Conversation recipients when a User has selected a Group in the UI.
     * Note: returns HTML as a String to avoid performance issues in ie8
     * @param userId ID of User retrieving available Conversation recipients
     * @param groupId ID of Group to find available Conversation recipients for
     * @return HTML String for drop-down select
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @Override
    public String getRecipientsFast(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        HashMap<String, List<BaseUser>> userMap = getRecipients(userId, groupId);

        StringBuilder sb = new StringBuilder();

        // sort correctly
        List<String> sortOrder = new ArrayList<>();
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
     * Get a Map of BaseUsers organised by Role type for staff members, used for potential Conversation recipients.
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
        List<Role> staffRoles = roleService.getRolesByType(RoleType.STAFF);
        List<Role> patientRoles = roleService.getRolesByType(RoleType.PATIENT);

        // specialty/unit staff and admin can contact all users in specialty/unit
        // staff & patient users can only contact those in their groups
        for (BaseGroup group : groupService.findMessagingGroupsByUserId(entityUser.getId())) {
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

            List<BaseUser> users = convertUsersToTransportBaseUsers(
                    userService.getUsersByGroupsRolesFeatures(getParameters).getContent());

            userMap.put(role.getName().getName(), users);
        }

        for (Role role : patientRoles) {
            getParameters.setRoleIds(new String[]{role.getId().toString()});

            List<BaseUser> users = convertUsersToTransportBaseUsers(
                    userService.getUsersByGroupsAndRolesNoFilter(getParameters).getContent());

            userMap.put(role.getName().getName(), users);
        }

        return userMap;
    }

    /**
     * Get the number of unread Messages (those with no read receipt) for a User.
     * @param userId ID of User to find number of unread messages for
     * @return Long containing number of unread messages
     * @throws ResourceNotFoundException
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
     * @return True if current logged in User has messaging features, false if not
     */
    private boolean loggedInUserHasMessagingFeatures() {
        User loggedInUser = getCurrentUser();
        if (Util.doesContainRoles(RoleName.PATIENT, RoleName.GLOBAL_ADMIN)) {
            return true;
        }

        return userHasStaffMessagingFeatures(userRepository.findOne(loggedInUser.getId()));
    }

    /**
     * Verify the current logged in User is a member of a Conversation.
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
     * Remove a User from a Conversation by deleting the ConversationUser.
     * @param conversationId ID of Conversation to remove User from
     * @param userId ID of User to be removed from Conversation
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
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
     * Remove a label from a User's Conversation, e.g. ConversationLabel.ARCHIVED for archived Conversations.
     * @param userId ID of User to remove Conversation label from
     * @param conversationId ID of Conversation to add label from
     * @param conversationLabel ConversationLabel label to remove from Conversation for this User
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
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
     * Update an existing Conversation.
     * @param conversation Conversation to update
     * @return Updated Conversation
     * @throws ResourceNotFoundException
     */
    public Conversation save(Conversation conversation) throws ResourceNotFoundException {
        Conversation entityConversation = conversationRepository.findOne(conversation.getId());
        if (entityConversation == null) {
            throw new ResourceNotFoundException(String.format("Could not find conversation %s", conversation.getId()));
        }

        // TODO: save conversation fields
        return conversationRepository.save(entityConversation);
    }

    /**
     * Send an email to PatientView central support.
     * @param entityUser User sending email
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
        sb.append("</a>)<br/><br/>Content: <br/>");
        sb.append(conversation.getMessages().get(0).getMessage());
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
     * @param conversationUsers Set of ConversationUsers to retrieve User details from
     */
    private void sendNewMessageEmails(Set<ConversationUser> conversationUsers) {
        for (ConversationUser conversationUser : conversationUsers) {
            User user = conversationUser.getUser();

            // only send messages to other users, not current user and only if user has email address
            if (!user.getId().equals(getCurrentUser().getId()) && StringUtils.isNotEmpty(user.getEmail())) {

                Email email = new Email();
                email.setSenderEmail(properties.getProperty("smtp.sender.email"));
                email.setSenderName(properties.getProperty("smtp.sender.name"));
                email.setSubject("PatientView - you have a new message");
                email.setRecipients(new String[]{user.getEmail()});

                StringBuilder sb = new StringBuilder();
                sb.append("Dear ");
                sb.append(user.getForename());
                sb.append(" ");
                sb.append(user.getSurname());
                sb.append(", <br/><br/>You have a new message on <a href=\"");
                sb.append(properties.getProperty("site.url"));
                sb.append("\">PatientView</a>");
                sb.append("<br/><br/>Please log in to view your message.<br/>");
                email.setBody(sb.toString());

                // try and send but ignore if exception and log
                try {
                    emailService.sendEmail(email);
                } catch (MailException | MessagingException me) {
                    LOG.error("Cannot send email: {}", me);
                }
            }
        }
    }

    /**
     * Verify at least one of a User's Groups has the MESSAGING Feature enabled.
     * @param user User to check has at least one Group with MESSAGING Feature enabled
     * @return True if User has at least one Group with MESSAGING Feature enabled, false if not
     */
    private boolean userGroupsHaveMessagingFeature(User user) {
        User entityUser = userRepository.findOne(user.getId());

        for (GroupRole groupRole : entityUser.getGroupRoles()) {
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
     * Check User has at least one of the RoleNames passed in.
     * @param user User to check Role membership for
     * @param roleNames RoleName(s) to check User has
     * @return true if User has at least one of the RoleNames passed in
     */
    private boolean userHasRole(User user, RoleName ... roleNames) {
        User entityUser = userRepository.findOne(user.getId());

        for (GroupRole groupRole : entityUser.getGroupRoles()) {
            for (RoleName roleNameArg : roleNames) {
                if (groupRole.getRole().getName().equals(roleNameArg)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check User has at least one Feature from a restricted list of staff messaging Features.
     * @param user User to check has staff messaging Feature
     * @return True if User has at least one Feature from a restricted list of staff messaging Features, false if not
     */
    private boolean userHasStaffMessagingFeatures(User user) {
        User entityUser = userRepository.findOne(user.getId());

        for (UserFeature userFeature : entityUser.getUserFeatures()) {
            if (Util.isInEnum(userFeature.getFeature().getName(), StaffMessagingFeatureType.class)) {
                return true;
            }
        }
        return false;
    }
}
