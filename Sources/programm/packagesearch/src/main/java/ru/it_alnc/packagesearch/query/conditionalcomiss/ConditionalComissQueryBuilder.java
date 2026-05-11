/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.query.conditionalcomiss;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.it_alnc.packagesearch.dto.conditionalcomiss.ConditionalComisRequestDto;
import ru.it_alnc.packagesearch.dto.conditionalcomiss.ConditionalComisResponseDto;
import ru.it_alnc.packagesearch.dto.conditionalcomiss.ConditionalCommissSettingsDto;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConditionalComissQueryBuilder {

    //Мапа, содержащая в себе кеш настроек по коду услуги
    @Setter
    @Getter
    private Map<String, List<ConditionalCommissSettingsDto>> conditionalComissDtoMap;

    //фильтруем списки с настройками по дате (должны быть активными на дату)
    public void filterSettingsByDate(ZonedDateTime date){
        conditionalComissDtoMap.replaceAll(
                (k, v) -> v.stream().filter(dto ->
                                (dto.getBegDate().isBefore(date) || dto.getBegDate().isEqual(date)) && dto.getEndDate().isAfter(date))
                        .collect(Collectors.toCollection(ArrayList::new)));
    }

    //Метод для фильтрации списков ParamMap по содержанию значения в code
    private List<ConditionalCommissSettingsDto.ParamMap> filterParamMapListByString(List<ConditionalCommissSettingsDto.ParamMap> paramMapList, String code){
        return paramMapList.stream().filter(
                dto -> {
                    var splittedCode = Arrays.asList(dto.getCode().split(";"));
                    return splittedCode.contains(code);
                }
        ).collect(Collectors.toCollection(ArrayList::new));
    }

    public void filterSettingsByCardType(String cardType) {
        conditionalComissDtoMap.replaceAll((k, v) ->
                {
                    if( v == null )
                        return null;
                    List<ConditionalCommissSettingsDto> filteredList = v.stream()
                            .filter(dto -> dto.getCardTypeList().stream()
                                    .anyMatch(paramMap -> filterByDynamicParams(paramMap, cardType)))
                            .toList();
                    return filteredList.isEmpty() ? null : filteredList;
                }
                );
    }
    public void filterSettingsByCardContractType(String cardContractType) {
        conditionalComissDtoMap.replaceAll((k, v) ->
                {
                    if( v == null )
                        return null;
                    List<ConditionalCommissSettingsDto> filteredList = v.stream()
                            .filter(dto -> dto.getCardContractTypeList().stream()
                                    .anyMatch(paramMap -> filterByDynamicParams(paramMap, cardContractType)))
                            .toList();
                    return filteredList.isEmpty() ? null : filteredList;
                }
        );
    }

    private boolean filterByDynamicParams(ConditionalCommissSettingsDto.ParamMap paramMap, String requestParams) {
        if (paramMap == null) return false;

        String value = paramMap.getValueType();
        String code = paramMap.getCode();

        if (value == null || value.isBlank() || code == null || code.isBlank()) return false;

        Set<String> targetParams = Arrays.stream(code.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        Set<String> requestParamSet = (requestParams == null || requestParams.isBlank())
                ? Collections.emptySet()
                : Arrays.stream(requestParams.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        return switch (value) {
            case "IN" -> requestParamSet.isEmpty() || targetParams.containsAll(requestParamSet);
            case "NOT_IN" -> requestParamSet.isEmpty() || !targetParams.containsAll(requestParamSet);
            default -> throw new IllegalArgumentException("Unexpected compare param: " + value);
        };
    }

    public Boolean isEmpty(){
        if(conditionalComissDtoMap == null || conditionalComissDtoMap.isEmpty())
            return true;
        return conditionalComissDtoMap.values().stream().allMatch(list -> list == null || list.isEmpty());
    }
}
