package org.patientview.api.controller;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.NewsService;
import org.patientview.persistence.model.NewsItem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    public ResponseEntity<NewsItem> createGroup(@RequestBody NewsItem newsItem, UriComponentsBuilder uriComponentsBuilder)
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
    public ResponseEntity<NewsItem> getNewsItem(@PathVariable("newsItemId") Long newsItemId) throws ResourceNotFoundException  {
        return new ResponseEntity<>(newsService.get(newsItemId), HttpStatus.OK);
    }

    @RequestMapping(value = "/news", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<NewsItem> saveNewsItem(@RequestBody NewsItem newsItem, UriComponentsBuilder uriComponentsBuilder) throws ResourceNotFoundException  {
        LOG.info("Updated new item with id " + newsItem.getId());
        UriComponents uriComponents = uriComponentsBuilder.path("/news/{id}").buildAndExpand(newsItem.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(newsService.save(newsItem), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/news/{newsItemId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteNewsItem(@PathVariable("newsItemId") Long newsItemId) throws ResourceNotFoundException {
        newsService.delete(newsItemId);
        return new ResponseEntity<>( HttpStatus.NO_CONTENT);
    }


}
