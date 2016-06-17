package org.patientview.persistence.repository;

import org.patientview.persistence.model.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/06/2016
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface CategoryRepository extends CrudRepository<Category, Long> {

    @Query("Select c from Category c WHERE c.hidden IS false")
    List<Category> findVisible();
}
