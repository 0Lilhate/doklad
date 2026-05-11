/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.checkInfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.it_alnc.packagesearch.config.JsonOffsetDateTimeDeserializer;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CheckInfoRequestDto {
    @JsonDeserialize(using = JsonOffsetDateTimeDeserializer.class)
    OffsetDateTime searchOnDate;
    @JsonProperty("packageList")
    List<String> packageCodes;
    @JsonProperty("service")
    String service;

    @JsonProperty("CLIENT")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    List<ParamMap> clientParams = new ArrayList<>();
    @JsonProperty("ACCOUNT")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    List<ParamMap> accountParams = new ArrayList<>();
    @JsonProperty("CARD")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    List<ParamMap> cardParams = new ArrayList<>();
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
