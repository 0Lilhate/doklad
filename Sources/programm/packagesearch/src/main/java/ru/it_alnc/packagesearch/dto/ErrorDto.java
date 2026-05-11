/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@Setter
@Getter
@NoArgsConstructor
public class ErrorDto {
    private String code;
    private String message;
    private String source;

    public ErrorDto(String code, String message) {
        this.code = code;
        this.message = message;
        this.source = "PACKAGESEARCH";
    }
}
