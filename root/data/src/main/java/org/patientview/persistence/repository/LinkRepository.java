package org.patientview.persistence.repository;

import org.patientview.persistence.model.Link;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface LinkRepository extends CrudRepository<Link, Long> {
}
