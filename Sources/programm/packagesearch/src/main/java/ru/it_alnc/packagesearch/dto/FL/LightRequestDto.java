/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.FL;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
public class LightRequestDto {
    List<String> clientPUCodeList = new ArrayList<>();

    @JsonProperty("CLIENT")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    List<ParamMap> clientParams = new ArrayList<>();


    @Getter
    @Setter
    @ToString
    public static class ParamMap {
        String code;
        String value;
    }
}
