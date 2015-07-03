package org.patientview.persistence.repository;

import org.patientview.persistence.model.QuestionOption;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 10/06/2015
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface QuestionOptionRepository extends CrudRepository<QuestionOption, Long> {

}
