package org.patientview.persistence.repository;

import org.patientview.persistence.model.UserToken;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface UserTokenRepository  extends CrudRepository<UserToken, Long> {

    public UserToken findByToken(String token);

    @Query("DELETE FROM UserToken u WHERE u.token = :token")
    public void deleteByToken(@Param("token") String token);
}
