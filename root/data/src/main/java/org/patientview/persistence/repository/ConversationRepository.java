package org.patientview.persistence.repository;

import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
            "AND     cu.user = :user " +
            "ORDER BY c.lastUpdate DESC")
    Page<Conversation> findByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT   c " +
            "FROM    Conversation c " +
            "JOIN    c.conversationUsers cu " +
            "WHERE   cu.conversation = c " +
            "AND     cu.creator = :user " +
            "ORDER BY c.lastUpdate DESC ")
    Page<Conversation> findByCreator(@Param("user") User user, Pageable pageable);

    @Query("SELECT   count(c.id) " +
            "FROM    Conversation c " +
            "JOIN    c.messages m " +
            "LEFT JOIN    m.readReceipts r " +
            "JOIN    c.conversationUsers cu " +
            "JOIN    cu.conversationUserLabels cul " +
            "WHERE   cu.user.id = :userId " +
            "AND     cul.conversationLabel = 'INBOX' " +
            "AND cu.user.id " +
            "NOT IN (SELECT r1.user.id FROM MessageReadReceipt r1 JOIN r1.message m1 WHERE m1.id = m.id)")
    Long getUnreadConversationCount(@Param("userId") Long userId);
}
