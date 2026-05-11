/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString
public class SearchCriteriaDto {
    String parentCode;

    String code;

    String label;

    LocalDateTime createdOn;

    LocalDateTime modifiedOn;

    String createdBy;

    String modifiedBy;

    List<DescriptorDto> descrs;
}
