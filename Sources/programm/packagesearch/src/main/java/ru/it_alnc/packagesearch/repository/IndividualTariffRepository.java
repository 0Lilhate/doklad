/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.it_alnc.packagesearch.entity.subscribe.IndividualTariffEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IndividualTariffRepository extends JpaRepository<IndividualTariffEntity, Integer> {
    @EntityGraph(attributePaths = {
            "serviceLineEntity"
    })
    @Query("SELECT it from IndividualTariffEntity it WHERE it.code IN (:codes) and it.serviceLineEntity.status = 'Approved'")
    List<IndividualTariffEntity> findAllByCodes(@Param("codes") List<String> codes);

    @EntityGraph(attributePaths = {
            "serviceLineEntity"
    })
    @Query("SELECT it from IndividualTariffEntity it WHERE it.serviceLineEntity.begDate <= :searchOnDate AND :searchOnDate < it.serviceLineEntity.endDate AND it.serviceLineEntity.status = 'Approved'")
    List<IndividualTariffEntity> findByDate(@Param("searchOnDate") LocalDateTime searchOnDate);

    @EntityGraph(attributePaths = {
            "serviceLineEntity"
    })
    @Query("SELECT it from IndividualTariffEntity it WHERE it.code IN (:individualTariffCodes) AND it.serviceLineEntity.begDate <= :searchOnDate AND :searchOnDate < it.serviceLineEntity.endDate and it.serviceLineEntity.status = 'Approved'")
    List<IndividualTariffEntity> findByCodesAndDate(@Param("individualTariffCodes") List<String> individualTariffCodes, @Param("searchOnDate") LocalDateTime searchOnDate);

    @EntityGraph(attributePaths = {
            "serviceLineEntity"
    })
    @Query("SELECT it from IndividualTariffEntity it WHERE it.serviceLineEntity.status = 'Approved'")
    List<IndividualTariffEntity> findAllWithApprovedServiceLine();
}
