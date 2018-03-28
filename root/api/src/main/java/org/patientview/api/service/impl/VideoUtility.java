package org.patientview.api.service.impl;

import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.media.Buffer;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Time;
import javax.media.control.FrameGrabbingControl;
import javax.media.control.FramePositioningControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;


/**
 * @author Krishna Vangapandu
 **/


public class VideoUtility
{
    /** * videoFile - path to the video File. */
    public static Player getPlayer(String videoFile)
            throws NoPlayerException, IOException
    {
        File f = new File(videoFile);

        if (!f.exists()) throw new FileNotFoundException("File doesnt exist");
        MediaLocator ml = new MediaLocator(f.toURL());
        Player player = Manager.createPlayer(ml);
        player.realize();
        while (player.getState() != Player.Realized) ;
        return player;
    }


    public static float getFrameRate(Player player) {
        return (float) noOfFrames(player) / (float) player.getDuration().getSeconds();
    }

    public static int noOfFrames(Player player) {
        FramePositioningControl fpc = (FramePositioningControl)
                player.getControl("javax.media.control.FramePositioningControl");
        Time duration = player.getDuration();

        int i = fpc.mapTimeToFrame(duration);

        if (i != FramePositioningControl.FRAME_UNKNOWN) return i;

        else return -1;

    }


    /**
     * * @param player - the player from which you want to get the image
     *
     * @param frameNumber - the framenumber you want to extract
     * @return Image at the current frame position
     */


    public static Image getImageOfCurrentFrame(Player player, int frameNumber) {
        FramePositioningControl fpc =
                (FramePositioningControl) player.getControl("javax.media.control.FramePositioningControl");

        FrameGrabbingControl fgc =
                (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");

        return getImageOfCurrentFrame(fpc, fgc, frameNumber);
    }


    public static Image getImageOfCurrentFrame(FramePositioningControl fpc,
                                               FrameGrabbingControl fgc,
                                               int frameNumber) {
        fpc.seek(frameNumber);
        Buffer frameBuffer = fgc.grabFrame();
        BufferToImage bti = new BufferToImage((VideoFormat) frameBuffer.getFormat());
        return bti.createImage(frameBuffer);
    }


    public static FramePositioningControl getFPC(Player player) {
        FramePositioningControl fpc =
                (FramePositioningControl) player.getControl("javax.media.control.FramePositioningControl");
        return fpc;
    }


    public static FrameGrabbingControl getFGC(Player player) {
        FrameGrabbingControl fgc = (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");
        return fgc;
    }


    public static ArrayList<Image> getAllImages(Player player) {
        ArrayList<Image> imageSeq = new ArrayList<Image>();
        int numberOfFrames = noOfFrames(player);
        FramePositioningControl fpc = getFPC(player);
        FrameGrabbingControl fgc = getFGC(player);
        for (int i = 0; i <= numberOfFrames; i++) {
            Image img = getImageOfCurrentFrame(fpc, fgc, i);
            if (img != null) imageSeq.add(img);
        }
        return imageSeq;
    }


    public static ArrayList<Image> getAllImages(String fileName) throws NoPlayerException, IOException {
        Player player = getPlayer(fileName);
        ArrayList<Image> img = getAllImages(player);
        player.close();
        return img;
    }
}