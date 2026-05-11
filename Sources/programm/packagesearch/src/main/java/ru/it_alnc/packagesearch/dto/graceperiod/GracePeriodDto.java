/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.graceperiod;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.it_alnc.packagesearch.dto.subscribe.SubscribeFilterParamDto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class GracePeriodDto implements Cloneable, Comparable<GracePeriodDto> {
    Long gpId;
    String clientCommonType;
    String service;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime begDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime endDate;
    String gracePeriod;
    String defaultPaymentDuration;
    List<SubscribeFilterParamDto> paramList = new ArrayList<>();

    @Override
    public GracePeriodDto clone() throws CloneNotSupportedException{
            GracePeriodDto clone = (GracePeriodDto) super.clone();
            if(!paramList.isEmpty()) {
                List<SubscribeFilterParamDto> clonedParamList = new ArrayList<>();
                for (SubscribeFilterParamDto subscribeFilterParamDto : this.paramList) {
                    clonedParamList.add(subscribeFilterParamDto.clone());
                }
                clone.setParamList(clonedParamList);
            }
            return clone;
    }

    @Override
    public int compareTo(GracePeriodDto o) {
        return this.getGpId() < o.getGpId() ? -1 : 1;
    }
}
