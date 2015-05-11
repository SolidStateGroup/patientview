package org.patientview.persistence.repository;

import org.patientview.persistence.model.FileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 01/05/2015
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface FileDataRepository extends JpaRepository<FileData, Long> {
}
