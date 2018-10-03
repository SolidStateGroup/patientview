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

    Page<Code> findAll(Pageable pageable);

    @Query("SELECT c FROM Code c " +
            "WHERE c.code IN :codes")
    List<Code> findAllByCodes(@Param("codes") List<String> codes);

    @Query("SELECT c FROM Code c " +
            "WHERE c.code = :code " +
            "AND c.codeType = :codeType")
    List<Code> findAllByCodeAndType(@Param("code") String code, @Param("codeType") Lookup codeType);

    @Query("SELECT c FROM Code c " +
            "WHERE c.code = :code " +
            "AND c.codeType = :codeType")
    Code findOneByCodeAndType(@Param("code") String code, @Param("codeType") Lookup codeType);

    @Query("SELECT c FROM Code c " +
            "WHERE c.codeType = :codeType")
    List<Code> findAllByType(@Param("codeType") Lookup codeType);

    @Query("SELECT c FROM Code c " +
            "WHERE c.standardType = :standardType")
    List<Code> findAllByStandardType(@Param("standardType") Lookup standardType);

    @Query("SELECT c FROM Code c " +
            "WHERE (UPPER(c.code) LIKE :filterText) " +
            "OR (UPPER(c.description) LIKE :filterText) " +
            "OR (UPPER(c.codeType.value) LIKE :filterText) " +
            "OR (UPPER(c.standardType.value) LIKE :filterText)")
    Page<Code> findAllFiltered(
            @Param("filterText") String filterText,
            Pageable pageable);

    @Query("SELECT c FROM Code c " +
            "WHERE ((UPPER(c.code) LIKE :filterText) " +
            "OR (UPPER(c.description) LIKE :filterText) " +
            "OR (UPPER(c.codeType.value) LIKE :filterText) " +
            "OR (UPPER(c.standardType.value) LIKE :filterText)) " +
            "AND (c.codeType.id IN (:codeTypes))")
    Page<Code> findAllByCodeTypesFiltered(
            @Param("filterText") String filterText,
            @Param("codeTypes") List<Long> codeTypes,
            Pageable pageable);

    @Query("SELECT c FROM Code c " +
            "WHERE ((UPPER(c.code) LIKE :filterText) " +
            "OR (UPPER(c.description) LIKE :filterText) " +
            "OR (UPPER(c.codeType.value) LIKE :filterText) " +
            "OR (UPPER(c.standardType.value) LIKE :filterText)) " +
            "AND (c.standardType.id IN (:standardTypes))")
    Page<Code> findAllByStandardTypesFiltered(
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
    Page<Code> findAllByCodeAndStandardTypesFiltered(
            @Param("filterText") String filterText,
            @Param("codeTypes") List<Long> codeTypes,
            @Param("standardTypes") List<Long> standardTypes,
            Pageable pageable);

    @Query("SELECT c FROM Code c " +
            "WHERE c.code = :code " +
            "AND c.description = :description " +
            "AND c.codeType = :codeType " +
            "AND c.standardType = :standardType ")
    Iterable<Code> findAllByExistingCodeDetails(@Param("code") String code,
                                                       @Param("description") String description,
                                                       @Param("codeType") Lookup codeType,
                                                       @Param("standardType") Lookup standardType);

    @Query("SELECT c FROM Code c WHERE c.code = :code")
    Code findOneByCode(@Param("code") String code);
}
