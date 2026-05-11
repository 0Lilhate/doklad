/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
//@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class CheckSetLineDto implements Cloneable {
    @JsonIgnore
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime begDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime endDate;
    private List<SubscribeParamDto> paramList = new ArrayList<>();
    @JsonIgnore
    private List<SubscribeFilterParamDto> filterParams;

    @Override
    protected CheckSetLineDto clone() throws CloneNotSupportedException {
        CheckSetLineDto clonedCheckSetLineDto = (CheckSetLineDto) super.clone();

        if(this.getParamList() != null) {
            List<SubscribeParamDto> subscribeParamDtoList = new ArrayList<>();
            for (SubscribeParamDto subscribeParamDto : this.getParamList()) {
                subscribeParamDtoList.add(subscribeParamDto.clone());
            }
            clonedCheckSetLineDto.setParamList(subscribeParamDtoList);
        }

        return clonedCheckSetLineDto;
    }
}
