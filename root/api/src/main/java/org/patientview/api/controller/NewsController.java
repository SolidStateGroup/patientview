package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.NewsService;
import org.patientview.persistence.model.NewsItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;

/**
 * Restful interface for the basic Crud operation for news.
 *
 * TODO Change a news link group and role.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@RestController
public class NewsController extends BaseController<NewsController> {

    @Inject
    private NewsService newsService;

    @RequestMapping(value = "/news", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NewsItem> add(@RequestBody NewsItem newsItem, UriComponentsBuilder uriComponentsBuilder)
    throws ResourceNotFoundException {

        newsItem = newsService.add(newsItem);

        LOG.info("Created new item with id " + newsItem.getId());
        UriComponents uriComponents = uriComponentsBuilder.path("/news/{id}").buildAndExpand(newsItem.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(newsItem, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/news/{newsItemId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<NewsItem> get(@PathVariable("newsItemId") Long newsItemId) throws ResourceNotFoundException  {
        return new ResponseEntity<>(newsService.get(newsItemId), HttpStatus.OK);
    }

    @RequestMapping(value = "/news", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<NewsItem> save(@RequestBody NewsItem newsItem, UriComponentsBuilder uriComponentsBuilder) throws ResourceNotFoundException  {

        try {
            NewsItem news = newsService.save(newsItem);
            LOG.info("Updated new item with id " + newsItem.getId());
            UriComponents uriComponents = uriComponentsBuilder.path("/news/{id}").buildAndExpand(newsItem.getId());
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(uriComponents.toUri());
            return new ResponseEntity<>(news, headers, HttpStatus.OK);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/news/{newsItemId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable("newsItemId") Long newsItemId) throws ResourceNotFoundException {
        newsService.delete(newsItemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/user/{userId}/news", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<NewsItem>> getByUser(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "page", required = false) String page) {

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : null;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : null;

        if (sizeConverted != null && sizeConverted != null) {
            pageable = new PageRequest(pageConverted, sizeConverted);
        } else {
            pageable = new PageRequest(0, Integer.MAX_VALUE);
        }

        try {
            LOG.debug("Request has been received for news of userId : {}", userId);
            Page<NewsItem> news = newsService.findByUserId(userId, pageable);
            return new ResponseEntity<>(news, HttpStatus.OK);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/news/{newsItemId}/group/{groupId}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> addGroup(@PathVariable("newsItemId") Long newsItemId,
                                         @PathVariable("groupId") Long groupId) throws ResourceNotFoundException {
        newsService.addGroup(newsItemId, groupId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/news/{newsItemId}/group/{groupId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> removeGroup(@PathVariable("newsItemId") Long newsItemId,
                                         @PathVariable("groupId") Long groupId) throws ResourceNotFoundException {
        newsService.removeGroup(newsItemId, groupId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/news/{newsItemId}/role/{roleId}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> addRole(@PathVariable("newsItemId") Long newsItemId,
                                         @PathVariable("roleId") Long roleId) throws ResourceNotFoundException {
        newsService.addRole(newsItemId, roleId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/news/{newsItemId}/role/{roleId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> removeRole(@PathVariable("newsItemId") Long newsItemId,
                                         @PathVariable("roleId") Long roleId) throws ResourceNotFoundException {
        newsService.removeRole(newsItemId, roleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/group/{groupId}/role/{roleId}/news/{newsItemId}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> addGroupAndRole(@PathVariable("groupId") Long groupId,
                                        @PathVariable("roleId") Long roleId,
                                        @PathVariable("newsItemId") Long newsItemId)
                                        throws ResourceNotFoundException {
        try {
            newsService.addGroupAndRole(newsItemId, groupId, roleId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/news/{newsItemId}/newslinks/{newsLinkId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> removeNewsLink(@PathVariable("newsItemId") Long newsItemId,
                                               @PathVariable("newsLinkId") Long newsLinkId)
                                        throws ResourceNotFoundException {
        try {
            newsService.removeNewsLink(newsItemId, newsLinkId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
