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
import ru.it_alnc.packagesearch.entity.subscribe.ServiceLine27277Entity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceLineRepository extends JpaRepository<ServiceLine27277Entity, Long> {
//    @Query("SELECT sl FROM ServiceLine27277Entity sl WHERE sl.pu4PriceService = :pu4PriceService AND sl.objTable = 'PRODUCT'")
    @Query(value = "Select * from serviceline sl WHERE sl.pu4priceservice = :pu4PriceService AND sl.objTable = 'PRODUCT'", nativeQuery = true)
    public List<ServiceLine27277Entity> findAllByPu4PriceService(Long pu4PriceService);

    @Query("SELECT sl FROM ServiceLine27277Entity sl JOIN ProductEntity pe ON sl.obj = pe.id JOIN ProductEntity p2 ON p2.id = sl.pu4PriceService " +
            " WHERE p2.id = :pu4PriceService " +
            " AND pe.code IN (:codes) AND sl.objTable = 'PRODUCT'")
    public List<ServiceLine27277Entity> findAllByPu4PriceService(Long pu4PriceService, List<String> codes);

    @Query("SELECT sl.begDate from ServiceLine27277Entity sl WHERE (sl.begDate BETWEEN :fromDate AND :toDate) OR (sl.modifiedOn BETWEEN :fromDate AND :toDate)")
    List<LocalDateTime> findNowService(LocalDateTime fromDate, LocalDateTime toDate);

    @Query("SELECT sl FROM ServiceLine27277Entity sl WHERE sl.serviceTypeEntity.code = :serviceType AND sl.puServiceGroup.id = :puServiceGroupId")
    List<ServiceLine27277Entity> findAllBySGAndTypeService(long puServiceGroupId, String serviceType);

    @Query("SELECT sl FROM ServiceLine27277Entity sl WHERE sl.endDate > current_date AND sl.objTable = 'PRODUCT' and sl.status = 'Approved'")
    List<ServiceLine27277Entity> findAllServiceLines();


    @Query("SELECT sl FROM ServiceLine27277Entity sl WHERE sl.begDate <= :ldt and sl.endDate > :ldt AND sl.objTable = 'PRODUCT' and sl.status = 'Approved'")
    List<ServiceLine27277Entity> findAllArchiveServiceLines(LocalDateTime ldt);
}
