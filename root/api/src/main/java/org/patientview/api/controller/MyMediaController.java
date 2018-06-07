package org.patientview.api.controller;

import org.apache.commons.io.IOUtils;
import org.im4java.core.IM4JavaException;
import org.jcodec.api.JCodecException;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ConversationService;
import org.patientview.api.service.MyMediaService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.MediaUserSpaceLimitException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.enums.MediaTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.Date;

import static org.terracotta.modules.ehcache.ToolkitInstanceFactoryImpl.LOGGER;

/**
 * Admin RESTful interface for managing MyMedia services
 */
@ExcludeFromApiDoc
@RestController
public class MyMediaController extends BaseController<MyMediaController> {

    @Inject
    private MyMediaService myMediaService;


    @Inject
    private ConversationService conversationService;

    @RequestMapping(value = "/user/{userId}/mymedia/upload", method = RequestMethod.POST, consumes = MediaType
            .APPLICATION_JSON_VALUE)
    @ResponseBody
    public org.patientview.api.model.MyMedia uploadMyMedia(@PathVariable("userId") Long userId, @RequestBody MyMedia
            myMedia)
            throws ResourceNotFoundException, ImportResourceException, ResourceForbiddenException,
            IOException, IM4JavaException, InterruptedException, JCodecException, MediaUserSpaceLimitException {
        return myMediaService.save(userId, myMedia);
    }

    @RequestMapping(value = "/user/{userId}/mymedia/{myMediaId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteMyMedia(@PathVariable("userId") Long userId, @PathVariable("myMediaId") Long myMediaId)
            throws ResourceNotFoundException, ImportResourceException, ResourceForbiddenException,
            IOException, IM4JavaException, InterruptedException {
        myMediaService.delete(myMediaId);
    }

    @RequestMapping(value = "/user/{userId}/mymedia", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageImpl<org.patientview.api.model.MyMedia>> getMyMedia(@PathVariable("userId") Long userId,
                                                                                  GetParameters getParameters)
            throws ResourceNotFoundException, UnsupportedEncodingException, ResourceForbiddenException {
        return new ResponseEntity<>(myMediaService.getAllForUser(userId, getParameters), HttpStatus.OK);
    }

    @RequestMapping(value = "/mymedia/{id}/content", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void getMyMediaContent(@PathVariable("id") final Long id, HttpServletResponse response)
            throws ResourceNotFoundException, UnsupportedEncodingException, ResourceForbiddenException {
        InputStream is = null;
        org.patientview.api.model.MyMedia myMedia = myMediaService.get(id);

        //Throw an exception if the current user isnt the owner
        if (!myMedia.getCreator().getId().equals(ApiUtil.getCurrentUser().getId()) &&
                !ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)) {
            throw new ResourceForbiddenException("You are not authorised to view this media");
        }

        //Check what kind of media it is and stream the response
        if (myMedia.getType().equals(MediaTypes.IMAGE)) {
            getMyMediaImage(myMedia.getContent(), response);
        } else {
            getMyMediaVideo(myMedia, response);
        }
    }

    @RequestMapping(value = "/mymedia/{id}/preview", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void getMyMediaPreview(@PathVariable("id") final Long id, HttpServletResponse response)
            throws ResourceNotFoundException, IOException, ResourceForbiddenException, IM4JavaException,
            InterruptedException {
        InputStream is = null;
        org.patientview.api.model.MyMedia myMedia = myMediaService.get(id);

        //Throw an exception if the current user isnt the owner
        if (!myMedia.getCreator().getId().equals(ApiUtil.getCurrentUser().getId()) &&
                !ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)) {
            throw new ResourceForbiddenException("You are not authorised to view this media");
        }

        //Check what kind of media it is and stream the response
        if (myMedia.getType().equals(MediaTypes.IMAGE)) {
            getMyMediaImage(myMedia.getThumbnailContent(), response);
        } else {
            getMyMediaVideo(myMedia, response);
        }
    }

    @RequestMapping(value = "/message/{messageId}/attachment", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void getAttachmentsForConversation(@PathVariable("messageId") final Long messageId,
                                              HttpServletResponse response)
            throws ResourceNotFoundException, UnsupportedEncodingException, ResourceForbiddenException {
        //Check that the current user is part of a conversation and get the message
        Message message = conversationService.getMessageById(messageId);

        if (message.getMyMedia().getType().equals(MediaTypes.IMAGE)) {
            getMyMediaImage(message.getMyMedia().getContent(), response);
        } else {
            getMyMediaVideo(message.getMyMedia(), response);
        }
    }


    @RequestMapping(value = "/message/{messageId}/attachment/preview", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void getAttachmentPreviewForConversation(@PathVariable("messageId") final Long messageId,
                                                    HttpServletResponse response)
            throws ResourceNotFoundException, UnsupportedEncodingException, ResourceForbiddenException {
        //Check that the current user is part of a conversation and get the message
        Message message = conversationService.getMessageById(messageId);

        if (message.getMyMedia() != null) {
            if (message.getMyMedia().getType().equals(MediaTypes.IMAGE)) {
                getMyMediaImage(message.getMyMedia().getThumbnailContent(), response);
            } else {
                getMyMediaVideo(message.getMyMedia(), response);
            }
        } else {
            LOGGER.warn("Trying to view non-existent media in a message (" + messageId + ")");
        }
    }


    /**
     * Internal method to get the image of a my media object
     *
     * @param content  - the media object to get
     * @param response the response to stream back to the FE
     * @throws ResourceNotFoundException
     * @throws UnsupportedEncodingException
     * @throws ResourceForbiddenException
     */
    private void getMyMediaImage(byte[] content, HttpServletResponse response)
            throws ResourceNotFoundException, UnsupportedEncodingException, ResourceForbiddenException {
        InputStream is = null;

        try {
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            is = new ByteArrayInputStream(content);

            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            LOGGER.error("Failed to my media image, exception: {}", e.getMessage(), e);
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
                                 HttpServletResponse response) throws UnsupportedEncodingException,
            ResourceForbiddenException, ResourceNotFoundException {
        getMyMediaVideo(myMediaService.createMyMediaDto(myMedia), response);
    }

    /**
     * Stream a video to the FE
     *
     * @param myMedia  - the media to stream
     * @param response - the response to stream back to
     * @throws ResourceNotFoundException
     * @throws UnsupportedEncodingException
     * @throws ResourceForbiddenException
     */
    private void getMyMediaVideo(org.patientview.api.model.MyMedia myMedia,
                                 HttpServletResponse response)
            throws ResourceNotFoundException, UnsupportedEncodingException, ResourceForbiddenException {
        InputStream is = null;

        try {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            // Set the content type and attachment header.
            String fileName = String.format("%d-%d", new Date().getTime(), ApiUtil.getCurrentUser().getId());
            String[] localPath = myMedia.getLocalPath().split("/");
            String fileExtension = localPath[localPath.length - 1].split("\\.")[1];

            response.addHeader("Content-disposition", String.format("attachment;filename=%s.%s", fileName,
                    fileExtension));

            is = new ByteArrayInputStream(myMedia.getContent());

            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            LOGGER.error("Failed to my media image, exception: {}", e.getMessage(), e);
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
