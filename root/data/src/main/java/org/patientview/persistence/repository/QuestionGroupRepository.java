package org.patientview.persistence.repository;

import org.patientview.persistence.model.QuestionGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface QuestionGroupRepository extends CrudRepository<QuestionGroup, Long> {

}
