/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class SearchServiceGuideDto {
    @JsonProperty(value = "clientCommonTypeList", required = true)
    private List<String> clientCommonTypeList;
    private List<String> serviceGuideList = new ArrayList<>();
    private List<String> serviceTypeList = new ArrayList<>();
    private List<String> serviceGuideItemList = new ArrayList<>();
}
