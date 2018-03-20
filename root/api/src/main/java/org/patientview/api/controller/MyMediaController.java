package org.patientview.api.controller;

import org.apache.commons.io.IOUtils;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.MyMediaService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.MyMedia;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

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


    @RequestMapping(value = "/mymedia/{id}/image", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void getSlipImage(@PathVariable("id") final Long id, HttpServletResponse response)
            throws ResourceNotFoundException, UnsupportedEncodingException, ResourceForbiddenException {
        InputStream is = null;
        MyMedia myMedia = myMediaService.get(id);

        if (!myMedia.getCreator().getId().equals(ApiUtil.getCurrentUser().getId())){

        }

        try {
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            is = new ByteArrayInputStream(myMedia.getContent());;
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            LOGGER.error("Failed to my media image id {}, exception: {}", id, e.getMessage(), e);
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
}
