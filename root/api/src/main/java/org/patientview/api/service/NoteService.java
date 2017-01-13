package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.model.Note;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.enums.NoteTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * NoteService service covers CRUD operation for Notes.
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface NoteService {

    /**
     * Add an Note for a User.
     *
     * @param userId ID of User adding Note
     * @param Note   a Note object to be created
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN,
            RoleName.SPECIALTY_ADMIN, RoleName.GLOBAL_ADMIN, RoleName.GP_ADMIN})
    void createNote(Long userId, Note Note)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a Note for a user.
     *
     * @param userId ID of User to retrieve
     * @param noteId an id of the Note to retrieve
     * @return a Note object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN,
            RoleName.SPECIALTY_ADMIN, RoleName.GLOBAL_ADMIN, RoleName.GP_ADMIN})
    Note getNote(Long userId, Long noteId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Update Note for a user.
     *
     * @param userId ID of User to update the Note  for
     * @param note   a Note containing updated properties
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN,
            RoleName.SPECIALTY_ADMIN, RoleName.GLOBAL_ADMIN, RoleName.GP_ADMIN})
    void updateNote(Long userId, Note note) throws ResourceNotFoundException, ResourceForbiddenException;


    /**
     * Remove a Note for a User.
     *
     * @param userId ID of User to remove Note from
     * @param noteId an ID of the Note to remove
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN,
            RoleName.SPECIALTY_ADMIN, RoleName.GLOBAL_ADMIN, RoleName.GP_ADMIN})
    void removeNote(Long userId, Long noteId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a list of Notes for a user.
     *
     * @param userId   ID of User to retrieve Note for
     * @param noteType Type of the Note, currently only NoteTypes.DONORVIEW exist
     * @return List of user's Notes
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = {RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN,
            RoleName.SPECIALTY_ADMIN, RoleName.GLOBAL_ADMIN, RoleName.GP_ADMIN})
    List<Note> getNotes(Long userId, NoteTypes noteType) throws ResourceNotFoundException;
}
