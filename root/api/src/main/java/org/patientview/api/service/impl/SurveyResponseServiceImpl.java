package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.enums.DummyUsernames;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.LookupService;
import org.patientview.api.service.RoleService;
import org.patientview.api.service.SurveyResponseService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.ConversationUser;
import org.patientview.persistence.model.ConversationUserLabel;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.Feature;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.mail.MessagingException;
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

    @Override
    @Transactional
    public void add(Long userId, SurveyResponse surveyResponse) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Survey survey = surveyRepository.findOne(surveyResponse.getSurvey().getId());
        if (survey == null) {
            throw new ResourceNotFoundException("Could not find survey");
        }

        if (surveyResponse.getDate() == null) {
            throw new ResourceNotFoundException("Must include symptom score date");
        }

        SurveyResponse newSurveyResponse = new SurveyResponse();
        newSurveyResponse.setSurvey(survey);
        newSurveyResponse.setUser(user);
        newSurveyResponse.setDate(surveyResponse.getDate());

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

        if (survey.getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE)
                || survey.getType().equals(SurveyTypes.COLITIS_SYMPTOM_SCORE)
                || survey.getType().equals(SurveyTypes.HEART_SYMPTOM_SCORE)) {
            SurveyResponseScoreTypes type = SurveyResponseScoreTypes.SYMPTOM_SCORE;
            Integer score = calculateScore(newSurveyResponse, type);

            newSurveyResponse.getSurveyResponseScores().add(
                new SurveyResponseScore(newSurveyResponse, type, score, calculateSeverity(newSurveyResponse, score)));
        } else if (survey.getType().equals(SurveyTypes.IBD_CONTROL)) {
            SurveyResponseScoreTypes type = SurveyResponseScoreTypes.IBD_CONTROL_EIGHT;
            Integer score = calculateScore(newSurveyResponse, type);
            newSurveyResponse.getSurveyResponseScores().add(
                new SurveyResponseScore(newSurveyResponse, type, score, calculateSeverity(newSurveyResponse, score)));

            type = SurveyResponseScoreTypes.IBD_CONTROL_VAS;
            score = calculateScore(newSurveyResponse, type);
            newSurveyResponse.getSurveyResponseScores().add(
                new SurveyResponseScore(newSurveyResponse, type, score, calculateSeverity(newSurveyResponse, score)));
        } else if (survey.getType().equals(SurveyTypes.IBD_FATIGUE)) {
            SurveyResponseScoreTypes type = SurveyResponseScoreTypes.IBD_FATIGUE;
            Integer score = calculateScore(newSurveyResponse, type);
            newSurveyResponse.getSurveyResponseScores().add(
                new SurveyResponseScore(newSurveyResponse, type, score, calculateSeverity(newSurveyResponse, score)));
        } else {
            newSurveyResponse.getSurveyResponseScores().add(
                new SurveyResponseScore(newSurveyResponse, SurveyResponseScoreTypes.UNKNOWN, 0, ScoreSeverity.UNKNOWN));
        }

        surveyResponseRepository.save(newSurveyResponse);

        // send emails, secure messages if staff present in patient groups with IBD_SCORING_ALERTS feature
        sendScoringAlerts(user, survey, newSurveyResponse);
    }

    private void sendScoringAlerts(User user, Survey survey, SurveyResponse surveyResponse) {
        // send emails, secure messages if staff present in patient groups with IBD_SCORING_ALERTS feature
        if (survey.getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE)
                || survey.getType().equals(SurveyTypes.COLITIS_SYMPTOM_SCORE)) {

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
                            if (Util.isInEnum(userFeature.getFeature().getName(), StaffMessagingFeatureType.class)) {
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
                            conversation.setTitle("Poor Symptom Score: " + user.getUsername());
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
                            msg.append("A poor symptom score has been entered by a patient with details:<br/>");
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
                            msg.append(survey.getType().getName());
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
                                email.setSubject("PatientView - Poor Symptom Score Recorded");
                                email.setRecipients(new String[]{staffUser.getEmail()});

                                email.setBody("Dear " + staffUser.getName()
                                        + ", <br/><br/>A patient has recorded a poor symptom score on <a href=\""
                                        + properties.getProperty("site.url") + "\">PatientView</a>"
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
        Map<QuestionTypes, Integer> questionTypeScoreMap = new HashMap<>();
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

        if (surveyResponse.getSurvey().getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE)) {
            if (questionTypeScoreMap.get(QuestionTypes.OPEN_BOWELS) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.OPEN_BOWELS);
            }
            if (questionTypeScoreMap.get(QuestionTypes.ABDOMINAL_PAIN) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.ABDOMINAL_PAIN);
            }
            if (questionTypeScoreMap.get(QuestionTypes.MASS_IN_TUMMY) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.MASS_IN_TUMMY);
            }
            if (questionTypeScoreMap.get(QuestionTypes.COMPLICATION) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.COMPLICATION);
            }
            if (questionTypeScoreMap.get(QuestionTypes.FEELING) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.FEELING);
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.COLITIS_SYMPTOM_SCORE)) {
            if (questionTypeScoreMap.get(QuestionTypes.NUMBER_OF_STOOLS_DAYTIME) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.NUMBER_OF_STOOLS_DAYTIME);
            }
            if (questionTypeScoreMap.get(QuestionTypes.NUMBER_OF_STOOLS_NIGHTTIME) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.NUMBER_OF_STOOLS_NIGHTTIME);
            }
            if (questionTypeScoreMap.get(QuestionTypes.TOILET_TIMING) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.TOILET_TIMING);
            }
            if (questionTypeScoreMap.get(QuestionTypes.PRESENT_BLOOD) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.PRESENT_BLOOD);
            }
            if (questionTypeScoreMap.get(QuestionTypes.COMPLICATION) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.COMPLICATION);
            }
            if (questionTypeScoreMap.get(QuestionTypes.FEELING) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.FEELING);
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.IBD_CONTROL)) {
            if (type.equals(SurveyResponseScoreTypes.IBD_CONTROL_EIGHT)) {
                if (questionTypeScoreMap.get(QuestionTypes.IBD_CONTROLLED_TWO_WEEKS) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_CONTROLLED_TWO_WEEKS);
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_CONTROLLED_CURRENT_TREATMENT) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_CONTROLLED_CURRENT_TREATMENT);
                } else {
                    if (questionTypeScoreMap.get(QuestionTypes.IBD_NO_TREATMENT) != null) {
                        score += questionTypeScoreMap.get(QuestionTypes.IBD_NO_TREATMENT);
                    }
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_MISS_PLANNED_ACTIVITIES) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_MISS_PLANNED_ACTIVITIES);
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_WAKE_UP) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_WAKE_UP);
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_SIGNIFICANT_PAIN) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_SIGNIFICANT_PAIN);
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_LACKING_ENERGY) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_LACKING_ENERGY);
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_FEEL_ANXIOUS) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_FEEL_ANXIOUS);
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_NEED_CHANGE) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_NEED_CHANGE);
                }
            } else if (type.equals(SurveyResponseScoreTypes.IBD_CONTROL_VAS)) {
                if (questionTypeScoreMap.get(QuestionTypes.IBD_OVERALL_CONTROL) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_OVERALL_CONTROL);
                }
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.HEART_SYMPTOM_SCORE)) {
            if (questionTypeScoreMap.get(QuestionTypes.HEART_SWELLING) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.HEART_SWELLING);
            }
            if (questionTypeScoreMap.get(QuestionTypes.HEART_FATIGUE) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.HEART_FATIGUE);
            }
            if (questionTypeScoreMap.get(QuestionTypes.HEART_SHORTNESS_OF_BREATH) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.HEART_SHORTNESS_OF_BREATH);
            }
            if (questionTypeScoreMap.get(QuestionTypes.HEART_SHORTNESS_OF_BREATH_SLEEP) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.HEART_SHORTNESS_OF_BREATH_SLEEP);
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.IBD_FATIGUE)) {
            // section 1 & 2
            for (QuestionTypes questionType : QuestionTypes.values()) {
                if ((questionType.toString().contains("IBD_FATIGUE_I") || questionType.toString().contains("IBD_DAS"))
                        && questionTypeScoreMap.get(questionType) != null) {
                    score += questionTypeScoreMap.get(questionType);
                }
            }
        }

        return score;
    }

    // note: these are hardcoded
    private ScoreSeverity calculateSeverity(SurveyResponse surveyResponse, Integer score) {
        if (surveyResponse.getSurvey().getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE)) {
            if (score != null) {
                if (score >= 16) {
                    return ScoreSeverity.HIGH;
                } else if (score >= 4) {
                    return ScoreSeverity.MEDIUM;
                } else if (score < 4) {
                    return ScoreSeverity.LOW;
                }
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.COLITIS_SYMPTOM_SCORE)) {
            if (score != null) {
                if (score >= 10) {
                    return ScoreSeverity.HIGH;
                } else if (score >= 4) {
                    return ScoreSeverity.MEDIUM;
                } else if (score < 4) {
                    return ScoreSeverity.LOW;
                }
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.HEART_SYMPTOM_SCORE)) {
            if (score != null) {
                if (score >= 16) {
                    return ScoreSeverity.LOW;
                } else if (score > 8) {
                    return ScoreSeverity.MEDIUM;
                } else if (score <= 8) {
                    return ScoreSeverity.HIGH;
                }
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.IBD_FATIGUE)) {
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
    public List<SurveyResponse> getByUserIdAndSurveyType(Long userId, SurveyTypes surveyType)
            throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }
        if (surveyType == null) {
            throw new ResourceNotFoundException("Must set survey type");
        }

        return surveyResponseRepository.findByUserAndSurveyType(user, surveyType);
    }

    @Override
    public SurveyResponse getSurveyResponse(Long userId, Long surveyResponseId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        return surveyResponseRepository.findOne(surveyResponseId);
    }
}
