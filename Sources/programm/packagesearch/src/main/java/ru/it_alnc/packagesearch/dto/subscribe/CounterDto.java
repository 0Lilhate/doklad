/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
//@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CounterDto implements Cloneable {
    @JsonIgnore
    Long id;
    String code;
    String label;
    String currency;
    String periodType;
    String periodValue;
    Integer isCalendarBorder;
    String contextType;

    //Заглушки, так надо для обратной совместимости
    String period = null;
    String type = null;
    String typeLabel= null;
    String parentCode = null;
    Boolean isUseQuantity = true;
    Boolean isUseSum = true;
    Boolean isUseFee = true;
    Boolean isUseNDS = true;
    String contextTypeLabel = null;
    Boolean isCollectOnAccount = false;
    Boolean isCollectOnPact = false;
    Boolean isCollectOnClient = false;
    Boolean isCollectOnClientGroup = false;
    Integer archiveLength = 0;
    Boolean isGetFromIT = false;
    Boolean isGetFromS = false;
    Boolean isGetFromPS = false;
    Boolean isGetFromTP = false;
    Boolean isPutToIT = false;
    Boolean isPutToS = false;
    Boolean isPutToPS = false;
    Boolean isPutToTP = false;

    @Override
    protected CounterDto clone() throws CloneNotSupportedException {
        return (CounterDto) super.clone();
    }
}
