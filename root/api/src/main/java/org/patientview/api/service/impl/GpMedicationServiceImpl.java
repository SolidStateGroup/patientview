package org.patientview.api.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.GpMedicationStatus;
import org.patientview.service.AuditService;
import org.patientview.api.service.GpMedicationService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.IdentifierService;
import org.patientview.api.service.RoleService;
import org.patientview.api.service.UserService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GpMedicationGroupCodes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
@Service
public class GpMedicationServiceImpl extends AbstractServiceImpl<GpMedicationServiceImpl>
        implements GpMedicationService {

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private UserFeatureRepository userFeatureRepository;

    @Inject
    private RoleService roleService;

    @Inject
    private UserService userService;

    @Inject
    private GroupService groupService;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private IdentifierService identifierService;

    @Inject
    private AuditService auditService;

    @Inject
    private Properties properties;

    @Override
    public GpMedicationStatus getGpMedicationStatus(final Long userId) throws ResourceNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        GpMedicationStatus gpMedicationStatus = new GpMedicationStatus();

        // if user has GP_MEDICATION feature (has either opted in or out) then return GP medication status (transport)
        for (UserFeature userFeature : user.getUserFeatures()) {
            if (userFeature.getFeature().getName().equals(FeatureType.GP_MEDICATION.toString())) {
                gpMedicationStatus = new GpMedicationStatus(userFeature);
            }
        }

        // set if available for user based on group features or if user has previously opted in
        gpMedicationStatus.setAvailable((userGroupsHaveGpMedicationFeature(user)
                || gpMedicationStatus.getOptInStatus()));

        return gpMedicationStatus;
    }

    @Override
    public void saveGpMedicationStatus(final Long userId, GpMedicationStatus gpMedicationStatus)
            throws ResourceNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));
        Role patientRole = roleService.findByRoleTypeAndName(RoleType.PATIENT, RoleName.PATIENT);

        Feature gpMedicationFeature = featureRepository.findByName(FeatureType.GP_MEDICATION.toString());
        UserFeature userFeature = userFeatureRepository.findByUserAndFeature(user, gpMedicationFeature);

        if (userFeature == null) {
            UserFeature newUserFeature = new UserFeature(gpMedicationFeature);
            newUserFeature.setFeature(gpMedicationFeature);
            newUserFeature.setUser(user);
            newUserFeature.setOptInStatus(gpMedicationStatus.getOptInStatus());
            newUserFeature.setOptInHidden(gpMedicationStatus.getOptInHidden());
            newUserFeature.setOptOutHidden(gpMedicationStatus.getOptOutHidden());
            newUserFeature.setCreator(user);
            if (gpMedicationStatus.getOptInDate() != null) {
                newUserFeature.setOptInDate(new Date(gpMedicationStatus.getOptInDate()));
            }
            user.getUserFeatures().add(newUserFeature);
            userRepository.save(user);
        } else {
            userFeature.setOptInStatus(gpMedicationStatus.getOptInStatus());
            userFeature.setOptInHidden(gpMedicationStatus.getOptInHidden());
            userFeature.setOptOutHidden(gpMedicationStatus.getOptOutHidden());
            userFeature.setLastUpdate(new Date());
            userFeature.setLastUpdater(user);
            if (gpMedicationStatus.getOptInDate() != null) {
                userFeature.setOptInDate(new Date(gpMedicationStatus.getOptInDate()));
            } else {
                userFeature.setOptInDate(null);
            }
            userFeatureRepository.save(userFeature);
        }

        if (gpMedicationStatus.getOptInStatus() && !userHasGpMedicationGroupRole(user)) {
            // add to GP_MEDICATION group
            GroupRole groupRole = new GroupRole();
            groupRole.setRole(patientRole);
            groupRole.setGroup(groupService.findByCode(GpMedicationGroupCodes.ECS.toString()));
            groupRole.setUser(user);
            groupRole.setCreator(user);
            user.getGroupRoles().add(groupRole);
            userRepository.save(user);
        }

        if (!gpMedicationStatus.getOptInStatus() && userHasGpMedicationGroupRole(user)) {
            // remove from GP_MEDICATION group
            GroupRole groupRole = null;
            for (GroupRole entityGroupRole : user.getGroupRoles()) {
                if (entityGroupRole.getGroup().getCode().equals(GpMedicationGroupCodes.ECS.toString())) {
                    groupRole = entityGroupRole;
                }
            }

            if (groupRole != null) {
                groupRoleRepository.delete(groupRole);
                user.getGroupRoles().remove(groupRole);
                userRepository.save(user);
            }
        }
    }

    @Override
    public List<String> getEcsIdentifiers(String username, String password)
            throws ResourceNotFoundException, ResourceForbiddenException {

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new ResourceForbiddenException("Incorrect username or password");
        }

        if (!username.equals(properties.getProperty("ecs.username"))) {
            throw new ResourceForbiddenException("Incorrect username or password");
        }

        User user = userService.findByUsernameCaseInsensitive(username);
        if (user == null) {
            throw new ResourceNotFoundException("Incorrect username or password");
        }

        // handle authentication of user for this method only (not added to session)
        if (!user.getPassword().equals(DigestUtils.sha256Hex(password))) {
            auditService.createAudit(AuditActions.LOGON_FAIL, user.getUsername(), user,
                    user.getId(), AuditObjectTypes.User, null);
            incrementFailedLogon(user);
            throw new ResourceForbiddenException("Incorrect username or password");
        }

        if (user.getLocked()) {
            throw new ResourceForbiddenException("This account is locked");
        }

        List<String> identifiers = identifierService.findByGroupCode(properties.getProperty("ecs.groupcode"));

        auditService.createAudit(AuditActions.GET_PATIENT_IDENTIFIERS_ECS, null, user, null, null, null);

        return identifiers;
    }

    // check if user's groupRoles include medication groupRole
    private boolean userHasGpMedicationGroupRole(User user) throws ResourceNotFoundException {
        User entityUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        for (GroupRole groupRole : entityUser.getGroupRoles()) {
            if (groupRole.getGroup().getCode().equals(GpMedicationGroupCodes.ECS.toString())) {
                return true;
            }
        }

        return false;
    }

    // verify at least one of the user's groups has GP medication feature enabled
    private boolean userGroupsHaveGpMedicationFeature(User user) throws ResourceNotFoundException {

        User entityUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        for (GroupRole groupRole : entityUser.getGroupRoles()) {
            for (GroupFeature groupFeature : groupRole.getGroup().getGroupFeatures()) {
                if (groupFeature.getFeature().getName().equals(FeatureType.GP_MEDICATION.toString())) {
                    return true;
                }
            }
        }

        return false;
    }

    private void incrementFailedLogon(User user) {
        Integer failedLogonAttempts = user.getFailedLogonAttempts();
        if (failedLogonAttempts == null) {
            failedLogonAttempts = 0;
        }
        ++failedLogonAttempts;
        if (failedLogonAttempts > Integer.parseInt(properties.getProperty("maximum.failed.logons"))) {
            user.setLocked(Boolean.TRUE);
        }

        user.setFailedLogonAttempts(failedLogonAttempts);
        userRepository.save(user);
    }
}
