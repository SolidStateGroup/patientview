package org.patientview.persistence.repository;

import org.patientview.persistence.model.Feature;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface FeatureRepository extends CrudRepository<Feature, Long> {
}
