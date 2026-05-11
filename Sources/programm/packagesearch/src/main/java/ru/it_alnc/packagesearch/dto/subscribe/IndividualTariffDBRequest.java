/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import ru.it_alnc.packagesearch.config.JsonOffsetDateTimeDeserializer;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class IndividualTariffDBRequest {
    @JsonDeserialize(using = JsonOffsetDateTimeDeserializer.class)
    OffsetDateTime searchOnDate;
    @JsonProperty("individualTariffCode")
    private List<String> individualTariffCodes = new ArrayList<>();
}
