package org.patientview.api.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.MyMediaService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.enums.MediaTypes;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import static org.terracotta.modules.ehcache.ToolkitInstanceFactoryImpl.LOGGER;

/**
 * Admin RESTful interface for managing MadlinePlus services
 */
@ExcludeFromApiDoc
@RestController
public class MyMediaController extends BaseController<MyMediaController> {

    @Inject
    private MyMediaService myMediaService;

    @RequestMapping(value = "/mymedia/upload", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MyMedia uploadMyMedia(@RequestBody MyMedia myMedia)
            throws ResourceNotFoundException, ImportResourceException, ResourceForbiddenException,
            UnsupportedEncodingException {
        return myMediaService.save(myMedia);
    }


    @RequestMapping(value = "/mymedia/{id}/content", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void getMYMediaContent(@PathVariable("id") final Long id, HttpServletResponse response)
            throws ResourceNotFoundException, UnsupportedEncodingException, ResourceForbiddenException {
        InputStream is = null;
        MyMedia myMedia = myMediaService.get(id);

        //Throw an exception if the current user isnt the owner
        if (!myMedia.getCreator().getId().equals(ApiUtil.getCurrentUser().getId())) {

        }


        if (myMedia.getType().equals(MediaTypes.IMAGE)) {
            getMyMediaImage(myMedia, response);
        } else {

        }
    }


    private void getMyMediaImage(MyMedia myMedia, HttpServletResponse response)
            throws ResourceNotFoundException, UnsupportedEncodingException, ResourceForbiddenException {
        InputStream is = null;

        try {
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            is = new ByteArrayInputStream(myMedia.getContent());

            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            LOGGER.error("Failed to my media image id {}, exception: {}", myMedia.getId(), e.getMessage(), e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close input stream {}", e.getMessage());
                }
            }
        }
    }


    private void getMyMediaVideo(MyMedia myMedia,
                                 HttpServletResponse response)
            throws ResourceNotFoundException, UnsupportedEncodingException, ResourceForbiddenException {
        InputStream is = null;

        try {
            String fileName = String.format("%d-%d", new Date().getTime(), ApiUtil.getCurrentUser().getId());
            File temp = File.createTempFile(fileName, ".tmp");
            FileUtils.writeByteArrayToFile(temp, myMedia.getContent());


            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            is = new ByteArrayInputStream(myMedia.getContent());

            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
