package org.patientview.persistence.repository;

import org.patientview.persistence.model.ExternalServiceTaskQueueItem;
import org.patientview.persistence.model.enums.ExternalServiceTaskQueueStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 30/04/2015
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ExternalServiceTaskQueueItemRepository extends CrudRepository<ExternalServiceTaskQueueItem, Long> {

    public List<ExternalServiceTaskQueueItem> findAll();

    @Query("Select i FROM ExternalServiceTaskQueueItem i WHERE status IN :statuses")
    List<ExternalServiceTaskQueueItem> findByStatus(@Param("statuses") List<ExternalServiceTaskQueueStatus> statuses);
}