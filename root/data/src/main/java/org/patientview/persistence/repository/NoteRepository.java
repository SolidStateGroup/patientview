package org.patientview.persistence.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Note JPA repository
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface NoteRepository extends CrudRepository<Note, Long> {

    List<Note> findByUser(@Param("user") User user);

    @Query("SELECT   n " +
            "FROM    Note n " +
            "JOIN    n.user u " +
            "WHERE   u = :user AND noteType:noteType")
    List<Note> findByUserAndNoteType(@Param("user") User user, @Param("noteType") NoteTypes noteType);
}
