/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.exception;

import lombok.Getter;
import ru.it_alnc.packagesearch.dto.ErrorDto;

public class CustomErrorMessageException extends RuntimeException {

    @Getter
    ErrorDto errorDto;

    public CustomErrorMessageException(ErrorDto errorDto) {
        super(errorDto.getMessage());
        this.errorDto = errorDto;
    }
}
