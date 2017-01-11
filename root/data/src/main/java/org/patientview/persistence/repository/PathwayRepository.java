package org.patientview.persistence.repository;

import org.patientview.persistence.model.Pathway;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pathway JPA repository
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface PathwayRepository extends CrudRepository<Pathway, Long> {

    List<Pathway> findByUser(@Param("user") User user);

    /**
     * Finds Pathway for user by pathway type.
     * <p/>
     * Should return only one per type
     *
     * @param user        a patient user
     * @param pathwayType a type of the pathway
     * @return a Pathway or null if nothing found
     */
    Pathway findByUserAndPathwayType(@Param("user") User user, @Param("pathwayType") PathwayTypes pathwayType);

}
