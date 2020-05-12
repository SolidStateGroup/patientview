package org.patientview.persistence.repository;

import org.patientview.persistence.model.ApiKey;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ApiKeyTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/03/2016
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    @Query("SELECT   a " +
            "FROM    ApiKey a " +
            "WHERE   a.key = :key " +
            "AND     a.type = :type")
    List<ApiKey> findByKeyAndType(@Param("key") String key, @Param("type") ApiKeyTypes type);

    @Query("SELECT   a " +
            "FROM    ApiKey a " +
            "WHERE   a.key = :key " +
            "AND     a.type = :type " +
            "AND     a.user = :user")
    List<ApiKey> findByKeyAndTypeAndUser(@Param("key") String key, @Param("type") ApiKeyTypes type,
                                         @Param("user") User user);


    @Query("SELECT   a " +
            "FROM    ApiKey a " +
            "WHERE   a.user = :user")
    List<ApiKey> getAllKeysForUser(@Param("user") User user);

    @Query("SELECT   a " +
            "FROM    ApiKey a " +
            "WHERE   a.user = :user " +
            "AND     a.type = :type")
    ApiKey findOneByUserAndType(@Param("user") User user, @Param("type") ApiKeyTypes type);

    ApiKey findOneByKey(@Param("key") String key);

    @Modifying(clearAutomatically = true) // note: clearAutomatically required to flush changes straight away
    @Query("DELETE FROM ApiKey WHERE user.id = :userId")
    void deleteByUser(@Param("userId") Long userId);
}
