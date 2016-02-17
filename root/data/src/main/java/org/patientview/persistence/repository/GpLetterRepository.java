package org.patientview.persistence.repository;

import org.patientview.persistence.model.GpLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/02/2016
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface GpLetterRepository extends JpaRepository<GpLetter, Long> {

    @Query("SELECT   gl " +
            "FROM    GpLetter gl " +
            "WHERE   gl.claimedPracticeCode = :claimedPracticeCode ")
    List<GpLetter> findByClaimedPracticeCode(@Param("claimedPracticeCode") String claimedPracticeCode);

    @Query("SELECT   gl " +
            "FROM    GpLetter gl " +
            "WHERE   gl.gpPostcode = :gpPostcode ")
    List<GpLetter> findByPostcode(@Param("gpPostcode") String gpPostcode);

    @Query("SELECT   gl " +
            "FROM    GpLetter gl " +
            "WHERE   gl.gpPostcode = :gpPostcode " +
            "AND     gl.gpName = :gpName")
    List<GpLetter> findByPostcodeAndName(@Param("gpPostcode") String gpPostcode, @Param("gpName") String gpName);

    @Query("SELECT   gl " +
            "FROM    GpLetter gl " +
            "WHERE   gl.signupKey = :signupKey " +
            "AND     gl.patientIdentifier = :patientIdentifier")
    List<GpLetter> findBySignupKeyAndIdentifier(
            @Param("signupKey") String signupKey, @Param("patientIdentifier") String patientIdentifier);
}
