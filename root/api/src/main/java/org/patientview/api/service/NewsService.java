package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * News service, for the management and retrieval of News. NewsItems are made visible to specific Groups, Roles and
 * combinations of the two using NewsLinks. NewsItems can be made publicly available where they will appear on the home
 * page without logging in.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface NewsService {

    /**
     * Add a NewsItem.
     *
     * @param newsItem News item to add
     * @return Long ID of the newly added NewsItem
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Long add(NewsItem newsItem);

    /**
     * Add a Group to a NewsItem, making it visible to that Group. Adds a NewsLink with Group set.
     *
     * @param newsItemId ID of NewsItem to make visible to Group
     * @param groupId    ID of Group to make NewsItem visible for
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void addGroup(Long newsItemId, Long groupId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Add a Group and Role to a NewsItem, making it visible to Users with that specific Group and Role. Adds a
     * NewsLink with Group and Role set.
     *
     * @param groupId    ID of Group to make NewsItem visible for
     * @param roleId     ID of Role Users must be a member of to see NewsItem
     * @param newsItemId ID of NewsItem to make visible to Group and Role
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void addGroupAndRole(Long newsItemId, Long groupId, Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Add a Role to a NewsItem, making it visible to Users with that Role. Adds a NewsLink with Role set.
     *
     * @param newsItemId ID of NewsItem to make visible to User
     * @param roleId     ID of Role Users must be a member of to see NewsItem
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void addRole(Long newsItemId, Long roleId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Delete a NewsItem.
     *
     * @param newsItemId ID of NewsItem to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void delete(Long newsItemId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a Page of NewsItems for a specific User.
     *
     * @param userId   ID of User to retrieve news for
     * @param pageable Pageable object containing pagination properties
     * @param newsType The news type to result (1=regular, 2=featured)
     * @return Page of NewsItem for a specific User
     * @throws ResourceNotFoundException
     */
    @UserOnly
    Page<org.patientview.api.model.NewsItem> findByUserId(Long userId,
                                                          int newsType,
                                                          boolean limitResults,
                                                          Pageable pageable)
            throws ResourceNotFoundException;

    /**
     * Get a single NewsItem.
     *
     * @param newsItemId ID of NewsItem to retrieve
     * @return NewsItem object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    NewsItem get(Long newsItemId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a Page of publicly available NewsItems given page size and number (pagination).
     *
     * @param pageable Pageable object containing pagination properties
     * @return Page of NewsItem
     * @throws ResourceNotFoundException
     */
    Page<org.patientview.api.model.NewsItem> getPublicNews(Pageable pageable) throws ResourceNotFoundException;

    /**
     * Remove a Group from a news item, making it invisible to that Group.
     *
     * @param newsItemId ID of NewsItem to hide from Group
     * @param groupId    ID of Group to hide NewsItem from
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void removeGroup(Long newsItemId, Long groupId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Delete a NewsLink from a NewsItem, removing visibility for the Group and/or Role set in that NewsLink from the
     * NewsItem.
     *
     * @param newsItemId ID of NewsItem to remove NewsLink from
     * @param newsLinkId ID of NewsLink to remove from NewsItem
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void removeNewsLink(Long newsItemId, Long newsLinkId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Remove visibility of a NewsItem for a specific Role.
     *
     * @param newsItemId ID of NewsItem to hide from a Role
     * @param roleId     ID of a Role to hide the NewsItem from
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void removeRole(Long newsItemId, Long roleId) throws ResourceNotFoundException;

    /**
     * Update a NewsItem.
     *
     * @param newsItem NewsItem to update
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void save(NewsItem newsItem) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Send email notification to all users in Group Roles for the NewsItem.
     *
     * @param newsItemId a NewsItem to sent notification for
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = {RoleName.GLOBAL_ADMIN, RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN,
            RoleName.DISEASE_GROUP_ADMIN, RoleName.GP_ADMIN})
    void notifyUsers(Long newsItemId) throws ResourceNotFoundException;
}
