/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MappedGracePeriodsDto {
    private List<String> codesNeededToBeCheckedList = new ArrayList<>();
    private List<String> codesNotNeededToBeCheckedList = new ArrayList<>();
    private List<CheckedGraceBySourceDto> checkedGraceBySourceDtoList = new ArrayList<>();
}
