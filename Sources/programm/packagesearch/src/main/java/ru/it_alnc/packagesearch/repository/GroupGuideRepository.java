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
import ru.it_alnc.packagesearch.entity.GroupGuideEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroupGuideRepository extends JpaRepository<GroupGuideEntity, Integer> {

    @Query("SELECT g FROM GroupGuideEntity g WHERE g.action in ('INSERT','UPDATE')")
    List<GroupGuideEntity> getGroupGuideEntitiesForInitCache();

    @Query("SELECT g from GroupGuideEntity g WHERE g.modifiedOn > :checkDate")
    List<GroupGuideEntity> findNewGroupGuideEntities(LocalDateTime checkDate);

}
