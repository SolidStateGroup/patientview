package org.patientview.api.service.impl;

import org.patientview.api.service.NoteService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Note;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.NoteTypes;
import org.patientview.persistence.repository.NoteRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Implementation of the NoteService
 */
@Service
public class NoteServiceImpl extends AbstractServiceImpl<NoteServiceImpl> implements NoteService {

    @Inject
    private NoteRepository noteRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public void createNote(Long userId, org.patientview.api.model.Note note)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User currentUser = getCurrentUser();
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        if (note.getNoteType() == null) {
            throw new ResourceNotFoundException("Missing note type");
        }

        Note newNote = new Note();
        newNote.setUser(user);
        newNote.setBody(note.getBody());
        newNote.setNoteType(note.getNoteType());
        newNote.setCreator(currentUser);

        noteRepository.save(newNote);
    }

    public org.patientview.api.model.Note getNote(Long userId, Long noteId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Note note = noteRepository.findOne(noteId);
        if (note == null) {
            throw new ResourceNotFoundException("Could not find note");
        }

        // extra check to make sure belongs to correct user
        if (!user.getId().equals(note.getUser().getId())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return new org.patientview.api.model.Note(note);
    }

    @Override
    public void updateNote(Long userId, org.patientview.api.model.Note note)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Note noteEntity = noteRepository.findOne(note.getId());
        if (noteEntity == null) {
            throw new ResourceNotFoundException("Could not find Note");
        }

        if (!user.getId().equals(noteEntity.getUser().getId())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        noteEntity.setBody(note.getBody());
        noteEntity.setLastUpdater(getCurrentUser());

        noteRepository.save(noteEntity);
    }

    @Override
    public void removeNote(Long userId, Long noteId) throws ResourceNotFoundException, ResourceForbiddenException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Note noteEntity = noteRepository.findOne(noteId);
        if (noteEntity == null) {
            throw new ResourceNotFoundException("Could not find Note");
        }

        if (!user.getId().equals(noteEntity.getUser().getId())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        noteRepository.delete(noteEntity);
    }

    @Override
    public List<org.patientview.api.model.Note> getNotes(Long userId, NoteTypes noteType)
            throws ResourceNotFoundException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<Note> notes = noteRepository.findByUserAndNoteType(user, noteType);
        List<org.patientview.api.model.Note> notesList = new ArrayList<>();

        for (Note note : notes) {
            notesList.add(new org.patientview.api.model.Note(note));
        }

        return notesList;
    }

}
