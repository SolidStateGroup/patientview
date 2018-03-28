package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface MyMediaService {

    //@RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.PATIENT })
    MyMedia save(MyMedia myMedia) throws ResourceNotFoundException, ResourceForbiddenException,
            UnsupportedEncodingException;

    MyMedia get(long id) throws ResourceNotFoundException, ResourceForbiddenException,
            UnsupportedEncodingException;

    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.PATIENT })
    void delete(MyMedia myMedia) throws ResourceNotFoundException, ResourceForbiddenException,
            UnsupportedEncodingException;


    //@RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.PATIENT })
    List<MyMedia> getAllForUser(User user) throws ResourceNotFoundException, ResourceForbiddenException,
            UnsupportedEncodingException;
}
