/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.query.graceperiod;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.it_alnc.packagesearch.dto.graceperiod.GracePeriodDto;
import ru.it_alnc.packagesearch.dto.subscribe.SubscribeFilterParamDto;
import ru.it_alnc.packagesearch.entity.graceperiod.GracePeriodEntity;
import ru.it_alnc.packagesearch.entity.subscribe.ParamValueEntity;
import ru.it_alnc.packagesearch.repository.GracePeriodGuideLineRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Slf4j
public class GracePeriodReader {

    @Autowired
    private GracePeriodGuideLineRepository gracePeriodGuideLineRepository;

    @Getter
    private List<GracePeriodDto> gracePeriodDtoList;

    @Getter
    private LocalDateTime cacheReadingTime;

    @Transactional(readOnly = true)
    public void readAllGracePeriods(LocalDateTime readDateTime) {
        List<GracePeriodEntity> gracePeriodEntityList = gracePeriodGuideLineRepository.getActiveGracePediods(readDateTime);
        List<GracePeriodDto> protoGPList = new ArrayList<>();
        for (GracePeriodEntity gracePeriodEntity : gracePeriodEntityList) {
            protoGPList.add(mapGracePeriod(gracePeriodEntity));
        }
        this.gracePeriodDtoList = protoGPList;
        log.info("grace periods cache read on date {}",readDateTime.toString());
    }

    private GracePeriodDto mapGracePeriod(GracePeriodEntity gracePeriodEntity) {
        GracePeriodDto gracePeriodDto = new GracePeriodDto();
        gracePeriodDto.setGpId(gracePeriodEntity.getId());
        gracePeriodDto.setClientCommonType(gracePeriodEntity.getClientCommonType());
        gracePeriodDto.setService(gracePeriodEntity.getServiceGuideItemEntity().getCode());
        gracePeriodDto.setBegDate(ZonedDateTime.of(gracePeriodEntity.getBegDate(), ZoneId.systemDefault()));
        gracePeriodDto.setEndDate(ZonedDateTime.of(gracePeriodEntity.getEndDate(), ZoneId.systemDefault()));
        gracePeriodDto.setGracePeriod(gracePeriodEntity.getGracePeriod());
        gracePeriodDto.setDefaultPaymentDuration(gracePeriodEntity.getDefaultPaymentDuration());
        List<SubscribeFilterParamDto> paramList = new ArrayList<>();
        for (ParamValueEntity paramValueEntity : gracePeriodEntity.getParamValueList()) {
            paramList.add(convertFilterParam(paramValueEntity));
        }
        gracePeriodDto.setParamList(paramList);
        return gracePeriodDto;
    }

    //да, это копия метода из SubscribeReader
    private SubscribeFilterParamDto convertFilterParam(ParamValueEntity filterParamEntity) {
        SubscribeFilterParamDto result = new SubscribeFilterParamDto();
        //result.setCompareType(filterParamEntity.getValueTypeEntity().getCode());
        result.setOrder(filterParamEntity.getSortOrder());
        result.setCode(filterParamEntity.getParam().getObjectType() + "." + filterParamEntity.getParam().getCode());
        result.setLabel(filterParamEntity.getParam().getLabel());
        result.setType(filterParamEntity.getValueTypeEntity().getGuideType());
        result.setVType(filterParamEntity.getValueTypeEntity().getCode());
        result.setVTypeLabel(filterParamEntity.getValueTypeEntity().getLabel());
        result.setGuide(filterParamEntity.getParam().getGuideEntity().getServiceGuide());
        result.setIsGroupGuide(filterParamEntity.getParam().getGuideEntity().getIsGroupGuide());
        if (filterParamEntity.getValueBoolean() != null && filterParamEntity.getValueBoolean() != 2)
            result.setVBoolean(filterParamEntity.getValueBoolean() == 1);
        result.setVString(filterParamEntity.getValueString());
        result.setVDate(mapToZonedDateTime(filterParamEntity.getValueDate()));
        result.setVDateMin(mapToZonedDateTime(filterParamEntity.getValueDateMin()));
        result.setVDateMax(mapToZonedDateTime(filterParamEntity.getValueDateMax()));
        result.setVInt(filterParamEntity.getValueInt());
        result.setVIntMin(filterParamEntity.getValueIntMin());
        result.setVIntMax(filterParamEntity.getValueIntMax());
        result.setVFloat(filterParamEntity.getValueFloat());
        result.setVFloatMin(filterParamEntity.getValueFloatMin());
        result.setVFloatMax(filterParamEntity.getValueFloatMax());
        //todo - мб перегнать в list сразу? в ответ все равно не попадает
        // вместе с *(1)
        result.setVCodePattern(filterParamEntity.getCodePattern());
        result.setVCodeList(filterParamEntity.getCodeList());
        result.setVLabelList(filterParamEntity.getLabelList());
        return result;
    }

    private ZonedDateTime mapToZonedDateTime(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(ldt -> ZonedDateTime.of(ldt, ZoneId.systemDefault()))
                .orElse(null);
    }
}
