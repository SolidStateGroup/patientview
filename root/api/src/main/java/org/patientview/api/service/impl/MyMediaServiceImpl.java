package org.patientview.api.service.impl;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.MyMediaService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.MyMediaRepository;
import org.apache.commons.codec.binary.Base64;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

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


    @Autowired
    private UserRepository userRepository;


    @Override
    public MyMedia save(Long userId, MyMedia myMedia) throws ResourceNotFoundException, ResourceForbiddenException,
            IOException {
        User currentUser = userRepository.findOne(userId);
        myMedia.setCreator(currentUser);

        if (myMedia.getData() != null) {
            byte[] decodedString = Base64.decodeBase64(new String(myMedia.getData()).getBytes("UTF-8"));
            myMedia.setContent(decodedString);

            //TODO need to create the video thumbnail here
        }

        return myMediaRepository.save(myMedia);
    }

    @Override
    public MyMedia get(long id) throws ResourceNotFoundException, ResourceForbiddenException,
            UnsupportedEncodingException {
        return myMediaRepository.findOne(id);
    }

    @Override
    public void delete(MyMedia myMedia) throws ResourceNotFoundException, ResourceForbiddenException,
            UnsupportedEncodingException {
        myMediaRepository.delete(myMedia);
    }

    @Override
    public Page<List<MyMedia>> getAllForUser(Long userId, GetParameters getParameters) throws
            ResourceNotFoundException, ResourceForbiddenException, UnsupportedEncodingException {
        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        PageRequest pageable = createPageRequest(pageConverted, sizeConverted, sortField, sortDirection);

        return myMediaRepository.getByCreator(userRepository.findOne(userId), pageable);
    }

    @Override
    public byte[] getPreviewImage(MyMedia myMedia, int height, int width) throws IOException {
        // convert byte array back to BufferedImage
        InputStream in = new ByteArrayInputStream(myMedia.getContent());
        BufferedImage bImageFromConvert = ImageIO.read(in);

        Image tmp = bImageFromConvert.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", baos);
        return baos.toByteArray();
    }
}
