package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.patientview.api.client.FirebaseClient;
import org.patientview.api.model.ContactAlert;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.Group;
import org.patientview.api.model.ImportAlert;
import org.patientview.api.service.AlertService;
import org.patientview.api.service.ApiObservationService;
import org.patientview.api.service.DocumentService;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.GroupService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/01/2015
 */
@Service
public class AlertServiceImpl extends AbstractServiceImpl<AlertServiceImpl> implements AlertService {

    @Inject
    private AlertRepository alertRepository;

    @Inject
    private AuditRepository auditRepository;

    @Inject
    private EmailService emailService;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupService groupService;

    @Inject
    private DocumentService documentService;

    @Inject
    private ApiObservationService apiObservationService;

    @Inject
    private ObservationHeadingRepository observationHeadingRepository;

    @Inject
    private Properties properties;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private FirebaseClient notificationClient;

    @Override
    public org.patientview.api.model.Alert addAlert(Long userId, org.patientview.api.model.Alert alert)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Alert newAlert = new Alert();

        if (alert.getAlertType() == null) {
            throw new ResourceNotFoundException("No alert type");
        }

        if (alert.getAlertType().equals(AlertTypes.RESULT)) {
            if (alert.getObservationHeading() != null) {
                ObservationHeading observationHeading
                        = observationHeadingRepository.findOne(alert.getObservationHeading().getId());
                if (observationHeading == null) {
                    throw new ResourceNotFoundException("Could not find result type");
                }

                // need to make sure we only have one alert for this result type per user
                List<Alert> alerts = alertRepository.findByUserAndObservationHeading(user, observationHeading);
                if (!CollectionUtils.isEmpty(alerts)) {
                    throw new ResourceForbiddenException("Alert for result already exist");
                }

                List<FhirObservation> fhirObservations
                        = apiObservationService.get(user.getId(), observationHeading.getCode(), null, null, null);

                if (!CollectionUtils.isEmpty(fhirObservations)) {

                    // order by date desc
                    Collections.sort(fhirObservations, new Comparator<FhirObservation>() {
                        @Override
                        public int compare(FhirObservation o1, FhirObservation o2) {
                            return o2.getApplies().compareTo(o1.getApplies());
                        }
                    });

                    newAlert.setLatestValue(fhirObservations.get(0).getValue());
                    newAlert.setLatestDate(fhirObservations.get(0).getApplies());
                }

                newAlert.setObservationHeading(observationHeading);
                newAlert.setAlertType(AlertTypes.RESULT);
            } else {
                throw new ResourceNotFoundException("Result type not set");
            }
        } else if (alert.getAlertType().equals(AlertTypes.LETTER)) {

            List<FhirDocumentReference> fhirDocumentReferences
                    = documentService.getByUserIdAndClass(user.getId(), null, null, null);

            if (!CollectionUtils.isEmpty(fhirDocumentReferences)) {
                newAlert.setLatestValue(fhirDocumentReferences.get(0).getType());
                newAlert.setLatestDate(fhirDocumentReferences.get(0).getDate());
            }

            newAlert.setAlertType(AlertTypes.LETTER);
        } else {
            throw new ResourceNotFoundException("Incorrect alert type");
        }

        newAlert.setUser(user);
        newAlert.setWebAlert(alert.isWebAlert());
        newAlert.setWebAlertViewed(true);
        newAlert.setEmailAlert(alert.isEmailAlert());
        newAlert.setEmailAlertSent(true);
        newAlert.setMobileAlert(alert.isMobileAlert());
        newAlert.setMobileAlertSent(true);
        newAlert.setCreated(new Date());
        newAlert.setCreator(user);

