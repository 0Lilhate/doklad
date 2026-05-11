/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.graceperiod;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.it_alnc.packagesearch.config.JsonOffsetDateTimeDeserializer;
import ru.it_alnc.packagesearch.config.JsonZonedDateTimeDeserializer;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class GracePeriodRequestDto {

    @JsonDeserialize(using = JsonZonedDateTimeDeserializer.class)
    ZonedDateTime calculationDate;

    @JsonProperty("service")
    String service;


    @JsonProperty("OPERATION")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    List<ParamMap> operationParams = new ArrayList<>();


    @Getter
    @Setter
    @ToString
    public static class ParamMap {
        String code;
        String value;
    }
}
