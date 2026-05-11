/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.it_alnc.packagesearch.entity.subscribe.ParamValueEntity;

import java.util.List;

@Repository
public interface ParamValueRepository extends JpaRepository<ParamValueEntity, Integer> {
    public List<ParamValueEntity> findAllByProduct(Integer productId);
}
