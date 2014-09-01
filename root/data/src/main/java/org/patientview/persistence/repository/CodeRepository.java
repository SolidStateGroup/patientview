package org.patientview.persistence.repository;

import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Lookup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface CodeRepository extends CrudRepository<Code, Long> {

    public Page<Code> findAll(Pageable pageable);

    // for reference only
    /*@Query("SELECT c FROM Code c " +
            "WHERE ((:filterText IS NULL OR UPPER(c.code) LIKE :filterText) " +
            "OR (:filterText IS NULL OR UPPER(c.description) LIKE :filterText) " +
            "OR (:filterText IS NULL OR UPPER(c.codeType.value) LIKE :filterText) " +
            "OR (:filterText IS NULL OR UPPER(c.standardType.value) LIKE :filterText)) " +
            "AND (:codeTypes IS NULL OR (c.codeType.id IN (:codeTypes))) " +
            "AND (:standardTypes IS NULL OR (c.standardType.id IN (:standardTypes)))")
    public Page<Code> findAllFiltered(
            @Param("codeTypes") List<Long> codeTypes,
            @Param("standardTypes") List<Long> standardTypes,
            @Param("filterText") String filterText,
            Pageable pageable);*/

    @Query("SELECT c FROM Code c " +
            "WHERE (UPPER(c.code) LIKE :filterText) " +
            "OR (UPPER(c.description) LIKE :filterText) " +
            "OR (UPPER(c.codeType.value) LIKE :filterText) " +
            "OR (UPPER(c.standardType.value) LIKE :filterText)")
    public Page<Code> findAllFiltered(
            @Param("filterText") String filterText,
            Pageable pageable);

    @Query("SELECT c FROM Code c " +
            "WHERE ((UPPER(c.code) LIKE :filterText) " +
            "OR (UPPER(c.description) LIKE :filterText) " +
            "OR (UPPER(c.codeType.value) LIKE :filterText) " +
            "OR (UPPER(c.standardType.value) LIKE :filterText)) " +
            "AND (c.codeType.id IN (:codeTypes))")
    public Page<Code> findAllByCodeTypesFiltered(
            @Param("filterText") String filterText,
            @Param("codeTypes") List<Long> codeTypes,
            Pageable pageable);

    @Query("SELECT c FROM Code c " +
            "WHERE ((UPPER(c.code) LIKE :filterText) " +
            "OR (UPPER(c.description) LIKE :filterText) " +
            "OR (UPPER(c.codeType.value) LIKE :filterText) " +
            "OR (UPPER(c.standardType.value) LIKE :filterText)) " +
            "AND (c.standardType.id IN (:standardTypes))")
    public Page<Code> findAllByStandardTypesFiltered(
            @Param("filterText") String filterText,
            @Param("standardTypes") List<Long> standardTypes,
            Pageable pageable);

    @Query("SELECT c FROM Code c " +
            "WHERE ((UPPER(c.code) LIKE :filterText) " +
            "OR (UPPER(c.description) LIKE :filterText) " +
            "OR (UPPER(c.codeType.value) LIKE :filterText) " +
            "OR (UPPER(c.standardType.value) LIKE :filterText)) " +
            "AND (c.codeType.id IN (:codeTypes)) " +
            "AND (c.standardType.id IN (:standardTypes))")
    public Page<Code> findAllByCodeAndStandardTypesFiltered(
            @Param("filterText") String filterText,
            @Param("codeTypes") List<Long> codeTypes,
            @Param("standardTypes") List<Long> standardTypes,
            Pageable pageable);

    @Query("SELECT c FROM Code c " +
            "WHERE c.code = :code " +
            "AND c.description = :description " +
            "AND c.codeType = :codeType " +
            "AND c.standardType = :standardType ")
    public Iterable<Code> findAllByExistingCodeDetails(@Param("code") String code,
                                                       @Param("description") String description,
                                                       @Param("codeType") Lookup codeType,
                                                       @Param("standardType") Lookup standardType);
}
