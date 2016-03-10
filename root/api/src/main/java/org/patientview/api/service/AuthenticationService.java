package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.model.Credentials;
import org.patientview.api.model.User;
import org.patientview.api.model.UserToken;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Authentication service, used for authenticate Users, login, logout and switch between Users.
 *
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AuthenticationService extends UserDetailsService {

    /**
     * Authenticate a User given username and password.
     * @param username String username
     * @param password String password
     * @return UserToken containing authentication token, used in all future authenticated requests
     * @throws AuthenticationServiceException
     * @throws UsernameNotFoundException
     */
    UserToken authenticate(String username, String password)
            throws AuthenticationServiceException, UsernameNotFoundException;

    /**
     * Store Authentication object in Spring Security
     * @param authentication Spring Security Authentication object
     * @return Spring Security Authentication object
     * @throws AuthenticationServiceException
     */
    Authentication authenticate(final Authentication authentication) throws AuthenticationServiceException;

    UserToken authenticateImporter(Credentials credentials) throws AuthenticationServiceException;

    /**
     * Validates a User's secret word given a Map of letter position to letter chosen, used as part of login and
     * get User information
     * @param user User to validate secret word
     * @param letterMap Map of position to letter chosen
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    void checkSecretWord(org.patientview.persistence.model.User user, Map<String, String> letterMap)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get basic user information given the token produced when a user successfully logs in. Performed after login.
     * @param token String token associated with a successfully logged in user
     * @return User object containing relevant user information
     */
    User getBasicUserInformation(String token) throws ResourceForbiddenException;

    /**
     * Get user information (security roles, groups etc) given the token produced when a user successfully logs in.
     * Performed after login.
     * @param userToken UserToken object containing token associated with a successfully logged in user
     * @return UserToken object containing relevant user information and static data
     * @throws AuthenticationServiceException
     * @throws ResourceForbiddenException
     */
    UserToken getUserInformation(UserToken userToken) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Log out User, clearing their session and deleting the token associated with their account, invalidating all
     * future requests with this token.
     * @param token String token associated with User
     * @param expired boolean if session has expired causing logout
     * @throws AuthenticationServiceException
     */
    void logout(String token, boolean expired) throws AuthenticationServiceException;

    /**
     * Check if the current session has expired given a String authentication token.
     * @param authToken String authentication token to check if session is expired
     * @return True if session expired, false if not
     */
    boolean sessionExpired(String authToken);

    /**
     * Used during testing.
     */
    void setParameter();

    /**
     * Switch from the current User to another by authenticating and returning a token associated with the new user.
     * This is the equivalent of logging in as the new User and acting as them entirely.
     * @param userId The User ID to switch to
     * @return String token used to authenticate all future requests, passed as a X-Auth-Token header by the UI
     * @throws AuthenticationServiceException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN,
            RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN, RoleName.GP_ADMIN })
    String switchToUser(Long userId) throws AuthenticationServiceException;

    /**
     * Switch back to the previous user using the token associated with the previous user.
     * @param token String token associated with the previous user
     * @param userId ID of the user to switch back
     * @return String token used to authenticate all future requests, passed as a X-Auth-Token header by the UI
     * @throws AuthenticationServiceException
     */
    @RoleOnly(roles = { RoleName.PATIENT })
    String switchBackFromUser(Long userId, String token) throws AuthenticationServiceException;
}
