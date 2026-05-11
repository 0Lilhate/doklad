/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import lombok.Data;

@Data
public class ParamGracePeriodDto {
    private String sourceCodeList;
    private String sourceLabelList;
    private Boolean checkGrace = false;
} 