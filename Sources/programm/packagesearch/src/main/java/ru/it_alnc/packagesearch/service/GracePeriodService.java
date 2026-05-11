/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.service;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.it_alnc.packagesearch.dto.ErrorDto;
import ru.it_alnc.packagesearch.dto.graceperiod.GracePeriodDto;
import ru.it_alnc.packagesearch.dto.graceperiod.GracePeriodRequestDto;
import ru.it_alnc.packagesearch.dto.graceperiod.GracePeriodResponseDto;
import ru.it_alnc.packagesearch.exception.ExceptionCustomErrorDto;
import ru.it_alnc.packagesearch.query.graceperiod.GracePeriodQueryBuilder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GracePeriodService {
    private final GracePeriodQueryBuilder gracePeriodQueryBuilder = ApplicationContextProvider.getBean(GracePeriodQueryBuilder.class);


    public GracePeriodResponseDto gracePeriodSearch(GracePeriodRequestDto gracePeriodRequestDto) throws ExceptionCustomErrorDto{
        GracePeriodResponseDto result = new GracePeriodResponseDto();
        this.gracePeriodQueryBuilder.filterGracePeriodsByRequest(gracePeriodRequestDto);
        if(this.gracePeriodQueryBuilder.getGracePeriods().isEmpty()){
            ErrorDto errorDto = new ErrorDto();
            errorDto.setMessage(String.format("Для услуги %s на дату запроса не настроен grace-период", gracePeriodRequestDto.getService()));
            errorDto.setCode("102");
            throw new ExceptionCustomErrorDto(errorDto, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        this.gracePeriodQueryBuilder.filterGracePeriodsByDynamicParams(gracePeriodRequestDto);
        if(this.gracePeriodQueryBuilder.getGracePeriods().isEmpty()){
            ErrorDto errorDto = new ErrorDto();
            errorDto.setMessage(String.format("Не удалось подобрать grace-период по параметрам операции для услуги %s", gracePeriodRequestDto.getService()));
            errorDto.setCode("103");
            throw new ExceptionCustomErrorDto(errorDto, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(this.gracePeriodQueryBuilder.getGracePeriods().size() > 1){
            ErrorDto errorDto = new ErrorDto();
            errorDto.setMessage(String.format("Найдено более 2-ух строк с настройками грейс-периода для услуги %s", gracePeriodRequestDto.getService()));
            errorDto.setCode("111");
            throw new ExceptionCustomErrorDto(errorDto, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        GracePeriodDto responseGracePeriodDto = this.gracePeriodQueryBuilder.getGracePeriods().get(0);
        ZonedDateTime calculationDate = getDateWithPeriod(gracePeriodRequestDto);
        result.setCalculationDate(calculationDate);
        ZonedDateTime graceEndDate = addPeriodToDate(calculationDate,responseGracePeriodDto.getGracePeriod());
        result.setGraceEndDate(graceEndDate);
        result.setPaymentDate(addPeriodToDate(graceEndDate,String.format("%sD",responseGracePeriodDto.getDefaultPaymentDuration())));
        result.setGracePeriod(responseGracePeriodDto.getGracePeriod());
        result.setBegDate(responseGracePeriodDto.getBegDate());
        result.setEndDate(responseGracePeriodDto.getEndDate());
        result.setParamList(responseGracePeriodDto.getParamList());

        return result;
    }

    private ZonedDateTime getDateWithPeriod(GracePeriodRequestDto gracePeriodRequestDto){
        return (gracePeriodRequestDto.getCalculationDate() != null ? gracePeriodRequestDto.getCalculationDate() : ZonedDateTime.now());
    }

    public ZonedDateTime addPeriodToDate(ZonedDateTime calculationDate, String periodCode) {
//        D  D - Дней
//        W  W - Недель
//        M  M - Месяцев
//        Y  Y - Лет
//        N  N - Без периода (бесконечный)
        String periodSymbol = periodCode.substring(periodCode.length() - 1);
        int periodNumber = Integer.parseInt(periodCode.substring(0, periodCode.length() - 1));
        switch (periodSymbol) {
            case "D":
                return calculationDate.plusDays(periodNumber);
            case "W":
                return calculationDate.plusWeeks(periodNumber);
            case "M":
                return calculationDate.plusMonths(periodNumber);
            case "Y":
                return calculationDate.plusYears(periodNumber);
            default:
                return calculationDate;
        }
    }

    public void filterGQBByRequest(GracePeriodRequestDto requestDto) throws CloneNotSupportedException {
        this.gracePeriodQueryBuilder.init(requestDto);
    }

}
