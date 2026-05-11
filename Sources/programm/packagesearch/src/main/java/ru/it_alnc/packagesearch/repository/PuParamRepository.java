/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.it_alnc.packagesearch.entity.subscribe.PuParamEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PuParamRepository extends JpaRepository<PuParamEntity, Long> {

    @Query("SELECT sp from PuParamEntity sp WHERE sp.productId = :productId " +
          //  " AND sp.version = :version " +
           // " AND sp.pgBegDate <= :todayDate " +
            "AND sp.pgEndDate >= current_date ")
    public List<PuParamEntity> getActualSimpleListSubscribeParams(@Param("productId") Long productId

           // , @Param("version") Integer version
                                                                  //, @Param("todayDate") LocalDateTime todayDate
    );
    @Query("SELECT sp from PuParamEntity sp WHERE sp.productId = :productId " +
            //  " AND sp.version = :version " +
             " AND sp.pgBegDate <= :requestDate " +
            "AND sp.pgEndDate >= :requestDate")
    public List<PuParamEntity> getArchiveListSubscribeParams(@Param("productId") Long productId,
                                                             @Param("requestDate") LocalDateTime requestDate
    );
    //todo - возм сделать один запрос на все параметры, подходящие по условиям (продукт, версия, дата), собрать их в мап
    // и потом в цикле раскидать по продуктам
}
