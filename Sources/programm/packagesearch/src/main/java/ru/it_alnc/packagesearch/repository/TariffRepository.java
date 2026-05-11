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
import ru.it_alnc.packagesearch.entity.subscribe.TariffGetTariffEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TariffRepository extends JpaRepository<TariffGetTariffEntity,Long> {

    List<TariffGetTariffEntity> findAllByCodeAndStatusEquals(String code, String status); //проверка на текущую дату ,отсеять будущие

    List<TariffGetTariffEntity> findAllByCodeAndBegDateEqualsAndStatus(String code, LocalDateTime begDate, String status);

    @Query("SELECT t FROM TariffGetTariffEntity t WHERE t.endDate > current_date AND t.status = 'Approved'")
    List<TariffGetTariffEntity> findAllTariffs();

}
