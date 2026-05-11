/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
//@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class SubscribeTariffDto implements Cloneable {
    @JsonIgnore
    Long id;
    String code;
    String label;
    @JsonIgnore
    boolean isSimple;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime begDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime endDate;
    @JsonProperty("tariffLineList")
    List<SubscribeTariffLineDto> subscribeTariffLineDtoList = new ArrayList<>();
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    List<CheckedGraceBySourceDto> checkGraceBySource;
    @JsonIgnore
    MappedGracePeriodsDto gracePeriodsDto; //dto, хранящая в себе списки каналов, по которым (не) нужно проверять был ли подключен ранее грейс период.
    String rateType;
    String entryRateType;
    String entryCurrency;
    String allLineCurrency;

    @Override
    protected SubscribeTariffDto clone() throws CloneNotSupportedException {
        SubscribeTariffDto clonedSubscribeTariffDto = (SubscribeTariffDto) super.clone();

        if(this.getSubscribeTariffLineDtoList() != null) {
            List<SubscribeTariffLineDto> subscribeTariffLineDtoList = new ArrayList<>();
            for (SubscribeTariffLineDto subscribeTariffLineDto : this.getSubscribeTariffLineDtoList()) {
                subscribeTariffLineDtoList.add(subscribeTariffLineDto.clone());
            }
            clonedSubscribeTariffDto.setSubscribeTariffLineDtoList(subscribeTariffLineDtoList);
        }

        return clonedSubscribeTariffDto;
    }
}
