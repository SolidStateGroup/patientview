package org.patientview.persistence.repository;

import org.patientview.persistence.model.ConversationUserLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 19/02/2015
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ConversationUserLabelRepository extends JpaRepository<ConversationUserLabel, Long> {
}