/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.it_alnc.packagesearch.entity.graceperiod.GracePeriodEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GracePeriodGuideLineRepository extends JpaRepository<GracePeriodEntity, Integer> {

    @Query("SELECT gp FROM GracePeriodEntity gp WHERE gp.begDate <= :cachedDate AND gp.endDate > :cachedDate and gp.status IN ('Approved', 'Edit')")
    List<GracePeriodEntity> getActiveGracePediods(LocalDateTime cachedDate);

    @Query("SELECT gp FROM GracePeriodEntity gp WHERE gp.modifiedOn >= :dateBefore AND gp.modifiedOn < :dateAfter")
    List<GracePeriodEntity> getModifiedGracePeriodLines(LocalDateTime dateBefore, LocalDateTime dateAfter);

}
