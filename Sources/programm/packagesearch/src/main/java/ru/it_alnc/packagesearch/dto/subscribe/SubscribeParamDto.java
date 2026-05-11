/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.it_alnc.packagesearch.dto.GroupGuideDto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
//@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class SubscribeParamDto implements Cloneable {
    @JsonIgnore
    Long id;
    Integer order;
    String code;
    String label;
    String guide;
    String type;
    @JsonProperty("vType")
    String vType;
    @JsonProperty("vTypeLabel")
    String vTypeLabel;
    @JsonProperty("vBoolean")
    Boolean vBoolean;
    @JsonProperty("vString")
    String vString;
    @JsonProperty("vDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime vDate;
    @JsonProperty("vDateMin")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime vDateMin;
    @JsonProperty("vDateMax")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime vDateMax;
    @JsonProperty("vInt")
    Long vInt;
    @JsonProperty("vIntMin")
    Long vIntMin;
    @JsonProperty("vIntMax")
    Long vIntMax;
    @JsonProperty("vFloat")
    BigDecimal vFloat;
    @JsonProperty("vFloatMin")
    BigDecimal vFloatMin;
    @JsonProperty("vFloatMax")
    BigDecimal vFloatMax;
    @JsonProperty("vCodePattern")
    String vCodePattern;
    @JsonProperty("vCodeList")
    String vCodeList;
    @JsonProperty("vLabelList")
    String vLabelList;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    Boolean isCheckPass;
    Boolean isGroupGuide;

    @JsonIgnore
    Boolean isGroupResolved = false;

    @JsonIgnore
    List<GroupGuideDto> listOfGroupGuideDtos = new ArrayList<>(); //here we have all matched groupGuideDtos from cache init/update

    @Override
    protected SubscribeParamDto clone() throws CloneNotSupportedException {
        return (SubscribeParamDto) super.clone();
    }
}
