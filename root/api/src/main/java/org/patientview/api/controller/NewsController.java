package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.NewsService;
import org.patientview.api.service.StaticDataManager;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.enums.LookupTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

/**
 * RESTful interface for the management and retrieval of News. NewsItems are made visible to specific Groups, Roles and
 * combinations of the two using NewsLinks. NewsItems can be made publicly available where they will appear on the home
 * page without logging in.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@RestController
@ExcludeFromApiDoc
public class NewsController extends BaseController<NewsController> {

    @Inject
    private NewsService newsService;

    @Inject
    private StaticDataManager staticDataManager;

    /**
     * Add a NewsItem.
     * @param newsItem News item to add
     * @return Long ID of the newly added NewsItem
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/news", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> add(@RequestBody NewsItem newsItem)
    throws ResourceNotFoundException {
        return new ResponseEntity<>(newsService.add(newsItem), HttpStatus.CREATED);
    }

    /**
     * Add a Group to a NewsItem, making it visible to that Group. Adds a NewsLink with Group set.
     * @param newsItemId ID of NewsItem to make visible to Group
     * @param groupId ID of Group to make NewsItem visible for
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/news/{newsItemId}/group/{groupId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addGroup(@PathVariable("newsItemId") Long newsItemId, @PathVariable("groupId") Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.addGroup(newsItemId, groupId);
    }

    /**
     * Add a Group and Role to a NewsItem, making it visible to Users with that specific Group and Role. Adds a
     * NewsLink with Group and Role set.
     * @param groupId ID of Group to make NewsItem visible for
     * @param roleId ID of Role Users must be a member of to see NewsItem
     * @param newsItemId ID of NewsItem to make visible to Group and Role
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/group/{groupId}/role/{roleId}/news/{newsItemId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addGroupAndRole(@PathVariable("groupId") Long groupId, @PathVariable("roleId") Long roleId,
                                @PathVariable("newsItemId") Long newsItemId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.addGroupAndRole(newsItemId, groupId, roleId);
    }

    /**
     * Add a Role to a NewsItem, making it visible to Users with that Role. Adds a NewsLink with Role set.
     * @param newsItemId ID of NewsItem to make visible to User
     * @param roleId ID of Role Users must be a member of to see NewsItem
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/news/{newsItemId}/role/{roleId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addRole(@PathVariable("newsItemId") Long newsItemId, @PathVariable("roleId") Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.addRole(newsItemId, roleId);
    }

    /**
     * Delete a NewsItem.
     * @param newsItemId ID of NewsItem to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/news/{newsItemId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("newsItemId") Long newsItemId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.delete(newsItemId);
    }

    /**
     * Get a Page of NewsItems for a specific User.
     * @param userId ID of User to retrieve news for
     * @param size Size of the page
     * @param newsTypeString the id of the items we want to show
     * @param limitResults if we want to show all items or just 2 items per group (dashboard only)
     * @param page Page number
     * @return Page of NewsItem for a specific User
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/news", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.NewsItem>> findByUserId(
            @PathVariable("userId") Long userId, @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "newsType", required = false) String newsTypeString,
            @RequestParam(value = "limitResults", required = false) boolean limitResults,
            @RequestParam(value = "page", required = false) String page) throws ResourceNotFoundException {

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : null;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : null;

        if (pageConverted != null && sizeConverted != null) {
            pageable = PageRequest.of(pageConverted, sizeConverted);
        } else {
            pageable = PageRequest.of(0, Integer.MAX_VALUE);
        }

        int newsTypeId;

        if (newsTypeString != null) {
            newsTypeId = Integer.parseInt(newsTypeString);
        } else {
            newsTypeId = Integer.parseInt(
                    staticDataManager.getLookupByTypeAndValue(LookupTypes.NEWS_TYPE, "ALL").getId().toString());
        }

        return new ResponseEntity<>(
                newsService.findByUserId(userId, newsTypeId, limitResults, pageable), HttpStatus.OK);
    }

    /**
     * Get a single NewsItem.
     * @param newsItemId ID of NewsItem to retrieve
     * @return NewsItem object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/news/{newsItemId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<NewsItem> get(@PathVariable("newsItemId") Long newsItemId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(newsService.get(newsItemId), HttpStatus.OK);
    }

    /**
     * Get a Page of publicly available NewsItems given page size and number (pagination).
     * @param size Size of the page
     * @param page Page number
     * @return Page of NewsItem
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/public/news", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.NewsItem>> getPublicNews(
            @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "page", required = false) String page) throws ResourceNotFoundException  {

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : null;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : null;

        if (pageConverted != null && sizeConverted != null) {
            pageable = PageRequest.of(pageConverted, sizeConverted, Sort.Direction.DESC, "lastUpdate");
        } else {
            pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.DESC, "lastUpdate");
        }

        return new ResponseEntity<>(newsService.getPublicNews(pageable), HttpStatus.OK);
    }

    /**
     * Remove a Group from a news item, making it invisible to that Group.
     * @param newsItemId ID of NewsItem to hide from Group
     * @param groupId ID of Group to hide NewsItem from
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/news/{newsItemId}/group/{groupId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeGroup(@PathVariable("newsItemId") Long newsItemId, @PathVariable("groupId") Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.removeGroup(newsItemId, groupId);
    }

    /**
     * Delete a NewsLink from a NewsItem, removing visibility for the Group and/or Role set in that NewsLink from the
     * NewsItem.
     * @param newsItemId ID of NewsItem to remove NewsLink from
     * @param newsLinkId ID of NewsLink to remove from NewsItem
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/news/{newsItemId}/newslinks/{newsLinkId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeNewsLink(@PathVariable("newsItemId") Long newsItemId, @PathVariable("newsLinkId") Long newsLinkId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.removeNewsLink(newsItemId, newsLinkId);
    }

    /**
     * Remove visibility of a NewsItem for a specific Role.
     * @param newsItemId ID of NewsItem to hide from a Role
     * @param roleId ID of a Role to hide the NewsItem from
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/news/{newsItemId}/role/{roleId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeRole(@PathVariable("newsItemId") Long newsItemId, @PathVariable("roleId") Long roleId)
            throws ResourceNotFoundException {
        newsService.removeRole(newsItemId, roleId);
    }

    /**
     * Update a NewsItem.
     * @param newsItem NewsItem to update
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/news", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody NewsItem newsItem) throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.save(newsItem);
    }


    /**
     * Send email notification to all users in Group Roles for the NewsItem.
     *
     * @param newsItemId ID of NewsItem to send notification for
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/news/{newsItemId}/notify", method = RequestMethod.POST)
    @ResponseBody
    public void notifyUsers(@PathVariable("newsItemId") Long newsItemId)
            throws ResourceNotFoundException {
        newsService.notifyUsers(newsItemId);
    }
}
