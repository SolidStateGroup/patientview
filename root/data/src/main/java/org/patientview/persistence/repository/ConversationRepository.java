package org.patientview.persistence.repository;

import org.patientview.persistence.model.Conversation;
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
 * Created on 10/07/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT   c " +
            "FROM    Conversation c " +
            "JOIN    c.conversationUsers cu " +
            "WHERE   cu.conversation = c " +
            "AND   cu.user = :user")
    List<Conversation> findByUser(@Param("user") User user);
}