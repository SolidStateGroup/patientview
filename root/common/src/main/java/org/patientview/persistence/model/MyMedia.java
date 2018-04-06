package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.patientview.persistence.model.enums.MediaTypes;

import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Properties;

@Entity
@Data
@Table(name = "pv_my_media")
public class MyMedia extends BaseModel {

    @Column(name = "media_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaTypes type;

    @Column(name = "file_description")
    private String fileDescription;

    @Column(name = "filename_ui")
    private String filenameUI;

    @Column(name = "local_path")
    private String localPath;

    @Column(name = "height")
    private int height;

    @Column(name = "filesize")
    private int filesize;

    @Column(name = "width")
    private int width;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "creation_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @OneToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User creator;

    @Column(name = "data")
    @JsonIgnore
    private byte[] content;

    @Column(name = "thumbnail_data")
    @JsonIgnore
    private byte[] thumbnailContent;

    //Used when Frontend sends content up
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String data;

    public String getThumbnail(){
        return String.format("/mymedia/%d/preview", getId());
    }


    public String getPath(){
        return String.format("/mymedia/%d/content", getId());
    }
}
