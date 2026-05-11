/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GetTariffDto {

    @JsonProperty("code")
    String code;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime begDate;
    @JsonProperty("lineSortOrder")
    List<Integer> lineSortOrder;

}
