/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.service;



import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.it_alnc.packagesearch.dto.ErrorDto;
import ru.it_alnc.packagesearch.exception.CustomErrorMessageException;
import ru.it_alnc.packagesearch.exception.ExceptionCustomErrorDto;
import ru.it_alnc.packagesearch.exception.NonExistantConditionalSettingException;
import ru.it_alnc.packagesearch.exception.NonSuitableConditionalSettingException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomErrorMessageException.class)
    public ResponseEntity<ErrorDto> handleCustomErrorMessageException(CustomErrorMessageException exception) {
        return ResponseEntity.badRequest()
                .body(exception.getErrorDto());
    }
    @ExceptionHandler(NonExistantConditionalSettingException.class)
    public ResponseEntity<ErrorDto> handleNonExistantConditionalSettingException(NonExistantConditionalSettingException ex) {

        var errorDto = new ErrorDto();
        errorDto.setCode("528");
        errorDto.setMessage("Для услуги: " + ex.getMessage() + " на дату запроса не настроено условное списание");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorDto);
    }
    @ExceptionHandler(NonSuitableConditionalSettingException.class)
    public ResponseEntity<ErrorDto> handleNonSuitableConditionalSettingException(NonSuitableConditionalSettingException ex) {
        var errorDto = new ErrorDto();
        errorDto.setCode("529");
        errorDto.setMessage("Не удалось подобрать настройку по параметрам операции для услуги: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorDto);
    }

}


