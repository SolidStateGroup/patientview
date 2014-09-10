package org.patientview.persistence.repository;

import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.UserInformationTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface UserInformationRepository extends JpaRepository<UserInformation, Long> {

    @Query("SELECT   ui " +
            "FROM    UserInformation ui " +
            "JOIN    ui.user u " +
            "WHERE   u = :user " +
            "AND     ui.type = :informationType ")
    public UserInformation findByUserAndType(@Param("user") User user,
                                             @Param("informationType") UserInformationTypes informationType);

    @Query("SELECT   ui " +
            "FROM    UserInformation ui " +
            "JOIN    ui.user u " +
            "WHERE   u = :user ")
    public List<UserInformation> findByUser(@Param("user") User user);
}
