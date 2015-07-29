package org.patientview.api.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.BaseGroup;
import org.patientview.api.model.Role;
import org.patientview.api.service.AuditService;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.RoleService;
import org.patientview.api.service.SecurityService;
import org.patientview.api.service.StaticDataManager;
import org.patientview.api.service.UserService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.PatientMessagingFeatureType;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Service
public class AuthenticationServiceImpl extends AbstractServiceImpl<AuthenticationServiceImpl>
        implements AuthenticationService {

    // retrieved from properties file
    private static Integer maximumLoginAttempts;
    private static Integer sessionLength;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private UserTokenRepository userTokenRepository;

    @Inject
    private SecurityService securityService;

    @Inject
    private RoleService roleService;

    @Inject
    private GroupService groupService;

    @Inject
    private UserService userService;

    @Inject
    private AuditService auditService;

    @Inject
    private StaticDataManager staticDataManager;

    @Inject
    private Properties properties;

    @PostConstruct
    public void setParameter() {
        maximumLoginAttempts = Integer.parseInt(properties.getProperty("maximum.failed.logons"));
        LOG.debug("Setting the maximum failed logons attempts to {}", maximumLoginAttempts);
        sessionLength = Integer.parseInt(properties.getProperty("session.length"));
        LOG.debug("Setting the session length to {}", sessionLength);
    }

    @Transactional(noRollbackFor = AuthenticationServiceException.class)
    public String switchToUser(Long userId) throws AuthenticationServiceException {

        LOG.debug("Switching to user with ID: {}", userId);
        User user = userRepository.findOne(userId);

        if (user == null) {
            throw new AuthenticationServiceException("Cannot switch user, user not found");
        }

        if (!userService.currentUserCanSwitchToUser(user)) {
            throw new AuthenticationServiceException("Forbidden");
        }

        Date now = new Date();
        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(CommonUtils.getAuthToken());
        userToken.setCreated(now);
        userToken.setExpiration(new Date(now.getTime() + sessionLength));
        userToken = userTokenRepository.save(userToken);
        userRepository.save(user);

        auditService.createAudit(AuditActions.PATIENT_VIEW, user.getUsername(), getCurrentUser(),
                user.getId(), AuditObjectTypes.User, null);

        return userToken.getToken();
    }

    @Transactional(noRollbackFor = AuthenticationServiceException.class)
    public String switchBackFromUser(Long userId, String token) throws AuthenticationServiceException {

        LOG.debug("Switching to user with ID: {}", userId);
        User user = userRepository.findOne(userId);

        if (user == null) {
            throw new AuthenticationServiceException("Cannot switch user, user not found");
        }

        UserToken userToken = getToken(token);

        if (userToken == null) {
            throw new AuthenticationServiceException("Cannot switch user, token not found");
        }

        return userToken.getToken();
    }

    @CacheEvict(value = "authenticateOnToken", allEntries = true)
    @Transactional(noRollbackFor = AuthenticationServiceException.class)
    public String authenticate(String username, String password)
            throws UsernameNotFoundException, AuthenticationServiceException {

        LOG.debug("Authenticating user: {}", username);

        // trim username (ipad adds space if you tap space after username to auto enter details)
        username = username.trim();
        User user = userRepository.findByUsernameCaseInsensitive(username);

        if (user == null) {
            throw new UsernameNotFoundException("Incorrect username or password");
        }

        if (user.getLocked()) {
            throw new AuthenticationServiceException("This account is locked");
        }

        if (user.getDeleted()) {
            throw new AuthenticationServiceException("This account has been deleted");
        }

        // strip spaces from beginning and end of password
        password = password.trim();

        if (!user.getPassword().equals(DigestUtils.sha256Hex(password)) && user.getSalt() == null) {
            auditService.createAudit(AuditActions.LOGON_FAIL, user.getUsername(), user,
                    user.getId(), AuditObjectTypes.User, null);
            incrementFailedLogon(user);
            throw new AuthenticationServiceException("Incorrect username or password");
        } else if (user.getSalt() != null
                && !user.getPassword().equals(
                DigestUtils.sha256Hex(password + user.getSalt()))) {
            auditService.createAudit(AuditActions.LOGON_FAIL, user.getUsername(), user,
                    user.getId(), AuditObjectTypes.User, null);
            incrementFailedLogon(user);
            throw new AuthenticationServiceException("Incorrect username or password");
        }

        Date now = new Date();

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(CommonUtils.getAuthToken());
        userToken.setCreated(now);
        userToken.setExpiration(new Date(now.getTime() + sessionLength));
        userToken = userTokenRepository.save(userToken);

        user.setFailedLogonAttempts(0);
        user.setLastLogin(new Date());
        //Salt password
        if (user.getSalt() == null) {
            try {
                String salt = userService.generateSalt();
                user.setSalt(salt);
                user.setPassword(DigestUtils.sha256Hex(password + salt));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // set last login IP address from headers if present
        HttpServletRequest request
                = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String forwardedFor = request.getHeader("X-Forwarded-For");
        String realIp = request.getHeader("X-Real-IP");

        if (StringUtils.isNotEmpty(forwardedFor)) {
            user.setLastLoginIpAddress(forwardedFor.split(",")[0]);
        } else if (StringUtils.isNotEmpty(realIp)) {
            user.setLastLoginIpAddress(realIp);
        } else {
            user.setLastLoginIpAddress(request.getRemoteAddr());
        }


        userRepository.save(user);

        auditService.createAudit(AuditActions.LOGGED_ON, user.getUsername(), user,
                user.getId(), AuditObjectTypes.User, null);

        return userToken.getToken();
    }

    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameCaseInsensitive(username);
    }

    @Caching(evict = { @CacheEvict(value = "unreadConversationCount", allEntries = true),
            @CacheEvict(value = "authenticateOnToken", allEntries = true) })
    public void logout(String token) throws AuthenticationServiceException {
        UserToken userToken = userTokenRepository.findByToken(token);

        if (userToken == null) {
            throw new AuthenticationServiceException("User is not currently logged in");
        }
        auditService.createAudit(AuditActions.LOGGED_OFF, userToken.getUser().getUsername(), userToken.getUser(),
                userToken.getUser().getId(), AuditObjectTypes.User, null);

        // delete all user tokens associated with this user (should only ever be one per user)
        userTokenRepository.deleteByUserId(userToken.getUser().getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    // retrieve static data and user specific data to avoid requerying
    @CacheEvict(value = "authenticateOnToken", allEntries = true)
    public org.patientview.api.model.UserToken getUserInformation(String token) throws ResourceForbiddenException {
        UserToken userToken = userTokenRepository.findByToken(token);

        if (userToken == null) {
            throw new AuthenticationServiceException("User is not currently logged in");
        }

        org.patientview.api.model.UserToken transportUserToken = new org.patientview.api.model.UserToken(userToken);

        // get information about user's available roles and groups as used in staff and patient views
        transportUserToken = setUserGroups(transportUserToken);
        transportUserToken = setUserFeatures(transportUserToken);
        transportUserToken = setRoutes(transportUserToken);

        if (doesContainRoles(RoleName.PATIENT)) {
            transportUserToken = setFhirInformation(transportUserToken, userToken.getUser());
            transportUserToken = setPatientMessagingFeatureTypes(transportUserToken);
            transportUserToken.setGroupMessagingEnabled(true);
        }

        if (!doesContainRoles(RoleName.PATIENT)) {
            transportUserToken = setSecurityRoles(transportUserToken);
            transportUserToken = setPatientRoles(transportUserToken);
            transportUserToken = setStaffRoles(transportUserToken);
            transportUserToken = setGroupFeatures(transportUserToken);
            transportUserToken = setPatientFeatures(transportUserToken);
            transportUserToken = setStaffFeatures(transportUserToken);
            transportUserToken = setAuditActions(transportUserToken);
        }

        return transportUserToken;
    }

    @Cacheable(value = "authenticateOnToken")
    public Authentication authenticate(final Authentication authentication) throws AuthenticationServiceException {
        UserToken userToken = userTokenRepository.findByToken(authentication.getName());

        if (userToken != null) {

            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

            for (GroupRole groupRole : userToken.getUser().getGroupRoles()) {
                try {
                    if (groupRole.getRole().getName().equals(RoleName.SPECIALTY_ADMIN)) {
                        grantedAuthorities.add(addChildGroupsToGroupRole(groupRole));
                    } else {
                        grantedAuthorities.add(groupRole);
                    }
                } catch (ResourceNotFoundException rnf) {
                    throw new AuthenticationServiceException("Error retrieving child groups");
                }
            }
            return new UsernamePasswordAuthenticationToken(userToken.getUser(), userToken.getUser().getId(),
                    grantedAuthorities);

        } else {
            throw new AuthenticationServiceException("Token could not be found");
        }
    }

    @Override
    public boolean sessionExpired(String authToken) {
        if (authToken == null) {
            return true;
        }

        // set expired to 30 mins in future, throw exception if expiration is set and before now
        Date now = new Date();
        Date future = new Date(now.getTime() + sessionLength);
        Date expiration = userTokenRepository.getExpiration(authToken);

        if (expiration == null) {
            return true;
        } else {
            if (userTokenRepository.sessionExpired(authToken)) {
                return true;
            } else {
                userTokenRepository.setExpiration(authToken, future);
                return false;
            }
        }
    }

    private GroupRole addChildGroupsToGroupRole(GroupRole groupRole) throws ResourceNotFoundException {
        groupRole.getGroup().setChildGroups(groupService.findChildren(groupRole.getGroup().getId()));
        return groupRole;
    }

    private org.patientview.api.model.UserToken setFhirInformation(org.patientview.api.model.UserToken userToken,
                                                                   User user) {
        // if user has fhir links set latestDataReceivedDate and latestDataReceivedBy (ignore PATIENT_ENTERED)
        if (user.getFhirLinks() != null && !user.getFhirLinks().isEmpty()) {
            Date latestDataReceivedDate = new Date(1, 1, 1);
            Group group = null;

            for (FhirLink fhirLink : user.getFhirLinks()) {
                if (fhirLink.getUpdated() != null) {
                    if (fhirLink.getUpdated().after(latestDataReceivedDate)
                            && !fhirLink.getGroup().getCode().equals(HiddenGroupCodes.PATIENT_ENTERED.toString())
                            && !fhirLink.getGroup().getCode().equals(HiddenGroupCodes.ECS.toString())) {
                        latestDataReceivedDate = fhirLink.getUpdated();
                        group = fhirLink.getGroup();
                    }
                }
            }

            if (group != null) {
                userToken.getUser().setLatestDataReceivedBy(new org.patientview.api.model.Group(group));
                userToken.getUser().setLatestDataReceivedDate(latestDataReceivedDate);
            }
        }
        return userToken;
    }

    private org.patientview.api.model.UserToken setSecurityRoles(org.patientview.api.model.UserToken userToken) {
        List<org.patientview.persistence.model.Role> availableRoles
                = Util.convertIterable(roleService.getUserRoles(userToken.getUser().getId()));

        userToken.setSecurityRoles(new ArrayList<Role>());

        for (org.patientview.persistence.model.Role availableRole : availableRoles) {
            userToken.getSecurityRoles().add(new Role(availableRole));
        }
        return userToken;
    }

    private org.patientview.api.model.UserToken setUserFeatures(org.patientview.api.model.UserToken userToken) {
        userToken.setUserFeatures(featureRepository.findByUser(userRepository.findOne(userToken.getUser().getId())));
        return userToken;
    }

    private org.patientview.api.model.UserToken setUserGroups(org.patientview.api.model.UserToken userToken)
            throws ResourceForbiddenException {
        List<org.patientview.persistence.model.Group> userGroups
                = groupService.getAllUserGroupsAllDetails(userToken.getUser().getId());

        userToken.setUserGroups(new ArrayList<BaseGroup>());
        userToken.setGroupMessagingEnabled(false);

        for (org.patientview.persistence.model.Group userGroup : userGroups) {
            // do not add groups that have code in HiddenGroupCode enum as these are used for patient entered results
            if (!Util.isInEnum(userGroup.getCode(), HiddenGroupCodes.class)) {
                userToken.getUserGroups().add(new BaseGroup(userGroup));

                // if group has MESSAGING feature then set in transportUserToken
                Group entityGroup = groupRepository.findOne(userGroup.getId());
                for (GroupFeature groupFeature : entityGroup.getGroupFeatures()) {
                    if (groupFeature.getFeature().getName().equals(FeatureType.MESSAGING.toString())) {
                        userToken.setGroupMessagingEnabled(true);
                    }
                }
            }
        }
        return userToken;
    }

    private org.patientview.api.model.UserToken setRoutes(org.patientview.api.model.UserToken userToken) {
        userToken.setRoutes(securityService.getUserRoutes(userToken.getUser().getId()));
        return userToken;
    }

    private org.patientview.api.model.UserToken setPatientRoles(org.patientview.api.model.UserToken userToken) {
        List<Role> patientRoles = new ArrayList<>();
        List<org.patientview.persistence.model.Role> fullPatientRoles = roleService.getRolesByType(RoleType.PATIENT);

        for (org.patientview.persistence.model.Role role : fullPatientRoles) {
            patientRoles.add(new Role(role));
        }

        userToken.setPatientRoles(patientRoles);
        return userToken;
    }

    private org.patientview.api.model.UserToken setStaffRoles(org.patientview.api.model.UserToken userToken) {
        List<Role> staffRoles = new ArrayList<>();
        List<org.patientview.persistence.model.Role> fullStaffRoles = roleService.getRolesByType(RoleType.STAFF);

        for (org.patientview.persistence.model.Role role : fullStaffRoles) {
            staffRoles.add(new Role(role));
        }

        userToken.setStaffRoles(staffRoles);
        return userToken;
    }

    private org.patientview.api.model.UserToken setGroupFeatures(org.patientview.api.model.UserToken userToken) {
        userToken.setGroupFeatures(staticDataManager.getFeaturesByType("GROUP"));
        return userToken;
    }

    private org.patientview.api.model.UserToken setPatientFeatures(org.patientview.api.model.UserToken userToken) {
        userToken.setPatientFeatures(staticDataManager.getFeaturesByType("PATIENT"));
        return userToken;
    }

    private org.patientview.api.model.UserToken setStaffFeatures(org.patientview.api.model.UserToken userToken) {
        userToken.setStaffFeatures(staticDataManager.getFeaturesByType("STAFF"));
        return userToken;
    }

    private org.patientview.api.model.UserToken setPatientMessagingFeatureTypes(
            org.patientview.api.model.UserToken userToken) {
        userToken.setPatientMessagingFeatureTypes(new ArrayList<>(Arrays.asList(PatientMessagingFeatureType.values())));
        return userToken;
    }

    private org.patientview.api.model.UserToken setAuditActions(
            org.patientview.api.model.UserToken userToken) {

        // use name of AuditActions for nicer UI in front end
        List<String> auditActions = new ArrayList<>();
        for (AuditActions auditAction : AuditActions.class.getEnumConstants()) {
            auditActions.add(auditAction.getName());
        }
        userToken.setAuditActions(auditActions);
        return userToken;
    }

    private UserToken getToken(String token) {
        return userTokenRepository.findByToken(token);
    }

    private void incrementFailedLogon(User user) {
        Integer failedLogonAttempts = user.getFailedLogonAttempts();
        if (failedLogonAttempts == null) {
            failedLogonAttempts = 0;
        }
        ++failedLogonAttempts;
        if (failedLogonAttempts > maximumLoginAttempts) {
            user.setLocked(Boolean.TRUE);

            auditService.createAudit(AuditActions.ACCOUNT_LOCKED, user.getUsername(), user,
                    user.getId(), AuditObjectTypes.User, null);
        }

        user.setFailedLogonAttempts(failedLogonAttempts);
        userRepository.save(user);
    }
}
