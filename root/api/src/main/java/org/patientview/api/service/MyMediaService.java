package org.patientview.api.service;

import org.im4java.core.IM4JavaException;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface MyMediaService {

    @UserOnly
    MyMedia save(Long userId, MyMedia myMedia) throws ResourceNotFoundException, ResourceForbiddenException,
            IOException, IM4JavaException, InterruptedException;

    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.PATIENT})
    MyMedia get(long id) throws ResourceNotFoundException, ResourceForbiddenException,
            UnsupportedEncodingException;

    @UserOnly
    void delete(Long myMediaId) throws ResourceNotFoundException, ResourceForbiddenException,
            UnsupportedEncodingException;


    @UserOnly
    Page<List<MyMedia>> getAllForUser(Long userId, GetParameters getParameters) throws ResourceNotFoundException,
            ResourceForbiddenException, UnsupportedEncodingException;


    /**
     * Resize an image
     *
     * @param myMedia my media object
     * @param height  height to use
     * @return byte array of the image
     */
    byte[] getPreviewImage(MyMedia myMedia, int height) throws IOException, IM4JavaException,
            InterruptedException;
}
