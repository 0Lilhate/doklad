/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.it_alnc.packagesearch.dto.conditionalcomiss.ConditionalComisRequestDto;
import ru.it_alnc.packagesearch.dto.conditionalcomiss.ConditionalComisResponseDto;
import ru.it_alnc.packagesearch.dto.conditionalcomiss.ConditionalComissDto;
import ru.it_alnc.packagesearch.exception.NonExistantConditionalSettingException;
import ru.it_alnc.packagesearch.exception.NonSuitableConditionalSettingException;
import ru.it_alnc.packagesearch.query.conditionalcomiss.ConditionalComissQueryBuilder;
import ru.it_alnc.packagesearch.query.conditionalcomiss.ConditionalComissReader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConditionalComissService {

    private final ConditionalComissReader conditionalComissReader;

    public ConditionalComisResponseDto findConditionalComisSettings(ConditionalComisRequestDto conditionalComisRequestDto) throws NonExistantConditionalSettingException, NonSuitableConditionalSettingException {
        ConditionalComissQueryBuilder queryBuilder = conditionalComissReader.getConditionalComissQueryBuilder(conditionalComisRequestDto);
        // Применяем фильтры
        if (conditionalComisRequestDto.getCalculationDate() != null) {
            queryBuilder.filterSettingsByDate(conditionalComisRequestDto.getCalculationDate());
        }
        if(queryBuilder.isEmpty())
            throw new NonExistantConditionalSettingException(conditionalComisRequestDto.getServiceCodes().toString());

        if (conditionalComisRequestDto.getCardType() != null) {
            queryBuilder.filterSettingsByCardType(conditionalComisRequestDto.getCardType());
        }
        if (conditionalComisRequestDto.getCardContractType() != null) {
            queryBuilder.filterSettingsByCardContractType(conditionalComisRequestDto.getCardContractType());
        }
        if(queryBuilder.isEmpty())
            throw new NonSuitableConditionalSettingException(conditionalComisRequestDto.getServiceCodes().toString());
        return createConditionalComisResponse(queryBuilder, conditionalComisRequestDto);
    }

    private ConditionalComisResponseDto createConditionalComisResponse(ConditionalComissQueryBuilder comissQueryBuilder, ConditionalComisRequestDto requestDto) {
        ConditionalComisResponseDto responseDto = new ConditionalComisResponseDto();
        responseDto.setCalculationDate(requestDto.getCalculationDate());
        
        // Преобразуем Map в List<ConditionalComissDto>
        List<ConditionalComissDto> serviceList = comissQueryBuilder.getConditionalComissDtoMap().entrySet().stream()
                .map(entry -> {
                    ConditionalComissDto dto = new ConditionalComissDto();
                    dto.setService(entry.getKey());
                    dto.setSettings(entry.getValue() == null ? new ArrayList<>() : entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
        responseDto.setServiceList(serviceList);
        return responseDto;
    }



}
