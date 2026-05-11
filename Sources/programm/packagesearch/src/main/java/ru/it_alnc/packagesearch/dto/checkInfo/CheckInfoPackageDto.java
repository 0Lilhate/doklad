/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.checkInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import ru.it_alnc.packagesearch.dto.subscribe.ServiceLineDto;

@Data
@JsonPropertyOrder({"package","serviceLine"})
public class CheckInfoPackageDto {
    @JsonProperty("package")
    String packageCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    CheckInfoServiceLineDto serviceLine;
}
