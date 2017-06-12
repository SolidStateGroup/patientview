package org.patientview.persistence.repository;

import org.patientview.persistence.model.Note;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.NoteTypes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Note JPA repository
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface NoteRepository extends CrudRepository<Note, Long> {

    List<Note> findByUser(@Param("user") User user);

    @Query("SELECT  n FROM Note n " +
            "WHERE  n.user = :user " +
            "AND    n.noteType = :noteType " +
            "ORDER BY n.created DESC")
    List<Note> findByUserAndNoteType(@Param("user") User user, @Param("noteType") NoteTypes noteType);
}