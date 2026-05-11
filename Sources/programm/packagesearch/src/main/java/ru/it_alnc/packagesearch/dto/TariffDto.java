/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Setter
@Getter
public class TariffDto {
    String code;
    String label;
    Boolean isSimple;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime begDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime endDate;
    private String rateType;
    private String entryRateType;
    private String entryCurrency;
    private String allLineCurrency;
    List<TariffLineDto> tariffLineList;

    public void addTariffLine(TariffLineDto tariffLineDto){
        this.tariffLineList.add(tariffLineDto);
    }
}
