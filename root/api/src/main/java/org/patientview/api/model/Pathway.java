package org.patientview.api.model;

import org.patientview.persistence.model.enums.NoteTypes;

import java.io.Serializable;
import java.util.Date;

/**
 * Note represent a note taken by admin for patient throughout application.
 */
public class Pathway implements Serializable {

    private Long id;
    private NoteTypes noteType;
    private String body;
    private Date lastUpdate;
    private Date created;
    private BaseUser creator;

    public Pathway() {
    }

    public Pathway(org.patientview.persistence.model.Pathway pathway) {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NoteTypes getNoteType() {
        return noteType;
    }

    public void setNoteType(NoteTypes noteType) {
        this.noteType = noteType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public BaseUser getCreator() {
        return creator;
    }

    public void setCreator(BaseUser creator) {
        this.creator = creator;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
