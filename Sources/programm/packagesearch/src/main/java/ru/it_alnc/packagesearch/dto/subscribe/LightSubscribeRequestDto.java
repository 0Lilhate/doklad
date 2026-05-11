/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LightSubscribeRequestDto {

    @JsonProperty("clientCommonType")
    List<String> clientCommonType;
    @JsonProperty("packageCategory")
    List<String> packageCategory;
    @JsonProperty("individualTariff")
    IndividualTariffInner individualTariff;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class IndividualTariffInner{
        @JsonProperty("getIndividualTariff")
        Boolean getIndividualTariff = false;
        @JsonProperty("service")
        List<String> service;
        @JsonProperty("serviceType")
        Set<String> serviceType;
    }
}
