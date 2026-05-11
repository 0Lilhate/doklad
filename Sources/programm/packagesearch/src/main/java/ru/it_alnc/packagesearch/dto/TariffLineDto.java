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

import java.math.BigDecimal;
import java.util.List;

@Data
@Setter
@Getter
public class TariffLineDto { //todo: вероятно, эта Dto вообще не нужна и нужно заменить её на SubscribeTariffLineDto.class
    private Integer order;
    private String currency;
    private BigDecimal amountFix;
    private Float rate;
    private BigDecimal amountMin;
    private BigDecimal amountMax;
    private String entryCurrency;
    private String preferenceCode;
    private String preferenceLabel;
    private List<TariffParamDto30214> paramList;

    public void addParamList(TariffParamDto30214 tariffParamDto30214){
        this.paramList.add(tariffParamDto30214);
    }
}
