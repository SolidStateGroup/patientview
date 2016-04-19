package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.enums.DummyUsernames;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.LookupService;
import org.patientview.api.service.RoleService;
import org.patientview.api.service.SurveyResponseService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.ConversationUser;
import org.patientview.persistence.model.ConversationUserLabel;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.SurveyResponseScore;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.model.enums.ConversationLabel;
import org.patientview.persistence.model.enums.ConversationTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.MessageTypes;
import org.patientview.persistence.model.enums.QuestionElementTypes;
import org.patientview.persistence.model.enums.QuestionTypes;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.model.enums.StaffMessagingFeatureType;
import org.patientview.persistence.model.enums.SurveyResponseScoreTypes;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.persistence.repository.ConversationRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.QuestionOptionRepository;
import org.patientview.persistence.repository.QuestionRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.patientview.util.Util;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2015
 */
@Service
public class SurveyResponseServiceImpl extends AbstractServiceImpl<SurveyResponseServiceImpl>
        implements SurveyResponseService {

    @Inject
    private ConversationRepository conversationRepository;

    @Inject
    private EmailService emailService;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private LookupService lookupService;

    @Inject
    private Properties properties;

    @Inject
    private QuestionRepository questionRepository;

    @Inject
    private QuestionOptionRepository questionOptionRepository;

    @Inject
    private RoleService roleService;

    @Inject
    private SurveyRepository surveyRepository;

    @Inject
    private SurveyResponseRepository surveyResponseRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private UserTokenRepository userTokenRepository;

    @Override
    @Transactional
    public void add(Long userId, SurveyResponse surveyResponse)
            throws ResourceForbiddenException, ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Survey survey = surveyRepository.findOne(surveyResponse.getSurvey().getId());
        if (survey == null) {
            throw new ResourceNotFoundException("Could not find survey");
        }

        if (surveyResponse.getDate() == null) {
            throw new ResourceNotFoundException("Must include date");
        }

        // if survey type is IBD_SELF_MANAGEMENT then need to check that a staff user is viewing as another user
        if (survey.getType().equals(SurveyTypes.IBD_SELF_MANAGEMENT.toString())) {
            // check survey response has a staff token
            if (StringUtils.isEmpty(surveyResponse.getStaffToken())) {
                throw new ResourceForbiddenException("Forbidden (must be staff)");
            }

            // check staff token exists in db
            UserToken userToken = userTokenRepository.findByToken(surveyResponse.getStaffToken());
            if (userToken == null) {
                throw new ResourceForbiddenException("Forbidden (must be staff)");
            }

            // check userToken matches user
            if (userToken.getUser() == null) {
                throw new ResourceForbiddenException("Forbidden (must be staff)");
            }

            // check staff user can switch to current user
            if (!userService.userCanSwitchToUser(userToken.getUser(), getCurrentUser())) {
                throw new ResourceForbiddenException("Forbidden (must be staff)");
            }

            surveyResponse.setStaffUser(userToken.getUser());
        }

        SurveyResponse newSurveyResponse = new SurveyResponse();
        newSurveyResponse.setSurvey(survey);
        newSurveyResponse.setUser(user);
        newSurveyResponse.setDate(surveyResponse.getDate());
        newSurveyResponse.setStaffUser(surveyResponse.getStaffUser());

        for (QuestionAnswer questionAnswer : surveyResponse.getQuestionAnswers()) {
            QuestionAnswer newQuestionAnswer = new QuestionAnswer();
            newQuestionAnswer.setSurveyResponse(newSurveyResponse);
            boolean answer = false;

            if (questionAnswer.getQuestionOption() != null) {
                // if QuestionTypes.SINGLE_SELECT, will have question option
                QuestionOption questionOption
                        = questionOptionRepository.findOne(questionAnswer.getQuestionOption().getId());

                if (questionOption == null) {
                    throw new ResourceNotFoundException("Question option not found");
                }

                newQuestionAnswer.setQuestionOption(questionOption);
                answer = true;
            } else if (StringUtils.isNotEmpty(questionAnswer.getValue())) {
                // if QuestionTypes.SINGLE_SELECT_RANGE, will have value
                newQuestionAnswer.setValue(questionAnswer.getValue());
                answer = true;
            }

            if (answer) {
                Question question = questionRepository.findOne(questionAnswer.getQuestion().getId());
                if (question == null) {
                    throw new ResourceNotFoundException("Invalid question");
                }

                newQuestionAnswer.setQuestion(question);
                newSurveyResponse.getQuestionAnswers().add(newQuestionAnswer);
            }
        }

        if (newSurveyResponse.getQuestionAnswers().isEmpty()) {
            throw new ResourceNotFoundException("No valid answers");
        }

        if (survey.getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE.toString())
                || survey.getType().equals(SurveyTypes.COLITIS_SYMPTOM_SCORE.toString())
                || survey.getType().equals(SurveyTypes.HEART_SYMPTOM_SCORE.toString())) {
            SurveyResponseScoreTypes type = SurveyResponseScoreTypes.SYMPTOM_SCORE;
            Integer score = calculateScore(newSurveyResponse, type);

            newSurveyResponse.getSurveyResponseScores().add(new SurveyResponseScore(
                    newSurveyResponse, type.toString(), score, calculateSeverity(newSurveyResponse, score)));
        } else if (survey.getType().equals(SurveyTypes.IBD_CONTROL.toString())) {
            SurveyResponseScoreTypes type = SurveyResponseScoreTypes.IBD_CONTROL_EIGHT;
            Integer score = calculateScore(newSurveyResponse, type);
            newSurveyResponse.getSurveyResponseScores().add(new SurveyResponseScore(
                    newSurveyResponse, type.toString(), score, calculateSeverity(newSurveyResponse, score)));

            type = SurveyResponseScoreTypes.IBD_CONTROL_VAS;
            score = calculateScore(newSurveyResponse, type);
            newSurveyResponse.getSurveyResponseScores().add(new SurveyResponseScore(
                    newSurveyResponse, type.toString(), score, calculateSeverity(newSurveyResponse, score)));
        } else if (survey.getType().equals(SurveyTypes.IBD_FATIGUE.toString())) {
            SurveyResponseScoreTypes type = SurveyResponseScoreTypes.IBD_FATIGUE;
            Integer score = calculateScore(newSurveyResponse, type);
            newSurveyResponse.getSurveyResponseScores().add(new SurveyResponseScore(
                    newSurveyResponse, type.toString(), score, calculateSeverity(newSurveyResponse, score)));
        } else {
            newSurveyResponse.getSurveyResponseScores().add(new SurveyResponseScore(
                    newSurveyResponse, SurveyResponseScoreTypes.UNKNOWN.toString(), 0, ScoreSeverity.UNKNOWN));
        }

        surveyResponseRepository.save(newSurveyResponse);

        // send emails, secure messages if staff present in patient groups with IBD_SCORING_ALERTS feature
        sendScoringAlerts(user, survey, newSurveyResponse);
    }

    private void sendScoringAlerts(User user, Survey survey, SurveyResponse surveyResponse) {
        // send emails, secure messages if staff present in patient groups with IBD_SCORING_ALERTS feature
        if (survey.getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE.toString())
                || survey.getType().equals(SurveyTypes.COLITIS_SYMPTOM_SCORE.toString())
                || survey.getType().equals(SurveyTypes.IBD_FATIGUE.toString())) {

            // check if score warrants an alert sending
            boolean sendAlerts = false;
            for (SurveyResponseScore score : surveyResponse.getSurveyResponseScores()) {
                if (score.getSeverity().equals(ScoreSeverity.HIGH)) {
                    sendAlerts = true;
                }
            }

            if (sendAlerts) {
                // get groups, roles and feature id from user/IBD_SCORING_ALERTS feature to find staff to send alert
                Set<Long> groupIds = new HashSet<>();
                Set<Long> roleIds = new HashSet<>();
                List<Long> featureIds = new ArrayList<>();

                // attempt to get survey name from enum if matches known survey type
                String surveyName = survey.getType();
                if (Util.isInEnum(survey.getType(), SurveyTypes.class)) {
                    surveyName = SurveyTypes.valueOf(survey.getType()).getName();
                }

                // get specialty group type, used to avoid getting all staff users in a user's specialty
                Lookup specialtyGroupType
                        = lookupService.findByTypeAndValue(LookupTypes.GROUP, GroupTypes.SPECIALTY.toString());

                // get staff roles
                List<Role> staffRoles = roleService.getRolesByType(RoleType.STAFF);
                for (Role role : staffRoles) {
                    roleIds.add(role.getId());
                }

                // get IBD_SCORING_ALERTS feature
                Feature scoringAlertFeature = featureRepository.findByName(FeatureType.IBD_SCORING_ALERTS.toString());
                if (scoringAlertFeature != null) {
                    featureIds.add(scoringAlertFeature.getId());
                }

                // get user's groups (ignoring specialty)
                for (GroupRole groupRole : user.getGroupRoles()) {
                    if (!groupRole.getGroup().getGroupType().equals(specialtyGroupType)) {
                        groupIds.add(groupRole.getGroup().getId());
                    }
                }

                // get list of suitable staff users to send alerts to
                Page<User> staffUsers = userRepository.findStaffByGroupsRolesFeatures(
                        "%%", new ArrayList<>(groupIds), new ArrayList<>(roleIds), featureIds,
                        new PageRequest(0, Integer.MAX_VALUE));

                if (staffUsers != null) {
                    // only send secure message and email if PatientView Notifications exists
                    User notificationUser = userRepository.findByUsernameCaseInsensitive(
                            DummyUsernames.PATIENTVIEW_NOTIFICATIONS.getName());

                    // staff users must have both IBD_SCORING_ALERTS feature and messaging features
                    Set<User> usersWithFeature = new HashSet<>();
                    for (User staffUser : staffUsers.getContent()) {
                        boolean hasMessagingFeature = false;
                        for (UserFeature userFeature : staffUser.getUserFeatures()) {
                            if (ApiUtil.isInEnum(userFeature.getFeature().getName(), StaffMessagingFeatureType.class)) {
                                hasMessagingFeature = true;
                            }
                        }
                        if (hasMessagingFeature) {
                            usersWithFeature.add(staffUser);
                        }
                    }

                    if (!usersWithFeature.isEmpty()) {
                        if (notificationUser != null) {
                            // send secure message from PatientView Notifications (patientviewnotifications) user with
                            // patient details and score
                            Date now = new Date();
                            Conversation conversation = new Conversation();
                            conversation.setTitle("Poor " + surveyName + ": " + user.getUsername());
                            conversation.setType(ConversationTypes.MESSAGE);
                            conversation.setCreator(notificationUser);
                            conversation.setCreated(now);
                            conversation.setOpen(false);

                            // ConversationUsers and associated ConversationUserLabel
                            ConversationUser notificationConversationUser
                                    = new ConversationUser(conversation, notificationUser);
                            notificationConversationUser.setCreator(notificationUser);
                            notificationConversationUser.setAnonymous(false);

                            ConversationUserLabel notificationConversationUserLabel = new ConversationUserLabel();
                            notificationConversationUserLabel.setConversationUser(notificationConversationUser);
                            notificationConversationUserLabel.setCreator(notificationUser);
                            notificationConversationUserLabel.setConversationLabel(ConversationLabel.INBOX);

                            notificationConversationUser.setConversationUserLabels(
                                    new HashSet<ConversationUserLabel>());
                            notificationConversationUser.getConversationUserLabels()
                                    .add(notificationConversationUserLabel);

                            conversation.setConversationUsers(new HashSet<ConversationUser>());
                            conversation.getConversationUsers().add(notificationConversationUser);

                            // Conversation Message
                            conversation.setMessages(new ArrayList<Message>());
                            Message message = new Message();
                            message.setConversation(conversation);
                            message.setCreator(notificationUser);
                            message.setCreated(now);
                            message.setUser(notificationUser);
                            message.setType(MessageTypes.MESSAGE);

                            StringBuilder msg = new StringBuilder();
                            msg.append("An alert has been triggered by a poor score with details:<br/>");
                            msg.append("<br/>Name: ");
                            msg.append(user.getName());
                            msg.append("<br/>Username: ");
                            msg.append(user.getUsername());
                            msg.append("<br/>Identifier(s): ");

                            for (Identifier identifier : user.getIdentifiers()) {
                                msg.append(identifier.getIdentifier());
                                msg.append(" ");
                            }

                            msg.append("<br/>Score Type: ");
                            msg.append(surveyName);
                            msg.append("<br/>Score Date: ");
                            msg.append(CommonUtils.dateToSimpleString(surveyResponse.getDate()));

                            if (!CollectionUtils.isEmpty(surveyResponse.getSurveyResponseScores())) {
                                msg.append("<br/>Score: ");
                                for (SurveyResponseScore score : surveyResponse.getSurveyResponseScores()) {
                                    msg.append(score.getScore());
                                    if (score.getSeverity() != null) {
                                        msg.append(" (");
                                        msg.append(score.getSeverity().getName());
                                        msg.append(")");
                                    }
                                }
                            }

                            message.setMessage(msg.toString());
                            conversation.getMessages().add(message);

                            // set updated, used in UI to order conversations
                            conversation.setLastUpdate(now);
                            conversation.setLastUpdater(notificationUser);

                            for (User staffUser : staffUsers.getContent()) {
                                // add conversation users to conversation
                                ConversationUser staffConversationUser = new ConversationUser(conversation, staffUser);
                                staffConversationUser.setCreator(notificationUser);
                                staffConversationUser.setAnonymous(false);

                                ConversationUserLabel staffConversationUserLabel = new ConversationUserLabel();
                                staffConversationUserLabel.setConversationUser(staffConversationUser);
                                staffConversationUserLabel.setCreator(notificationUser);
                                staffConversationUserLabel.setConversationLabel(ConversationLabel.INBOX);

                                staffConversationUser.setConversationUserLabels(new HashSet<ConversationUserLabel>());
                                staffConversationUser.getConversationUserLabels().add(staffConversationUserLabel);

                                conversation.getConversationUsers().add(staffConversationUser);

                                // send personalised email to staff member with no patient identifiable information
                                Email email = new Email();
                                email.setSenderEmail(properties.getProperty("smtp.sender.email"));
                                email.setSenderName(properties.getProperty("smtp.sender.name"));
                                email.setSubject("PatientView - " + surveyName + " Alert Recorded");
                                email.setRecipients(new String[]{staffUser.getEmail()});

                                email.setBody("Dear " + staffUser.getName()
                                        + ", <br/><br/>A patient has recorded a poor "
                                        + surveyName
                                        + "  on <a href=\""
                                        + properties.getProperty("site.url")
                                        + "\">PatientView</a>"
                                        + "<br/><br/>Please log in to view a message containing more details.<br/>");

                                // try and send but ignore if exception and log
                                try {
                                    emailService.sendEmail(email);
                                } catch (MailException | MessagingException me) {
                                    LOG.error("Cannot send scoring alert email (continuing): {}", me);
                                }
                            }

                            // persist conversation
                            conversationRepository.save(conversation);
                        } else {
                            LOG.error("Cannot send scoring alert conversation, email "
                                    + "(PatientView Notifications does not exist)");
                        }
                    }
                }
            }
        }
    }

    private Integer calculateScore(SurveyResponse surveyResponse, SurveyResponseScoreTypes type) {
        Map<String, Integer> questionTypeScoreMap = new HashMap<>();
        for (QuestionAnswer questionAnswer : surveyResponse.getQuestionAnswers()) {
            if (questionAnswer.getQuestionOption() != null
                    && questionAnswer.getQuestionOption().getScore() != null
                    && questionAnswer.getQuestion() != null
                    && questionAnswer.getQuestion().getType() != null) {
                questionTypeScoreMap.put(
                    questionAnswer.getQuestion().getType(), questionAnswer.getQuestionOption().getScore());
            }

            // add scoring for ranged values
            if (questionAnswer.getQuestion() != null
                    && questionAnswer.getQuestion().getType() != null
                    && questionAnswer.getQuestion().getElementType().equals(QuestionElementTypes.SINGLE_SELECT_RANGE)
                    && questionAnswer.getValue() != null) {
                try {
                    questionTypeScoreMap.put(
                            questionAnswer.getQuestion().getType(), Integer.valueOf(questionAnswer.getValue()));
                } catch (NumberFormatException e) {
                    questionTypeScoreMap.put(questionAnswer.getQuestion().getType(), 0);
                }
            }
        }

        Integer score = 0;

        if (surveyResponse.getSurvey().getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE.toString())) {
            if (questionTypeScoreMap.get(QuestionTypes.OPEN_BOWELS.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.OPEN_BOWELS.toString());
            }
            if (questionTypeScoreMap.get(QuestionTypes.ABDOMINAL_PAIN.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.ABDOMINAL_PAIN.toString());
            }
            if (questionTypeScoreMap.get(QuestionTypes.MASS_IN_TUMMY.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.MASS_IN_TUMMY.toString());
            }
            if (questionTypeScoreMap.get(QuestionTypes.COMPLICATION.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.COMPLICATION.toString());
            }
            if (questionTypeScoreMap.get(QuestionTypes.FEELING.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.FEELING.toString());
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.COLITIS_SYMPTOM_SCORE.toString())) {
            if (questionTypeScoreMap.get(QuestionTypes.NUMBER_OF_STOOLS_DAYTIME.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.NUMBER_OF_STOOLS_DAYTIME.toString());
            }
            if (questionTypeScoreMap.get(QuestionTypes.NUMBER_OF_STOOLS_NIGHTTIME.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.NUMBER_OF_STOOLS_NIGHTTIME.toString());
            }
            if (questionTypeScoreMap.get(QuestionTypes.TOILET_TIMING.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.TOILET_TIMING.toString());
            }
            if (questionTypeScoreMap.get(QuestionTypes.PRESENT_BLOOD.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.PRESENT_BLOOD.toString());
            }
            if (questionTypeScoreMap.get(QuestionTypes.COMPLICATION.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.COMPLICATION.toString());
            }
            if (questionTypeScoreMap.get(QuestionTypes.FEELING.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.FEELING.toString());
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.IBD_CONTROL.toString())) {
            if (type.equals(SurveyResponseScoreTypes.IBD_CONTROL_EIGHT)) {
                if (questionTypeScoreMap.get(QuestionTypes.IBD_CONTROLLED_TWO_WEEKS.toString()) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_CONTROLLED_TWO_WEEKS.toString());
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_CONTROLLED_CURRENT_TREATMENT.toString()) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_CONTROLLED_CURRENT_TREATMENT.toString());
                } else {
                    if (questionTypeScoreMap.get(QuestionTypes.IBD_NO_TREATMENT.toString()) != null) {
                        score += questionTypeScoreMap.get(QuestionTypes.IBD_NO_TREATMENT.toString());
                    }
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_MISS_PLANNED_ACTIVITIES.toString()) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_MISS_PLANNED_ACTIVITIES.toString());
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_WAKE_UP.toString()) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_WAKE_UP.toString());
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_SIGNIFICANT_PAIN.toString()) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_SIGNIFICANT_PAIN.toString());
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_LACKING_ENERGY.toString()) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_LACKING_ENERGY.toString());
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_FEEL_ANXIOUS.toString()) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_FEEL_ANXIOUS.toString());
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_NEED_CHANGE.toString()) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_NEED_CHANGE.toString());
                }
            } else if (type.equals(SurveyResponseScoreTypes.IBD_CONTROL_VAS)) {
                if (questionTypeScoreMap.get(QuestionTypes.IBD_OVERALL_CONTROL.toString()) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_OVERALL_CONTROL.toString());
                }
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.HEART_SYMPTOM_SCORE.toString())) {
            if (questionTypeScoreMap.get(QuestionTypes.HEART_SWELLING.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.HEART_SWELLING.toString());
            }
            if (questionTypeScoreMap.get(QuestionTypes.HEART_FATIGUE.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.HEART_FATIGUE.toString());
            }
            if (questionTypeScoreMap.get(QuestionTypes.HEART_SHORTNESS_OF_BREATH.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.HEART_SHORTNESS_OF_BREATH.toString());
            }
            if (questionTypeScoreMap.get(QuestionTypes.HEART_SHORTNESS_OF_BREATH_SLEEP.toString()) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.HEART_SHORTNESS_OF_BREATH_SLEEP.toString());
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.IBD_FATIGUE.toString())) {
            // section 1 & 2
            for (QuestionTypes questionType : QuestionTypes.values()) {
                if ((questionType.toString().contains("IBD_FATIGUE_I") || questionType.toString().contains("IBD_DAS"))
                        && questionTypeScoreMap.get(questionType.toString()) != null) {
                    score += questionTypeScoreMap.get(questionType.toString());
                }
            }
        }

        return score;
    }

    // note: these are hardcoded
    private ScoreSeverity calculateSeverity(SurveyResponse surveyResponse, Integer score) {
        if (surveyResponse.getSurvey().getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE.toString())) {
            if (score != null) {
                if (score >= 16) {
                    return ScoreSeverity.HIGH;
                } else if (score >= 4) {
                    return ScoreSeverity.MEDIUM;
                } else if (score < 4) {
                    return ScoreSeverity.LOW;
                }
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.COLITIS_SYMPTOM_SCORE.toString())) {
            if (score != null) {
                if (score >= 10) {
                    return ScoreSeverity.HIGH;
                } else if (score >= 4) {
                    return ScoreSeverity.MEDIUM;
                } else if (score < 4) {
                    return ScoreSeverity.LOW;
                }
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.HEART_SYMPTOM_SCORE.toString())) {
            if (score != null) {
                if (score >= 16) {
                    return ScoreSeverity.LOW;
                } else if (score > 8) {
                    return ScoreSeverity.MEDIUM;
                } else if (score <= 8) {
                    return ScoreSeverity.HIGH;
                }
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.IBD_FATIGUE.toString())) {
            if (score != null) {
                if (score >= 80) {
                    return ScoreSeverity.HIGH;
                } else if (score >= 40) {
                    return ScoreSeverity.MEDIUM;
                } else if (score < 40) {
                    return ScoreSeverity.LOW;
                }
            }
        }

        return ScoreSeverity.UNKNOWN;
    }

    @Override
    public List<SurveyResponse> getByUserIdAndSurveyType(Long userId, String surveyType)
            throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }
        if (surveyType == null) {
            throw new ResourceNotFoundException("Must set survey type");
        }

        List<SurveyResponse> responses = surveyResponseRepository.findByUserAndSurveyType(user, surveyType);

        // clean up and reduced info about staff user if present
        if (!CollectionUtils.isEmpty(responses)) {
            List<SurveyResponse> reducedResponses = new ArrayList<>();
            for (SurveyResponse surveyResponse : responses) {
                reducedResponses.add(reduceStaffUser(surveyResponse));
            }

            responses = reducedResponses;
        }

        return responses;
    }

    @Override
    public SurveyResponse getSurveyResponse(Long userId, Long surveyResponseId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        return reduceStaffUser(surveyResponseRepository.findOne(surveyResponseId));
    }

    /**
     * Reduce the information passed back to ui about staff user (only used when a staff member fills in survey when
     * viewing as a patient
     * @param surveyResponse SurveyResponse to reduce staff user information for
     * @return SurveyResponse where staff user info has been reduced
     */
    private SurveyResponse reduceStaffUser(SurveyResponse surveyResponse) {
        if (surveyResponse.getStaffUser() != null) {
            // create new reduced staff user
            User newStaffUser = new User();

            try {
                User staffUser = userRepository.getOne(surveyResponse.getStaffUser().getId());

                if (staffUser != null) {
                    newStaffUser.setGroupRoles(new HashSet<GroupRole>());
                    newStaffUser.setForename(staffUser.getForename());
                    newStaffUser.setSurname(staffUser.getSurname());
                    newStaffUser.setId(staffUser.getId());

                    if (!CollectionUtils.isEmpty(staffUser.getGroupRoles())) {
                        for (GroupRole groupRole : staffUser.getGroupRoles()) {
                            GroupRole newGroupRole = new GroupRole();

                            // generate cut down groups and roles
                            Group newGroup = new Group();
                            newGroup.setName(groupRole.getGroup().getName());
                            newGroup.setShortName(groupRole.getGroup().getShortName());
                            newGroup.setCode(groupRole.getGroup().getCode());
                            newGroup.setGroupType(groupRole.getGroup().getGroupType());

                            Role newRole = new Role();
                            newRole.setName(groupRole.getRole().getName());
                            newRole.setDescription(groupRole.getRole().getDescription());

                            // add to new reduced staff user
                            newGroupRole.setGroup(newGroup);
                            newGroupRole.setRole(newRole);
                            newStaffUser.getGroupRoles().add(newGroupRole);
                        }
                    }
                }
            } catch (EntityNotFoundException enf) {
                newStaffUser.setName("Unknown");
            }

            surveyResponse.setStaffUser(newStaffUser);
        }

        return surveyResponse;
    }
}
