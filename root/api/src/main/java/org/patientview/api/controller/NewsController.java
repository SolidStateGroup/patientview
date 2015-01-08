package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.NewsService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.NewsItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * Restful interface for the basic Crud operation for news.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@RestController
public class NewsController extends BaseController<NewsController> {

    @Inject
    private NewsService newsService;

    @RequestMapping(value = "/news", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> add(@RequestBody NewsItem newsItem)
    throws ResourceNotFoundException {
        return new ResponseEntity<>(newsService.add(newsItem), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/news/{newsItemId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<NewsItem> get(@PathVariable("newsItemId") Long newsItemId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(newsService.get(newsItemId), HttpStatus.OK);
    }

    @RequestMapping(value = "/public/news", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.NewsItem>> getPublicNews(
            @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "page", required = false) String page) throws ResourceNotFoundException  {

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : null;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : null;

        if (pageConverted != null && sizeConverted != null) {
            pageable = new PageRequest(pageConverted, sizeConverted);
        } else {
            pageable = new PageRequest(0, Integer.MAX_VALUE);
        }

        return new ResponseEntity<>(newsService.getPublicNews(pageable), HttpStatus.OK);
    }

    @RequestMapping(value = "/news", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody NewsItem newsItem) throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.save(newsItem);
    }

    @RequestMapping(value = "/news/{newsItemId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("newsItemId") Long newsItemId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.delete(newsItemId);
    }

    @RequestMapping(value = "/user/{userId}/news", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.NewsItem>> getByUser(
            @PathVariable("userId") Long userId, @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "page", required = false) String page) throws ResourceNotFoundException {

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : null;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : null;

        if (pageConverted != null && sizeConverted != null) {
            pageable = new PageRequest(pageConverted, sizeConverted);
        } else {
            pageable = new PageRequest(0, Integer.MAX_VALUE);
        }

        return new ResponseEntity<>(newsService.findByUserId(userId, pageable), HttpStatus.OK);
    }

    @RequestMapping(value = "/news/{newsItemId}/group/{groupId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addGroup(@PathVariable("newsItemId") Long newsItemId, @PathVariable("groupId") Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.addGroup(newsItemId, groupId);
    }

    @RequestMapping(value = "/news/{newsItemId}/group/{groupId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeGroup(@PathVariable("newsItemId") Long newsItemId, @PathVariable("groupId") Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.removeGroup(newsItemId, groupId);
    }

    @RequestMapping(value = "/news/{newsItemId}/role/{roleId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addRole(@PathVariable("newsItemId") Long newsItemId, @PathVariable("roleId") Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.addRole(newsItemId, roleId);
    }

    @RequestMapping(value = "/news/{newsItemId}/role/{roleId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeRole(@PathVariable("newsItemId") Long newsItemId, @PathVariable("roleId") Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.removeRole(newsItemId, roleId);
    }

    @RequestMapping(value = "/group/{groupId}/role/{roleId}/news/{newsItemId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addGroupAndRole(@PathVariable("groupId") Long groupId, @PathVariable("roleId") Long roleId,
                                @PathVariable("newsItemId") Long newsItemId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.addGroupAndRole(newsItemId, groupId, roleId);
    }

    @RequestMapping(value = "/news/{newsItemId}/newslinks/{newsLinkId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeNewsLink(@PathVariable("newsItemId") Long newsItemId, @PathVariable("newsLinkId") Long newsLinkId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        newsService.removeNewsLink(newsItemId, newsLinkId);
    }
}
