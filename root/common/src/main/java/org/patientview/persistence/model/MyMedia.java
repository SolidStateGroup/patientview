package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.patientview.persistence.model.enums.MediaTypes;

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

@Entity
@Getter
@Setter
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

    @Column(name = "path")
    private String path;

    @Column(name = "height")
    private int height;

    @Column(name = "filesize")
    private int filesize;

    @Column(name = "width")
    private int width;

    @Column(name = "thumbnail")
    private String thumbnail;

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

    //Used when Frontend sends content up
    @Transient
    private String data;

}
