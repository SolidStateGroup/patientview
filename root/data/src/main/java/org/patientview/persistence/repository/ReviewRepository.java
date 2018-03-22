package org.patientview.persistence.repository;

import org.patientview.persistence.model.Review;
import org.patientview.persistence.model.enums.ReviewSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Internal repository of all facebook reviews
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ReviewRepository  extends JpaRepository<Review, Long> {

    @Query("DELETE FROM Review r WHERE r.reviewSource = :source")
    void deleteByType(ReviewSource source);

    Review getByExternalId(String externalId);

}