        newAlert = alertRepository.save(newAlert);
        return new org.patientview.api.model.Alert(newAlert, user);
    }

    @Override
    public List<org.patientview.api.model.Alert> getAlerts(Long userId, AlertTypes alertType)
            throws ResourceNotFoundException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<Alert> alerts = alertRepository.findByUserAndAlertType(user, alertType);
        List<org.patientview.api.model.Alert> transportAlerts = new ArrayList<>();

        for (Alert alert : alerts) {
            transportAlerts.add(new org.patientview.api.model.Alert(alert, user));
        }

        return transportAlerts;
    }

    @Override
    public List<ContactAlert> getContactAlerts(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<Group> groups
                = groupService.getUserGroups(userId, new GetParameters()).getContent();
        List<ContactAlert> contactAlerts = new ArrayList<>();

        for (Group group : groups) {
            if (!group.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                if (groupIsMissingUserWithFeature(group, FeatureType.DEFAULT_MESSAGING_CONTACT)) {
                    contactAlerts.add(new ContactAlert(group, FeatureType.DEFAULT_MESSAGING_CONTACT));
                }
                if (groupIsMissingUserWithFeature(group, FeatureType.PATIENT_SUPPORT_CONTACT)) {
                    contactAlerts.add(new ContactAlert(group, FeatureType.PATIENT_SUPPORT_CONTACT));
                }
                if (groupIsMissingUserWithFeature(group, FeatureType.UNIT_TECHNICAL_CONTACT)) {
                    contactAlerts.add(new ContactAlert(group, FeatureType.UNIT_TECHNICAL_CONTACT));
                }
            }
        }

        return contactAlerts;
    }

    @Override
    public List<ImportAlert> getImportAlerts(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<Group> groups = groupService.getUserGroups(userId, new GetParameters()).getContent();
        List<Long> groupIds = new ArrayList<>();
        Map<Long, Group> groupMap = new HashMap<>();
        List<ImportAlert> importAlerts = new ArrayList<>();

        for (Group group : groups) {
            if (group.getGroupType().getValue().equals(GroupTypes.UNIT.toString())) {
                groupIds.add(group.getId());
                groupMap.put(group.getId(), group);
            }
        }

        if (CollectionUtils.isEmpty(groupIds)) {
            return importAlerts;
        }

        Date oneWeekAgo = new DateTime(new Date()).minusWeeks(1).toDate();
        List<AuditActions> auditActions = new ArrayList<>();
        auditActions.add(AuditActions.PATIENT_DATA_FAIL);
        auditActions.add(AuditActions.PATIENT_DATA_VALIDATE_FAIL);

        List<Object[]> audits = auditRepository.findAllByCountGroupAction(groupIds, oneWeekAgo, auditActions);

        if (CollectionUtils.isEmpty(audits)) {
            return importAlerts;
        }

        for (Object[] obj : audits) {
            if (obj[0] != null) {
                Group group = groupMap.get(obj[0]);
                if (group != null) {
                    ImportAlert importAlert = new ImportAlert(groupMap.get((Long) obj[0]), (Long) obj[1], oneWeekAgo);
                    importAlerts.add(importAlert);
                }
            }
        }

        return importAlerts;
    }

    private boolean groupIsMissingUserWithFeature(Group group, FeatureType featureType) {
        List<Role> roles = roleRepository.findByRoleType(RoleType.STAFF);
        List<Long> roleIds = new ArrayList<>();
        for (Role role : roles) {
            roleIds.add(role.getId());
        }

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());

        List<Long> featureIds = new ArrayList<>();
        Feature feature = featureRepository.findByName(featureType.toString());
        featureIds.add(feature.getId());

        return !(userRepository.findStaffByGroupsRolesFeatures("%%", groupIds, roleIds, featureIds,
                new PageRequest(0, Integer.MAX_VALUE)).getTotalElements() > 0L);
    }

    @Override
    @Async
    public void sendAlertEmails() {
        List<Alert> alerts = alertRepository.findByEmailAlertSetAndNotSent();
        //LOG.info("Alerts: " + alerts.size() + " alerts found with email alert set and not sent");
        Set<String> emailAddresses = new HashSet<>();

        for (Alert alert : alerts) {
            String email = alertRepository.findOne(alert.getId()).getUser().getEmail();
            if (StringUtils.isNotEmpty(email)) {
                emailAddresses.add(email);
            }
        }

        if (!CollectionUtils.isEmpty(emailAddresses)) {
            LOG.info("Alerts: Sending new data emails to " + emailAddresses.size() + " patients");
            Email email = new Email();
            email.setBcc(true);
            email.setSenderEmail(properties.getProperty("smtp.sender.email"));
            email.setSenderName(properties.getProperty("smtp.sender.name"));
            email.setRecipients(emailAddresses.toArray(new String[emailAddresses.size()]));
            email.setSubject("PatientView - You have new data");

            StringBuilder sb = new StringBuilder();
            sb.append("Dear Sir/Madam");
            sb.append(", <br/><br/>New data has arrived on <a href=\"");
            sb.append(properties.getProperty("site.url"));
            sb.append("\">PatientView</a>");
            sb.append("<br/><br/>Please log in to view.<br/>");
            email.setBody(sb.toString());

            try {
                emailService.sendEmail(email);
                Date now = new Date();
                for (Alert alert : alerts) {
                    alert.setEmailAlertSent(true);
                    alert.setLastUpdate(now);
                    alertRepository.save(alert);
                }
            } catch (MessagingException | MailException me) {
                LOG.error("Could not bulk send result alert emails: ", me);
            }
        }
    }

    @Override
    @Async
    public void sendIndividualAlertEmails() {
        List<Alert> alerts = alertRepository.findByEmailAlertSetAndNotSent();
        //LOG.info("Alerts: " + alerts.size() + " alerts found with email alert set and not sent");
        Set<String> emailAddresses = new HashSet<>();

        for (Alert alert : alerts) {
            String email = alertRepository.findOne(alert.getId()).getUser().getEmail();
            if (StringUtils.isNotEmpty(email)) {
                emailAddresses.add(email);
            }
        }

        if (!CollectionUtils.isEmpty(emailAddresses)) {
            LOG.info("Alerts: Sending new data emails to " + emailAddresses.size() + " patients");
            for (String emailAddress : emailAddresses) {
                String[] recipients = {emailAddress};
                Email email = new Email();
                email.setBcc(false);
                email.setSenderEmail(properties.getProperty("smtp.sender.email"));
                email.setSenderName(properties.getProperty("smtp.sender.name"));
                email.setRecipients(recipients);
                email.setSubject("PatientView - You have new data");

                StringBuilder sb = new StringBuilder();
                sb.append("Dear Sir/Madam");
                sb.append(", <br/><br/>New data has arrived on <a href=\"");
                sb.append(properties.getProperty("site.url"));
                sb.append("\">PatientView</a>");
                sb.append("<br/><br/>Please log in to view.<br/>");
                email.setBody(sb.toString());

                try {
                    emailService.sendEmail(email);
                } catch (MessagingException | MailException me) {
                    LOG.error("Could not bulk send result alert emails: ", me);
                }
            }
        }

        Date now = new Date();
        for (Alert alert : alerts) {
            alert.setEmailAlertSent(true);
            alert.setLastUpdate(now);
            alertRepository.save(alert);
        }
    }

    @Override
    @Async
    public void pushNotifications() {
        List<Alert> alerts = alertRepository.findByMobileAlertSetAndNotSent();
        LOG.info("Notifications: " + alerts.size() + " alerts found for push notification");

        // Patient might have multiple alerts setup,
        // we only need to send one notification per Patient
        Set<Long> userIds = new HashSet<>();
        for (Alert alert : alerts) {
            userIds.add(alert.getUser().getId());
        }

        // send notification to user using firebase
        for (Long userId : userIds) {
            notificationClient.notifyResult(userId);
        }

        Date now = new Date();
        for (Alert alert : alerts) {
            alert.setMobileAlertSent(true);
            alert.setLastUpdate(now);
            alertRepository.save(alert);
        }
    }

    @Override
    public void removeAlert(Long userId, Long alertId) throws ResourceNotFoundException, ResourceForbiddenException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Alert alert = alertRepository.findOne(alertId);
        if (alert == null) {
            throw new ResourceNotFoundException("Could not find alert");
        }

        if (!user.getId().equals(alert.getUser().getId())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        alertRepository.delete(alert);
    }

    @Override
    public void updateAlert(Long userId, org.patientview.api.model.Alert alert)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Alert entityAlert = alertRepository.findOne(alert.getId());
        if (entityAlert == null) {
            throw new ResourceNotFoundException("Could not find alert");
        }

        if (!user.getId().equals(entityAlert.getUser().getId())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        if (alert.getAlertType().equals(AlertTypes.RESULT)) {
            entityAlert.setWebAlert(alert.isWebAlert());
            entityAlert.setWebAlertViewed(alert.isWebAlertViewed());
            entityAlert.setEmailAlert(alert.isEmailAlert());
            entityAlert.setMobileAlert(alert.isMobileAlert());
            alertRepository.save(entityAlert);
        } else if (alert.getAlertType().equals(AlertTypes.LETTER)) {
            if (!alert.isWebAlert() && !alert.isEmailAlert()) {
                alertRepository.delete(entityAlert);
            } else {
                entityAlert.setWebAlert(alert.isWebAlert());
                entityAlert.setWebAlertViewed(alert.isWebAlertViewed());
                entityAlert.setEmailAlert(alert.isEmailAlert());
                entityAlert.setMobileAlert(alert.isMobileAlert());
                alertRepository.save(entityAlert);
            }
        } else {
            throw new ResourceNotFoundException("Incorrect alert type");
        }
    }
}
