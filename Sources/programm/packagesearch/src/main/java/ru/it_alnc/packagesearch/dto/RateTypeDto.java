/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class RateTypeDto {
    private String code;
    private String status;
    private String label;
    private Integer type;
    private String crossCurrency;
    private Boolean isInner;
    private String currencyList;
    private String currencyListWidget;
}
