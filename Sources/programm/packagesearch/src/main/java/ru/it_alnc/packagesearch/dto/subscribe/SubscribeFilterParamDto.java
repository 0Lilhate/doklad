/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SubscribeFilterParamDto extends SubscribeParamDto {
    @JsonIgnore
    private String compareType;
    @JsonIgnore
    private String objectType;
    @JsonIgnore
    private Integer TariffPart;

    //private Integer isGroupGuide; //28089, 33790

    @Override
    public SubscribeFilterParamDto clone() throws CloneNotSupportedException {
        return (SubscribeFilterParamDto) super.clone();
    }
}
