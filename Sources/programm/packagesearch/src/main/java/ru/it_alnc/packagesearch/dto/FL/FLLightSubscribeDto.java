/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.FL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.it_alnc.packagesearch.dto.subscribe.ServiceLineDto;
import ru.it_alnc.packagesearch.dto.subscribe.SubscribeDto27277;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FLLightSubscribeDto extends ru.it_alnc.packagesearch.dto.subscribe.LightSubscribeDto {

    String excludePUGroup;

    public FLLightSubscribeDto(SubscribeDto27277 subscribeDto27277){
        this.setCode(subscribeDto27277.getCode());
        this.setLabel(subscribeDto27277.getLabel());
        this.setBegDate(subscribeDto27277.getBegDate());
        this.setEndDate(subscribeDto27277.getEndDate());
        this.setExcludePUGroup(subscribeDto27277.getExcludePUGroup());
        this.id = subscribeDto27277.getId();
        this.isIndependent = subscribeDto27277.getIsIndependent();
    }
    //служебные поля, в ответе не передавать
    @JsonIgnore
    Boolean isIndependent;
    @JsonIgnore
    Long id;
    @JsonIgnore
    List<ServiceLineDto> serviceLineDtoList;
    @JsonIgnore
    List<BegEndDatesDto> flPeriodsList = new ArrayList<>();
}
