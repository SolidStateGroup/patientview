package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.BaseGroup;
import org.patientview.api.model.BaseUser;
import org.patientview.api.model.Email;
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
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.MessageReadReceipt;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.ConversationTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.PatientMessagingFeatureType;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.model.enums.StaffMessagingFeatureType;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.MessageRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.mail.MessagingException;
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

    @Inject
    private EmailService emailService;

    @Inject
    private Properties properties;

    public Conversation get(Long conversationId) {
        return anonymiseConversation(conversationRepository.findOne(conversationId));
    }

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
            throws ResourceNotFoundException, ResourceForbiddenException {
        User entityUser = findEntityUser(userId);

        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden");
        }

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

        conversationRepository.save(entityConversation);

        // send email notification to conversation users
        sendNewMessageEmails(entityConversation.getConversationUsers());
    }

    private User findEntityUser(Long userId) throws ResourceNotFoundException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException(String.format("Could not find user %s", userId));
        }
        return entityUser;
    }

    private Set<ConversationUser> createEntityConversationUserSet(Set<ConversationUser> conversationUsers,
            Conversation conversation, User creator) throws ResourceNotFoundException {

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

    public void addConversation(Long userId, Conversation conversation)
            throws ResourceNotFoundException, ResourceForbiddenException {

        if (!loggedInUserHasMessagingFeatures() || !conversationUsersAndGroupsHaveMessagingFeatures(conversation)) {
            throw new ResourceForbiddenException("Forbidden");
        }

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
        Set<ConversationUser> conversationUsers = createEntityConversationUserSet(conversation.getConversationUsers(),
                newConversation, creator);
        newConversation.setConversationUsers(conversationUsers);

        // send email notification to conversation users
        sendNewMessageEmails(conversationUsers);

        // set updated, used in UI to order conversations
        newConversation.setLastUpdate(new Date());

        // persist conversation
        conversationRepository.save(newConversation);
    }

    private void sendNewMessageEmails(Set<ConversationUser> conversationUsers) {

        for (ConversationUser conversationUser : conversationUsers) {
            User user = conversationUser.getUser();

            // only send messages to other users, not current user and only if user has email address
            if (!user.equals(getCurrentUser()) && StringUtils.isNotEmpty(user.getEmail())) {

                Email email = new Email();
                email.setSender(properties.getProperty("smtp.sender"));
                email.setSubject("PatientView - you have a new message");
                email.setRecipients(new String[]{user.getEmail()});

                StringBuilder sb = new StringBuilder();
                sb.append("Dear ");
                sb.append(user.getForename());
                sb.append(" ");
                sb.append(user.getSurname());
                sb.append(", <br/><br/>You have a new message on PatientView.");
                sb.append("<br/><br/>Please visit <a href=\"");
                sb.append(properties.getProperty("site.url"));
                sb.append("\">the PatientView website</a> and log in to view your message.");
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

    public HashMap<String, List<BaseUser>> getRecipients(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        if (!loggedInUserHasMessagingFeatures()) {
            throw new ResourceForbiddenException("Forbidden");
        }

        User entityUser = findEntityUser(userId);
        List<String> groupIdList = new ArrayList<>();
        List<Role> staffRoles = roleService.getRolesByType(RoleType.STAFF);
        List<Role> patientRoles = roleService.getRolesByType(RoleType.PATIENT);

        // to store list of users per role
        HashMap<String, List<BaseUser>> userMap = new HashMap<>();

        GetParameters getParameters = new GetParameters();

        if (doesContainRoles(RoleName.GLOBAL_ADMIN)) {

            if (groupId != null) {
                getParameters.setGroupIds(new String[]{groupId.toString()});
            } else {
                for (Group group : groupService.findAll()) {
                    groupIdList.add(group.getId().toString());
                }
            }

            for (Role role : staffRoles) {
                getParameters.setRoleIds(new String[]{role.getId().toString()});

                List<BaseUser> users = convertUsersToTransportBaseUsers(
                        userService.getUsersByGroupsAndRoles(getParameters).getContent());

                userMap.put(role.getName().getName(), users);
            }

            for (Role role : patientRoles) {
                getParameters.setRoleIds(new String[]{role.getId().toString()});

                List<BaseUser> users = convertUsersToTransportBaseUsers(
                        userService.getUsersByGroupsAndRoles(getParameters).getContent());

                userMap.put(role.getName().getName(), users);
            }

        } else if (doesContainRoles(RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN,
                RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN)) {

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
                        userService.getUsersByGroupsAndRoles(getParameters).getContent());

                userMap.put(role.getName().getName(), users);
            }

            for (Role role : patientRoles) {
                getParameters.setRoleIds(new String[]{role.getId().toString()});

                List<BaseUser> users = convertUsersToTransportBaseUsers(
                        userService.getUsersByGroupsAndRoles(getParameters).getContent());

                userMap.put(role.getName().getName(), users);
            }
        }

        // patients can only contact staff in their units with feature names passed in
        if (doesContainRoles(RoleName.PATIENT)) {
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

            // only search for groups patient is in (excluding specialties so only units)
            List<Group> patientGroups
                    = Util.convertIterable(groupRepository.findGroupsByUserNoSpecialties(
                    "%%", entityUser, new PageRequest(0, Integer.MAX_VALUE)));
            groupIdList = new ArrayList<>();

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
        }

        if (userMap.entrySet().isEmpty()) {
            throw new ResourceNotFoundException("No suitable recipients");
        }

        return userMap;
    }

    private boolean userHasStaffMessagingFeatures(User user) {
        User entityUser = userRepository.findOne(user.getId());

        for (UserFeature userFeature : entityUser.getUserFeatures()) {
            if (Util.isInEnum(userFeature.getFeature().getName(), StaffMessagingFeatureType.class)) {
                return true;
            }
        }
        return false;
    }

    // verify at least one of the user's groups has messaging enabled
    private boolean userGroupsHaveMessagingFeature(User user) {

        User entityUser = userRepository.findOne(user.getId());

        for (GroupRole groupRole : entityUser.getGroupRoles()) {
            for (GroupFeature groupFeature : groupRole.getGroup().getGroupFeatures()) {
                if (groupFeature.getFeature().getName().equals(FeatureType.MESSAGING.toString())) {
                    return true;
                }
            }
        }

        return false;
    }

    // if user has any number of roles passed in, return true
    private boolean UserHasRole(User user, RoleName ... roleNames) {
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

    // verify all conversation users have messaging features and member of group with messaging enabled
    private boolean conversationUsersAndGroupsHaveMessagingFeatures(Conversation conversation) {
        int usersWithMessagingFeaturesCount = 0;

        for (ConversationUser conversationUser : conversation.getConversationUsers()) {

            // GLOBAL_ADMIN and PATIENT users always have messaging features
            if (UserHasRole(conversationUser.getUser(), RoleName.GLOBAL_ADMIN, RoleName.PATIENT)) {
                usersWithMessagingFeaturesCount++;
            } else if (userHasStaffMessagingFeatures(conversationUser.getUser())) {
                usersWithMessagingFeaturesCount++;
            }

            // check conversation user member of at least one group with messaging enabled
            if (!userGroupsHaveMessagingFeature(conversationUser.getUser())) {
                return false;
            }
        }

        return (conversation.getConversationUsers().size() == usersWithMessagingFeaturesCount);
    }

    // verify logged in user has messaging feature
    private boolean loggedInUserHasMessagingFeatures() {
        User loggedInUser = getCurrentUser();
        if (Util.doesContainRoles(RoleName.PATIENT, RoleName.GLOBAL_ADMIN)) {
            return true;
        }

        return userHasStaffMessagingFeatures(userRepository.findOne(loggedInUser.getId()));
    }

    // verify logged in user can open conversation
    private boolean loggedInUserIsMemberOfConversation(Conversation conversation) {
        User loggedInUser = getCurrentUser();

        for (ConversationUser conversationUser : conversation.getConversationUsers()) {
            if (conversationUser.getUser().getId().equals(loggedInUser.getId())) {
                return true;
            }
        }

        return false;
    }
}
