package org.patientview.api.model;

import org.patientview.persistence.model.enums.NoteTypes;

import java.io.Serializable;
import java.util.Date;

/**
 * Note represent a note taken by admin for patient throughout application.
 */
public class Note implements Serializable {

    private Long id;
    private NoteTypes noteType;
    private String body;
    private BaseUser creator;
    private Date created;
    private BaseUser lastUpdater;
    private Date lastUpdate;

    public Note() {
    }

    public Note(org.patientview.persistence.model.Note note) {
        this.setId(note.getId());
        this.noteType = note.getNoteType();
        this.body = note.getBody();
        this.lastUpdate = note.getLastUpdate();
        this.created = note.getCreated();
        if (note.getCreator() != null) {
            setCreator(new BaseUser(note.getCreator()));
        }
        if (note.getLastUpdater() != null) {
            setLastUpdater(new BaseUser(note.getLastUpdater()));
        }
        this.lastUpdate = note.getLastUpdate();
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

    public BaseUser getLastUpdater() {
        return lastUpdater;
    }

    public void setLastUpdater(BaseUser lastUpdater) {
        this.lastUpdater = lastUpdater;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}