package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.ContactAlert;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.api.service.AlertService;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.LetterService;
import org.patientview.api.service.ObservationService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
    private EmailService emailService;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupService groupService;

    @Inject
    private LetterService letterService;

    @Inject
    private ObservationService observationService;

    @Inject
    private ObservationHeadingRepository observationHeadingRepository;

    @Inject
    private Properties properties;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private UserRepository userRepository;

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

        List<Group> groups = groupService.findGroupsByUser(user);
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
                new PageRequest(0, Integer.MAX_VALUE)).getTotalElements() > 0l);
    }

    @Override
    public void addAlert(Long userId, org.patientview.api.model.Alert alert)
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

                List<FhirObservation> fhirObservations
                    = observationService.get(user.getId(), observationHeading.getCode(), null, null, null);

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

            List<FhirDocumentReference> fhirDocumentReferences = letterService.getByUserId(user.getId());

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
        newAlert.setCreated(new Date());
        newAlert.setCreator(user);

        alertRepository.save(newAlert);
    }

    @Override
    public void sendAlertEmails() {

        List<Alert> alerts = alertRepository.findByEmailAlertSetAndNotSent();

        Set<String> emailAddresses = new HashSet<>();

        for (Alert alert : alerts) {
            String email = alertRepository.findOne(alert.getId()).getUser().getEmail();
            if (StringUtils.isNotEmpty(email)) {
                emailAddresses.add(email);
            }
        }

        if (!CollectionUtils.isEmpty(emailAddresses)) {
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
                for (Alert alert : alerts) {
                    alert.setEmailAlertSent(true);
                    alertRepository.save(alert);
                }
            } catch (MessagingException | MailException me) {
                LOG.error("Could not bulk send result alert emails: ", me);
            }
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
            alertRepository.save(entityAlert);
        } else if (alert.getAlertType().equals(AlertTypes.LETTER)) {
            if (!alert.isWebAlert() && !alert.isEmailAlert()) {
                alertRepository.delete(entityAlert);
            } else {
                entityAlert.setWebAlert(alert.isWebAlert());
                entityAlert.setWebAlertViewed(alert.isWebAlertViewed());
                entityAlert.setEmailAlert(alert.isEmailAlert());
                alertRepository.save(entityAlert);
            }
        } else {
            throw new ResourceNotFoundException("Incorrect alert type");
        }
    }
}
