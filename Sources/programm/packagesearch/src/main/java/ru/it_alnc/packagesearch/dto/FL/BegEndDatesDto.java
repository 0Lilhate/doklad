/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.FL;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.it_alnc.packagesearch.dto.subscribe.CheckSetLineDto;
import ru.it_alnc.packagesearch.dto.subscribe.ServiceLineDto;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BegEndDatesDto {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime begDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime endDate;

    public BegEndDatesDto(CheckSetLineDto checkSetLineDto){
        this.begDate = checkSetLineDto.getBegDate();
        this.endDate = checkSetLineDto.getEndDate();
    }

    public BegEndDatesDto(ServiceLineDto serviceLineDto){
        this.begDate = serviceLineDto.getBegDate();
        this.endDate = serviceLineDto.getEndDate();
    }
}
