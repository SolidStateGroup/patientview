package org.patientview.persistence.repository;

import org.patientview.persistence.model.Review;
import org.patientview.persistence.model.enums.ReviewSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Internal repository of all facebook reviews
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ReviewRepository  extends JpaRepository<Review, Long> {

    List<Review> findByReviewSource(ReviewSource reviewSource);

    Review getByExternalId(String externalId);

    @Query("SELECT r " +
            "FROM   Review r " +
            "WHERE  r.excluded = :excluded ")
    List<Review> findByExcluded(@Param("excluded") Boolean excluded);
}
