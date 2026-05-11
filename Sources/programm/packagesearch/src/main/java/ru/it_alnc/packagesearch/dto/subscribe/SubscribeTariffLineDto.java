/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SubscribeTariffLineDto implements Cloneable {
    @JsonIgnore
    private Long id;
    private Integer order;
    private String currency;
    private BigDecimal amountFix;
    private Float rate;
    @JsonProperty("amountMIN")
    private BigDecimal amountMin;
    @JsonProperty("amountMAX")
    private BigDecimal amountMax;
    private String entryCurrency;
    private String preferenceCode;
    private String preferenceLabel;
    private List<SubscribeParamDto> paramList = new ArrayList<>();
    @JsonIgnore
    private List<SubscribeFilterParamDto> filterParams;

    @Override
    protected SubscribeTariffLineDto clone() throws CloneNotSupportedException {
        SubscribeTariffLineDto clonedSubscribeTariffLineDto = (SubscribeTariffLineDto) super.clone();

        if(this.getParamList() != null) {
            List<SubscribeParamDto> subscribeParamDtoList = new ArrayList<>();
            for (SubscribeParamDto subscribeParamDto : this.getParamList()) {
                subscribeParamDtoList.add(subscribeParamDto.clone());
            }
            clonedSubscribeTariffLineDto.setParamList(subscribeParamDtoList);
        }

        return clonedSubscribeTariffLineDto;
    }
}
