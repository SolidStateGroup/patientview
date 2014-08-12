package org.patientview.persistence.repository;

import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT DISTINCT n FROM NewsItem n JOIN n.newsLinks l JOIN l.group.groupRoles gr WHERE gr.user = :user")
    public Page<NewsItem> findGroupNewsByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT n FROM NewsItem n JOIN n.newsLinks l JOIN l.role.groupRoles gr WHERE gr.user = :user")
    public Page<NewsItem> findRoleNewsByUser(@Param("user") User user, Pageable pageable);

    //@Query("SELECT DISTINCT n FROM NewsItem n, NewsLink l, GroupRole gr WHERE l IN (n.newsLinks) AND gr.user = :user AND gr IN (l.group.groupRoles)")
    //@Query("SELECT DISTINCT n FROM NewsItem n, User u WHERE u = :user")
    //public Page<NewsItem> findGroupAndRoleNewsByUser(@Param("user") User user, Pageable pageable);
}
