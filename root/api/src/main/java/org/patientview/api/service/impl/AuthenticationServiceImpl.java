package org.patientview.api.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.BaseGroup;
import org.patientview.api.model.Credentials;
import org.patientview.api.model.Role;
import org.patientview.api.service.ApiConditionService;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.SecurityService;
import org.patientview.api.service.StaticDataManager;
import org.patientview.api.service.UserService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.ApiKey;
import org.patientview.persistence.model.ExternalStandard;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.model.enums.ApiKeyTypes;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.PatientMessagingFeatureType;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.ApiKeyRepository;
import org.patientview.persistence.repository.ExternalStandardRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.patientview.service.AuditService;
import org.patientview.util.Util;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import static org.patientview.api.util.ApiUtil.getCurrentUser;
import static org.patientview.api.util.ApiUtil.userHasRole;

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

    private static final int SECRET_WORD_LETTER_COUNT = 2;

    @Inject
    private ApiConditionService apiConditionService;

    @Inject
    private ApiKeyRepository apiKeyRepository;

    @Inject
    private AuditService auditService;

    @Inject
    private ExternalStandardRepository externalStandardRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupService groupService;

    @Inject
    private Properties properties;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private SecurityService securityService;

    @Inject
    private StaticDataManager staticDataManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private UserTokenRepository userTokenRepository;

    @PostConstruct
    public void setParameter() {
        maximumLoginAttempts = Integer.parseInt(properties.getProperty("maximum.failed.logons"));
        LOG.debug("Setting the maximum failed logons attempts to {}", maximumLoginAttempts);
        sessionLength = Integer.parseInt(properties.getProperty("session.length"));
        LOG.debug("Setting the session length to {}", sessionLength);
    }

    private GroupRole addChildGroupsToGroupRole(GroupRole groupRole) throws ResourceNotFoundException {
        groupRole.getGroup().setChildGroups(groupService.findChildren(groupRole.getGroup().getId()));
        return groupRole;
    }

    @Cacheable(value = "authenticateOnToken")
    @Override
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
            return new UsernamePasswordAuthenticationToken(userToken.getUser(), userToken, grantedAuthorities);

        } else {
            throw new AuthenticationServiceException("Token could not be found");
        }
    }

    @CacheEvict(value = "authenticateOnToken", allEntries = true)
    @Transactional(noRollbackFor = AuthenticationServiceException.class)
    @Override
    public org.patientview.api.model.UserToken authenticate(Credentials credentials)
            throws AuthenticationServiceException {
        String username = credentials.getUsername();
        String password = credentials.getPassword();

        // temporary logging for PSQLException testing on production
        LOG.info("Authenticating '" + username + "'");

        // validate null
        if (username == null || password == null) {
            throw new AuthenticationServiceException("Incorrect username or password.");
        }

        // trim username (ipad adds space if you tap space after username to auto enter details)
        // also replace null character (causing PSQLException: ERROR: invalid byte sequence for encoding "UTF8": 0x00)
        username = username.trim().replaceAll("\\x00", "");

        // strip spaces from beginning and end of password
        password = password.trim();

        // check not empty
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new AuthenticationServiceException("Incorrect username or password");
        }

        User user = userRepository.findByUsernameCaseInsensitive(username);

        if (user == null) {
            throw new AuthenticationServiceException("Incorrect username or password");
        }
        if (user.getLocked()) {
            throw new AuthenticationServiceException("This account is locked");
        }
        if (user.getDeleted()) {
            throw new AuthenticationServiceException("This account has been deleted");
        }

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

        // if user has IMPORTER role then stop and advise importer endpoint
        if (ApiUtil.userHasRole(user, RoleName.IMPORTER)) {
            throw new AuthenticationServiceException("This account has IMPORTER role, "
                    + "please use import login endpoint");
        }

        Date now = new Date();

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setCreated(now);
        userToken.setExpiration(new Date(now.getTime() + sessionLength));

        org.patientview.api.model.UserToken toReturn = new org.patientview.api.model.UserToken();

        // if valid CKD api key then can bypass secret word check
        boolean validApiKey = false;
        if (StringUtils.isNotEmpty(credentials.getApiKey())) {

            /**
             * Need to validate different key types and apply logic accordingly
             * Currently supports CKD and PATIENT key types
             */

            ApiKey apiKey = apiKeyRepository.findOneByKey(credentials.getApiKey());

            // based on type
            if (apiKey == null) {
                throw new AuthenticationServiceException("API key not found");
            }

            switch (apiKey.getType()) {
                case CKD:
                case PATIENT:
                    // check if has not expired yet
                    if (apiKey.getExpiryDate() == null) {
                        validApiKey = true;
                    } else if (apiKey.getExpiryDate().getTime() > now.getTime()) {
                        validApiKey = true;
                    }
                    break;
                default:
                    throw new AuthenticationServiceException("Invalid API key type");
            }

            if (!validApiKey) {
                throw new AuthenticationServiceException("Invalid API key");
            }

            // set rate limit for CKD
            if (ApiKeyTypes.CKD == apiKey.getType()) {
                setRateLimit(userToken, ApiKeyTypes.CKD);
            }
        } else {
            setRateLimit(userToken, null);
        }

        // if user has a secret word set then set check secret word to true, informs ui and is used as second part
        // of multi factor authentication
        if (!StringUtils.isEmpty(user.getSecretWord()) && !validApiKey) {
            // has secret word
            userToken.setCheckSecretWord(true);

            boolean foundIndexes = false;
            List<String> existingIndexes;

            // get a list of none expired user token, could be more then one, multiple devices
            List<UserToken> validTokens = userTokenRepository.findActiveByUser(user.getId());
            // check if we have secret word indexes stored
            for(UserToken token : validTokens){
                if(null != token.getSecretWordIndexes() && !token.getSecretWordIndexes().isEmpty()){

                    existingIndexes = new ArrayList<>(token.getSecretWordIndexes());
                    foundIndexes = true;
                }
            }

            if(foundIndexes){

            }else{
                // follow the standard flow
                // choose two characters to check and add to secret word indexes for ui
                try {
                    Map<String, String> secretWordMap = new Gson().fromJson(
                            user.getSecretWord(), new TypeToken<HashMap<String, String>>() { } .getType());

                    if (secretWordMap == null || secretWordMap.isEmpty()) {
                        throw new AuthenticationServiceException("Secret word cannot be retrieved");
                    }


                    Map<String, String> secretWordMapNoSalt = new HashMap<>(secretWordMap);
                    secretWordMapNoSalt.remove("salt");

                    List<String> possibleIndexes = new ArrayList<>(secretWordMapNoSalt.keySet());
                    List<String> secretWordIndexes = new ArrayList<>();

                    // choose 2 secret word letters
                    Random ran = new Random();
                    int randomInt = ran.nextInt(possibleIndexes.size() - 1);
                    String indexOne = possibleIndexes.get(randomInt);

                    possibleIndexes.remove(randomInt);
                    randomInt = ran.nextInt(possibleIndexes.size() - 1);
                    String indexTwo = possibleIndexes.get(randomInt);

                    // need to make sure indexes are returned in ASC order
                    secretWordIndexes.add(indexOne);
                    secretWordIndexes.add(indexTwo);
                    Collections.sort(secretWordIndexes);
                    toReturn.setSecretWordIndexes(secretWordIndexes);

                    toReturn.setCheckSecretWord(userToken.isCheckSecretWord());

                    // set temporary token
                    userToken.setSecretWordToken(CommonUtils.getAuthToken());
                    toReturn.setSecretWordToken(userToken.getSecretWordToken());

                    // set user token (must not be null)
                    userToken.setToken(CommonUtils.getAuthToken().substring(0, 40) + "secret");
                    userToken.setSecretWordIndexes(Arrays.toString(secretWordIndexes.toArray()));

                    userTokenRepository.save(userToken);
                } catch (JsonSyntaxException jse) {
                    throw new AuthenticationServiceException("Error retrieving secret word");
                }
            }


        } else {
            // no secret word, log in as usual
            userToken.setToken(CommonUtils.getAuthToken());
            userToken = userTokenRepository.save(userToken);
            toReturn.setToken(userToken.getToken());

            updateUserAndAuditLogin(user, password);
        }

        return toReturn;
    }

    @CacheEvict(value = "authenticateOnToken", allEntries = true)
    @Transactional(noRollbackFor = AuthenticationServiceException.class)
    @Override
    public org.patientview.api.model.UserToken authenticateImporter(Credentials credentials)
            throws AuthenticationServiceException {
        String username = credentials.getUsername();
        String password = credentials.getPassword();

        if (StringUtils.isEmpty(username)) {
            throw new AuthenticationServiceException("username not set");
        }
        if (StringUtils.isEmpty(credentials.getPassword())) {
            throw new AuthenticationServiceException("password not set");
        }
        if (StringUtils.isEmpty(credentials.getApiKey())) {
            throw new AuthenticationServiceException("api key not set");
        }

        // trim username (ipad adds space if you tap space after username to auto enter details)
        username = username.trim();
        User user = userRepository.findByUsernameCaseInsensitive(username);

        if (user == null) {
            throw new AuthenticationServiceException("Incorrect username or password");
        }
        if (user.getLocked()) {
            throw new AuthenticationServiceException("This account is locked");
        }
        if (user.getDeleted()) {
            throw new AuthenticationServiceException("This account has been deleted");
        }

        Date now = new Date();

        // validate api key
        List<ApiKey> apiKeys
                = apiKeyRepository.findByKeyAndTypeAndUser(credentials.getApiKey(), ApiKeyTypes.IMPORTER, user);

        if (CollectionUtils.isEmpty(apiKeys)) {
            throw new AuthenticationServiceException("No API key found");
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
            throw new AuthenticationServiceException("API key has expired");
        }

        // strip spaces from beginning and end of password
        password = password.trim();

        // check username and password
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

        // check user has IMPORTER role
        if (!ApiUtil.userHasRole(user, RoleName.IMPORTER) && !ApiUtil.userHasRole(user, RoleName.GLOBAL_ADMIN)) {
            throw new AuthenticationServiceException("Importer role missing");
        }

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setCreated(now);
        userToken.setExpiration(new Date(now.getTime() + sessionLength));

        org.patientview.api.model.UserToken toReturn = new org.patientview.api.model.UserToken();

        setRateLimit(userToken, ApiKeyTypes.IMPORTER);

        // no secret word, log in as usual
        userToken.setToken(CommonUtils.getAuthToken());
        userToken = userTokenRepository.save(userToken);
        toReturn.setToken(userToken.getToken());

        updateUserAndAuditLogin(user, password);

        return toReturn;
    }

    /**
     * Check if User must set a secret word. Based on presence of RoleName and if secret word set).
     *
     * @param user User to check if must set a secret word
     * @return boolean if secret word must be set
     */
    private boolean checkMustSetSecretWord(User user) {
        return userHasRole(user,
                RoleName.SPECIALTY_ADMIN, RoleName.GP_ADMIN, RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN)
                && StringUtils.isEmpty(user.getSecretWord());
    }

    @Override
    public void checkSecretWord(User user, Map<String, String> letterMap)
            throws ResourceNotFoundException, ResourceForbiddenException {
        if (letterMap == null) {
            throw new ResourceForbiddenException("Letters must be chosen");
        }
        if (letterMap.isEmpty()) {
            throw new ResourceForbiddenException("Letters must be chosen");
        }
        if (user == null) {
            throw new ResourceForbiddenException("User not found");
        }
        if (StringUtils.isEmpty(user.getSecretWord())) {
            throw new ResourceForbiddenException("Secret word is not set");
        }

        // convert from JSON string to map
        Map<String, String> secretWordMap = new Gson().fromJson(
                user.getSecretWord(), new TypeToken<HashMap<String, String>>() { } .getType());

        if (secretWordMap.isEmpty()) {
            throw new ResourceForbiddenException("Secret word not found");
        }
        if (StringUtils.isEmpty(secretWordMap.get("salt"))) {
            throw new ResourceForbiddenException("Secret word salt not found");
        }

        String salt = secretWordMap.get("salt");

        if (letterMap.keySet().size() < SECRET_WORD_LETTER_COUNT) {
            throw new ResourceForbiddenException("Must include all requested secret word letters");
        }

        // check entered letters against salted values
        for (String toCheck : letterMap.keySet()) {
            if (!secretWordMap.get(toCheck).equals(DigestUtils.sha256Hex(letterMap.get(toCheck) + salt))) {
                throw new ResourceForbiddenException("Letters do not match your secret word");
            }
        }
    }

    private org.patientview.api.model.UserToken createApiUserToken(UserToken userToken)
            throws ResourceForbiddenException {
        org.patientview.api.model.UserToken transportUserToken
                = new org.patientview.api.model.UserToken(userToken);

        // get information about user's available roles and groups as used in staff and patient views
        setUserGroups(transportUserToken);
        transportUserToken.setUserFeatures(featureRepository.findByUser(userToken.getUser()));
        transportUserToken.setRoutes(securityService.getUserRoutes(userToken.getUser()));

        // patient
        if (ApiUtil.userHasRole(userToken.getUser(), RoleName.PATIENT)) {
            setFhirInformation(transportUserToken, userToken.getUser());
            transportUserToken.setPatientMessagingFeatureTypes(
                    new ArrayList<>(Arrays.asList(PatientMessagingFeatureType.values())));
            transportUserToken.setGroupMessagingEnabled(true);

            setShouldEnterCondition(transportUserToken, userToken.getUser());
        }

        // staff
        if (!ApiUtil.userHasRole(userToken.getUser(), RoleName.PATIENT)) {
            setSecurityRoles(transportUserToken);
            setPatientRoles(transportUserToken);
            setStaffRoles(transportUserToken);
            transportUserToken.setGroupFeatures(staticDataManager.getFeaturesByType("GROUP"));
            transportUserToken.setPatientFeatures(staticDataManager.getFeaturesByType("PATIENT"));
            transportUserToken.setStaffFeatures(staticDataManager.getFeaturesByType("STAFF"));
            setAuditActions(transportUserToken);
            setExternalStandards(transportUserToken);
        }

        // global admins
        if (ApiUtil.userHasRole(userToken.getUser(), RoleName.GLOBAL_ADMIN)) {
            // global admins can add hidden IMPORTER Role
            org.patientview.persistence.model.Role importerRole
                    = roleRepository.findByRoleTypeAndName(RoleType.STAFF, RoleName.IMPORTER, false);
            if (importerRole != null) {
                transportUserToken.getStaffRoles().add(new Role(importerRole));
                transportUserToken.getSecurityRoles().add(new Role(importerRole));
            }
        }

        // tell ui user must set secret word
        transportUserToken.setMustSetSecretWord(checkMustSetSecretWord(userToken.getUser()));

        return transportUserToken;
    }

    @Override
    public org.patientview.api.model.User getBasicUserInformation(String token) throws ResourceForbiddenException {
        UserToken userToken = userTokenRepository.findByToken(token);

        if (userToken == null) {
            throw new AuthenticationServiceException("User is not currently logged in");
        }

        return new org.patientview.api.model.User(userToken.getUser());
    }

    // retrieve static data and user specific data to avoid requerying
    @CacheEvict(value = "authenticateOnToken", allEntries = true)
    @Override
    public org.patientview.api.model.UserToken getUserInformation(org.patientview.api.model.UserToken userToken)
            throws ResourceNotFoundException, ResourceForbiddenException {
        UserToken foundUserToken = userTokenRepository.findByToken(userToken.getToken());

        if (foundUserToken == null) {
            if (StringUtils.isEmpty(userToken.getSecretWordToken())) {
                throw new AuthenticationServiceException("Forbidden, secret word token not found");
            }

            // not found, user token could have secret word, check if can get by temporary secret word token
            foundUserToken = userTokenRepository.findBySecretWordToken(userToken.getSecretWordToken());
            if (foundUserToken == null) {
                throw new AuthenticationServiceException("Forbidden, Token not found");
            }
            if (foundUserToken.getUser() == null) {
                throw new AuthenticationServiceException("Forbidden, User not found");
            }

            boolean userHasSecretWord = StringUtils.isNotEmpty(foundUserToken.getUser().getSecretWord());

            // check if the secret word needs to be checked
            if (foundUserToken.isCheckSecretWord() && userHasSecretWord) {
                // user has a secret word and has included their chosen characters, check that they match
                checkSecretWord(foundUserToken.getUser(), userToken.getSecretWordChoices());

                // passed secret word check so set check to false and return user information
                foundUserToken.setCheckSecretWord(false);
                foundUserToken.setSecretWordToken(null);
                foundUserToken.setToken(CommonUtils.getAuthToken());
                userTokenRepository.save(foundUserToken);

                updateUserAndAuditLogin(foundUserToken.getUser(), null);

                return createApiUserToken(foundUserToken);
            } else {
                // standard login with no secret word
                throw new AuthenticationServiceException("Forbidden, failed checking secret word");
            }
        } else {
            // standard get user information
            if (foundUserToken.getUser() == null) {
                throw new AuthenticationServiceException("Forbidden, User not found");
            }

            return createApiUserToken(foundUserToken);
        }
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

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameCaseInsensitive(username);
    }

    @Caching(evict = { @CacheEvict(value = "unreadConversationCount", allEntries = true),
            @CacheEvict(value = "authenticateOnToken", allEntries = true) })
    @Override
    public void logout(String token, boolean expired) throws AuthenticationServiceException {
        UserToken userToken = userTokenRepository.findByToken(token);

        if (userToken == null) {
            throw new AuthenticationServiceException("User is not currently logged in");
        }

        if (!expired) {
            auditService.createAudit(AuditActions.LOGGED_OFF, userToken.getUser().getUsername(), userToken.getUser(),
                    userToken.getUser().getId(), AuditObjectTypes.User, null);
        }

        // delete all user tokens associated with this user (should only ever be one per user)
        userTokenRepository.deleteByUserId(userToken.getUser().getId());
        SecurityContextHolder.getContext().setAuthentication(null);
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

    private void setAuditActions(org.patientview.api.model.UserToken userToken) {
        // use name of AuditActions for nicer UI in front end
        List<String> auditActions = new ArrayList<>();
        for (AuditActions auditAction : AuditActions.class.getEnumConstants()) {
            auditActions.add(auditAction.getName());
        }
        userToken.setAuditActions(auditActions);
    }

    private void setExternalStandards(org.patientview.api.model.UserToken userToken) {
        List<ExternalStandard> externalStandards = new ArrayList<>();
        for (ExternalStandard externalStandard : externalStandardRepository.findAll()) {
            externalStandards.add(externalStandard);
        }
        userToken.setExternalStandards(externalStandards);
    }

    private void setFhirInformation(org.patientview.api.model.UserToken userToken, User user) {
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
    }

    private void setPatientRoles(org.patientview.api.model.UserToken userToken) {
        List<Role> patientRoles = new ArrayList<>();
        for (org.patientview.persistence.model.Role role : roleRepository.findByRoleType(RoleType.PATIENT)) {
            patientRoles.add(new Role(role));
        }
        userToken.setPatientRoles(patientRoles);
    }

    private void setRateLimit(UserToken userToken, ApiKeyTypes apiKeyType) {
        String rateLimitType = apiKeyType == null ? "DEFAULT" : apiKeyType.toString();
        String rateLimit = properties.getProperty("rate.limit." + rateLimitType);

        if (StringUtils.isNotEmpty(rateLimit)) {
            try {
                userToken.setRateLimit(Double.parseDouble(rateLimit));
            } catch (NumberFormatException nfe) {
                LOG.info("Error converting rate.limit." + rateLimitType + " to Double, continuing..");
            }
        }
    }

    private void setShouldEnterCondition(org.patientview.api.model.UserToken userToken, User user) {
        // user must be member of group with ENTER_OWN_DIAGNOSES feature
        boolean groupHasFeature = false;

        if (user != null && !CollectionUtils.isEmpty(user.getGroupRoles())) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                if (!CollectionUtils.isEmpty(groupRole.getGroup().getGroupFeatures())) {
                    for (GroupFeature groupFeature : groupRole.getGroup().getGroupFeatures()) {
                        if (groupFeature.getFeature() != null
                                && groupFeature.getFeature().getName().equals(
                                FeatureType.ENTER_OWN_DIAGNOSES.toString())) {
                            groupHasFeature = true;
                        }
                    }
                }
            }
        }

        if (!groupHasFeature) {
            return;
        }

        // check patient has a any entered Condition
        try {
            if (!apiConditionService.hasAnyConditions(user.getId(), true)) {
                userToken.setShouldEnterCondition(true);
            }
        } catch (FhirResourceException | ResourceForbiddenException | ResourceNotFoundException e) {
            LOG.info("Error retrieving patient Conditions on authenticate, continuing..");
        }
    }

    private void setStaffRoles(org.patientview.api.model.UserToken userToken) {
        List<Role> staffRoles = new ArrayList<>();
        for (org.patientview.persistence.model.Role role : roleRepository.findByRoleType(RoleType.STAFF)) {
            staffRoles.add(new Role(role));
        }
        userToken.setStaffRoles(staffRoles);
    }

    private void setSecurityRoles(org.patientview.api.model.UserToken userToken) {
        List<org.patientview.persistence.model.Role> availableRoles
                = Util.convertIterable(roleRepository.findValidRolesByUser(userToken.getUser().getId()));

        userToken.setSecurityRoles(new ArrayList<Role>());

        for (org.patientview.persistence.model.Role availableRole : availableRoles) {
            userToken.getSecurityRoles().add(new Role(availableRole));
        }
    }

    private void setUserGroups(org.patientview.api.model.UserToken userToken) throws ResourceForbiddenException {
        List<org.patientview.persistence.model.Group> userGroups
                = groupService.getAllUserGroupsAllDetails(userToken.getUser().getId());

        userToken.setUserGroups(new ArrayList<BaseGroup>());
        userToken.setGroupMessagingEnabled(false);

        // global admin can also get general practice specialty
        List<String> extraGroupCodes = new ArrayList<>();
        if (ApiUtil.userHasRole(userRepository.findOne(userToken.getUser().getId()), RoleName.GLOBAL_ADMIN)) {
            extraGroupCodes.add(HiddenGroupCodes.GENERAL_PRACTICE.toString());
        }

        for (org.patientview.persistence.model.Group userGroup : userGroups) {
            // do not add groups that have code in HiddenGroupCode enum as these are used for patient entered results
            if (!ApiUtil.isInEnum(userGroup.getCode(), HiddenGroupCodes.class)
                    || extraGroupCodes.contains(userGroup.getCode())) {
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
    }

    @Transactional(noRollbackFor = AuthenticationServiceException.class)
    @Override
    public String switchBackFromUser(Long userId, String token) throws AuthenticationServiceException {
        LOG.debug("Switching to user with ID: {}", userId);
        User user = userRepository.findOne(userId);

        if (user == null) {
            throw new AuthenticationServiceException("Cannot switch user, user not found");
        }

        UserToken userToken = userTokenRepository.findByToken(token);

        if (userToken == null) {
            throw new AuthenticationServiceException("Cannot switch user, token not found");
        }

        return userToken.getToken();
    }

    @Transactional(noRollbackFor = AuthenticationServiceException.class)
    @Override
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

    private void updateUserAndAuditLogin(User user, String password) {
        user.setFailedLogonAttempts(0);

        // Salt password
        if (user.getSalt() == null && password != null) {
            try {
                String salt = CommonUtils.generateSalt();
                user.setSalt(salt);
                user.setPassword(DigestUtils.sha256Hex(password + salt));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // handle current and last login time and IP, updating last login with current then setting new current
        if (user.getCurrentLogin() != null) {
            user.setLastLogin(user.getCurrentLogin());
        }

        if (StringUtils.isNotEmpty(user.getCurrentLoginIpAddress())) {
            user.setLastLoginIpAddress(user.getCurrentLoginIpAddress());
        }

        user.setCurrentLogin(new Date());

        // set last login IP address from headers if present
        HttpServletRequest request
                = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String forwardedFor = request.getHeader("X-Forwarded-For");
        String realIp = request.getHeader("X-Real-IP");

        if (StringUtils.isNotEmpty(forwardedFor)) {
            user.setCurrentLoginIpAddress(forwardedFor.split(",")[0]);
        } else if (StringUtils.isNotEmpty(realIp)) {
            user.setCurrentLoginIpAddress(realIp);
        } else {
            user.setCurrentLoginIpAddress(request.getRemoteAddr());
        }

        userRepository.save(user);

        auditService.createAudit(AuditActions.LOGGED_ON, user.getUsername(), user,
                user.getId(), AuditObjectTypes.User, null);
    }
}
