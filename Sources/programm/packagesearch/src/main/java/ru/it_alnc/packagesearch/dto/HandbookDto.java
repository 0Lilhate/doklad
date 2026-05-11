/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HandbookDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String referenceCode;

    private String code;

    private String label;

    private String description;

    private Integer showOrder;

    private LocalDateTime createdOn;

    private LocalDateTime modifiedOn;

    private String createdBy;

    private String modifiedBy;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DescriptorDto> descriptors;
}
