package org.patientview.api.service.impl;

import org.patientview.api.service.ConversationService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.RoleService;
import org.patientview.api.service.UserService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.ConversationUser;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.MessageReadReceipt;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ConversationTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.MessageRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@Service
public class ConversationServiceImpl extends AbstractServiceImpl<ConversationServiceImpl>
        implements ConversationService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private ConversationRepository conversationRepository;

    @Inject
    private MessageRepository messageRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupService groupService;

    @Inject
    private RoleService roleService;

    @Inject
    private UserService userService;

    public Conversation get(Long conversationId) {
        return anonymiseConversation(conversationRepository.findOne(conversationId));
    }

    public org.patientview.api.model.Conversation findByConversationId(Long conversationId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        Conversation conversation = conversationRepository.findOne(conversationId);

        if (conversation == null) {
            throw new ResourceNotFoundException("Conversation does not exist");
        }

        if (!loggedInUserIsMemberOfConversation(conversation)) {
            throw new ResourceForbiddenException("You do not have permission");
        }

        return new org.patientview.api.model.Conversation(anonymiseConversation(conversation));
    }

    public Conversation add(Conversation conversation) {
        // TODO: add conversation
        return conversationRepository.findOne(conversation.getId());
    }

    public Conversation save(Conversation conversation) throws ResourceNotFoundException {
        Conversation entityConversation = conversationRepository.findOne(conversation.getId());
        if (entityConversation == null) {
            throw new ResourceNotFoundException(String.format("Could not find conversation %s", conversation.getId()));
        }

        // TODO: save conversation fields
        return conversationRepository.save(entityConversation);
    }

    public void delete(Long conversationId) {
        conversationRepository.delete(conversationId);
    }

    private Conversation anonymiseConversation(Conversation conversation) {

        Conversation newConversation = new Conversation();
        newConversation.setConversationUsers(new HashSet<ConversationUser>());
        List<Long> anonUserIds = new ArrayList<>();
        User anonUser = new User();
        anonUser.setForename("Anonymous");
        anonUser.setSurname("User");

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

            if (anonUserIds.contains(message.getUser().getId())) {
                newMessage.setUser(anonUser);
            } else {
                newMessage.setUser(message.getUser());
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

    public Page<org.patientview.api.model.Conversation> findByUserId(Long userId, Pageable pageable)
            throws ResourceNotFoundException {
        User entityUser = findEntityUser(userId);
        Page<Conversation> conversationPage = conversationRepository.findByUser(entityUser, pageable);
        List<org.patientview.api.model.Conversation> conversations = new ArrayList<>();

        // make anonymous if necessary
        for (Conversation conversation : conversationPage.getContent()) {
            conversations.add(new org.patientview.api.model.Conversation(anonymiseConversation(conversation)));
        }

        return new PageImpl<>(conversations, pageable, conversations.size());
    }

    public void addMessage(Long conversationId, org.patientview.api.model.Message message)
            throws ResourceNotFoundException, ResourceForbiddenException {
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

        conversationRepository.save(entityConversation);
    }

    private User findEntityUser(Long userId) throws ResourceNotFoundException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException(String.format("Could not find user %s", userId));
        }
        return entityUser;
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

        return conversationUserSet;
    }

    public void addConversation(Long userId, Conversation conversation) throws ResourceNotFoundException {

        User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        creator = userRepository.findOne(creator.getId());
        User entityUser = findEntityUser(userId);

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
        newConversation.setConversationUsers(createEntityConversationUserSet(conversation.getConversationUsers(),
                newConversation, creator));

        // set updated, used in UI to order conversations
        newConversation.setLastUpdate(new Date());

        // persist conversation
        conversationRepository.save(newConversation);
    }

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

    public Long getUnreadConversationCount(Long userId) throws ResourceNotFoundException {

        if (!userRepository.exists(userId)) {
            throw new ResourceNotFoundException("User does not exist");
        }

        return conversationRepository.getUnreadConversationCount(userId);
    }

    private List<org.patientview.api.model.User> convertUsersToTransportUsers(List<User> users) {
        List<org.patientview.api.model.User> transportUsers = new ArrayList<>();

        for (User user : users) {
            transportUsers.add(new org.patientview.api.model.User(user, null));
        }

        return transportUsers;
    }

    public List<org.patientview.api.model.User> getRecipients(Long userId, String[] featureTypes)
            throws ResourceNotFoundException {
        User entityUser = findEntityUser(userId);

        // global admin can contact all users
        if (doesContainRoles(RoleName.GLOBAL_ADMIN)) {
            return convertUsersToTransportUsers(userRepository.findAll());
        }

        List<String> groupIdList = new ArrayList<>();
        List<String> roleIdList = new ArrayList<>();

        // staff & patient users can only contact those in their groups
        for (Group group : groupService.findGroupByUser(entityUser)) {
            groupIdList.add(group.getId().toString());
        }

        // assuming patients cannot contact other patients
        for (Role role : roleService.getRolesByType(RoleType.STAFF)) {
            roleIdList.add(role.getId().toString());
        }

        GetParameters getParameters = new GetParameters();
        getParameters.setGroupIds(groupIdList.toArray(new String[groupIdList.size()]));
        getParameters.setRoleIds(roleIdList.toArray(new String[roleIdList.size()]));

        // specialty/unit staff and admin can contact all users in specialty/unit
        if (doesContainRoles(RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN)) {
            return userService.getUsersByGroupsAndRoles(getParameters).getContent();
        }

        // patients can only contact staff with feature names passed in
        if (doesContainRoles(RoleName.PATIENT)) {

            List<String> featureIdList = new ArrayList<>();

            for (String featureType : featureTypes) {
                Feature feat = featureRepository.findByName(featureType);
                if (feat != null) {
                    featureIdList.add(feat.getId().toString());
                }
            }
            getParameters.setFeatureIds(featureIdList.toArray(new String[featureIdList.size()]));
            return userService.getUsersByGroupsRolesFeatures(getParameters).getContent();
        }

        throw new ResourceNotFoundException("No suitable roles");
    }

    // verify logged in user can open conversation
    private boolean loggedInUserIsMemberOfConversation(Conversation conversation) {
        User loggedInUser = getCurrentUser();

        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            if (conversationUser.getUser().equals(loggedInUser)) {
                return true;
            }
        }

        return false;
    }
}
