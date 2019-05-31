package org.patientview.persistence.repository;

import org.patientview.persistence.model.Message;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/08/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m " +
            "WHERE m.user = :user OR m.creator = :user")
    List<Message> findByUserOrCreator(@Param("user") User user);
}