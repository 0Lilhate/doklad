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
import ru.it_alnc.packagesearch.entity.ProductEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    public ProductEntity getById(Long id);

    //subscribes

//    @Deprecated
//    @Query("SELECT p FROM ProductEntity p WHERE p.versions.size > 0 AND p.nodeType = 'product' AND p.status = 'Approved' " +
//            " AND p.category IN :categories AND p.code = :clientId")
//    public List<ProductEntity> getSubscribeByClientId(String clientId, List<String> categories);
//
//    @Query("SELECT p FROM ProductEntity p WHERE p.versions.size > 0 AND p.nodeType = 'product' AND p.status = 'Approved' " +
//            "AND p.code IN (:codes) AND p.category IN :categories")
//    public List<ProductEntity> getSubscribeByCodes(List<String> codes, List<String> categories);

    @Query("SELECT p FROM ProductEntity p WHERE p.nodeType = 'product' AND p.status IN ('Approved', 'Edit') " + //добавить условие "где есть VersionEntity"
            "AND p.category IN ('TARIFF_PLAN', 'SERVICE_PACKAGE', 'SUBSCRIPTION') AND p.beginDate <= :localDateTime and p.endDate > :localDateTime")
    public List<ProductEntity> getArchiveSubscribes(LocalDateTime localDateTime);


    @Query("SELECT p FROM ProductEntity p WHERE p.nodeType = 'product' AND p.status IN ('Approved', 'Edit') " + //добавить условие "где есть VersionEntity"
            "AND p.category IN ('TARIFF_PLAN', 'SERVICE_PACKAGE', 'SUBSCRIPTION') AND p.endDate > current_date")
    public List<ProductEntity> getAllActiveAndFutureSubscribes();


    @Query("SELECT p.beginDate from ProductEntity p WHERE (p.beginDate BETWEEN :fromDate AND :toDate) OR (p.modifiedOn BETWEEN :fromDate AND :toDate)")
    List<LocalDateTime> findNowService(LocalDateTime fromDate, LocalDateTime toDate);

    @Query("SELECT p FROM ProductEntity p WHERE  p.nodeType = 'product' AND p.status IN ('Approved', 'Edit') " +
            "AND p.category IN ('TARIFF_PLAN', 'SERVICE_PACKAGE', 'SUBSCRIPTION') AND p.code IN (:codes)")
    public List<ProductEntity> findByClientPUCodes(List<String> codes);

    //test repo methods
//    public List<ProductEntity> findAllByNodeTypeAndStatusInAndCategoryInAndCode(String nodeType, List<String> status, List<String> category, String code);
//
//    public List<ProductEntity> findAllByNodeTypeAndStatusInAndCategoryInAndCodeIn(String nodeType, List<String> status, List<String> category, List<String> codes);
}
