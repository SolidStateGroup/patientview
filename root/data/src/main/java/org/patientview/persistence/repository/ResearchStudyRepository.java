package org.patientview.persistence.repository;

import org.patientview.persistence.model.ResearchStudy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * This gets all the news associated with a user. News can be link via
 * group or role.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ResearchStudyRepository extends CrudRepository<ResearchStudy, Long> {


}
