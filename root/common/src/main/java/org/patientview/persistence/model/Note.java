package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.persistence.model.enums.NoteTypes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.util.Date;

/**
 * Notes entity model
 */
@Entity
@Table(name = "pv_note")
public class Note extends AuditModel {

    @Column(name = "note_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NoteTypes noteType;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "body")
    private String body;

    @PrePersist
    public void prePersist() {
        setCreated(new Date());
        setLastUpdate(new Date());
    }

    @PreUpdate
    public void preUpdate() {
        setLastUpdate(new Date());
    }

    public NoteTypes getNoteType() {
        return noteType;
    }

    public void setNoteType(NoteTypes noteType) {
        this.noteType = noteType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
