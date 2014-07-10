package org.patientview.persistence.repository;

import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * This gets all the news associated with a user. News can be link via
 * group or role.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface NewsItemRepository extends CrudRepository<NewsItem, Long> {

    @Query("SELECT  n FROM NewsItem n JOIN n.newsLinks l JOIN l.group.groupRoles gr WHERE gr.user = :user")
    public Iterable<NewsItem> findGroupNewsByUser(@Param("user") User user);

    @Query("SELECT  n FROM NewsItem n JOIN n.newsLinks l JOIN l.role.groupRoles gr WHERE gr.user = :user")
    public Iterable<NewsItem> findRoleNewsByUser(@Param("user") User user);

}
