package org.patientview.persistence.repository;

import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.Lookup;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 08/07/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface GroupRelationshipRepository extends CrudRepository<GroupRelationship, Long> {

    @Modifying
    @Query("DELETE FROM GroupRelationship gr WHERE gr.sourceGroup = :group")
    public void deleteBySourceGroup(@Param("group") Group sourceGroup);

    @Modifying
    @Query("DELETE FROM GroupRelationship gr " +
           "WHERE gr.sourceGroup = :sourceGroup " +
           "AND gr.objectGroup = :objectGroup " +
           "AND gr.lookup = :relationshipType ")
    public void deleteBySourceObjectRelationshipType(
            @Param("sourceGroup") Group sourceGroup,
            @Param("objectGroup") Group objectGroup,
            @Param("relationshipType") Lookup relationshipType);
}
