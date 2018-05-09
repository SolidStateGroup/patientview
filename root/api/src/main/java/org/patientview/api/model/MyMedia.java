package org.patientview.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Setter;
import org.patientview.persistence.model.enums.MediaTypes;

import javax.persistence.Entity;
import java.util.Date;

@Entity
@Setter
public class MyMedia {

    private Long id;
    private MediaTypes type;
    private String fileDescription;
    private String filenameUI;
    private String localPath;
    private int height;
    private int filesize;
    private int width;
    private Boolean deleted;
    private Date created;
    private BaseUser creator;
    private String thumbnail;
    private String path;


    @JsonIgnore
    private byte[] content;
    @JsonIgnore
    private byte[] thumbnailContent;
    @JsonIgnore
    private String data;

    public MyMedia(org.patientview.persistence.model.MyMedia myMedia) {
        if (myMedia != null) {
            this.id = myMedia.getId();
            this.type = myMedia.getType();
            this.fileDescription = myMedia.getFileDescription();
            this.filenameUI = myMedia.getFilenameUI();
            this.localPath = myMedia.getLocalPath();
            this.height = myMedia.getHeight();
            this.filesize = myMedia.getFilesize();
            this.width = myMedia.getWidth();
            this.deleted = myMedia.getDeleted();
            this.created = myMedia.getCreated();
            if (myMedia.getCreator() != null) {
                this.creator = new BaseUser(myMedia.getCreator());
            }
            this.thumbnail = myMedia.getThumbnail();
            this.path = myMedia.getPath();
            this.content = myMedia.getContent();
            this.thumbnailContent = myMedia.getThumbnailContent();
            this.data = myMedia.getData();
        }
    }

    public Long getId() {
        return id;
    }

    public MediaTypes getType() {
        return type;
    }

    public byte[] getContent() {
        return content;
    }

    public byte[] getThumbnailContent() {
        return thumbnailContent;
    }

    public String getFileDescription() {
        return fileDescription;
    }

    public String getFilenameUI() {
        return filenameUI;
    }

    public String getLocalPath() {
        return localPath;
    }

    public int getHeight() {
        return height;
    }

    public int getFilesize() {
        return filesize;
    }

    public int getWidth() {
        return width;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public Date getCreated() {
        return created;
    }

    public BaseUser getCreator() {
        return creator;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getPath() {
        return path;
    }

    public void setCreator(BaseUser creator) {
        this.creator = creator;
    }
}