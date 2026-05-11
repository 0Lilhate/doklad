/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.it_alnc.packagesearch.entity.subscribe.ActionOnPUChangeLineEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface ActionOnPUChangeRepository extends JpaRepository<ActionOnPUChangeLineEntity, Long> {

    @Query("SELECT pu.modifiedOn from ActionOnPUChangeLineEntity pu WHERE (pu.modifiedOn BETWEEN :fromDate AND :toDate)")
    List<LocalDateTime> findNowAction(LocalDateTime fromDate, LocalDateTime toDate);


}
