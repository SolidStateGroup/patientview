package org.patientview.persistence.repository;

import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.model.enums.UserTokenTypes;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface UserTokenRepository extends CrudRepository<UserToken, Long> {

    UserToken findBySecretWordToken(String secretWordToken);

    UserToken findByToken(String token);

    @Query("SELECT t FROM UserToken t WHERE user.id = :userId")
    List<UserToken> findByUser(@Param("userId") Long userId);

    @Query("SELECT t FROM UserToken t WHERE user.id = :userId AND t.expiration > CURRENT_TIMESTAMP")
    List<UserToken> findActiveByUser(@Param("userId") Long userId);

    @Query("SELECT expiration FROM UserToken WHERE token = :token")
    Date getExpiration(@Param("token") String token);

    @Modifying(clearAutomatically = true) // note: clearAutomatically required to flush changes straight away
    @Query("UPDATE UserToken SET expiration = :date WHERE token = :token")
    void setExpiration(@Param("token") String token, @Param("date") Date date);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserToken WHERE user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserToken t WHERE user.id = :userId AND t.expiration < CURRENT_TIMESTAMP")
    void deleteExpiredByUserId(@Param("userId") Long userId);

    @Query("SELECT type FROM UserToken WHERE token = :token")
    UserTokenTypes getType(@Param("token") String token);
}
