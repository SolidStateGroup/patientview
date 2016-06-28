package org.patientview.persistence.repository;

import org.patientview.persistence.model.CodeExternalStandard;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/06/2016
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface CodeExternalStandardRepository extends CrudRepository<CodeExternalStandard, Long> {

}
