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

    @Query("SELECT DISTINCT n FROM NewsItem n JOIN n.newsLinks l JOIN l.group.groupRoles gr WHERE gr.user = :user AND l.role IS NULL AND n.newsType = :newsType")
    public Page<NewsItem> findGroupNewsByUserAndType(@Param("user") User user, @Param("newsType") int newsType,
                                                     Pageable pageable);

    @Query("SELECT DISTINCT n FROM NewsItem n JOIN n.newsLinks l JOIN l.role.groupRoles gr WHERE gr.user = :user AND l.group IS NULL AND n.newsType = :newsType")
    public Page<NewsItem> findRoleNewsByUserAndType(@Param("user") User user, @Param("newsType") int newsType
            , Pageable pageable);

    @Query("SELECT DISTINCT n FROM NewsItem n JOIN n.newsLinks l JOIN l.group.groupRoles ggr JOIN l.role.groupRoles rgr WHERE ggr.user = :user AND rgr.user = :user AND n.newsType = :newsType")
    public Page<NewsItem> findGroupRoleNewsByUserAndType(@Param("user") User user, @Param("newsType") int newsType,
                                                         Pageable pageable);

    @Query("SELECT DISTINCT n FROM NewsItem n " +
            "JOIN n.newsLinks l " +
            "JOIN l.group g " +
            "JOIN g.groupRelationships grl " +
            "JOIN grl.objectGroup pg " +
            "JOIN pg.groupRoles pgr " +
            "WHERE pgr.user = :user AND grl.relationshipType = 'PARENT' " +
            "AND n.newsType = :newsType")
    public Page<NewsItem> findSpecialtyNewsByUserAndType(@Param("user") User user, @Param("newsType") int newsType, Pageable pageable);


    @Query("SELECT DISTINCT n FROM NewsItem n JOIN n.newsLinks l JOIN l.group.groupRoles gr WHERE gr.user = :user AND l.role IS NULL")
    public Page<NewsItem> findGroupNewsByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT n FROM NewsItem n JOIN n.newsLinks l JOIN l.role.groupRoles gr WHERE gr.user = :user AND l.group IS NULL")
    public Page<NewsItem> findRoleNewsByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT n FROM NewsItem n JOIN n.newsLinks l JOIN l.group.groupRoles ggr JOIN l.role.groupRoles rgr WHERE ggr.user = :user AND rgr.user = :user")
    public Page<NewsItem> findGroupRoleNewsByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT n FROM NewsItem n " +
            "JOIN n.newsLinks l " +
            "JOIN l.group g " +
            "JOIN g.groupRelationships grl " +
            "JOIN grl.objectGroup pg " +
            "JOIN pg.groupRoles pgr " +
            "WHERE pgr.user = :user AND grl.relationshipType = 'PARENT'")
    public Page<NewsItem> findSpecialtyNewsByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT n FROM NewsItem n " +
            "JOIN n.newsLinks l " +
            "JOIN l.role r " +
            "WHERE r.name = org.patientview.persistence.model.enums.RoleName.PUBLIC ")
    Page<NewsItem> getPublicNews(Pageable pageable);
}
