package org.patientview.api.service.impl;

import org.patientview.api.service.MyMediaService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.MyMediaRepository;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Class to control the crud operations of the News.
 * <p>
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Service
public class MyMediaServiceImpl extends AbstractServiceImpl<MyMediaServiceImpl> implements MyMediaService {

    @Autowired
    private MyMediaRepository myMediaRepository;


    @Override
    public MyMedia save(MyMedia myMedia) throws ResourceNotFoundException, ResourceForbiddenException, UnsupportedEncodingException {
        User currentUser = getCurrentUser();
        myMedia.setCreator(currentUser);

        if (myMedia.getData() != null) {
            byte[] decodedString = Base64.decodeBase64(new String(myMedia.getData()).getBytes("UTF-8"));
            myMedia.setContent(decodedString);
        }

        return myMediaRepository.save(myMedia);
    }

    @Override
    public MyMedia get(long id) throws ResourceNotFoundException, ResourceForbiddenException, UnsupportedEncodingException {
        return myMediaRepository.findOne(id);
    }

    @Override
    public void delete(MyMedia myMedia) throws ResourceNotFoundException, ResourceForbiddenException, UnsupportedEncodingException {
        myMediaRepository.delete(myMedia);
    }

    @Override
    public List<MyMedia> getAllForUser(User user) throws ResourceNotFoundException, ResourceForbiddenException, UnsupportedEncodingException {
        return myMediaRepository.getByCreator(user);
    }
}
