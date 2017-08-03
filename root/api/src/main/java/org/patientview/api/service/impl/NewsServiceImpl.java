package org.patientview.api.service.impl;

import org.patientview.api.service.NewsService;
import org.patientview.api.service.StaticDataManager;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.NewsLink;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Class to control the crud operations of the News.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Service
public class NewsServiceImpl extends AbstractServiceImpl<NewsServiceImpl> implements NewsService {

    @Inject
    private EntityManager entityManager;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private NewsItemRepository newsItemRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private StaticDataManager staticDataManager;

    @Inject
    private UserRepository userRepository;

    @CacheEvict(value = "findNewsByUserId", allEntries = true)
    public Long add(final NewsItem newsItem) {
        if (!CollectionUtils.isEmpty(newsItem.getNewsLinks())) {
            for (NewsLink newsLink : newsItem.getNewsLinks()) {
                if (newsLink.getGroup() != null && newsLink.getGroup().getId() != null) {
                    newsLink.setGroup(groupRepository.findOne(newsLink.getGroup().getId()));
                } else {
                    newsLink.setGroup(null);
                }

                if (newsLink.getRole() != null && newsLink.getRole().getId() != null) {
                    newsLink.setRole(roleRepository.findOne(newsLink.getRole().getId()));
                } else {
                    newsLink.setRole(null);
                }

                newsLink.setNewsItem(newsItem);
                newsLink.setCreator(getCurrentUser());
            }
        }

        // set updater and update time (used for ordering correctly)
        User currentUser = getCurrentUser();
        newsItem.setCreator(currentUser);
        newsItem.setLastUpdater(currentUser);
        newsItem.setLastUpdate(newsItem.getCreated());

        return newsItemRepository.save(newsItem).getId();
    }

    @CacheEvict(value = "findNewsByUserId", allEntries = true)
    public void addGroup(Long newsItemId, Long groupId) throws ResourceNotFoundException, ResourceForbiddenException {
        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException(String.format("Could not find news %s", newsItemId));
        }

        Group entityGroup = groupRepository.findOne(groupId);
        if (entityGroup == null) {
            throw new ResourceNotFoundException(String.format("Could not find group %s", groupId));
        }

        if (!isUserMemberOfGroup(getCurrentUser(), entityGroup)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        boolean found = false;

        for (NewsLink newsLink : entityNewsItem.getNewsLinks()) {
            Group newsLinkGroup = newsLink.getGroup();
            if (newsLink.getGroup() != null && (newsLinkGroup.getId().equals(entityGroup.getId()))) {
                found = true;
            }
        }

        if (!found) {
            NewsLink newsLink = new NewsLink();
            newsLink.setNewsItem(entityNewsItem);
            newsLink.setGroup(entityGroup);
            newsLink.setCreator(getCurrentUser());
            entityNewsItem.getNewsLinks().add(newsLink);
            newsItemRepository.save(entityNewsItem);
        }
    }

