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
public class PuParamDto extends SubscribeParamDto implements Cloneable {
    String puGroupCode;
    String puGroupLabel;
    @JsonIgnore
    Integer version;

    @Override
    protected PuParamDto clone() throws CloneNotSupportedException {
        return (PuParamDto) super.clone();
    }
}
