package org.patientview.persistence.repository;

import org.patientview.persistence.model.ConversationUserLabel;
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
 * Created on 19/02/2015
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ConversationUserLabelRepository extends JpaRepository<ConversationUserLabel, Long> {

    @Query("SELECT cul FROM ConversationUserLabel cul " +
            "WHERE cul.conversationUser.user = :user")
    List<ConversationUserLabel> findByUser(@Param("user") User user);

    @Query("SELECT cul FROM ConversationUserLabel cul " +
            "WHERE cul.creator = :user")
    List<ConversationUserLabel> findByCreator(@Param("user") User user);
}