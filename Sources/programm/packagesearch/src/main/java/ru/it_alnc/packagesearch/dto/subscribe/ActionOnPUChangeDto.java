/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.it_alnc.packagesearch.entity.subscribe.ActionOnPUChangeLineEntity;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;


@Data
public class ActionOnPUChangeDto {
    @JsonIgnore
    private Long id;
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    //private ZonedDateTime createdOn;
    //private String createdBy;
    //private String createdByName;
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    //private ZonedDateTime modifiedOn;
    //private String modifiedBy;
    //private String modifiedByName;
    @JsonIgnore
    private String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime begDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime endDate;
    @JsonProperty("actionOnPUChangeLineList")
    private List<ActionOnPUChangeLineDto> actionOnPuChangeLines;
}
