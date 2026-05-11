/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity.subscribe;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
public class ServiceGuideEntity29735 {
    private String serviceGuideClientCommonType;
    private String serviceGuideCode;
    private String serviceGuideLabel;
    private Long serviceGuideId;
    private String clientCommonType;
    private String code;
    private String label;
    private String indexCode;
    private String serviceType;
    private LocalDateTime begDate;
    private LocalDateTime endDate;
    private Integer sortOrder;
    private Integer isMultyCopiesCalcAllowed;
}
