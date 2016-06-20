package org.patientview.persistence.repository;

import org.patientview.persistence.model.Category;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.CodeCategory;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/06/2016
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface CodeCategoryRepository extends CrudRepository<CodeCategory, Long> {

    @Modifying
    @Query("DELETE FROM CodeCategory cc WHERE cc.code = :code AND cc.category = :category")
    void deleteByCodeAndCategory (@Param("code") Code code, @Param("category") Category category);
}
