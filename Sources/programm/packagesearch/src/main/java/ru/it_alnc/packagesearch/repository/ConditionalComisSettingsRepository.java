/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.it_alnc.packagesearch.entity.conditionalcomiss.ConditionalComisSettingsEntity;
import ru.it_alnc.packagesearch.entity.graceperiod.GracePeriodEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface ConditionalComisSettingsRepository extends JpaRepository<ConditionalComisSettingsEntity, Long>,
        JpaSpecificationExecutor<ConditionalComisSettingsEntity> {

    @EntityGraph(attributePaths = {
        "serviceGuideItemEntity",
        "cardTypevalueTypeEntity", 
        "cardContractvalueTypeEntity"
    })
    @Query("""
            select ccse from ConditionalComisSettingsEntity ccse
            where ccse.status = 'Approved'
            and :date between ccse.begDate and ccse.endDate
            """)
    List<ConditionalComisSettingsEntity> findAllByDateAndStatus(@Param("date") LocalDateTime date);

    @EntityGraph(attributePaths = {
            "serviceGuideItemEntity",
            "cardTypevalueTypeEntity",
            "cardContractvalueTypeEntity"
    })
    @Query("""
            select ccse from ConditionalComisSettingsEntity ccse
            where ccse.status = 'Approved'
            """)
    List<ConditionalComisSettingsEntity> findAllApproved();


    @Query("SELECT ccse.begDate from ConditionalComisSettingsEntity ccse WHERE (ccse.begDate BETWEEN :fromDate AND :toDate) OR (ccse.modifiedOn BETWEEN :fromDate AND :toDate)")
    List<LocalDateTime> findNewSettings(LocalDateTime fromDate, LocalDateTime toDate);


} 