/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import ru.it_alnc.packagesearch.dto.ErrorDto;

public class ExceptionCustomErrorDto extends RuntimeException{

    @Getter
    ErrorDto errorDto;

    @Getter
    @Setter
    HttpStatus httpStatus;

    public ExceptionCustomErrorDto(ErrorDto errorDto){
        super(errorDto.getMessage());
        this.errorDto = errorDto;
    }

    public ExceptionCustomErrorDto(ErrorDto errorDto, HttpStatus httpStatus){
        super(errorDto.getMessage());
        this.errorDto = errorDto;
        this.httpStatus = httpStatus;
    }
}
