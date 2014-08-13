package org.patientview.api.service.impl;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.NewsService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.NewsLink;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleTypes;
import org.patientview.persistence.model.enums.Roles;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to control the crud operations of the News.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Service
public class NewsServiceImpl extends AbstractServiceImpl<NewsServiceImpl> implements NewsService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private NewsItemRepository newsItemRepository;

    @Inject
    private EntityManager entityManager;

    public NewsItem add(final NewsItem newsItem) {

        User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        creator = userRepository.findOne(creator.getId());

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
                newsLink.setCreator(creator);
            }
        }
        return newsItemRepository.save(newsItem);
    }

    public NewsItem get(final Long newsItemId) {
        return newsItemRepository.findOne(newsItemId);
    }

    public NewsItem save(final NewsItem newsItem) throws ResourceNotFoundException {

        NewsItem entityNewsItem = newsItemRepository.findOne(newsItem.getId());
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException(String.format("Could not find news %s", newsItem.getId()));
        }

        entityNewsItem.setHeading(newsItem.getHeading());
        entityNewsItem.setStory(newsItem.getStory());
        return newsItemRepository.save(entityNewsItem);
    }

    public void delete(final Long newsItemId) {
        newsItemRepository.delete(newsItemId);
    }

    private NewsItem setEditable(final NewsItem newsItem, User user) {
        boolean edit = false;
        boolean delete = false;
        boolean specialtyOrGlobalAdmin = false;
        boolean singleGroup = true;

        // todo: single group check, required?

        // specialty and global admins can always edit/delete
        // (assume no users are specialty admin in one specialty and unit admin/patient in another)
        for (GroupRole groupRole : user.getGroupRoles()) {
            Roles groupRoleName = groupRole.getRole().getName();
            if (groupRoleName.equals(Roles.GLOBAL_ADMIN) || groupRoleName.equals(Roles.SPECIALTY_ADMIN)){
                edit = delete = specialtyOrGlobalAdmin = true;
            }
        }

        // for other users, can edit/delete if unit admin in group
        if (!specialtyOrGlobalAdmin) {
            for (NewsLink newsLink : newsItem.getNewsLinks()) {
                Group newsLinkGroup = newsLink.getGroup();
                Role newsLinkRole = newsLink.getRole();
                // can only edit/delete if user has exclusive access to newsitem (not attached to multiple groups)
                // todo: discuss
                if (singleGroup) {
                    // ignore newsLink where global admin role and no group (added by default during creation)
                    if (!(newsLinkRole != null && newsLinkRole.equals(Roles.GLOBAL_ADMIN))) {
                        if (newsLinkGroup != null) {
                            for (GroupRole groupRole : user.getGroupRoles()) {
                                Role groupRoleRole = groupRole.getRole();
                                Group groupRoleGroup = groupRole.getGroup();
                                // only STAFF role types can edit/delete
                                if (groupRoleRole.getRoleType().getValue().equals(RoleTypes.STAFF)) {
                                    // allow edit/delete if newsLink linked to your group and UNIT_ADMIN
                                    if (groupRoleGroup.equals(newsLinkGroup) && groupRoleRole.getName().equals(Roles.UNIT_ADMIN)) {
                                        edit = delete = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        newsItem.setEdit(edit);
        newsItem.setDelete(delete);
        return newsItem;
    }

    public Page<NewsItem> findByUserId(Long userId, Pageable pageable) throws ResourceNotFoundException {
        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException(String.format("Could not find user %s", userId));
        }

        // get both role and group news (directly accessed through newsLink)
        PageRequest pageableAll = new PageRequest(0, Integer.MAX_VALUE);
        Page<NewsItem> roleNews = newsItemRepository.findRoleNewsByUser(entityUser, pageableAll);
        Page<NewsItem> groupNews = newsItemRepository.findGroupNewsByUser(entityUser, pageableAll);

        // get specialty news (accessed by parent/child relationships from groups in newsLink)
        Page<NewsItem> specialtyNews = newsItemRepository.findSpecialtyNewsByUser(entityUser, pageableAll);

        // combine results
        Set<NewsItem> newsItemSet = new HashSet<>();
        if (roleNews != null && roleNews.getNumberOfElements() > 0) {
            newsItemSet.addAll(roleNews.getContent());
        }
        if (groupNews != null && groupNews.getNumberOfElements() > 0) {
            newsItemSet.addAll(groupNews.getContent());
        }
        if (specialtyNews != null && specialtyNews.getNumberOfElements() > 0) {
            newsItemSet.addAll(specialtyNews.getContent());
        }
        List<NewsItem> newsItems = new ArrayList<>(newsItemSet);

        // sort combined list
        Collections.sort(newsItems);

        // manually do pagination
        int startIndex = pageable.getOffset();
        int endIndex;

        if ((startIndex + pageable.getPageSize()) > newsItems.size()) {
            endIndex = newsItems.size();
        } else {
            endIndex = pageable.getPageSize();
        }

        List<NewsItem> pagedNewsItems = new ArrayList<>();

        if (!newsItems.isEmpty()) {
            pagedNewsItems = newsItems.subList(startIndex, endIndex);
        }

        // set if user can edit or delete (used for UNIT_ADMIN)
        for (NewsItem newsItem : pagedNewsItems) {
            newsItem = setEditable(newsItem, entityUser);
        }

        return new PageImpl<>(pagedNewsItems, pageable, newsItems.size());
    }

    public void addGroup(Long newsItemId, Long groupId) throws ResourceNotFoundException {
        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException(String.format("Could not find news %s", newsItemId));
        }

        Group entityGroup = groupRepository.findOne(groupId);
        if (entityGroup == null) {
            throw new ResourceNotFoundException(String.format("Could not find group %s", groupId));
        }

        boolean found = false;

        for (NewsLink newsLink : entityNewsItem.getNewsLinks()) {
            Group newsLinkGroup = newsLink.getGroup();
            if (newsLink.getGroup() != null && (newsLinkGroup.getId().equals(entityGroup.getId()))) {
                found = true;
            }
        }

        if (!found) {
            User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            creator = userRepository.findOne(creator.getId());

            NewsLink newsLink = new NewsLink();
            newsLink.setNewsItem(entityNewsItem);
            newsLink.setGroup(entityGroup);
            newsLink.setCreator(creator);

            entityNewsItem.getNewsLinks().add(newsLink);
            newsItemRepository.save(entityNewsItem);
        }
    }

    public void removeGroup(Long newsItemId, Long groupId) throws ResourceNotFoundException {
        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException(String.format("Could not find news %s", newsItemId));
        }

        Group entityGroup = groupRepository.findOne(groupId);
        if (entityGroup == null) {
            throw new ResourceNotFoundException(String.format("Could not find group %s", groupId));
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

    public void addRole(Long newsItemId, Long roleId) throws ResourceNotFoundException {
        NewsItem entityNewsItem = newsItemRepository.findOne(newsItemId);
        if (entityNewsItem == null) {
            throw new ResourceNotFoundException(String.format("Could not find news %s", newsItemId));
        }

        Role entityRole = roleRepository.findOne(roleId);
        if (entityRole == null) {
            throw new ResourceNotFoundException(String.format("Could not find role %s", roleId));
        }

        boolean found = false;

        for (NewsLink newsLink : entityNewsItem.getNewsLinks()) {
            Role newsLinkRole = newsLink.getRole();
            if (newsLink.getRole() != null && (newsLinkRole.getId().equals(entityRole.getId()))) {
                found = true;
            }
        }

        if (!found) {
            User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            creator = userRepository.findOne(creator.getId());

            NewsLink newsLink = new NewsLink();
            newsLink.setNewsItem(entityNewsItem);
            newsLink.setRole(entityRole);
            newsLink.setCreator(creator);

            entityNewsItem.getNewsLinks().add(newsLink);
            newsItemRepository.save(entityNewsItem);
        }
    }

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

    public void addGroupAndRole(Long newsItemId, Long groupId, Long roleId) throws ResourceNotFoundException {
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
            User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            NewsLink newsLink = new NewsLink();
            newsLink.setNewsItem(entityNewsItem);
            newsLink.setGroup(entityGroup);
            newsLink.setRole(entityRole);
            newsLink.setCreator(userRepository.findOne(creator.getId()));

            entityNewsItem.getNewsLinks().add(newsLink);
            newsItemRepository.save(entityNewsItem);
        }
    }

    public void removeNewsLink(Long newsItemId, Long newsLinkId) throws ResourceNotFoundException {
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

        entityNewsItem.getNewsLinks().remove(tempNewsLink);
        entityManager.remove(tempNewsLink);
        newsItemRepository.save(entityNewsItem);
    }
}
