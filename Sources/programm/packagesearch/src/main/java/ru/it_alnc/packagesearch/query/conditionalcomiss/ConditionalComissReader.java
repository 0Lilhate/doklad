/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.query.conditionalcomiss;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.it_alnc.packagesearch.dto.conditionalcomiss.ConditionalComisRequestDto;
import ru.it_alnc.packagesearch.dto.conditionalcomiss.ConditionalCommissSettingsDto;
import ru.it_alnc.packagesearch.entity.conditionalcomiss.ConditionalComisSettingsEntity;
import ru.it_alnc.packagesearch.repository.ConditionalComisSettingsRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConditionalComissReader {

    private final ConditionalComisSettingsRepository conditionalComisSettingsRepository;

    //Мапа, содержащая в себе кеш настроек по коду услуги
    private Map<String, List<ConditionalCommissSettingsDto>> conditionalComissDtoMap;


    public void updateConditionalComissDtoList() {
        log.info("Update of conditional comiss cache started");
        var conditionalComissMap = new HashMap<String, List<ConditionalCommissSettingsDto>>();
        var conditionalComissEntityList = conditionalComisSettingsRepository.findAllApproved();
        if (conditionalComissEntityList.isEmpty()) {
            this.conditionalComissDtoMap = conditionalComissMap;
            log.info("Update of conditional comiss cache finished with empty result");
            return;
        }
        this.conditionalComissDtoMap = toDtoMap(conditionalComissEntityList);
        log.info("Update of conditional comiss cache finished");
    }

    public ConditionalComissQueryBuilder getConditionalComissQueryBuilder(ConditionalComisRequestDto requestDto) {
        ConditionalComissQueryBuilder builder = new ConditionalComissQueryBuilder();
        if(conditionalComissDtoMap == null)
            updateConditionalComissDtoList();

        Stream<Map.Entry<String, List<ConditionalCommissSettingsDto>>> streamForCopy = conditionalComissDtoMap.entrySet()
                .stream();
        if(requestDto.getServiceCodes() != null && !requestDto.getServiceCodes().isEmpty())
                streamForCopy = streamForCopy.filter(entry -> requestDto.getServiceCodes().contains(entry.getKey()));

        var calcDate = requestDto.getCalculationDate();

        var deepCopy = streamForCopy.collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().stream()
                        // фильтруем по дате, если она задана
                        .filter(dto -> {
                            if (calcDate == null) return true;
                            return (dto.getBegDate() == null || !dto.getBegDate().isAfter(calcDate)) &&
                                   (dto.getEndDate() == null || dto.getEndDate().isAfter(calcDate));
                        })
                        .map(ConditionalCommissSettingsDto::clone)
                        .collect(Collectors.toList())
        ));
        // Добавляем отсутствующие в мапе услуги из запроса, чтобы они шли с пустым списком настроек
        if (requestDto.getServiceCodes() != null)
            requestDto.getServiceCodes().forEach(code -> deepCopy.computeIfAbsent(code, k -> new ArrayList<>()));

        builder.setConditionalComissDtoMap(deepCopy);
        return builder;
    }


    public Map<String, List<ConditionalCommissSettingsDto>> toDtoMap(List<ConditionalComisSettingsEntity> conditionalComisSettingsEntityList) {
        return conditionalComisSettingsEntityList.stream()
                .collect(Collectors.groupingBy(e -> e.getServiceGuideItemEntity().getCode(),
                        Collectors.mapping(this::toSettingsDto, Collectors.toList())));

    }

    private ConditionalCommissSettingsDto toSettingsDto(ConditionalComisSettingsEntity ent) {
        ConditionalCommissSettingsDto dto = new ConditionalCommissSettingsDto();
        dto.setBegDate(ent.getBegDate() == null ? null : ent.getBegDate().atZone(ZoneId.systemDefault()));
        dto.setEndDate(ent.getEndDate() == null ? null : ent.getEndDate().atZone(ZoneId.systemDefault()));
        var cardTypeList = new ArrayList<ConditionalCommissSettingsDto.ParamMap>();
        if (ent.getCardTypevalueTypeEntity() != null) {
            var paramMap =  new ConditionalCommissSettingsDto.ParamMap();
            paramMap.setCode(ent.getCardType());
            paramMap.setValueType(ent.getCardTypevalueTypeEntity().getCode());
            cardTypeList.add(paramMap);
        }
        dto.setCardTypeList(cardTypeList);

        var cardContractList = new ArrayList<ConditionalCommissSettingsDto.ParamMap>();
        if (ent.getCardContractvalueTypeEntity() != null) {
            var paramMap =  new ConditionalCommissSettingsDto.ParamMap();
            paramMap.setCode(ent.getCardContractType());
            paramMap.setValueType(ent.getCardContractvalueTypeEntity().getCode());
            cardContractList.add(paramMap);
        }
        dto.setCardContractTypeList(cardContractList);
        return dto;
    }
}
