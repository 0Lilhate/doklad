/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.it_alnc.packagesearch.entity.subscribe.types.RateTypeEntity;

import java.util.List;

public interface RateTypeRepository extends JpaRepository<RateTypeEntity, Long> {

    List<RateTypeEntity> findAllByStatusEquals(String status);

    List<RateTypeEntity> findAllByStatusEqualsAndCodeIn(String status, List<String> codes);
}
