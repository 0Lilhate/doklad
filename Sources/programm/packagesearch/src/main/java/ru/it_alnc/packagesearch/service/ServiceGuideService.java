/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.it_alnc.packagesearch.dto.subscribe.SearchServiceGuideDto;
import ru.it_alnc.packagesearch.dto.subscribe.ServiceGuideDto;
import ru.it_alnc.packagesearch.dto.subscribe.ServiceGuideItemDto;
import ru.it_alnc.packagesearch.entity.subscribe.ServiceGuideEntity29735;
import ru.it_alnc.packagesearch.repository.ServiceGuideDao;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ServiceGuideService {
    @Autowired
    private ServiceGuideDao serviceGuideDao;

    public List<ServiceGuideDto> searchSGs(SearchServiceGuideDto serviceGuideRequestDto) {
        if (serviceGuideRequestDto.getClientCommonTypeList().contains("P"))
            serviceGuideRequestDto.getClientCommonTypeList().add("U");

        List<ServiceGuideDto> initList = findActiveSGs();
        initList = filterByClientCommonType(initList, serviceGuideRequestDto.getClientCommonTypeList());
        if (serviceGuideRequestDto.getServiceGuideList() != null && !serviceGuideRequestDto.getServiceGuideList().isEmpty())
            initList = filterByServiceGuideCodes(initList, serviceGuideRequestDto.getServiceGuideList());
        if(serviceGuideRequestDto.getServiceTypeList() != null && !serviceGuideRequestDto.getServiceTypeList().isEmpty())
            initList = filterByServiceGuideItemTypes(initList,serviceGuideRequestDto.getServiceTypeList());
        if(serviceGuideRequestDto.getServiceGuideItemList() !=null && !serviceGuideRequestDto.getServiceGuideItemList().isEmpty())
            initList = filterByServiceGuideItemCodes(initList, serviceGuideRequestDto.getServiceGuideItemList());
        return initList;
    }

    private List<ServiceGuideDto> findActiveSGs() {
        List<ServiceGuideEntity29735> rawSGs = serviceGuideDao.getAllActiveServiceGuides(); //todo: пока пусть лазит
        List<ServiceGuideDto> result = new ArrayList<>();
        int resultSize = rawSGs.size();
        for (int i = 0; i < rawSGs.size(); i++) {
            ServiceGuideDto serviceGuideDto = new ServiceGuideDto();
            serviceGuideDto.setId(rawSGs.get(i).getServiceGuideId());
            serviceGuideDto.setClientCommonType(rawSGs.get(i).getServiceGuideClientCommonType());
            serviceGuideDto.setCode(rawSGs.get(i).getServiceGuideCode());
            serviceGuideDto.setLabel(rawSGs.get(i).getServiceGuideLabel());
            List<ServiceGuideItemDto> serviceGuideItemDtos = mapServiceGuideItem(rawSGs.subList(i, resultSize));
            serviceGuideDto.setServiceGuideItemList(serviceGuideItemDtos);
            result.add(serviceGuideDto);
            i = i + serviceGuideItemDtos.size() - 1;
        }
        return result;
    }

    private List<ServiceGuideDto> filterByClientCommonType(List<ServiceGuideDto> serviceGuideDtoList, List<String> clientCommonTypes) {
        return serviceGuideDtoList.stream().filter(serviceGuideDto -> clientCommonTypes.contains(serviceGuideDto.getClientCommonType()))
                .collect(Collectors.toList());
    }

    private List<ServiceGuideDto> filterByServiceGuideCodes(List<ServiceGuideDto> serviceGuideDtoList, List<String> serviceGuideCodes) {
        return serviceGuideDtoList.stream().filter(serviceGuideDto -> serviceGuideCodes.contains(serviceGuideDto.getCode()))
                .collect(Collectors.toList());
    }

    private List<ServiceGuideDto> filterByServiceGuideItemTypes(List<ServiceGuideDto> serviceGuideDtoList, List<String> serviceTypes) {
        List<ServiceGuideDto> result = serviceGuideDtoList.stream()
                .filter(serviceGuideDto -> serviceGuideDto.getServiceGuideItemList().stream()
                        .anyMatch(serviceGuideItemDto -> serviceTypes.contains(serviceGuideItemDto.getServiceType()))
                ).collect(Collectors.toList());
        result.stream().forEach(serviceGuideDto -> {
            List<ServiceGuideItemDto> filteredSGIDs = serviceGuideDto.getServiceGuideItemList().stream()
                    .filter(serviceGuideItemDto -> serviceTypes.contains(serviceGuideItemDto.getServiceType()))
                    .collect(Collectors.toList());
            serviceGuideDto.setServiceGuideItemList(filteredSGIDs);
        });
        return result;
    }

    private List<ServiceGuideDto> filterByServiceGuideItemCodes(List<ServiceGuideDto> serviceGuideDtoList, List<String> serviceCodes) {
        List<ServiceGuideDto> result = serviceGuideDtoList.stream()
                .filter(serviceGuideDto -> serviceGuideDto.getServiceGuideItemList().stream()
                        .anyMatch(serviceGuideItemDto -> serviceCodes.contains(serviceGuideItemDto.getCode()))
                ).collect(Collectors.toList());
        result.stream().forEach(serviceGuideDto -> {
            List<ServiceGuideItemDto> filteredSGIDs = serviceGuideDto.getServiceGuideItemList().stream()
                    .filter(serviceGuideItemDto -> serviceCodes.contains(serviceGuideItemDto.getCode()))
                    .collect(Collectors.toList());
            serviceGuideDto.setServiceGuideItemList(filteredSGIDs);
        });
        return result;
    }

    private List<ServiceGuideItemDto> mapServiceGuideItem(List<ServiceGuideEntity29735> serviceGuideEntities) {
        List<ServiceGuideItemDto> result = new ArrayList<>();
        for (ServiceGuideEntity29735 serviceGuideEntity29735 : serviceGuideEntities) {
            if (!serviceGuideEntity29735.getServiceGuideId().equals(serviceGuideEntities.get(0).getServiceGuideId()))
                return result;
            ServiceGuideItemDto serviceGuideItemDto = new ServiceGuideItemDto();
            serviceGuideItemDto.setClientCommonType(serviceGuideEntity29735.getClientCommonType());
            serviceGuideItemDto.setCode(serviceGuideEntity29735.getCode());
            serviceGuideItemDto.setLabel(serviceGuideEntity29735.getLabel());
            serviceGuideItemDto.setIndexСode(serviceGuideEntity29735.getIndexCode());
            serviceGuideItemDto.setServiceType(serviceGuideEntity29735.getServiceType());
            serviceGuideItemDto.setBegDate(ZonedDateTime.of(serviceGuideEntity29735.getBegDate(), ZoneId.systemDefault()));
            serviceGuideItemDto.setEndDate(ZonedDateTime.of(serviceGuideEntity29735.getEndDate(), ZoneId.systemDefault()));
            serviceGuideItemDto.setSortOrder(serviceGuideEntity29735.getSortOrder());
            serviceGuideItemDto.setIsMultyCopiesCalcAllowed(Objects.equals(serviceGuideEntity29735.getIsMultyCopiesCalcAllowed(), 1));
            result.add(serviceGuideItemDto);
        }
        return result;
    }

}
