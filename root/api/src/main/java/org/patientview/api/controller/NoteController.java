package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.Note;
import org.patientview.api.service.NoteService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.enums.NoteTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * RESTful interface for CRUD operation for Note.
 */
@RestController
@ExcludeFromApiDoc
public class NoteController extends BaseController<NoteController> {

    @Inject
    private NoteService noteService;

    /**
     * Creates new Note for a User.
     *
     * @param userId an ID of User adding Note
     * @param note   a Note to be added for a user
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/notes", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Note> createNote(@PathVariable("userId") Long userId, @RequestBody Note note)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(noteService.createNote(userId, note), HttpStatus.OK);
    }

    /**
     * Get a Note for a user.
     *
     * @param userId an ID of User to retrieve
     * @param noteId an id of the Note to retrieve
     * @return a Note object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/notes/{noteId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Note> getNote(@PathVariable("userId") Long userId, @PathVariable("noteId") Long noteId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(noteService.getNote(userId, noteId), HttpStatus.OK);
    }

    /**
     * Update Note for a user.
     *
     * @param userId an ID of User to update the Note  for
     * @param note   a Note containing updated properties
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/notes", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateNote(@PathVariable("userId") Long userId, @RequestBody Note note)
            throws ResourceNotFoundException, ResourceForbiddenException {
        noteService.updateNote(userId, note);
    }

    /**
     * Remove a Note for a User.
     *
     * @param userId an ID of User to remove Note from
     * @param noteId an ID of the Note to remove
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/notes/{noteId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteNote(@PathVariable("userId") Long userId, @PathVariable("noteId") Long noteId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        noteService.removeNote(userId, noteId);
    }

    /**
     * Get a User's Notes, given the NoteTypes type of Note.
     *
     * @param userId   an ID of User to retrieve Note for
     * @param noteType a Type of the Note, currently only NoteTypes.DONORVIEW exist
     * @return A List of Note of type NoteTypes
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/notes/{noteType}/type", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Note>> getNotes(
            @PathVariable("userId") Long userId, @PathVariable("noteType") NoteTypes noteType)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(noteService.getNotes(userId, noteType), HttpStatus.OK);
    }
}
