package org.patientview.persistence.repository;

import org.patientview.persistence.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT c FROM Category c " +
            "WHERE (UPPER(c.friendlyDescription) LIKE :filterText) " +
            "OR (UPPER(c.icd10Description) LIKE :filterText) " +
            "OR (CAST(c.number AS string) LIKE :filterText)")
    Page<Category> findAllFiltered(@Param("filterText") String filterText, Pageable pageable);

    @Query("Select c from Category c WHERE c.number = :number")
    List<Category> findByNumber(@Param("number") Integer number);

    @Query("Select c from Category c WHERE c.hidden IS false")
    List<Category> findVisible();
}
