package org.patientview.api.service.impl;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Picture;
import org.im4java.core.IM4JavaException;
import org.patientview.api.service.MyMediaService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.MediaTypes;
import org.patientview.persistence.repository.MyMediaRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Date;
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
            IOException, IM4JavaException, InterruptedException {
        User currentUser = userRepository.findOne(userId);
        myMedia.setCreator(currentUser);

        if (myMedia.getData() != null) {
            byte[] decodedString = Base64.decodeBase64(new String(myMedia.getData()).getBytes("UTF-8"));
            myMedia.setContent(decodedString);

            if (myMedia.getType().equals(MediaTypes.IMAGE)) {
                myMedia.setThumbnailContent(this.getPreviewImage(myMedia, 200));
            } else if (myMedia.getType().equals(MediaTypes.VIDEO)) {
                transcodeVideo(myMedia);
            }
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
    public void delete(Long myMediaId) throws ResourceNotFoundException, ResourceForbiddenException,
            UnsupportedEncodingException {
        myMediaRepository.delete(myMediaId);
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
    public byte[] getPreviewImage(MyMedia myMedia, int height) throws IOException, IM4JavaException,
            InterruptedException {
        // convert byte array back to BufferedImage
        InputStream in = new ByteArrayInputStream(myMedia.getContent());
        BufferedImage image = ImageIO.read(in);
        Double newWidth = image.getWidth() / (image.getHeight() / height * 1.00);

        final int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
        final BufferedImage scaledImg = new BufferedImage(newWidth.intValue(), height, type);
        final Graphics2D g = scaledImg.createGraphics();
        g.drawImage(image, 0, 0, height, newWidth.intValue(), null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(scaledImg, "jpg", baos);
        return baos.toByteArray();
    }


    private byte[] getVideoThumbnail(){
        int frameNumber = 42;
        Picture picture = FrameGrab.getFrameFromFile(new File("video.mp4"), frameNumber);

        //for JDK (jcodec-javase)
        BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
        ImageIO.write(bufferedImage, "png", new File("frame42.png"));
    }

    private byte[] transcodeVideo(MyMedia myMedia) throws IOException {
        String[] localPath = myMedia.getLocalPath().split("/");
        String fileExtension = localPath[localPath.length - 1].split("\\.")[1];

        String inputFileName = String.format("%d-%d", new Date().getTime(), ApiUtil.getCurrentUser().getId());
        File temp = File.createTempFile(inputFileName, "." + fileExtension);
        File outputTemp = File.createTempFile(inputFileName, ".mp4");

        FileUtils.writeByteArrayToFile(temp, myMedia.getContent());
        Long st = System.currentTimeMillis();

        // create a media reader
        IMediaReader mediaReader = ToolFactory.makeReader(temp.getPath());

        // create a media writer
        IMediaWriter mediaWriter = ToolFactory.makeWriter(outputTemp.getPath(), mediaReader);

        // add a writer to the reader, to create the output file
        mediaReader.addListener(mediaWriter);

        // create a media viewer with stats enabled
        IMediaViewer mediaViewer = ToolFactory.makeViewer(true);

        // add a viewer to the reader, to see the decoded media
        mediaReader.addListener(mediaViewer);

        // read and decode packets from the source file and
        // and dispatch decoded audio and video to the writer
        while (mediaReader.readPacket() == null) ;

        Long end = System.currentTimeMillis();
        System.out.println("Time Taken In Milli Seconds: " + (end - st));
        byte[] toReturn = Files.readAllBytes(outputTemp.toPath());

        temp.delete();
        outputTemp.delete();

        return toReturn;
    }
}