    /**
     * Card #458: news permissions.
     */
    private void checkRolePermissions(Role entityRole) throws ResourceForbiddenException {
        if (ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)) {
            // can do everything
        } else if (ApiUtil.currentUserHasRole(RoleName.SPECIALTY_ADMIN)) {
            // no public or global admin news
            if (Arrays.asList(new String[]{"PUBLIC", "GLOBAL_ADMIN"}).contains(entityRole.getName().toString())) {
                throw new ResourceForbiddenException("Forbidden");
            }
        } else {
            // #458 "Unit Admin can create news for Unit Admins/Unit Staff/Patients/Logged In Users"
            // unit admin, staff admin, gp admin, disease group admin
            if (Arrays.asList(new String[]{"PUBLIC", "SPECIALTY_ADMIN", "GLOBAL_ADMIN"})
                    .contains(entityRole.getName().toString())) {
                throw new ResourceForbiddenException("Forbidden");
            }
        }
    }

    @CacheEvict(value = "findNewsByUserId", allEntries = true)
    public void addGroupAndRole(Long newsItemId, Long groupId, Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException(String.format("Could not find news %s", newsItemId));
        }

        Group entityGroup = groupRepository.findOne(groupId);
        if (entityGroup == null) {
            throw new ResourceNotFoundException(String.format("Could not find group %s", groupId));
        }

        Role entityRole = roleRepository.findOne(roleId);
        if (entityRole == null) {
            throw new ResourceNotFoundException(String.format("Could not find role %s", roleId));
        }

        if (!isUserMemberOfGroup(getCurrentUser(), entityGroup)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        // #458 PUBLIC role is not allowed when adding with a group
        if (entityRole.getName().equals(RoleName.PUBLIC)) {
            throw new ResourceForbiddenException("Can only add non logged in users to All Groups");
        }

        // #458 restrict roles
        checkRolePermissions(entityRole);

        boolean found = false;

        for (NewsLink newsLink : entityNewsItem.getNewsLinks()) {
            Group newsLinkGroup = newsLink.getGroup();
            Role newsLinkRole = newsLink.getRole();
            if ((newsLink.getGroup() != null && (newsLinkGroup.getId().equals(entityGroup.getId())))
                    && (newsLink.getRole() != null && (newsLinkRole.getId().equals(entityRole.getId())))) {
                found = true;
            }
        }

        if (!found) {
            NewsLink newsLink = new NewsLink();
            newsLink.setNewsItem(entityNewsItem);
            newsLink.setGroup(entityGroup);
            newsLink.setRole(entityRole);
            newsLink.setCreator(getCurrentUser());
            entityNewsItem.getNewsLinks().add(newsLink);

            newsItemRepository.save(entityNewsItem);
        }
    }

    @CacheEvict(value = "findNewsByUserId", allEntries = true)
    public void addRole(Long newsItemId, Long roleId) throws ResourceNotFoundException, ResourceForbiddenException {
        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException(String.format("Could not find news %s", newsItemId));
        }

        Role entityRole = roleRepository.findOne(roleId);
        if (entityRole == null) {
            throw new ResourceNotFoundException(String.format("Could not find role %s", roleId));
        }

        // only global admin can add public roles
        if (entityRole.getName().equals(RoleName.PUBLIC) && !ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        // #458 restrict roles
        checkRolePermissions(entityRole);

        boolean found = false;

        for (NewsLink newsLink : entityNewsItem.getNewsLinks()) {
            Role newsLinkRole = newsLink.getRole();
            if (newsLink.getRole() != null && newsLink.getGroup() == null
                    && (newsLinkRole.getId().equals(entityRole.getId()))) {
                found = true;
            }
        }

        if (!found) {
            User creator = getCurrentUser();

            NewsLink newsLink = new NewsLink();
            newsLink.setNewsItem(entityNewsItem);
            newsLink.setRole(entityRole);
            newsLink.setCreator(creator);

            entityNewsItem.getNewsLinks().add(newsLink);
            newsItemRepository.save(entityNewsItem);
        }
    }

    private boolean canModifyNewsItem(NewsItem newsItem) {
        if (ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)) {
            return true;
        }

        for (NewsLink newsLink : newsItem.getNewsLinks()) {
            if (newsLink.getGroup() == null) {
                // ignore GLOBAL_ADMIN and PUBLIC roles
                if (newsLink.getRole().getName().equals(RoleName.PUBLIC)) {
                    return true;
                }
            } else if (isUserMemberOfGroup(getCurrentUser(), newsLink.getGroup())) {
                return true;
            }
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return (newsItem.getCreator() != null && newsItem.getCreator().getId().equals(currentUser.getId()))
                || (newsItem.getLastUpdater() != null && newsItem.getLastUpdater().getId().equals(currentUser.getId()));
    }

    private boolean canModifyNewsLink(final NewsLink newsLink) {
        if (newsLink.getGroup() != null && ApiUtil.currentUserHasRole(RoleName.UNIT_ADMIN)) {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return false;
            }

            for (GroupRole groupRole : currentUser.getGroupRoles()) {
                if (groupRole.getGroup().equals(newsLink.getGroup())) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @CacheEvict(value = "findNewsByUserId", allEntries = true)
    public void delete(final Long newsItemId) throws ResourceNotFoundException, ResourceForbiddenException {
        NewsItem newsItem = newsItemRepository.findOne(newsItemId);
        if (newsItem == null) {
            throw new ResourceNotFoundException("NewsItem does not exist");
        }

        if (!canModifyNewsItem(newsItem)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        newsItemRepository.delete(newsItemId);
    }

    private List<NewsItem> extractNewsItems(Page<NewsItem> newsItemPage) {
        if (newsItemPage != null && newsItemPage.getNumberOfElements() > 0) {
            return newsItemPage.getContent();
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable(value = "findNewsByUserId")
    public Page<org.patientview.api.model.NewsItem> findByUserId(Long userId, int newsTypeId, boolean limitResults,
                                                                 Pageable pageable) throws ResourceNotFoundException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException(String.format("Could not find user %s", userId));
        }

        // get role, group and grouprole specific news (directly accessed through newsLink)
        PageRequest pageableAll = new PageRequest(0, Integer.MAX_VALUE);
        Set<NewsItem> newsItemSet = new HashSet<>();

        if (newsTypeId != staticDataManager.getLookupByTypeAndValue(LookupTypes.NEWS_TYPE, "ALL").getId()) {

            newsItemSet.addAll(extractNewsItems(
                    newsItemRepository.findRoleNewsByUserAndType(entityUser, newsTypeId, pageableAll)));
            newsItemSet.addAll(extractNewsItems(
                    newsItemRepository.findGroupNewsByUserAndType(entityUser, newsTypeId, pageableAll)));
            newsItemSet.addAll(extractNewsItems(
                    newsItemRepository.findGroupRoleNewsByUserAndType(entityUser, newsTypeId, pageableAll)));
            newsItemSet.addAll(extractNewsItems(
                    newsItemRepository.findSpecialtyNewsByUserAndType(entityUser, newsTypeId, pageableAll)));
            newsItemSet.addAll(extractNewsItems(
                    newsItemRepository.findCreatorUpdaterNewsByUserAndType(entityUser, newsTypeId, pageableAll)));
        } else {
            newsItemSet.addAll(extractNewsItems(newsItemRepository.findRoleNewsByUser(entityUser, pageableAll)));
            newsItemSet.addAll(extractNewsItems(newsItemRepository.findGroupNewsByUser(entityUser, pageableAll)));
            newsItemSet.addAll(extractNewsItems(newsItemRepository.findGroupRoleNewsByUser(entityUser, pageableAll)));
            newsItemSet.addAll(extractNewsItems(newsItemRepository.findSpecialtyNewsByUser(entityUser, pageableAll)));
            newsItemSet.addAll(extractNewsItems(
                    newsItemRepository.findCreatorUpdaterNewsByUser(entityUser, pageableAll)));
        }

        List<NewsItem> newsItems = new ArrayList<>(newsItemSet);

        // sort by last updated DESC
        Collections.sort(newsItems, new Comparator<NewsItem>() {
            @Override
            public int compare(NewsItem n1, NewsItem n2) {
                if (n1.getLastUpdate() == null || n2.getLastUpdate() == null) {
                    if (n1.getLastUpdate() == null && n2.getLastUpdate() != null) {
                        return n1.getCreated().compareTo(n2.getLastUpdate()) * -1;
                    }
                    if (n1.getLastUpdate() != null && n2.getLastUpdate() == null) {
                        return n1.getLastUpdate().compareTo(n2.getCreated()) * -1;
                    }
                    return 0;
                }
                return n1.getLastUpdate().compareTo(n2.getLastUpdate()) * -1;
            }
        });

        //Limit featured articles to 1 per group if we are on the dashboard
        if (newsTypeId == staticDataManager.getLookupByTypeAndValue(LookupTypes.NEWS_TYPE, "DASHBOARD").getId()
                && limitResults) {
            List<Group> groups = new ArrayList<>();
            List<NewsItem> tmpNewsItems = new ArrayList<>();

            for (NewsItem newsItem : newsItems) {
                boolean containsGroup = false;
                int i = 0;
                for (NewsLink newsLink : newsItem.getNewsLinks()) {
                    if (newsLink.getGroup() != null) {
                        //Check if we already have an item for this group
                        if (groups.contains(newsLink.getGroup())) {
                            if (i <= 0 || containsGroup) {
                                containsGroup = true;
                            }
                        } else {
                            //If not, add to the groups list
                            groups.add(newsLink.getGroup());
                            containsGroup = false;
                        }
                    }
                    i++;
                }
                if (!containsGroup) {
                    tmpNewsItems.add(newsItem);
                }
            }
            //Replace the set with the reduced one.
            newsItems = tmpNewsItems;
        }

        // manually do pagination
        int startIndex = pageable.getOffset();
        int endIndex;

        if ((startIndex + pageable.getPageSize()) > newsItems.size()) {
            endIndex = newsItems.size();
        } else {
            endIndex = startIndex + pageable.getPageSize();
        }

        List<NewsItem> pagedNewsItems = new ArrayList<>();

        if (!newsItems.isEmpty()) {
            pagedNewsItems = newsItems.subList(startIndex, endIndex);
        }

        // set if user can edit or delete (used for UNIT_ADMIN)
        List<org.patientview.api.model.NewsItem> transportNewsItems = new ArrayList<>();
        for (NewsItem newsItem : pagedNewsItems) {
            transportNewsItems.add(new org.patientview.api.model.NewsItem(setEditable(newsItem, entityUser)));
        }

        return new PageImpl<>(transportNewsItems, pageable, newsItems.size());
    }

    public NewsItem get(final Long newsItemId) throws ResourceNotFoundException, ResourceForbiddenException {
        NewsItem newsItem = newsItemRepository.findOne(newsItemId);
        if (newsItem == null) {
            throw new ResourceNotFoundException("NewsItem does not exist");
        }

        return newsItem;
    }

    public Page<org.patientview.api.model.NewsItem> getPublicNews(Pageable pageable)
            throws ResourceNotFoundException {
        //return newsItemRepository.getPublicNews(pageable);
        List<NewsItem> newsItems = new ArrayList<>(extractNewsItems(newsItemRepository.getPublicNews(pageable)));

        // set if user can edit or delete (used for UNIT_ADMIN)
        List<org.patientview.api.model.NewsItem> transportNewsItems = new ArrayList<>();
        for (NewsItem newsItem : newsItems) {
            transportNewsItems.add(new org.patientview.api.model.NewsItem(newsItem));
        }

        return new PageImpl<>(transportNewsItems, pageable, newsItems.size());
    }

    @CacheEvict(value = "findNewsByUserId", allEntries = true)
    public void removeRole(Long newsItemId, Long roleId) throws ResourceNotFoundException {
        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException(String.format("Could not find news %s", newsItemId));
        }

        Role entityRole = roleRepository.findOne(roleId);
        if (entityRole == null) {
            throw new ResourceNotFoundException(String.format("Could not find role %s", roleId));
        }

        NewsLink tempNewsLink = null;
        for (NewsLink newsLink : entityNewsItem.getNewsLinks()) {
            if (newsLink.getRole() != null && (newsLink.getRole().getId().equals(entityRole.getId()))) {
                tempNewsLink = newsLink;
            }
        }

        entityNewsItem.getNewsLinks().remove(tempNewsLink);
        entityManager.remove(tempNewsLink);
        newsItemRepository.save(entityNewsItem);
    }

    @CacheEvict(value = "findNewsByUserId", allEntries = true)
    public void removeGroup(Long newsItemId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException(String.format("Could not find news %s", newsItemId));
        }

        Group entityGroup = groupRepository.findOne(groupId);
        if (entityGroup == null) {
            throw new ResourceNotFoundException(String.format("Could not find group %s", groupId));
        }

        if (!isUserMemberOfGroup(getCurrentUser(), entityGroup)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        NewsLink tempNewsLink = null;
        for (NewsLink newsLink : entityNewsItem.getNewsLinks()) {
            if (newsLink.getGroup() != null && (newsLink.getGroup().getId().equals(entityGroup.getId()))) {
                tempNewsLink = newsLink;
            }
        }

        entityNewsItem.getNewsLinks().remove(tempNewsLink);
        entityManager.remove(tempNewsLink);
        newsItemRepository.save(entityNewsItem);
    }

    @CacheEvict(value = "findNewsByUserId", allEntries = true)
    public void removeNewsLink(Long newsItemId, Long newsLinkId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException(String.format("Could not find news %s", newsItemId));
        }

        NewsLink tempNewsLink = null;
        for (NewsLink newsLink : entityNewsItem.getNewsLinks()) {
            if ((newsLink.getId().equals(newsLinkId))) {
                tempNewsLink = newsLink;
            }
        }

        // unit admins can only delete where group is in their group roles
        if (!canModifyNewsLink(tempNewsLink)) {
            throw new ResourceForbiddenException("You cannot delete this Group and Role");
        }

        entityNewsItem.getNewsLinks().remove(tempNewsLink);
        entityManager.remove(tempNewsLink);
        newsItemRepository.save(entityNewsItem);
    }

    @CacheEvict(value = "findNewsByUserId", allEntries = true)
    public void save(final NewsItem newsItem) throws ResourceNotFoundException, ResourceForbiddenException {
        NewsItem entityNewsItem = newsItemRepository.findOne(newsItem.getId());
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException(String.format("Could not find news %s", newsItem.getId()));
        }

        entityNewsItem.setHeading(newsItem.getHeading());
        entityNewsItem.setStory(newsItem.getStory());
        entityNewsItem.setLastUpdate(new Date());
        entityNewsItem.setLastUpdater(userRepository.findOne(getCurrentUser().getId()));
        newsItemRepository.save(entityNewsItem);
    }

    private NewsItem setEditable(final NewsItem newsItem, final User user) {
        // todo: single group check, required?
        // specialty and global admins can always edit/delete
        // (assume no users are specialty admin in one specialty and unit admin/patient in another)
        boolean editDelete = userIsGlobalAdmin(user) || userCanEditDeleteNewsItem(newsItem, user);
        newsItem.setEdit(editDelete);
        newsItem.setDelete(editDelete);
        return newsItem;
    }

    private boolean userCanEditDeleteNewsItem(final NewsItem newsItem, final User user) {
        boolean canEditDeleteNewsItem = false;
        for (NewsLink newsLink : newsItem.getNewsLinks()) {
            Group newsLinkGroup = newsLink.getGroup();
            Role newsLinkRole = newsLink.getRole();

            // ignore newsLink where global admin role and no group (added by default during creation)
            if (!(newsLinkRole != null
                    && newsLinkRole.getName().equals(RoleName.GLOBAL_ADMIN))
                    && newsLinkGroup != null) {
                if (userIsUnitAdminForNewsLink(newsLink, user) || userIsSpecialtyAdminForNewsLink(newsLink, user)) {
                    canEditDeleteNewsItem = true;
                }
            }
        }

        if ((newsItem.getCreator() != null && newsItem.getCreator().equals(user))
                || (newsItem.getLastUpdater() != null && newsItem.getLastUpdater().equals(user))) {
            canEditDeleteNewsItem = true;
        }

        return canEditDeleteNewsItem;
    }

    private boolean userIsGlobalAdmin(User user) {
        for (GroupRole groupRole : user.getGroupRoles()) {
            RoleName groupRoleName = groupRole.getRole().getName();
            if (groupRoleName.equals(RoleName.GLOBAL_ADMIN)) {
                return true;
            }
        }
        return false;
    }

    private boolean userIsUnitAdminForNewsLink(final NewsLink newsLink, final User user) {
        for (GroupRole groupRole : user.getGroupRoles()) {
            RoleType groupRoleRoleType = groupRole.getRole().getRoleType().getValue();
            RoleName groupRoleRoleName = groupRole.getRole().getName();
            Group groupRoleGroup = groupRole.getGroup();

            // only STAFF role types can edit/delete, allow edit/delete if newsLink linked to your group and UNIT_ADMIN
            if (groupRoleRoleType.equals(RoleType.STAFF)
                    && (groupRoleGroup.equals(newsLink.getGroup())
                    && groupRoleRoleName.equals(RoleName.UNIT_ADMIN))) {
                return true;
            }
        }
        return false;
    }

    private boolean userIsSpecialtyAdminForNewsLink(final NewsLink newsLink, final User user) {
        for (GroupRole groupRole : user.getGroupRoles()) {
            RoleType roleType = groupRole.getRole().getRoleType().getValue();
            RoleName roleName = groupRole.getRole().getName();
            Group group = groupRole.getGroup();

            if (group.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                if (roleType.equals(RoleType.STAFF)
                        && (groupRepository.findChildren(group).contains(newsLink.getGroup())
                            || group.equals(newsLink.getGroup()))
                        && roleName.equals(RoleName.SPECIALTY_ADMIN)) {
                    return true;
                }
            } else {
                if (roleType.equals(RoleType.STAFF)
                        && group.equals(newsLink.getGroup())
                        && roleName.equals(RoleName.SPECIALTY_ADMIN)) {
                    return true;
                }
            }
        }

        return false;
    }
}
