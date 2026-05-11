/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.query.subscribe;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.it_alnc.packagesearch.dto.FL.BegEndDatesDto;
import ru.it_alnc.packagesearch.dto.FL.FLLightSubscribeDto;
import ru.it_alnc.packagesearch.dto.FL.LightRequestDto;
import ru.it_alnc.packagesearch.dto.checkInfo.CheckInfoRequestDto;
import ru.it_alnc.packagesearch.dto.subscribe.CheckSetLineDto;
import ru.it_alnc.packagesearch.dto.subscribe.CheckedGraceBySourceDto;
import ru.it_alnc.packagesearch.dto.subscribe.IndividualTariffDto27277;
import ru.it_alnc.packagesearch.dto.subscribe.IndividualTariffRequest;
import ru.it_alnc.packagesearch.dto.subscribe.LightSubscribeRequestDto;
import ru.it_alnc.packagesearch.dto.subscribe.PackageListRequestDto;
import ru.it_alnc.packagesearch.dto.subscribe.PuParamDto;
import ru.it_alnc.packagesearch.dto.subscribe.ServiceGroupDto;
import ru.it_alnc.packagesearch.dto.subscribe.ServiceLineDto;
import ru.it_alnc.packagesearch.dto.subscribe.SubscribeDto27277;
import ru.it_alnc.packagesearch.dto.subscribe.SubscribeFilterParamDto;
import ru.it_alnc.packagesearch.dto.subscribe.SubscribeRequest27277;
import ru.it_alnc.packagesearch.dto.subscribe.SubscribeTariffDto;
import ru.it_alnc.packagesearch.dto.subscribe.SubscribeTariffLineDto;
import ru.it_alnc.packagesearch.dto.subscribe.VersionDto;
import ru.it_alnc.packagesearch.exception.NonExistPUException;
import ru.it_alnc.packagesearch.exception.NonExistServiceException;
import ru.it_alnc.packagesearch.repository.ViewDao;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@ConditionalOnBean(SubscribeReaderInitializer.class)
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SubscribeQueryBuilder {
    @Autowired
    private ViewDao viewDao;
    private LocalDateTime targetDate = LocalDateTime.now();
    @Getter
    private List<SubscribeDto27277> subscribes;
    @Getter
    private List<IndividualTariffDto27277> tariffs;

    @Getter
    private List<ServiceLineDto> serviceLines;

    @Autowired
    private SubscribeReader subscribeReader;

    public static final String DATETYPE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";


    public void init(SubscribeRequest27277 request27277) throws CloneNotSupportedException {
        initSubscribes(request27277);
        if (request27277.isGetIndividualTariff() || (request27277.getIndividualTariffCodes() != null && !request27277.getIndividualTariffCodes().isEmpty())) {
            initIndividualTariffs(request27277);
        }
        if (request27277.getServices() != null && !request27277.getServices().isEmpty())
            initServiceLines(request27277.getServices());
        else
            initServiceLines();

        if (request27277.getSearchOnDate() != null) {

            if (request27277.getSearchOnDate().isBefore(OffsetDateTime.now())) {
                initOnArchiveDate(request27277.getSearchOnDate());
            } else {
                initOnRequestDate(request27277.getSearchOnDate());
            }
        } else {
            initOnRequestDate(null);
        }
    }


    public void init(LightRequestDto requestDto) throws CloneNotSupportedException {
        initSubscribes();
        initServiceLines();
        filterForLightSearch();
    }

    public void init(CheckInfoRequestDto requestDto) throws CloneNotSupportedException {
        SubscribeRequest27277 subscribeRequest27277 = new SubscribeRequest27277();
        subscribeRequest27277.setPackageCodes(requestDto.getPackageCodes());
        subscribeRequest27277.setSearchOnDate(requestDto.getSearchOnDate());
        initSubscribes(subscribeRequest27277);
        initServiceLines(requestDto.getService());

        if (requestDto.getSearchOnDate() != null) {

            if (requestDto.getSearchOnDate().isBefore(OffsetDateTime.now())) {
                initOnArchiveDate(requestDto.getSearchOnDate());
            } else {
                initOnRequestDate(requestDto.getSearchOnDate());
            }
        } else {
            initOnRequestDate(null);
        }
    }


    public void init(IndividualTariffRequest individualTariffRequest) throws CloneNotSupportedException {
        initIndividualTariffs(individualTariffRequest);
        initOnRequestDate(null);

    }

    public void init(PackageListRequestDto packageListRequestDto) throws CloneNotSupportedException {
        initSubscribes();
    }

    public void init(LightSubscribeRequestDto lightSubscribeRequestDto) throws CloneNotSupportedException {
        initSubscribes();
        initServiceLines();
        if(lightSubscribeRequestDto.getIndividualTariff() != null &&
                lightSubscribeRequestDto.getIndividualTariff().getGetIndividualTariff() != null &&
                lightSubscribeRequestDto.getIndividualTariff().getGetIndividualTariff())
            initIndividualTariffs();
    }


    public void initIndividualTariffs(SubscribeRequest27277 request27277) throws CloneNotSupportedException {
        if (subscribeReader.getAllActiveTariffs() == null)
            subscribeReader.readAllTariffs();
        List<IndividualTariffDto27277> tariffs = new ArrayList<>();
        if (request27277.getIndividualTariffCodes() != null && !request27277.getIndividualTariffCodes().isEmpty()) {
            List<IndividualTariffDto27277> filteredTariffs = subscribeReader.getAllActiveTariffs().stream().filter(
                    individualTariffDto27277 -> request27277.getIndividualTariffCodes().contains(individualTariffDto27277.getCode())
            ).toList();
            for (IndividualTariffDto27277 individualTariffDto : filteredTariffs) {
                tariffs.add(individualTariffDto.clone());
            }
            this.tariffs = tariffs;
        } else {
            for (IndividualTariffDto27277 individualTariffDto : this.subscribeReader.getAllActiveTariffs()) {
                tariffs.add(individualTariffDto.clone());
            }
            this.tariffs = tariffs;
        }

        if (request27277.getSearchOnDate() == null) {
            filterTariffsByDate(OffsetDateTime.now());
        } else {
            filterTariffsByDate(request27277.getSearchOnDate());
        }

    }

    public void initIndividualTariffs(IndividualTariffRequest individualTariffRequest) throws CloneNotSupportedException {
        if (subscribeReader.getAllActiveTariffs() == null)
            subscribeReader.readAllTariffs();
        List<IndividualTariffDto27277> tariffs = new ArrayList<>();
        if (individualTariffRequest.getIndividualTariffCodes() != null && !individualTariffRequest.getIndividualTariffCodes().isEmpty()) {
            List<IndividualTariffDto27277> filteredTariffs = subscribeReader.getAllActiveTariffs().stream().filter(
                    individualTariffDto27277 -> individualTariffRequest.getIndividualTariffCodes().contains(individualTariffDto27277.getCode())
            ).toList();
            for (IndividualTariffDto27277 individualTariffDto : filteredTariffs) {
                tariffs.add(individualTariffDto.clone());
            }
            this.tariffs = tariffs;
        } else {
            for (IndividualTariffDto27277 individualTariffDto : this.subscribeReader.getAllActiveTariffs()) {
                tariffs.add(individualTariffDto.clone());
            }
            this.tariffs = tariffs;
        }
        filterTariffsByDate(OffsetDateTime.now());

    }
    public void initIndividualTariffs() throws CloneNotSupportedException {
        if (subscribeReader.getAllActiveTariffs() == null)
            subscribeReader.readAllTariffs();
        List<IndividualTariffDto27277> tempTariffs = new ArrayList<>();
        for (IndividualTariffDto27277 individualTariffDto : subscribeReader.getAllActiveTariffs()) {
            tempTariffs.add(individualTariffDto.clone());
        }
        this.tariffs = tempTariffs;
        filterTariffsByDate(OffsetDateTime.now());
    }

    public void initSubscribes(SubscribeRequest27277 request27277) throws CloneNotSupportedException {
        if (subscribeReader.getAllActiveSubscribes() == null)
            subscribeReader.readAllSubscribes();
        List<SubscribeDto27277> subscribeDto27277s = new ArrayList<>();
        if (request27277.getPackageCodes() != null && !request27277.getPackageCodes().isEmpty()) {
            List<SubscribeDto27277> filteredSubs = this.subscribeReader.getAllActiveSubscribes().stream().filter(
                    subscribeDto27277 -> request27277.getPackageCodes().contains(subscribeDto27277.getCode()) ||
                            request27277.getClientPUCodesAsList().contains(subscribeDto27277.getCode())
            ).toList();
            for (SubscribeDto27277 subscribeDto27277 : filteredSubs) {
                subscribeDto27277s.add(subscribeDto27277.clone());
            }
            this.subscribes = subscribeDto27277s;
        } else {
            for (SubscribeDto27277 subscribeDto27277 : this.subscribeReader.getAllActiveSubscribes()) {
                subscribeDto27277s.add(subscribeDto27277.clone());
            }
            this.subscribes = subscribeDto27277s;
        }


    }

    public void initSubscribes() throws CloneNotSupportedException {
        if (subscribeReader.getAllActiveSubscribes() == null)
            subscribeReader.readAllSubscribes();
        List<SubscribeDto27277> subscribeDto27277s = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto27277 : this.subscribeReader.getAllActiveSubscribes()) {
            subscribeDto27277s.add(subscribeDto27277.clone());
        }
        this.subscribes = subscribeDto27277s;
    }

    public void initServiceLines() throws CloneNotSupportedException { //метод на случай если нужно будет клонировать также как подписки и тарифы
        if (subscribeReader.getAllActiveSLs() == null)
            subscribeReader.readAllSLs();
        List<ServiceLineDto> serviceLineDtos = new ArrayList<>();
        for (ServiceLineDto serviceLineDto : subscribeReader.getAllActiveSLs()) {
            serviceLineDtos.add(serviceLineDto.clone());
        }
        this.serviceLines = serviceLineDtos;
    }

    public void initServiceLines(List<String> serviceLineCodes) throws CloneNotSupportedException { //метод на случай если нужно будет клонировать также как подписки и тарифы
        if (subscribeReader.getAllActiveSLs() == null)
            subscribeReader.readAllSLs();
        List<ServiceLineDto> serviceLineDtos = new ArrayList<>();
        List<ServiceLineDto> tempServiceLineList = this.subscribeReader.getAllActiveSLs().stream().filter(
                serviceLineDto -> serviceLineCodes.contains(serviceLineDto.getService())
        ).collect(Collectors.toCollection(ArrayList::new));
        for (ServiceLineDto serviceLineDto : tempServiceLineList) {
            serviceLineDtos.add(serviceLineDto.clone());
        }
        this.serviceLines = serviceLineDtos;
    }

    public void initServiceLines(String serviceLineCode) throws CloneNotSupportedException { //метод на случай если нужно будет клонировать также как подписки и тарифы
        if (subscribeReader.getAllActiveSLs() == null)
            subscribeReader.readAllSLs();
        List<ServiceLineDto> serviceLineDtos = new ArrayList<>();
        List<ServiceLineDto> tempServiceLineList = this.subscribeReader.getAllActiveSLs().stream().filter(
                serviceLineDto -> serviceLineCode.equals(serviceLineDto.getService())
        ).collect(Collectors.toCollection(ArrayList::new));
        for (ServiceLineDto serviceLineDto : tempServiceLineList) {
            serviceLineDtos.add(serviceLineDto.clone());
        }
        this.serviceLines = serviceLineDtos;
    }


    public void initOnRequestDate(OffsetDateTime targetDate) throws CloneNotSupportedException {
        if (targetDate != null) {
            filterByDate(targetDate);
            return;
        }
        filterByDate(OffsetDateTime.now());
    }

    public void initOnArchiveDate(OffsetDateTime targetDate) throws CloneNotSupportedException {
        log.debug("init sqb on archive date!");
        this.subscribes = subscribeReader.readAllSubscribesSingleRequest(targetDate);
        this.tariffs = subscribeReader.readAllTariffsSingleRequest(targetDate.toLocalDateTime());
        this.serviceLines = subscribeReader.readAllSLsSingleRequest(targetDate.toLocalDateTime());
        filterByDate(targetDate);
        filterTariffsByDate(targetDate);
        log.info("Done cache reading");
        log.debug("Sizes of subs, tariffs, SLs : {}, {}, {}, ", this.subscribes.size(), this.tariffs.size(), this.serviceLines.size());
    }

    //30286 check for existing ClientPUCode from request, throw exception if there is no such code
    //32316 checking list of codes now, throw exception if all is non-existing
    public void checkClientPuCodes(List<String> clientPuCodes) throws NonExistPUException {
        Optional<SubscribeDto27277> clientPU = subscribes.stream()     //todo: проверить что это вообще работает
                .filter(subscribeDto27277 -> clientPuCodes.contains(subscribeDto27277.getCode()))
                .findFirst();
        //log.debug("дёрнули базу чтобы узнать существует ли PUCodes");
        if (clientPU.isEmpty()) {
            throw new NonExistPUException(clientPuCodes.toString());
        }
    }

    //30378 check for existing service codes from request, throw exception if there are no any matching codes
    public void checkServiceCodes(List<String> serviceCodes) throws NonExistServiceException {
        Optional<ServiceLineDto> serviceLineWithCode = this.serviceLines.stream().filter(
                serviceLineDto -> serviceCodes.contains(serviceLineDto.getService())
        ).findFirst();
        log.debug("checked services to actually exist");
        if (serviceLineWithCode.isEmpty()) {
            throw new NonExistServiceException(serviceCodes.toString());
        }
    }

    public List<ServiceLineDto> findAllByPu4PriceService(Long pu4PriceService, List<String> clientPUCodes, List<ServiceLineDto> filteredSLsTemp) { // sql запрос для наглядности
        String sql = """
                SELECT sl FROM ServiceLine27277Entity sl
                          JOIN ProductEntity          pe ON pe.id = sl.obj
                                                        AND pe.code IN (:codes)
                          JOIN ProductEntity          p2 ON p2.id = sl.pu4PriceService
                                                        AND p2.id = :pu4PriceService
                """;
        //log.debug("начали поиск по всем serviceLine");
        List<ServiceLineDto> filteredSLs = new ArrayList<>();
        //log.debug("    ВОШЛИ В FINDWITHPU");
        for (ServiceLineDto serviceLineDto : filteredSLsTemp) {
            //log.debug("pu4PriceService {}", serviceLineDto.getPu4PriceService());1
            Optional<SubscribeDto27277> filteredProducts = subscribes.stream()
                    .filter(subscribeDto27277 -> {
                        return Objects.equals(subscribeDto27277.getId(), (serviceLineDto.getObj())) &&
                                clientPUCodes.contains(subscribeDto27277.getCode());
                    })
                    //.toList();
                    .findFirst();
            if (filteredProducts.isEmpty())
                continue;

            Optional<SubscribeDto27277> filteredProducts2 = subscribes.stream()
                    .filter(subscribeDto27277 -> serviceLineDto.getPu4PriceServiceLong() != null &&
                            Objects.equals(subscribeDto27277.getId(), pu4PriceService) &&
                            Objects.equals(subscribeDto27277.getId(), serviceLineDto.getPu4PriceServiceLong())
                    ).findFirst();
            if (filteredProducts2.isEmpty())
                continue;
            filteredSLs.add(serviceLineDto);
        }
        //log.debug("закончили поиск по всем serviceLine условие 2, размер {}",filteredSLs.size());
        return filteredSLs;
    }

    public List<ServiceLineDto> findAllByPu4PriceService(Long pu4PriceService) { // sql запрос для наглядности
        String sql = """
                Select * 
                from serviceline sl 
                WHERE sl.pu4priceservice = :pu4PriceService 
                AND sl.objTable = 'PRODUCT'
                """;
        //log.debug("начали поиск по всем serviceLine");
        List<ServiceLineDto> filteredSLs;
        filteredSLs = serviceLines.stream()
                .filter(serviceLineDto -> serviceLineDto.getPu4PriceServiceLong() != null &&
                        serviceLineDto.getPu4PriceServiceLong().equals(pu4PriceService) &&
                        "PRODUCT".equals(serviceLineDto.getObjTable()))
                .toList();
        //log.debug("закончили поиск по всем serviceLine, размер {}",filteredSLs.size());
        return filteredSLs;
    }

    //плевать на версии, плевать на puParamDto, главное нафильтровать serviceLines для подписки
    public void filterForLightSearch() throws CloneNotSupportedException { //
        if (this.subscribes != null) {
            this.subscribes = this.subscribes.stream()
                    .filter(subscribeDto27277 -> {
                        return subscribeDto27277.getEndDate().toOffsetDateTime().isAfter(OffsetDateTime.now());
                    })
                    .toList();

            for (SubscribeDto27277 subscribeDto27277 : this.subscribes) {
                for (ServiceGroupDto serviceGroupDto : subscribeDto27277.getServiceGroupList()) {   //todo: остановился тут, а зачем я вообще тут? чистить SL?
                    serviceGroupDto.setServiceLineList(serviceGroupDto.getServiceLineList().stream()
                            .filter(serviceLineDto -> (serviceLineDto.getEndDate().toLocalDateTime().isAfter(LocalDateTime.now()))).toList());
                    for (ServiceLineDto serviceLineDto : serviceGroupDto.getServiceLineList()) {
                        serviceLineDto.setTariffList(serviceLineDto.getTariffList().stream()
                                .filter(subscribeTariffDto -> (subscribeTariffDto.getEndDate().toLocalDateTime().isAfter(LocalDateTime.now()))).toList());
                        serviceLineDto.setCheckSetLineList(serviceLineDto.getCheckSetLineList().stream()
                                .filter(checkSetLineDto -> (checkSetLineDto.getEndDate().toLocalDateTime().isAfter(LocalDateTime.now()))).toList());
                    }
                }

            }
        }
        if (this.serviceLines != null) {
            List<ServiceLineDto> resultList = new ArrayList<>();
            List<ServiceLineDto> filteredList = this.serviceLines.stream()
                    .filter(serviceLineDto -> {
                        if (serviceLineDto.getEndDate().toOffsetDateTime().isAfter(OffsetDateTime.now())) {
                            return true;
                        } else {
                            return false;
                        }
                    })
                    .toList();
            for (ServiceLineDto serviceLineDto : filteredList) {
                resultList.add(serviceLineDto.clone());
            }
            this.serviceLines = resultList;
        }

    }

    public void filterByDate(OffsetDateTime ldt) throws CloneNotSupportedException { //todo: проверить
        if (this.subscribes != null) {
            this.subscribes = this.subscribes.stream()
                    .filter(subscribeDto27277 -> {
                        if ((subscribeDto27277.getBegDate().toOffsetDateTime().isBefore(ldt) || subscribeDto27277.getBegDate().toOffsetDateTime().equals(ldt)) &&
                                subscribeDto27277.getEndDate().toOffsetDateTime().isAfter(ldt))
                            return true;
                        else {
                            return false;
                        }
                    })
                    .toList();

            for (SubscribeDto27277 subscribeDto27277 : this.subscribes) {
                Optional<VersionDto> currentVersion = null;
                if (subscribeDto27277.getVersionList() != null) {
                    currentVersion = subscribeDto27277.getVersionList().stream().filter(
                            versionDto -> ((versionDto.getBeginDate().isBefore(ldt.toLocalDateTime()) ||
                                    versionDto.getBeginDate().isEqual(ldt.toLocalDateTime()))
                                    && versionDto.getEndDate().isAfter(ldt.toLocalDateTime()))
                    ).findFirst();
                }
                if (currentVersion == null || currentVersion.isEmpty()) continue;
                int version = currentVersion.get().getVersion();
                subscribeDto27277.setPuParamList(subscribeDto27277.getPuParamList().stream().filter(
                        puParamDto -> puParamDto.getVersion().intValue() == version
                ).toList());
                if (!subscribeDto27277.getPuParamList().isEmpty()) { //if тут не нужен
                    for (PuParamDto puParamDto : subscribeDto27277.getPuParamList()) {
                        puParamDto.setListOfGroupGuideDtos(puParamDto.getListOfGroupGuideDtos().stream().filter(
                                groupGuide -> (groupGuide.getBegDate().isBefore(ldt.toZonedDateTime()) ||
                                        groupGuide.getBegDate().isEqual(ldt.toZonedDateTime()))
                                        && groupGuide.getEndDate().isAfter(ldt.toZonedDateTime())
                        ).collect(Collectors.toCollection(ArrayList::new)));
                    }
                }
                if (!subscribeDto27277.getFilterParamList().isEmpty()) { //if тут не нужен
                    for (SubscribeFilterParamDto subscribeFilterParamDto : subscribeDto27277.getFilterParamList()) {
                        subscribeFilterParamDto.setListOfGroupGuideDtos(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().filter(
                                groupGuide -> (groupGuide.getBegDate().isBefore(ldt.toZonedDateTime()) ||
                                        groupGuide.getBegDate().isEqual(ldt.toZonedDateTime()))
                                        && groupGuide.getEndDate().isAfter(ldt.toZonedDateTime())
                        ).collect(Collectors.toCollection(ArrayList::new)));
                    }
                }
                for (ServiceGroupDto serviceGroupDto : subscribeDto27277.getServiceGroupList()) {   //todo: остановился тут, а зачем я вообще тут? чистить SL?
                    serviceGroupDto.setServiceLineList(serviceGroupDto.getServiceLineList().stream()
                            .filter(serviceLineDto -> (serviceLineDto.getBegDate().toLocalDateTime().isBefore(ldt.toLocalDateTime()) || serviceLineDto.getBegDate().toLocalDateTime().isEqual(ldt.toLocalDateTime())
                                    && serviceLineDto.getEndDate().toLocalDateTime().isAfter(ldt.toLocalDateTime()))).toList());
                    for (ServiceLineDto serviceLineDto : serviceGroupDto.getServiceLineList()) {
                        serviceLineDto.setTariffList(serviceLineDto.getTariffList().stream()
                                .filter(subscribeTariffDto -> (subscribeTariffDto.getBegDate().toLocalDateTime().isBefore(ldt.toLocalDateTime()) || subscribeTariffDto.getBegDate().toLocalDateTime().isEqual(ldt.toLocalDateTime()))
                                        && subscribeTariffDto.getEndDate().toLocalDateTime().isAfter(ldt.toLocalDateTime())).toList());
                        serviceLineDto.setCheckSetLineList(serviceLineDto.getCheckSetLineList().stream()
                                .filter(checkSetLineDto -> (checkSetLineDto.getBegDate().toLocalDateTime().isBefore(ldt.toLocalDateTime()) || checkSetLineDto.getBegDate().toLocalDateTime().isEqual(ldt.toLocalDateTime()))
                                        && checkSetLineDto.getEndDate().toLocalDateTime().isAfter(ldt.toLocalDateTime())).toList());
                        for (SubscribeFilterParamDto subscribeFilterParamDto : serviceLineDto.getFilterParams()) {
                            subscribeFilterParamDto.setListOfGroupGuideDtos(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().filter(
                                    groupGuide -> (groupGuide.getBegDate().isBefore(ldt.toZonedDateTime()) ||
                                            groupGuide.getBegDate().isEqual(ldt.toZonedDateTime()))
                                            && groupGuide.getEndDate().isAfter(ldt.toZonedDateTime())
                            ).collect(Collectors.toCollection(ArrayList::new)));
                        }
                        for (SubscribeTariffDto subscribeTariffDto : serviceLineDto.getTariffList()) {
                            for (SubscribeTariffLineDto tariffLineDto : subscribeTariffDto.getSubscribeTariffLineDtoList()) {
                                for (SubscribeFilterParamDto subscribeFilterParamDto : tariffLineDto.getFilterParams()) {
                                    subscribeFilterParamDto.setListOfGroupGuideDtos(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().filter(
                                            groupGuide -> (groupGuide.getBegDate().isBefore(ldt.toZonedDateTime()) ||
                                                    groupGuide.getBegDate().isEqual(ldt.toZonedDateTime()))
                                                    && groupGuide.getEndDate().isAfter(ldt.toZonedDateTime())
                                    ).collect(Collectors.toCollection(ArrayList::new)));
                                }

                            }
                        }
                        for (CheckSetLineDto checkSetLineDto : serviceLineDto.getCheckSetLineList()) {
                            for (SubscribeFilterParamDto subscribeFilterParamDto : checkSetLineDto.getFilterParams()) {
                                subscribeFilterParamDto.setListOfGroupGuideDtos(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().filter(
                                        groupGuide -> (groupGuide.getBegDate().isBefore(ldt.toZonedDateTime()) ||
                                                groupGuide.getBegDate().isEqual(ldt.toZonedDateTime()))
                                                && groupGuide.getEndDate().isAfter(ldt.toZonedDateTime())
                                ).collect(Collectors.toCollection(ArrayList::new)));
                            }
                        }
                    }
                }

            }
        }
        if (this.serviceLines != null) {
            List<ServiceLineDto> resultList = new ArrayList<>();
            List<ServiceLineDto> filteredList = this.serviceLines.stream()
                    .filter(serviceLineDto -> {
                        if ((serviceLineDto.getBegDate().toOffsetDateTime().isBefore(ldt) || serviceLineDto.getBegDate().toOffsetDateTime().equals(ldt)) &&
                                serviceLineDto.getEndDate().toOffsetDateTime().isAfter(ldt)) {
                            return true;
                        } else {
                            return false;
                        }
                    })
                    .toList();
            for (ServiceLineDto serviceLineDto : filteredList) {
                serviceLineDto.setTariffList(serviceLineDto.getTariffList().stream()
                        .filter(subscribeTariffDto -> (subscribeTariffDto.getBegDate().toLocalDateTime().isBefore(ldt.toLocalDateTime()) || subscribeTariffDto.getBegDate().toLocalDateTime().isEqual(ldt.toLocalDateTime()))
                                && subscribeTariffDto.getEndDate().toLocalDateTime().isAfter(ldt.toLocalDateTime())).toList());
                serviceLineDto.setCheckSetLineList(serviceLineDto.getCheckSetLineList().stream()
                        .filter(checkSetLineDto -> (checkSetLineDto.getBegDate().toLocalDateTime().isBefore(ldt.toLocalDateTime()) || checkSetLineDto.getBegDate().toLocalDateTime().isEqual(ldt.toLocalDateTime()))
                                && checkSetLineDto.getEndDate().toLocalDateTime().isAfter(ldt.toLocalDateTime())).toList());
                resultList.add(serviceLineDto.clone());
            }
            for (ServiceLineDto serviceLineDto : resultList) { //todo: это, скорее всего, вообще не нужно, но будем уж держать данные консистентными (если сильно влияет на время запроса убрать)
                serviceLineDto.setTariffList(serviceLineDto.getTariffList().stream()
                        .filter(subscribeTariffDto -> (subscribeTariffDto.getBegDate().toLocalDateTime().isBefore(ldt.toLocalDateTime()) || subscribeTariffDto.getBegDate().toLocalDateTime().isEqual(ldt.toLocalDateTime())
                                && subscribeTariffDto.getEndDate().toLocalDateTime().isAfter(ldt.toLocalDateTime()))).toList());
                serviceLineDto.setCheckSetLineList(serviceLineDto.getCheckSetLineList().stream()
                        .filter(checkSetLineDto -> (checkSetLineDto.getBegDate().toLocalDateTime().isBefore(ldt.toLocalDateTime()) || checkSetLineDto.getBegDate().toLocalDateTime().isEqual(ldt.toLocalDateTime()))
                                && checkSetLineDto.getEndDate().toLocalDateTime().isAfter(ldt.toLocalDateTime())).toList());
                for (SubscribeFilterParamDto subscribeFilterParamDto : serviceLineDto.getFilterParams()) {
                    subscribeFilterParamDto.setListOfGroupGuideDtos(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().filter(
                            groupGuide -> (groupGuide.getBegDate().isBefore(ldt.toZonedDateTime()) ||
                                    groupGuide.getBegDate().isEqual(ldt.toZonedDateTime()))
                                    && groupGuide.getEndDate().isAfter(ldt.toZonedDateTime())
                    ).collect(Collectors.toCollection(ArrayList::new)));
                }
                for (SubscribeTariffDto subscribeTariffDto : serviceLineDto.getTariffList()) {
                    for (SubscribeTariffLineDto tariffLineDto : subscribeTariffDto.getSubscribeTariffLineDtoList()) {
                        for (SubscribeFilterParamDto subscribeFilterParamDto : tariffLineDto.getFilterParams()) {
                            subscribeFilterParamDto.setListOfGroupGuideDtos(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().filter(
                                    groupGuide -> (groupGuide.getBegDate().isBefore(ldt.toZonedDateTime()) ||
                                            groupGuide.getBegDate().isEqual(ldt.toZonedDateTime()))
                                            && groupGuide.getEndDate().isAfter(ldt.toZonedDateTime())
                            ).collect(Collectors.toCollection(ArrayList::new)));
                        }

                    }
                }
                for (CheckSetLineDto checkSetLineDto : serviceLineDto.getCheckSetLineList()) {
                    for (SubscribeFilterParamDto subscribeFilterParamDto : checkSetLineDto.getFilterParams()) {
                        subscribeFilterParamDto.setListOfGroupGuideDtos(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().filter(
                                groupGuide -> (groupGuide.getBegDate().isBefore(ldt.toZonedDateTime()) ||
                                        groupGuide.getBegDate().isEqual(ldt.toZonedDateTime()))
                                        && groupGuide.getEndDate().isAfter(ldt.toZonedDateTime())
                        ).collect(Collectors.toCollection(ArrayList::new)));
                    }
                }
            }
            this.serviceLines = resultList;
        }

    }

    public void filterTariffsByDate(OffsetDateTime ldt) {
        this.tariffs = this.tariffs.stream()
                .filter(tariffDto27277 -> tariffDto27277.getServiceLine() != null && (
                        tariffDto27277.getServiceLine().getBegDate().toOffsetDateTime().isBefore(ldt) ||
                                tariffDto27277.getServiceLine().getBegDate().toOffsetDateTime().equals(ldt)) &&
                        tariffDto27277.getServiceLine().getEndDate().toOffsetDateTime().isAfter(ldt))
                .toList();
        for (IndividualTariffDto27277 tariffDto27277 : this.tariffs) {
            //todo: это, скорее всего, вообще не нужно, но будем уж держать данные консистентными (если сильно влияет на время запроса убрать)
            tariffDto27277.getServiceLine().setTariffList(tariffDto27277.getServiceLine().getTariffList().stream()
                    .filter(subscribeTariffDto -> (subscribeTariffDto.getBegDate().toLocalDateTime().isBefore(ldt.toLocalDateTime()) || subscribeTariffDto.getBegDate().toLocalDateTime().isEqual(ldt.toLocalDateTime())
                            && subscribeTariffDto.getEndDate().toLocalDateTime().isAfter(ldt.toLocalDateTime()))).toList());
            tariffDto27277.getServiceLine().setCheckSetLineList(tariffDto27277.getServiceLine().getCheckSetLineList().stream()
                    .filter(checkSetLineDto -> (checkSetLineDto.getBegDate().toLocalDateTime().isBefore(ldt.toLocalDateTime()) || checkSetLineDto.getBegDate().toLocalDateTime().isEqual(ldt.toLocalDateTime()))
                            && checkSetLineDto.getEndDate().toLocalDateTime().isAfter(ldt.toLocalDateTime())).toList());
            for (SubscribeFilterParamDto subscribeFilterParamDto : tariffDto27277.getServiceLine().getFilterParams()) {
                subscribeFilterParamDto.setListOfGroupGuideDtos(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().filter(
                        groupGuide -> (groupGuide.getBegDate().isBefore(ldt.toZonedDateTime()) ||
                                groupGuide.getBegDate().isEqual(ldt.toZonedDateTime()))
                                && groupGuide.getEndDate().isAfter(ldt.toZonedDateTime())
                ).collect(Collectors.toCollection(ArrayList::new)));
            }
            for (SubscribeTariffDto subscribeTariffDto : tariffDto27277.getServiceLine().getTariffList()) {
                for (SubscribeTariffLineDto tariffLineDto : subscribeTariffDto.getSubscribeTariffLineDtoList()) {
                    for (SubscribeFilterParamDto subscribeFilterParamDto : tariffLineDto.getFilterParams()) {
                        subscribeFilterParamDto.setListOfGroupGuideDtos(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().filter(
                                groupGuide -> (groupGuide.getBegDate().isBefore(ldt.toZonedDateTime()) ||
                                        groupGuide.getBegDate().isEqual(ldt.toZonedDateTime()))
                                        && groupGuide.getEndDate().isAfter(ldt.toZonedDateTime())
                        ).collect(Collectors.toCollection(ArrayList::new)));
                    }

                }
            }
            for (CheckSetLineDto checkSetLineDto : tariffDto27277.getServiceLine().getCheckSetLineList()) {
                for (SubscribeFilterParamDto subscribeFilterParamDto : checkSetLineDto.getFilterParams()) {
                    subscribeFilterParamDto.setListOfGroupGuideDtos(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().filter(
                            groupGuide -> (groupGuide.getBegDate().isBefore(ldt.toZonedDateTime()) ||
                                    groupGuide.getBegDate().isEqual(ldt.toZonedDateTime()))
                                    && groupGuide.getEndDate().isAfter(ldt.toZonedDateTime())
                    ).collect(Collectors.toCollection(ArrayList::new)));
                }
            }
        }


    }

    public void addPriceInOtherPu(SubscribeRequest27277 subscribeRequest) {
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : subscribes) {
            log.trace("\t\t{}", subscribeDto.getCode());
            //searching service price in other PU and concat it to result DTO
            if (!subscribeDto.getIsIndependent()) {
                //log.debug("вошли в условие зависимой подписки");
                List<ServiceLineDto> rawOtherProductsSLs = findAllByPu4PriceService(subscribeDto.getId());
                if (subscribeRequest.getClientPUCodesAsList() != null && !subscribeRequest.getClientPUCodesAsList().isEmpty()
                        && !rawOtherProductsSLs.isEmpty()
                ) {
                    rawOtherProductsSLs = findAllByPu4PriceService(subscribeDto.getId(), subscribeRequest.getClientPUCodesAsList(), rawOtherProductsSLs);
                }
                //log.debug("\t\t\trawOtherProductsSLs finish, size {}",rawOtherProductsSLs.size());
                List<ServiceLineDto> otherProductsSLs = rawOtherProductsSLs.stream()
                        .filter(serviceLine -> {
                            if (subscribeRequest.getServiceGroups() != null && !subscribeRequest.getServiceGroups().isEmpty())
                                return subscribeRequest.getServiceGroups().contains(serviceLine.getPuServiceGroupCode());
                            return true;
                        })
                        .filter(serviceLine -> {
                            if (subscribeRequest.getServices() != null && !subscribeRequest.getServices().isEmpty())
                                return subscribeRequest.getServices().contains(serviceLine.getServiceGuideItemCode());
                            return true;
                        }).toList();
                //log.debug("\t\t\totherProductsSLs filter finish, size = {}", otherProductsSLs.size());

                if (otherProductsSLs != null && !otherProductsSLs.isEmpty()) {
                    ServiceGroupDto serviceGroupDto =
                            new ServiceGroupDto();
                    serviceGroupDto.setOrder(1);
                    serviceGroupDto.setId(0L);
                    serviceGroupDto.setCode("PRICE_IN_OTHER_PU");
                    serviceGroupDto.setLabel("Цена пакета, указанная в других ПУ");
                    List<ServiceLineDto> serviceLineList = new ArrayList<>();
                    for (ServiceLineDto serviceLineDto : otherProductsSLs) {
                        //check dates
                        LocalDateTime entityBegDate = serviceLineDto.getBegDate().toLocalDateTime(); //todo converttoutc?
                        LocalDateTime entityEndDate = serviceLineDto.getEndDate().toLocalDateTime(); //todo converttoutc?
                        LocalDateTime currentDate = subscribeRequest.getSearchOnDate() != null ?
                                (subscribeRequest.getSearchOnDate().toLocalDateTime()) : (LocalDateTime.now());
                        if (entityBegDate.isAfter(currentDate)
                                || entityEndDate.isBefore(currentDate)
                                || !serviceLineDto.getStatus().equals("Approved"))
                            continue;
                        //ServiceLineDto serviceLineDto = subscribeReader.mapServiceLine(serviceLineEntity, subscribeRequest); todo: мапить не надо
                        if (serviceLineDto.getServiceType().equals("4"))
                            serviceLineDto.setServiceType("3");
                        serviceLineDto.setPu4PriceService(serviceLineDto.getPuServiceGroupProductCode());
                        serviceLineDto.setServiceTypeLabel("Цена своего Пакета");
                        serviceLineList.add(serviceLineDto);
                        //log.debug("        добавили строку");
                        log.trace("\t\t\tAdding price inOtherPU filter finish");
                    }
                    serviceGroupDto.setServiceLineList(serviceLineList);
                    if (subscribeDto.getServiceGroupList() != null) {
                        //По согласованию с А.Черкасовым - группа PRICE_IN_OTHER_PU виртуальная, так что легитимно убивается при каждой итерации
                        List<ServiceGroupDto> newServiceGroupList = subscribeDto.getServiceGroupList().stream()
                                .filter(sg -> !sg.getCode().equals("PRICE_IN_OTHER_PU"))
                                .collect(toList());
                        newServiceGroupList.add(serviceGroupDto);
                        subscribeDto.setServiceGroupList(newServiceGroupList);
                    } else {
                        subscribeDto.setServiceGroupList(Arrays.asList(serviceGroupDto));
                    }
                }

                filteredSubscribes.add(subscribeDto);
            } else {
                filteredSubscribes.add(subscribeDto);
            }
        }
        this.subscribes = filteredSubscribes;
    }

    public void filterByClientType(String clientType) {
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : subscribes) {
            if (subscribeDto.getClientTypeList() == null || subscribeDto.getClientTypeList().size() == 0
                    || subscribeDto.getClientTypeList().contains(clientType))
                filteredSubscribes.add(subscribeDto);
        }
        this.subscribes = filteredSubscribes;
    }

    public void filterByAccountBranch(String accountBranch, String branchGroup) {
        List<SubscribeDto27277> filteredSubscribes = new CopyOnWriteArrayList<>();
        for (SubscribeDto27277 subscribeDto : subscribes) {
            if (subscribeDto.getBranchList() == null || subscribeDto.getBranchList().size() == 0
                    || subscribeDto.getBranchList().contains(accountBranch)
                    || subscribeDto.getBranchList().contains(branchGroup))
                filteredSubscribes.add(subscribeDto);
        }
        this.subscribes = filteredSubscribes;
    }

    public void filterByAccountType(String accountType) {
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : subscribes) {
            if (subscribeDto.getAccountTypeList() == null || subscribeDto.getAccountTypeList().size() == 0
                    || subscribeDto.getAccountTypeList().contains(accountType))
                filteredSubscribes.add(subscribeDto);
        }
        this.subscribes = filteredSubscribes;
    }

    public void filterByClientSegment(String clientSegment) {
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : subscribes) {
            if (subscribeDto.getSegmentList() == null || subscribeDto.getSegmentList().size() == 0
                    || subscribeDto.getSegmentList().contains(clientSegment))
                filteredSubscribes.add(subscribeDto);
        }
        this.subscribes = filteredSubscribes;
    }

    public void filterByClientCommonType(List<String> clientCommonType) {
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();
        if(clientCommonType == null || clientCommonType.isEmpty())
            return;
        for (SubscribeDto27277 subscribeDto : subscribes) {
            if (clientCommonType.contains(subscribeDto.getClientCommonType()))
                filteredSubscribes.add(subscribeDto);
        }
        this.subscribes = filteredSubscribes;
    }

    public void filterByCategory(List<String> categories) {
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : subscribes) {
            if (subscribeDto.getCategory() != null && categories.contains(subscribeDto.getCategory()))
                filteredSubscribes.add(subscribeDto);
        }
        this.subscribes = filteredSubscribes;
    }

    public void filterByServiceGroups(List<String> serviceGroups) {
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();

        for (SubscribeDto27277 subscribeDto : subscribes) {
            if (subscribeDto.getServiceGroupList() != null) {
                List<ServiceGroupDto> serviceGroupFilteredList = new ArrayList<>();
                for (ServiceGroupDto serviceGroupDto : subscribeDto.getServiceGroupList()) {
                    if (serviceGroups.contains(serviceGroupDto.getCode()))
                        serviceGroupFilteredList.add(serviceGroupDto);
                }
                if (serviceGroupFilteredList.size() != 0) {
                    subscribeDto.setServiceGroupList(serviceGroupFilteredList);
                    filteredSubscribes.add(subscribeDto);
                }
            }
        }

        this.subscribes = filteredSubscribes;
    }

    public void filterByServiceLines(List<String> services) {
        log.debug("\t\t\t\tfiltering by services {}", services);
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();

        for (SubscribeDto27277 subscribeDto : subscribes) {
            if (subscribeDto.getServiceGroupList() != null) {
                List<ServiceGroupDto> serviceGroupFilteredList = new ArrayList<>();
                for (ServiceGroupDto serviceGroupDto : subscribeDto.getServiceGroupList()) {
                    List<ServiceLineDto> filteredServices = new ArrayList<>();
                    for (ServiceLineDto serviceLineDto : serviceGroupDto.getServiceLineList()) {
                        if (services.contains(serviceLineDto.getService()))
                            filteredServices.add(serviceLineDto);
                    }
                    if (filteredServices.size() != 0) {
                        serviceGroupDto.setServiceLineList(filteredServices);
                        serviceGroupFilteredList.add(serviceGroupDto);
                    }
                }
                if (serviceGroupFilteredList.size() != 0) {
                    subscribeDto.setServiceGroupList(serviceGroupFilteredList);
                    filteredSubscribes.add(subscribeDto);
                }
            }
        }

        this.subscribes = filteredSubscribes;
    }

    public void filterByMarketing(Boolean isMarketing) {
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : subscribes) {
            if (subscribeDto.getIsMarketing().equals(isMarketing))
                filteredSubscribes.add(subscribeDto);
        }
        this.subscribes = filteredSubscribes;

    }

    public void filterByHavingOwnPrice() {
        log.debug("Filtering subscribes by having own price");
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : subscribes) {

            //проверка работает ТОЛЬКО для SUBSCRIPTION, остальные не проверяем
            if (!subscribeDto.getCategory().equals("SUBSCRIPTION")) {
                filteredSubscribes.add(subscribeDto);
                continue;
            }

            if (subscribeDto.getServiceGroupList() != null)
                for (ServiceGroupDto serviceGroupDto : subscribeDto.getServiceGroupList()) {
                    if (serviceGroupDto.getServiceLineList() != null) {
                        for (ServiceLineDto serviceLineDto : serviceGroupDto.getServiceLineList()) {
                            if (serviceLineDto.getServiceType().equals("3")) {
                                filteredSubscribes.add(subscribeDto);
                                break;
                            }
                        }
                    }
                }
        }
        this.subscribes = filteredSubscribes;
        log.debug("Filtering subscribes by having own price done, size={}", subscribes.size());
    }

    public void filterTariffsByIsSimple() {
        List<IndividualTariffDto27277> tariffs = new CopyOnWriteArrayList<>();
        for (IndividualTariffDto27277 individualTariffDto : this.tariffs) {
            List<SubscribeTariffDto> simpleTariffs = new ArrayList<>();
            for (SubscribeTariffDto subscribeTariffDto : individualTariffDto.getServiceLine().getTariffList()) {
                if (subscribeTariffDto.isSimple())
                    simpleTariffs.add(subscribeTariffDto);
            }
            if (simpleTariffs.size() != 0) {
                individualTariffDto.getServiceLine().setTariffList(simpleTariffs);
                tariffs.add(individualTariffDto);
            }
        }
        this.tariffs = tariffs;
    }

    public void filterByClientPUCode(String clientPUCOde) {
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : subscribes) {
            if (subscribeDto.getCode().equals(clientPUCOde))
                filteredSubscribes.add(subscribeDto);
        }
        this.subscribes = filteredSubscribes;
    }

    public void filterDependentPUs() {
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : subscribes) {
            if (subscribeDto.getIsIndependent())
                filteredSubscribes.add(subscribeDto);
        }
        this.subscribes = filteredSubscribes;
    }

    public List<SubscribeDto27277> filterByCodes(List<String> subscribeCodes) {
        log.debug("\t\t\t\tfiltering by codes {}", subscribeCodes);
        List<SubscribeDto27277> filteredSubscribes = this.subscribes.stream().filter(subscribeDto27277 -> subscribeCodes.contains(subscribeDto27277.getCode())).collect(Collectors.toCollection(() -> new ArrayList<>()));
        this.subscribes = filteredSubscribes;
        return this.subscribes;
    }

    public void presetParamsFilter(SubscribeRequest27277 subscribeRequest) {
        List<SubscribeDto27277> filteredSubscribes = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : this.subscribes) {
            if (checkPresetParams(subscribeDto, subscribeRequest))
                filteredSubscribes.add(subscribeDto);
            else
                log.debug("{} filtered out by preset params", subscribeDto.getCode());
        }
        this.subscribes = filteredSubscribes;
    }

    private boolean checkPresetParams(SubscribeDto27277 subscribeDto, SubscribeRequest27277 subscribeRequest) {
        if (subscribeRequest.isCheckPackageCode() || (subscribeRequest.getPackageCodes() == null || subscribeRequest.getPackageCodes().isEmpty())) {
            if (subscribeDto.getFilterParamList() != null) {
                for (SubscribeFilterParamDto subscribeParamDto : subscribeDto.getFilterParamList()) {
                    Optional<SubscribeRequest27277.ParamMap> requestClientFilterParam = Optional.empty();
                    Optional<SubscribeRequest27277.ParamMap> requestAccountFilterParam = Optional.empty();
                    Optional<SubscribeRequest27277.ParamMap> requestCardFilterParam = Optional.empty();
                    Optional<SubscribeRequest27277.ParamMap> requestOperationFilterParam = Optional.empty();
                    Optional<SubscribeRequest27277.ParamMap> requestProductFilterParam = Optional.empty();

                    switch (subscribeParamDto.getObjectType()) {
                        case "CLIENT":
                            if (subscribeRequest.getClientParams() != null)
                                requestClientFilterParam = subscribeRequest.getClientParams()
                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                            if (!compareParam(requestClientFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                log.debug("\t{} Product pre-set CLIENT filter param {} mismatch!", subscribeDto.getCode(), subscribeParamDto.getCode());
                                return false;
                            }
                            break;
                        case "ACCOUNT":
                            if (subscribeRequest.getAccountParams() != null)
                                requestAccountFilterParam = subscribeRequest.getAccountParams()
                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                            if (!compareParam(requestAccountFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                log.debug("\t{} Product pre-set ACCOUNT filter param {} mismatch!", subscribeDto.getCode(), subscribeParamDto.getCode());
                                return false;
                            }
                            break;
                        case "CARD":
                            if (subscribeRequest.getCardParams() != null)
                                requestCardFilterParam = subscribeRequest.getCardParams()
                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                            if (!compareParam(requestCardFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                log.debug("\t{} Product pre-set CARD filter param {} mismatch!", subscribeDto.getCode(), subscribeParamDto.getCode());
                                return false;
                            }
                            break;
                        case "OPERATION":
                            if (subscribeRequest.getOperationParams() != null)
                                requestOperationFilterParam = subscribeRequest.getOperationParams()
                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                            if (!compareParam(requestOperationFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                log.debug("\t{} Product pre-set OPERATION filter param {} mismatch!", subscribeDto.getCode(), subscribeParamDto.getCode());
                                return false;
                            }
                            break;
                        case "PRODUCT":
                            if (subscribeRequest.getProductParams() != null)
                                requestProductFilterParam = subscribeRequest.getProductParams()
                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                            if (!compareParam(requestProductFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                log.debug("\t{} Product pre-set PRODUCT filter param {} mismatch!", subscribeDto.getCode(), subscribeParamDto.getCode());
                                return false;
                            }
                            break;

                    }

                }
            }
        }

        if ((subscribeRequest.getServices() != null && subscribeRequest.getServices().size() == 1) || subscribeRequest.isSkipService() || !subscribeRequest.getVerifyMultyServiceTariffByType().isEmpty()) {
            List<ServiceGroupDto> filteredServiceGroupList = new ArrayList<>();
            for (ServiceGroupDto serviceGroupDto : subscribeDto.getServiceGroupList()) {
                //29008 - additional filtering affects only when requested only one service
                List<ServiceLineDto> filteredServiceLines = new ArrayList<>();
                for (ServiceLineDto serviceLineDto : serviceGroupDto.getServiceLineList()) {
                    boolean serviceLineFilterParamIsOk = true;
                    boolean serviceLineIsTariffOk = true;
                    boolean serviceLineIsCheckSetOk = true;
                    if (subscribeRequest.isVerifyServiceParamList()
                            && serviceLineDto.getFilterParams() != null
                            && !serviceLineDto.getFilterParams().isEmpty()) {

                        for (SubscribeFilterParamDto subscribeParamDto : serviceLineDto.getFilterParams()) {
                            Optional<SubscribeRequest27277.ParamMap> requestFilterParam = subscribeRequest.getProductParams()
                                    .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();

                            if (!compareParam(requestFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                log.debug("\t{} ServiceLine pre-set param {} mismatch!", subscribeDto.getCode(), subscribeParamDto.getCode());
                                serviceLineFilterParamIsOk = false;
                                log.debug("{} ServiceLine ID {} serviceLineFilter is not OK!", serviceLineDto.getService(), serviceLineDto.getId());
                            }
                        }
                        if (!serviceLineFilterParamIsOk && !subscribeRequest.isReturnEmptyBlocks()) continue;
                    }

                    if ((subscribeRequest.isVerifyServiceTariff() || subscribeRequest.getVerifyMultyServiceTariffByType().contains(serviceLineDto.getServiceType())) && serviceLineDto.getTariffList() != null) {
                        List<SubscribeTariffDto> filteredTariffs = new ArrayList<>();
                        for (SubscribeTariffDto subscribeTariffDto : serviceLineDto.getTariffList()) {
                            List<SubscribeTariffLineDto> filteredTariffLines = new ArrayList<>();
                            for (SubscribeTariffLineDto subscribeTariffLineDto : subscribeTariffDto.getSubscribeTariffLineDtoList()) {
                                boolean isTariffLineMatch = true;
                                if (subscribeTariffLineDto.getParamList() != null) {
                                    for (SubscribeFilterParamDto subscribeParamDto : subscribeTariffLineDto.getFilterParams()) {
                                        if (subscribeParamDto.getTariffPart() != 1 && subscribeParamDto.getTariffPart() != 2) {
                                            log.debug("skipping param check bcs it's part not 1 and 2, param is {}", subscribeParamDto);
                                            continue;
                                        }

                                        Optional<SubscribeRequest27277.ParamMap> requestClientFilterParam = Optional.empty();
                                        Optional<SubscribeRequest27277.ParamMap> requestAccountFilterParam = Optional.empty();
                                        Optional<SubscribeRequest27277.ParamMap> requestCardFilterParam = Optional.empty();
                                        Optional<SubscribeRequest27277.ParamMap> requestOperationFilterParam = Optional.empty();
                                        Optional<SubscribeRequest27277.ParamMap> requestProductFilterParam = Optional.empty();

                                        switch (subscribeParamDto.getObjectType()) {
                                            case "CLIENT":
                                                if (subscribeRequest.getClientParams() != null)
                                                    requestClientFilterParam = subscribeRequest.getClientParams()
                                                            .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                                                if (subscribeRequest.isVerifyServiceTariffRequestParam() && requestClientFilterParam.isEmpty())
                                                    continue;
                                                if (!compareParam(requestClientFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                                    log.debug("\t{} TariffLine from {} pre-set CLIENT filter param {} mismatch!", subscribeTariffLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                                    isTariffLineMatch = false;
                                                }
                                                break;
                                            case "ACCOUNT":
                                                if (subscribeRequest.getAccountParams() != null)
                                                    requestAccountFilterParam = subscribeRequest.getAccountParams()
                                                            .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                                                if (subscribeRequest.isVerifyServiceTariffRequestParam() && requestAccountFilterParam.isEmpty())
                                                    continue;
                                                if (!compareParam(requestAccountFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                                    log.debug("\t{} TariffLine from {} pre-set ACCOUNT filter param {} mismatch!", subscribeTariffLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                                    isTariffLineMatch = false;
                                                }
                                                break;
                                            case "CARD":
                                                if (subscribeRequest.getCardParams() != null)
                                                    requestCardFilterParam = subscribeRequest.getCardParams()
                                                            .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                                                if (subscribeRequest.isVerifyServiceTariffRequestParam() && requestCardFilterParam.isEmpty())
                                                    continue;
                                                if (!compareParam(requestCardFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                                    log.debug("\t{} TariffLine from {} pre-set CARD filter param {} mismatch!", subscribeTariffLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                                    isTariffLineMatch = false;
                                                }
                                                break;
                                            case "OPERATION":
                                                if (subscribeRequest.getOperationParams() != null)
                                                    requestOperationFilterParam = subscribeRequest.getOperationParams()
                                                            .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                                                if (subscribeRequest.isVerifyServiceTariffRequestParam() && requestOperationFilterParam.isEmpty())
                                                    continue;
                                                if (!compareParam(requestOperationFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                                    log.debug("\t{} TariffLine from {} pre-set OPERATION filter param {} mismatch!", subscribeTariffLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                                    isTariffLineMatch = false;
                                                }
                                                break;
                                            case "PRODUCT":
                                                if (subscribeRequest.getProductParams() != null)
                                                    requestProductFilterParam = subscribeRequest.getProductParams()
                                                            .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                                                if (subscribeRequest.isVerifyServiceTariffRequestParam() && requestProductFilterParam.isEmpty())
                                                    continue;
                                                if (!compareParam(requestProductFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                                    log.debug("\t{} TariffLine from {} pre-set PRODUCT filter param {} mismatch!", subscribeTariffLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                                    isTariffLineMatch = false;
                                                }
                                                break;

                                        }
                                        if (!isTariffLineMatch) break;
                                    }
                                }
                                if (isTariffLineMatch)
                                    filteredTariffLines.add(subscribeTariffLineDto);
                            }
                            if (!filteredTariffLines.isEmpty()) {
                                subscribeTariffDto.setSubscribeTariffLineDtoList(filteredTariffLines);
                                filteredTariffs.add(subscribeTariffDto);
                            } else {
                                log.debug("\t{} Tariff {} has no matching TariffLines!", subscribeDto.getCode(), subscribeTariffDto.getCode());
                            }
                        }
                        if (filteredTariffs.isEmpty() && !subscribeRequest.isReturnEmptyBlocks()) {
                            log.debug("\t{} ServiceLine with ID {} has no matching Tariffs!", serviceLineDto.getService(), serviceLineDto.getId());
                            serviceLineIsTariffOk = false;
                        } else {
                            serviceLineDto.setTariffList(filteredTariffs);
                        }
                    }
                    if (!serviceLineIsTariffOk && !subscribeRequest.isReturnEmptyBlocks()) continue;
                    if (subscribeRequest.isVerifyServiceCheckSet()
                            && serviceLineDto.getCheckSetLineList() != null
                            && serviceLineDto.getCheckSetLineList().stream()
                            .anyMatch(csl -> csl.getParamList() != null && !csl.getParamList().isEmpty())) {
                        boolean isCheckSetLineMatch;
                        List<CheckSetLineDto> filteredCheckSetLines = new ArrayList<>();
                        for (CheckSetLineDto checkSetLineDto : serviceLineDto.getCheckSetLineList()) {
                            isCheckSetLineMatch = true;
                            if (checkSetLineDto.getFilterParams() != null && checkSetLineDto.getFilterParams().size() != 0) {
                                for (SubscribeFilterParamDto subscribeParamDto : checkSetLineDto.getFilterParams()) {
                                    //todo собирать со всех списков (account, card, operation, product)
                                    Optional<SubscribeRequest27277.ParamMap> requestClientFilterParam = Optional.empty();
                                    Optional<SubscribeRequest27277.ParamMap> requestAccountFilterParam = Optional.empty();
                                    Optional<SubscribeRequest27277.ParamMap> requestCardFilterParam = Optional.empty();
                                    Optional<SubscribeRequest27277.ParamMap> requestOperationFilterParam = Optional.empty();
                                    Optional<SubscribeRequest27277.ParamMap> requestProductFilterParam = Optional.empty();

                                    switch (subscribeParamDto.getObjectType()) {
                                        case "CLIENT":
                                            if (subscribeRequest.getClientParams() != null)
                                                requestClientFilterParam = subscribeRequest.getClientParams()
                                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                                            if (!compareParam(requestClientFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                                log.debug("\t{} CheckSet from {} pre-set CLIENT filter param {} mismatch!", checkSetLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                                isCheckSetLineMatch = false;
                                            }
                                            break;
                                        case "ACCOUNT":
                                            if (subscribeRequest.getAccountParams() != null)
                                                requestAccountFilterParam = subscribeRequest.getAccountParams()
                                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                                            if (!compareParam(requestAccountFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                                log.debug("\t{} CheckSet from {} pre-set ACCOUNT filter param {} mismatch!", checkSetLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                                isCheckSetLineMatch = false;
                                            }
                                            break;
                                        case "CARD":
                                            if (subscribeRequest.getCardParams() != null)
                                                requestCardFilterParam = subscribeRequest.getCardParams()
                                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                                            if (!compareParam(requestCardFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                                log.debug("\t{} CheckSet from {} pre-set CARD filter param {} mismatch!", checkSetLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                                isCheckSetLineMatch = false;
                                            }
                                            break;
                                        case "OPERATION":
                                            if (subscribeRequest.getOperationParams() != null)
                                                requestOperationFilterParam = subscribeRequest.getOperationParams()
                                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                                            if (!compareParam(requestOperationFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                                log.debug("\t{} CheckSet from {} pre-set OPERATION filter param {} mismatch!", checkSetLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                                isCheckSetLineMatch = false;
                                            }
                                            break;
                                        case "PRODUCT":
                                            if (subscribeRequest.getProductParams() != null)
                                                requestProductFilterParam = subscribeRequest.getProductParams()
                                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                                            if (!compareParam(requestProductFilterParam.orElse(new SubscribeRequest27277.ParamMap()), subscribeParamDto)) {
                                                log.debug("\t{} CheckSet from {} pre-set PRODUCT filter param {} mismatch!", checkSetLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                                isCheckSetLineMatch = false;
                                            }
                                            break;

                                    }
                                }
                                if (isCheckSetLineMatch) {
                                    filteredCheckSetLines.add(checkSetLineDto);

                                }
                            }
                        }
                        if (filteredCheckSetLines.isEmpty()) {
                            //if (!isCheckSetLineMatch) {
                            log.debug("\t{} has no matching CheckSetLines!", subscribeDto.getCode());
                            serviceLineIsCheckSetOk = false;
                        }
                    }
                    if ((serviceLineIsTariffOk && serviceLineIsCheckSetOk && serviceLineFilterParamIsOk) || subscribeRequest.isReturnEmptyBlocks()) //смысла не имеет, но почему бы не оставить проверку всех трёх
                        filteredServiceLines.add(serviceLineDto);
                }
                serviceGroupDto.setServiceLineList(filteredServiceLines);
                if (!serviceGroupDto.getServiceLineList().isEmpty())
                    filteredServiceGroupList.add(serviceGroupDto);
            }
            if (filteredServiceGroupList.isEmpty() && !subscribeRequest.isReturnEmptyBlocks())
                return false;

            subscribeDto.setServiceGroupList(filteredServiceGroupList);
        }
        return true;
    }

    public SubscribeFilterParamDto convertParamToFilter(SubscribeFilterParamDto tariffParamDto) {//todo: mb удалить вообще?
        SubscribeFilterParamDto filterParamDto = new SubscribeFilterParamDto();
        filterParamDto.setId(tariffParamDto.getId());
        filterParamDto.setOrder(tariffParamDto.getOrder());
        filterParamDto.setCode(tariffParamDto.getCode());
        filterParamDto.setLabel(tariffParamDto.getLabel());
        filterParamDto.setGuide(tariffParamDto.getGuide());
        filterParamDto.setType(tariffParamDto.getType());
        filterParamDto.setVType(tariffParamDto.getVType());
        filterParamDto.setVTypeLabel(tariffParamDto.getVTypeLabel());
        filterParamDto.setVBoolean(tariffParamDto.getVBoolean());
        filterParamDto.setVString(tariffParamDto.getVString());
        filterParamDto.setVDate(tariffParamDto.getVDate());
        filterParamDto.setVDateMin(tariffParamDto.getVDateMin());
        filterParamDto.setVDateMax(tariffParamDto.getVDateMax());
        filterParamDto.setVInt(tariffParamDto.getVInt());
        filterParamDto.setVIntMin(tariffParamDto.getVIntMin());
        filterParamDto.setVIntMax(tariffParamDto.getVIntMax());
        filterParamDto.setVFloat(tariffParamDto.getVFloat());
        filterParamDto.setVFloatMin(tariffParamDto.getVFloatMin());
        filterParamDto.setVFloatMax(tariffParamDto.getVFloatMax());
        filterParamDto.setVCodePattern(tariffParamDto.getVCodePattern());
        filterParamDto.setVCodeList(tariffParamDto.getVCodeList());
        filterParamDto.setVLabelList(tariffParamDto.getVLabelList());
        filterParamDto.setCompareType(tariffParamDto.getVType());
        filterParamDto.setObjectType(tariffParamDto.getObjectType());
        return filterParamDto;
    }

    public boolean checkInfoCompareParam(CheckInfoRequestDto.ParamMap requestParam, SubscribeFilterParamDto paramDto) {
        SubscribeRequest27277.ParamMap paramMap = new SubscribeRequest27277.ParamMap();
        paramMap.setCode(requestParam.getCode());
        paramMap.setValue(requestParam.getValue());
        return compareParam(paramMap, paramDto);
    }

    private boolean compareParam(SubscribeRequest27277.ParamMap requestParam, SubscribeFilterParamDto filterParam) {
        if ("ALL".equals(filterParam.getCompareType()))
            return true;
        if (filterParam.getCompareType().equals("NOT_EMPTY") && requestParam.getValue() != null && !requestParam.getValue().isEmpty())
            return true;
        switch (filterParam.getType().toUpperCase()) {
            //28310 - null в boolean означает "любой", т.е. сравнение с таким параметром всегда истинное
            case "BOOLEANTYPE":
                if (filterParam.getCompareType().equals("BOOLEAN")) {
                    if (requestParam.getValue() == null || requestParam.getValue().isEmpty())
                        return false;
                    return Boolean.valueOf(requestParam.getValue()).equals(filterParam.getVBoolean());
                }
                if (filterParam.getCompareType().equals("BOOLEAN_OR_NULL")) {
                    if (requestParam.getValue() == null || requestParam.getValue().isEmpty())
                        return true;
                    return Boolean.valueOf(requestParam.getValue()).equals(filterParam.getVBoolean());
                }
                if (filterParam.getVBoolean() == null
                        || Boolean.valueOf(requestParam.getValue()).equals(filterParam.getVBoolean()))
                    return true;
                break;
            case "STRINGTYPE":
                if (requestParam.getValue() == null || requestParam.getValue().isEmpty())
                    return false;
                if (filterParam.getCompareType().equals("LIKE")) {
                    String likeSource = filterParam.getVString();
                    String regexPattern = Pattern.quote(likeSource)
                            .replace("*", "\\E.*\\Q")
                            .replace("_", "\\E.\\Q");
                    Pattern filterPattern = Pattern.compile(regexPattern);
                    return filterPattern.matcher(requestParam.getValue()).matches();
                } else if (filterParam.getCompareType().equals("NOT_LIKE")) {
                    String notLikeSource = filterParam.getVString();
                    String regexPattern = Pattern.quote(notLikeSource)
                            .replace("*", "\\E.*\\Q")
                            .replace("_", "\\E.\\Q");
                    Pattern filterPattern = Pattern.compile(regexPattern);
                    return !filterPattern.matcher(requestParam.getValue()).matches();
                }
                if (filterParam.getCompareType().equals("NOT_EQUAL"))
                    return !Objects.equals(requestParam.getValue(), filterParam.getVString());

                if (filterParam.getCompareType().equals("EQUAL"))
                    return Objects.equals(filterParam.getVString(), requestParam.getValue());
                break;
            case "CODETYPE":
                Set<String> targetParamList;
                //todo: проверять группировочный ли, подставлять здесь значения из groupGuideDto
                if (!filterParam.getIsGroupResolved())
                    throw new RuntimeException("somehow getIsGroupResolved in filterParam is false! cache groupGuide resolving is broken?"); //должно всегда быть true, так как резолвятся они в кеше, но вдруг? выкину Exception если false
                if (!filterParam.getIsGroupGuide() || (filterParam.getIsGroupGuide() && filterParam.getListOfGroupGuideDtos().isEmpty())) {
                    targetParamList = filterParam.getVCodeList() != null ?
                            new HashSet<>(Arrays.asList(filterParam.getVCodeList().split(";")))
                            : new HashSet<>();
                } else {
                    targetParamList = filterParam.getListOfGroupGuideDtos().stream()
                            .flatMap(group -> Arrays.stream(group.getLinkCodeList().split(";")))
                            .collect(Collectors.toCollection(HashSet::new));
                }
                Set<String> requestParamList = (requestParam.getValue() != null && !requestParam.getValue().isEmpty())
                        ? new HashSet<>(Arrays.asList(requestParam.getValue().split(";")))
                        : new HashSet<>();
                switch (filterParam.getCompareType().toUpperCase()) {
                    case "EMPTY":
                        return (requestParam.getValue() == null || requestParam.getValue().isEmpty());
                    case "LIKE":
                        if (filterParam.getVCodePattern() == null) {
                            log.warn("{} LIKE pattern is empty!");
                            return false;
                        }
                        String likePattern = filterParam.getVCodePattern();
                        String regexFromLike = Pattern.quote(likePattern)
                                .replace("*", "\\E.*\\Q")
                                .replace("_", "\\E.\\Q");
                        return Pattern.compile(regexFromLike).matcher(requestParam.getValue()).matches();
                    case "NOT_LIKE":
                        if (filterParam.getVCodePattern() == null) {
                            log.warn("{} NOT_LIKE pattern is empty!");
                            return false;
                        }
                        String notLikePattern = filterParam.getVCodePattern();
                        String regexFromNotLike = Pattern.quote(notLikePattern)
                                .replace("*", "\\E.*\\Q")
                                .replace("_", "\\E.\\Q");
                        return !Pattern.compile(regexFromNotLike).matcher(requestParam.getValue()).matches();
                    case "IN":
                        if (requestParamList.isEmpty() || targetParamList.containsAll(requestParamList))
                            return true;
                        break;
                    case "IN_NOTNULL":
                        if (!requestParamList.isEmpty() && targetParamList.containsAll(requestParamList))
                            return true;
                        break;
                    case "NOT_IN":
                        if (requestParamList.isEmpty() || !targetParamList.containsAll(requestParamList))
                            return true;
                        break;
                    case "NOT_IN_NOTNULL":
                        if (!requestParamList.isEmpty() && !targetParamList.containsAll(requestParamList))
                            return true;
                        break;

                    case "IN_EXT":
                        if (requestParamList.isEmpty() || !Sets.intersection(requestParamList, targetParamList).isEmpty())
                            return true;
                        break;
                    case "IN_EXT_NOTNULL":
                        if (!requestParamList.isEmpty() && !Sets.intersection(requestParamList, targetParamList).isEmpty())
                            return true;
                        break;
                    case "NOT_IN_EXT":
                        if (requestParamList.isEmpty() || Sets.intersection(requestParamList, targetParamList).isEmpty())
                            return true;
                        break;
                    case "NOT_IN_EXT_NOTNULL":
                        if (!requestParamList.isEmpty() && Sets.intersection(requestParamList, targetParamList).isEmpty())
                            return true;
                        break;

                    case "INCLUDE":
                        if (requestParamList.isEmpty() || (targetParamList.equals(requestParamList)))
                            return true;
                        break;
                    case "INCLUDE_NOTNULL":
                        if (!requestParamList.isEmpty() && (targetParamList.equals(requestParamList)))
                            return true;
                        break;
                    case "NOT_INCLUDE":
                        if (requestParamList.isEmpty() || !(targetParamList.equals(requestParamList)))
                            return true;
                        break;
                    case "NOT_INCLUDE_NOTNULL":
                        if (!requestParamList.isEmpty() && !(targetParamList.equals(requestParamList)))
                            return true;
                        break;
                    case "INCLUDE_EXT":
                        if (requestParamList.isEmpty() || requestParamList.containsAll(targetParamList))
                            return true;
                        break;
                    case "INCLUDE_EXT_NOTNULL":
                        if (!requestParamList.isEmpty() && requestParamList.containsAll(targetParamList))
                            return true;
                        break;
                    case "NOT_INCLUDE_EXT":
                        if (requestParamList.isEmpty() || !requestParamList.containsAll(targetParamList))
                            return true;
                        break;
                    case "NOT_INCLUDE_EXT_NOTNULL":
                        if (!requestParamList.isEmpty() && !requestParamList.containsAll(targetParamList))
                            return true;
                        break;
                }
                break;
            case "DATETYPE":
                if (requestParam.getValue() == null || requestParam.getValue().isEmpty()) {
                    log.warn("DATETYPE request param is empty or null! returning false on check");
                    return false;
                }
                ZonedDateTime requestDate = OffsetDateTime.parse(requestParam.getValue(), DateTimeFormatter.ofPattern(DATETYPE_FORMAT)).toZonedDateTime();
                switch (filterParam.getCompareType().toUpperCase()) {
                    case "EQUAL":
                        if (filterParam.getVDate().equals(requestDate)) {
                            return true;
                        }
                        break;
                    case "NOT_EQUAL":
                        if (!filterParam.getVDate().equals(requestDate)) {
                            return true;
                        }
                        break;
                    case "LESS":
                        if (filterParam.getVDate().isAfter(requestDate)) {
                            return true;
                        }
                        break;
                    case "LESS_EQUAL":
                        if (filterParam.getVDate().equals(requestDate) ||
                                filterParam.getVDate().isAfter(requestDate)) {
                            return true;
                        }
                        break;
                    case "MORE":
                        if (filterParam.getVDate().isBefore(requestDate)) {
                            return true;
                        }
                        break;
                    case "MORE_EQUAL":
                        if (filterParam.getVDate().equals(requestDate) ||
                                filterParam.getVDate().isBefore(requestDate)) {
                            return true;
                        }
                        break;
                    case "RANGE[]":
                        if ((filterParam.getVDateMin().equals(requestDate) ||
                                filterParam.getVDateMin().isBefore(requestDate))
                                && (filterParam.getVDateMax().equals(requestDate) ||
                                filterParam.getVDateMax().isAfter(requestDate)))
                            return true;
                        break;
                    case "RANGE[)":
                        if ((filterParam.getVDateMin().equals(requestDate)
                                || filterParam.getVDateMin().isBefore(requestDate))
                                && filterParam.getVDateMax().isAfter(requestDate))
                            return true;
                        break;
                    case "RANGE(]":
                        if (filterParam.getVDateMin().isBefore(requestDate)
                                && (filterParam.getVDateMax().equals(requestDate)
                                || filterParam.getVDateMax().isAfter(requestDate)))
                            return true;
                        break;
                    case "RANGE()":
                        if (filterParam.getVDateMin().isBefore(requestDate)
                                && filterParam.getVDateMax().isAfter(requestDate))
                            return true;
                        break;
                }
                break;
            case "INTTYPE":
                String str = requestParam.getValue();
                if (str == null || str.isBlank()) return false;
                Long value = Long.parseLong(requestParam.getValue());
                switch (filterParam.getCompareType().toUpperCase()) {
                    case "EQUAL":
                        return filterParam.getVInt().equals(value);
                    case "NOT_EQUAL":
                        return !filterParam.getVInt().equals(value);
                    case "LESS":
                        return filterParam.getVInt() > value;
                    case "LESS_EQUAL":
                        return filterParam.getVInt() >= value;
                    case "MORE":
                        return filterParam.getVInt() < value;
                    case "MORE_EQUAL":
                        return filterParam.getVInt() <= value;
                    case "RANGE[]":
                        return filterParam.getVIntMin() <= value && filterParam.getVIntMax() >= value;
                    case "RANGE[)":
                        return filterParam.getVIntMin() <= value && filterParam.getVIntMax() > value;
                    case "RANGE(]":
                        return filterParam.getVIntMin() < value && filterParam.getVIntMax() >= value;
                    case "RANGE()":
                        return filterParam.getVIntMin() < value && filterParam.getVIntMax() > value;
                }
                break;
            case "FLOATTYPE":
                if (requestParam.getValue() == null || requestParam.getValue().isEmpty())
                    return false;
                switch (filterParam.getCompareType().toUpperCase()) {
                    case "EQUAL":
                        if (filterParam.getVFloat().compareTo(new BigDecimal(requestParam.getValue())) == 0)
                            return true;
                        break;
                    case "NOT_EQUAL":
                        if (filterParam.getVFloat().compareTo(new BigDecimal(requestParam.getValue())) != 0)
                            return true;
                        break;
                    case "LESS":
                        if (filterParam.getVFloat().compareTo(new BigDecimal(requestParam.getValue())) > 0)
                            return true;
                        break;
                    case "LESS_EQUAL":
                        if (filterParam.getVFloat().compareTo(new BigDecimal(requestParam.getValue())) >= 0)
                            return true;
                        break;
                    case "MORE":
                        if (filterParam.getVFloat().compareTo(new BigDecimal(requestParam.getValue())) < 0)
                            return true;
                        break;
                    case "MORE_EQUAL":
                        if (filterParam.getVFloat().compareTo(new BigDecimal(requestParam.getValue())) <= 0)
                            return true;
                        break;
                    case "RANGE[]":
                        if (filterParam.getVFloatMin().compareTo(new BigDecimal(requestParam.getValue())) <= 0
                                && filterParam.getVFloatMax().compareTo(new BigDecimal(requestParam.getValue())) >= 0)
                            return true;
                        break;
                    case "RANGE[)":
                        if (filterParam.getVFloatMin().compareTo(new BigDecimal(requestParam.getValue())) <= 0
                                && filterParam.getVFloatMax().compareTo(new BigDecimal(requestParam.getValue())) > 0)
                            return true;
                        break;
                    case "RANGE(]":
                        if (filterParam.getVFloatMin().compareTo(new BigDecimal(requestParam.getValue())) < 0
                                && filterParam.getVFloatMax().compareTo(new BigDecimal(requestParam.getValue())) >= 0)
                            return true;
                        break;
                    case "RANGE()":
                        if (filterParam.getVFloatMin().compareTo(new BigDecimal(requestParam.getValue())) < 0
                                && filterParam.getVFloatMax().compareTo(new BigDecimal(requestParam.getValue())) > 0)
                            return true;
                        break;
                }
                break;
        }
        return false;
    }

//    public List<IndividualTariffDto> searchTariffsByCodes(List<Map<String, String>> codes) {
//        List<IndividualTariffDto> result = new ArrayList<>();
//
//        for (Map<String, String> map : codes) {
//            IndividualTariffDto individualTariffDto = mapIndividualTariff(map.get("SERVICECODE"), map.get("TARIFFCODE"));
//            if (individualTariffDto != null)
//                result.add(individualTariffDto);
//        }
//        return result;
//    }

    public List<IndividualTariffDto27277> searchTariffsByCodes(List<String> individualTariffCodes) {
        return this.tariffs.stream().filter(
                individualTariffDto27277 -> individualTariffCodes.contains(individualTariffDto27277.getCode())
        ).toList();
    }

    public void filterTariffsByCodes(List<String> individualTariffCodes) {
        this.tariffs = this.tariffs.stream().filter(
                individualTariffDto27277 -> individualTariffCodes.contains(individualTariffDto27277.getCode())
        ).toList();
    }

    public List<IndividualTariffDto27277> searchTariffsByCodes30737(List<String> individualTariffCodes) {
        return searchTariffsByCodes(individualTariffCodes)
                .stream()
                .filter(tariff -> tariff.getServiceLine() != null && tariff.getServiceLine().getStatus().equals("Approved"))
                .collect(toList());
    }

    public void filterTariffsByServiceGroups(List<String> serviceGroups) {
        List<IndividualTariffDto27277> filteredTariffs = new ArrayList<>();

        for (IndividualTariffDto27277 individualTariffDto : tariffs) {
            if (individualTariffDto.getServiceLine().getServiceGuide() != null
                    && serviceGroups.contains(individualTariffDto.getServiceLine().getServiceGuide()))
                filteredTariffs.add(individualTariffDto);
        }

        this.tariffs = filteredTariffs;
    }

    public void filterTariffsByServices(List<String> services) {
        List<IndividualTariffDto27277> filteredTariffs = new ArrayList<>();

        for (IndividualTariffDto27277 individualTariffDto : tariffs) {
            if (individualTariffDto.getServiceLine().getService() != null
                    && services.contains(individualTariffDto.getServiceLine().getService()))
                filteredTariffs.add(individualTariffDto);
        }

        this.tariffs = filteredTariffs;
    }

    public void filterTariffsByClientCommonType(List<String> clientCommonType) {
        List<IndividualTariffDto27277> filteredTariffs = new ArrayList<>();

        for (IndividualTariffDto27277 individualTariffDto : tariffs) {
            if (clientCommonType.contains(individualTariffDto.getClientCommonType()))
                filteredTariffs.add(individualTariffDto);
        }

        this.tariffs = filteredTariffs;
    }

    public void filterTariffsByServiceType(Set<String> serviceTypes) {
        List<IndividualTariffDto27277> filteredTariffs = new ArrayList<>();
        for (IndividualTariffDto27277 individualTariffDto : tariffs) {
            if (serviceTypes.contains(individualTariffDto.getServiceLine().getServiceType())) {
                filteredTariffs.add(individualTariffDto);
            }
        }
        this.tariffs = filteredTariffs;
    }

    private LocalDateTime convertToUtc(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    //Start block for FL/packageLightSearch 32843

    public List<FLLightSubscribeDto> filterSubsForFL(LightRequestDto lightRequest) { //2-4 parts
        List<Long> puSubsId;
        if (lightRequest.getClientPUCodeList() != null && !lightRequest.getClientPUCodeList().isEmpty()) {
            puSubsId = this.subscribes.stream()
                    .filter(subscribeDto27277 -> lightRequest.getClientPUCodeList().contains(subscribeDto27277.getCode()))
                    .toList().stream()
                    .map(SubscribeDto27277::getId)
                    .toList();
        } else {
            puSubsId = new ArrayList<>();
        }
        List<SubscribeDto27277> filteredSubs = this.subscribes.stream()
                .filter(subscribeDto27277 -> "F".equals(subscribeDto27277.getClientCommonType()) &&
                                "SUBSCRIPTION".equals(subscribeDto27277.getCategory()) &&
                                !subscribeDto27277.getIsMarketing() &&
                                !subscribeDto27277.getIsIndividual()
                        //&& subscribeDto27277.getIsIndependent()
                ).toList();
        if (lightRequest.getClientPUCodeList() == null || lightRequest.getClientPUCodeList().isEmpty()) {
            filteredSubs = filteredSubs.stream()
                    .filter(SubscribeDto27277::getIsIndependent)
                    .toList();
        }
        String clientCodeTypeValue = "";
        String clientCodeSegmentValue = "";
        boolean requestContainsAge = false;

        for (LightRequestDto.ParamMap clientParam : lightRequest.getClientParams()) {
            if ("TYPE".equals(clientParam.getCode())) {
                clientCodeTypeValue = clientParam.getValue() == null ? "" : clientParam.getValue();
            }
            if ("SEGMENT".equals(clientParam.getCode())) {
                clientCodeSegmentValue = clientParam.getValue() == null ? "" : clientParam.getValue();
            }
            if ("AGE".equals(clientParam.getCode())) {
                requestContainsAge = (clientParam.getValue() != null && !clientParam.getValue().isEmpty());
            }
        }


        if (!clientCodeTypeValue.isEmpty()) {
            String finalClientCodeTypeValue = clientCodeTypeValue;
            filteredSubs = filteredSubs.stream().filter(subscribeDto27277 ->
                            subscribeDto27277.getClientTypeList().isEmpty() || subscribeDto27277.getClientTypeList().contains(finalClientCodeTypeValue))
                    .toList();

        }
        if (!clientCodeSegmentValue.isEmpty()) {
            String finalClientSegmentValue = clientCodeSegmentValue;
            filteredSubs = filteredSubs.stream().filter(subscribeDto27277 ->
                            subscribeDto27277.getSegmentList().isEmpty() || subscribeDto27277.getSegmentList().contains(finalClientSegmentValue))
                    .toList();
        }
        boolean finalRequestContainsAge = requestContainsAge;
        filteredSubs = filteredSubs.stream().filter(
                subscribeDto27277 -> {
                    if (subscribeDto27277.getFilterParamList() == null) return true;
                    List<SubscribeFilterParamDto> paramList;
                    paramList = subscribeDto27277.getFilterParamList().stream().filter(
                            filterParamDto -> (filterParamDto.getObjectType().equals("CLIENT") && filterParamDto.getCode().equals("AGE"))
                    ).collect(Collectors.toCollection(ArrayList::new));
                    return paramList.isEmpty() || finalRequestContainsAge;
                }
        ).collect(Collectors.toCollection(ArrayList::new));
        //todo: отфильтровать подписки, у которых нет в параметрах CLIENT.AGE настройки
        this.subscribes = filteredSubs;
        filterSubsByDynamicFieldsFL(lightRequest); //end of 4
        List<ServiceLineDto> filteredServiceLines = new ArrayList<>();
        filteredSubs = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto27277 : this.subscribes) {
            List<ServiceLineDto> serviceLineList;
            if (subscribeDto27277.getIsIndependent()) {
                serviceLineList = this.serviceLines.stream().filter(
                        serviceLineDto -> Objects.equals(subscribeDto27277.getId(), serviceLineDto.getObj()) &&
                                "Approved".equals(serviceLineDto.getStatus()) &&
                                serviceLineDto.getEndDate().isAfter(ZonedDateTime.now()) &&
                                (Objects.equals(serviceLineDto.getServiceType(), "3"))
                ).toList();
            } else {
                serviceLineList = this.serviceLines.stream().filter(
                        serviceLineDto -> puSubsId.contains(serviceLineDto.getObj()) &&
                                "Approved".equals(serviceLineDto.getStatus()) &&
                                serviceLineDto.getEndDate().isAfter(ZonedDateTime.now()) &&
                                "4".equals(serviceLineDto.getServiceType())
                                && serviceLineDto.getPu4PriceServiceLong().equals(subscribeDto27277.getId())
                ).toList();
            }
            filteredServiceLines.addAll(serviceLineList);
            if (!serviceLineList.isEmpty()) {
                filteredSubs.add(subscribeDto27277);
            }
        }
        this.serviceLines = filteredServiceLines;
        this.subscribes = filteredSubs;
        filterCheckSets(lightRequest);
        filterSLsByChecksets();
        List<FLLightSubscribeDto> lightSubscribeDtoList = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto27277 : this.subscribes) {
            FLLightSubscribeDto lightSubscribeDto = new FLLightSubscribeDto(subscribeDto27277);
            if (lightSubscribeDto.getIsIndependent()) {
                lightSubscribeDto.setServiceLineDtoList(
                        this.serviceLines.stream().filter(
                                serviceLineDto -> Objects.equals(serviceLineDto.getObj(), lightSubscribeDto.getId())
                                        && serviceLineDto.getServiceType().equals("3")
                        ).collect(Collectors.toCollection(ArrayList::new)));
            } else {
                lightSubscribeDto.setServiceLineDtoList(
                        this.serviceLines.stream().filter(
                                serviceLineDto -> Objects.equals(serviceLineDto.getPu4PriceServiceLong(), lightSubscribeDto.getId())
                                        && serviceLineDto.getServiceType().equals("4")
                        ).collect(Collectors.toCollection(ArrayList::new)));
            }
            lightSubscribeDtoList.add(lightSubscribeDto);
        }
        flPeriodsForSls(lightSubscribeDtoList);
        List<FLLightSubscribeDto> lightSubscribeDtoList2 = new ArrayList<>(); // лист подписок с несколькими периодами
        lightSubscribeDtoList = lightSubscribeDtoList.stream().filter(
                lightSubscribeDto -> !lightSubscribeDto.getFlPeriodsList().isEmpty()
        ).collect(Collectors.toCollection(ArrayList::new));
        lightSubscribeDtoList2 = lightSubscribeDtoList.stream().filter(
                lightSubscribeDto -> lightSubscribeDto.getFlPeriodsList().size() > 1
        ).toList();
        lightSubscribeDtoList.removeAll(lightSubscribeDtoList2);
        for (FLLightSubscribeDto subscribeDto : lightSubscribeDtoList2) {
            for (BegEndDatesDto begEndDatesDto : subscribeDto.getFlPeriodsList()) {
                FLLightSubscribeDto lightSubscribeDto = new FLLightSubscribeDto();
                lightSubscribeDto.setCode(subscribeDto.getCode());
                lightSubscribeDto.setLabel(subscribeDto.getLabel());
                lightSubscribeDto.setBegDate(begEndDatesDto.getBegDate());
                lightSubscribeDto.setEndDate(begEndDatesDto.getEndDate());
                lightSubscribeDto.setExcludePUGroup(subscribeDto.getExcludePUGroup());
                lightSubscribeDtoList.add(lightSubscribeDto);
            }
        }
        return lightSubscribeDtoList;
    }

    public void filterSLsByChecksets() {
        for (ServiceLineDto serviceLineDto : this.serviceLines) {
            if (serviceLineDto.getCheckSetLineList().isEmpty()) {
                continue;
            }
            if (serviceLineDto.getCheckSetLineList().size() == 1) { //если checksetline только одна её и сравнивать не с чем
                List<BegEndDatesDto> listOfDates = Collections.singletonList(new BegEndDatesDto(serviceLineDto.getCheckSetLineList().get(0)));
                listOfDates = trimDatePeriods(listOfDates, serviceLineDto.getBegDate(), serviceLineDto.getEndDate());
                serviceLineDto.setFlPeriodsList(listOfDates);
                continue;
            }
            serviceLineDto.getCheckSetLineList().sort((o1, o2) -> {
                int result;
                result = o1.getBegDate().isBefore(o2.getBegDate()) ? -1 : 1;
                if (o1.getBegDate().isEqual(o2.getBegDate())) result = 0;
                return result;
            });
            List<BegEndDatesDto> listOfDates = new ArrayList<>();
            listOfDates = getDatesListForCheckSets(serviceLineDto.getCheckSetLineList());
            listOfDates = trimDatePeriods(listOfDates, serviceLineDto.getBegDate(), serviceLineDto.getEndDate()); //отобрали сроки для serviceline'ов, а куда их?
            serviceLineDto.setFlPeriodsList(listOfDates);
        }
    }

    public void flPeriodsForSls(List<FLLightSubscribeDto> lightSubscribeDtoList) {
        for (FLLightSubscribeDto lightSubscribeDto : lightSubscribeDtoList) {
            if (lightSubscribeDto.getServiceLineDtoList().isEmpty()) { //такого существовать не должно вообще, но пусть будет
                continue;
            }
            if (lightSubscribeDto.getServiceLineDtoList().size() == 1) { //если serviceline только одна её и сравнивать не с чем
                List<BegEndDatesDto> listOfDates = Collections.singletonList(new BegEndDatesDto(lightSubscribeDto.getServiceLineDtoList().get(0)));
                listOfDates = trimDatePeriods(listOfDates, lightSubscribeDto.getBegDate(), lightSubscribeDto.getEndDate());
                lightSubscribeDto.setFlPeriodsList(listOfDates);
                continue;
            }
            lightSubscribeDto.getServiceLineDtoList().sort((o1, o2) -> {
                int result;
                result = o1.getBegDate().isBefore(o2.getBegDate()) ? -1 : 1;
                if (o1.getBegDate().isEqual(o2.getBegDate())) result = 0;
                return result;
            });
            List<BegEndDatesDto> listOfDates = new ArrayList<>();
            listOfDates = getDatesListForServiceLines(lightSubscribeDto.getServiceLineDtoList());
            listOfDates = trimDatePeriods(listOfDates, lightSubscribeDto.getBegDate(), lightSubscribeDto.getEndDate()); //отобрали сроки для serviceline'ов, а куда их?
            lightSubscribeDto.setFlPeriodsList(listOfDates);
        }
    }


    public void filterSubsByDynamicFieldsFL(LightRequestDto lightRequest) {
        List<SubscribeDto27277> filteredSubs = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto27277 : this.subscribes) {
            if (checkFLSubsPresetParams(subscribeDto27277, lightRequest)) {
                filteredSubs.add(subscribeDto27277);
            } else log.debug("{} filtered out by preset params", subscribeDto27277.getCode());
        }
        this.subscribes = filteredSubs;
    }

    private int checkDateCrossing(BegEndDatesDto begEndDatesDtoFirst, BegEndDatesDto begEndDatesDtoSecond) {
        if ((begEndDatesDtoFirst.getBegDate().isBefore(begEndDatesDtoSecond.getBegDate()) || begEndDatesDtoFirst.getBegDate().isEqual(begEndDatesDtoSecond.getBegDate()))
                && (begEndDatesDtoFirst.getEndDate().isBefore(begEndDatesDtoSecond.getEndDate()) || begEndDatesDtoFirst.getEndDate().isEqual(begEndDatesDtoSecond.getEndDate()))
                && (begEndDatesDtoFirst.getEndDate().isAfter(begEndDatesDtoSecond.getBegDate()) || begEndDatesDtoFirst.getEndDate().isEqual(begEndDatesDtoSecond.getBegDate()))) { //условие 1, второй задевает первый
            return 1;
        } else if (Duration.between(begEndDatesDtoFirst.getEndDate(), begEndDatesDtoSecond.getBegDate()).getSeconds() == 1) { //условие 2, время различается на секунду
            return 2;
        } else if ((begEndDatesDtoFirst.getBegDate().isBefore(begEndDatesDtoSecond.getBegDate()) || begEndDatesDtoFirst.getBegDate().isEqual(begEndDatesDtoSecond.getBegDate()))
                && (begEndDatesDtoFirst.getEndDate().isAfter(begEndDatesDtoSecond.getEndDate()) || begEndDatesDtoFirst.getEndDate().isEqual(begEndDatesDtoSecond.getEndDate()))) { //условие 3, второй целиком внутри первого
            return 3;
        }
        return -1; //ситуация 4 и любая мной неучтённая
    }

    private List<BegEndDatesDto> getDatesListForCheckSets(List<CheckSetLineDto> checkSetLineDtoList) {//todo: сделать универсальным для serviceline и checkset как минимум
        List<BegEndDatesDto> begEndDatesDtoList = new ArrayList<>();
        BegEndDatesDto tempBegDateDto = new BegEndDatesDto(checkSetLineDtoList.get(0));
        for (CheckSetLineDto checkSetLineDto : checkSetLineDtoList.subList(1, checkSetLineDtoList.size())) {
            int checkResult = checkDateCrossing(tempBegDateDto, new BegEndDatesDto(checkSetLineDto));
            if (checkResult == 3) continue;
            if ((checkResult == 1) || (checkResult == 2)) {
                tempBegDateDto.setEndDate(checkSetLineDto.getEndDate());
                continue;
            }
            if (checkResult == -1) {
                begEndDatesDtoList.add(tempBegDateDto);
                tempBegDateDto = new BegEndDatesDto(checkSetLineDto);
            }
        }
        begEndDatesDtoList.add(tempBegDateDto);
        return begEndDatesDtoList;
    }

    private List<BegEndDatesDto> getDatesListForServiceLines(List<ServiceLineDto> serviceLineDtoList) {//todo: сделать универсальным для serviceline и checkset как минимум
        List<BegEndDatesDto> begEndDatesDtoList = new ArrayList<>();
        BegEndDatesDto tempBegDateDto = new BegEndDatesDto(serviceLineDtoList.get(0));
        for (ServiceLineDto serviceLineDto : serviceLineDtoList.subList(1, serviceLineDtoList.size())) {
            int checkResult = checkDateCrossing(tempBegDateDto, new BegEndDatesDto(serviceLineDto));
            if (checkResult == 3) continue;
            if ((checkResult == 1) || (checkResult == 2)) {
                tempBegDateDto.setEndDate(serviceLineDto.getEndDate());
                continue;
            }
            if (checkResult == -1) {
                begEndDatesDtoList.add(tempBegDateDto);
                tempBegDateDto = new BegEndDatesDto(serviceLineDto);
            }
        }
        begEndDatesDtoList.add(tempBegDateDto);
        return begEndDatesDtoList;
    }

    private List<BegEndDatesDto> trimDatePeriods(List<BegEndDatesDto> begEndDatesDtoList, ZonedDateTime begDate, ZonedDateTime endDate) {
        List<BegEndDatesDto> filteredPeriods = new ArrayList<>();
        for (BegEndDatesDto begEndDatesDto : begEndDatesDtoList) {
            if ((begEndDatesDto.getBegDate().isBefore(begDate) || begEndDatesDto.getBegDate().isEqual(begDate))
                    && (begEndDatesDto.getEndDate().isAfter(begDate) || begEndDatesDto.getEndDate().isEqual(begDate))
            ) { //условие 2, частично находится раньше begDate
                begEndDatesDto.setBegDate(begDate);
                filteredPeriods.add(begEndDatesDto);
                continue;
            }
            if ((begDate.isBefore(begEndDatesDto.getBegDate()) || begDate.isEqual(begEndDatesDto.getBegDate()))
                    && (endDate.isAfter(begEndDatesDto.getEndDate()) || endDate.isEqual(begEndDatesDto.getEndDate()))) { //условие 3, находится внутри отрезка целиком
                filteredPeriods.add(begEndDatesDto);
                continue;
            }
            if ((begEndDatesDto.getBegDate().isAfter(begDate) || begEndDatesDto.getBegDate().isEqual(begDate))
                    && (begEndDatesDto.getEndDate().isAfter(endDate) || begEndDatesDto.getEndDate().isEqual(endDate))
            ) { //условие 4, частично находится позже endDate
                begEndDatesDto.setEndDate(endDate);
                filteredPeriods.add(begEndDatesDto);
                continue;
            }
            if (begEndDatesDto.getEndDate().isBefore(begDate)) {   //условие 5, целиком после endDate (существует для видимости)
                continue;
            }
            if (begEndDatesDto.getEndDate().isBefore(begDate)) {   //условие 1 (существует для видимости)
                continue;
            }
        }
        return filteredPeriods;
    }

    private boolean checkFLSubsPresetParams(SubscribeDto27277 subscribeDto, LightRequestDto subscribeRequest) {
        if (subscribeDto.getFilterParamList() != null) {
            for (SubscribeFilterParamDto subscribeParamDto : subscribeDto.getFilterParamList()) {
                Optional<LightRequestDto.ParamMap> requestClientFilterParam = Optional.empty();

                if (subscribeRequest.getClientParams() != null)
                    requestClientFilterParam = subscribeRequest.getClientParams()
                            .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();

                if (!requestClientFilterParam.isPresent()
                        && subscribeParamDto.getObjectType().equals("CLIENT")
                        && !subscribeParamDto.getCompareType().equals("NOT_EMPTY"))
                    continue;
                if (subscribeParamDto.getObjectType().equals("CLIENT")
                        && !compareParam(requestClientFilterParam.orElse(new LightRequestDto.ParamMap()), subscribeParamDto)) {
                    log.debug("\t{} Product pre-set client filter param {} mismatch!", subscribeDto.getCode(), subscribeParamDto.getCode());
                    return false;
                }


            }
        }

        return true;
    }

    //crutch for not duplicate code
    private boolean compareParam(LightRequestDto.ParamMap paramMap, SubscribeFilterParamDto subscribeParamDto) {
        SubscribeRequest27277.ParamMap crutchParamMap = new SubscribeRequest27277.ParamMap();
        crutchParamMap.setCode(paramMap.getCode());
        crutchParamMap.setValue(paramMap.getValue());
        return compareParam(crutchParamMap, subscribeParamDto);
    }


    private void filterCheckSets(LightRequestDto subscribeRequest) {
        List<ServiceLineDto> filteredSLs = new ArrayList<>();

        for (ServiceLineDto serviceLineDto : this.serviceLines) {
            if (serviceLineDto.getCheckSetLineList() == null || serviceLineDto.getCheckSetLineList().isEmpty()) {
                filteredSLs.add(serviceLineDto);
                continue;
            }
            List<CheckSetLineDto> checkSetLineDtoList = serviceLineDto.getCheckSetLineList();
            if (checkSetLineDtoList.stream().anyMatch(csl -> csl.getParamList() != null && !csl.getParamList().isEmpty())) {
                boolean isCheckSetLineMatch = false;
                List<CheckSetLineDto> filteredCheckSetLines = new ArrayList<>();
                for (CheckSetLineDto checkSetLineDto : checkSetLineDtoList) {
                    isCheckSetLineMatch = true;
                    if (checkSetLineDto.getFilterParams() != null && !checkSetLineDto.getFilterParams().isEmpty()) {
                        for (SubscribeFilterParamDto subscribeParamDto : checkSetLineDto.getFilterParams()) {
                            //todo собирать со всех списков (account, card, operation, product)
                            Optional<LightRequestDto.ParamMap> requestClientFilterParam = Optional.empty();
                            if (subscribeRequest.getClientParams() != null)
                                requestClientFilterParam = subscribeRequest.getClientParams()
                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();

                            if (!requestClientFilterParam.isPresent()
                                    && subscribeParamDto.getObjectType().equals("CLIENT")
                                    && !subscribeParamDto.getCompareType().equals("NOT_EMPTY"))
                                continue;
                            if (subscribeParamDto.getObjectType().equals("CLIENT")
                                    && !compareParam(requestClientFilterParam.orElse(new LightRequestDto.ParamMap()), subscribeParamDto)) {
                                //log.debug("\t{} CheckSetLine {} pre-set client param {} mismatch!", subscribeDto.getCode(), checkSetLineDto.getId(), subscribeParamDto.getCode());
                                isCheckSetLineMatch = false;
                                break;
                            }
                        }
                        if (isCheckSetLineMatch) {
                            filteredCheckSetLines.add(checkSetLineDto);

                        }
                    }
                }
                serviceLineDto.setCheckSetLineList(filteredCheckSetLines);
                if (!serviceLineDto.getCheckSetLineList().isEmpty()) {
                    filteredSLs.add(serviceLineDto);
                }
            }
        }
        this.serviceLines = filteredSLs;
    }

    public void filterServiceLinesByTypesInPackages(Set<String> serviceTypes) {
        if (serviceTypes.contains("3") || serviceTypes.contains("4")) { //если пришла 3 или 4 нужно оставить обе
            serviceTypes.add("3");
            serviceTypes.add("4");
        }
        this.subscribes = filterSubscribesByServices(serviceTypes);
    }

    /**
     * Фильтрует ServiceLineDto в каждой ServiceGroupDto по списку сервисов,
     * удаляет пустые группы и возвращает только подписки с непустыми группами
     *
     * @param serviceTypes список типов услуг для фильтрации
     * @return отфильтрованный список подписок
     */
    private List<SubscribeDto27277> filterSubscribesByServices(Set<String> serviceTypes) {
        return subscribes.stream()
                .map(subscribe -> {
                    // Фильтруем группы, оставляя только те, в которых есть услуги с нужными кодами
                    List<ServiceGroupDto> filteredGroups = subscribe.getServiceGroupList().stream()
                            .map(group -> {
                                // Фильтруем услуги по кодам
                                List<ServiceLineDto> filteredLines = group.getServiceLineList().stream()
                                        .filter(line -> serviceTypes.contains(line.getServiceType()))
                                        .collect(Collectors.toList());
                                group.setServiceLineList(filteredLines);
                                return group;
                            })
                            .filter(group -> !group.getServiceLineList().isEmpty())
                            .collect(Collectors.toList());

                    subscribe.setServiceGroupList(filteredGroups);
                    return subscribe;
                })
                .filter(subscribe -> !subscribe.getServiceGroupList().isEmpty())
                .collect(Collectors.toList());
    }

    public void resolveGracePeriods(SubscribeRequest27277 request) {
        SubscribeRequest27277.ParamMap requestSourceConnect = null;
        for (SubscribeRequest27277.ParamMap paramMap : request.getOperationParams()) {
            if (paramMap.getCode().equals("SOURCECONNECT")) {
                requestSourceConnect = paramMap;
                break;
            }
        }

        for (SubscribeDto27277 subscribe : this.subscribes) {
            for (ServiceGroupDto serviceGroupDto : subscribe.getServiceGroupList()) {
                for (ServiceLineDto serviceLineDto : serviceGroupDto.getServiceLineList()) {
                    for (SubscribeTariffDto tariffDto : serviceLineDto.getTariffList()) {
                        List<CheckedGraceBySourceDto> checkedGraceBySourceDtoList = new ArrayList<>();
                        if (requestSourceConnect == null) {
                            tariffDto.setCheckGraceBySource(tariffDto.getGracePeriodsDto().getCheckedGraceBySourceDtoList());
                            continue;
                        }
                        List<Integer> ordersList = new ArrayList<>();
                        for (SubscribeTariffLineDto tariffLineDto : tariffDto.getSubscribeTariffLineDtoList()) {
                            ordersList.add(tariffLineDto.getOrder());
                        }
                        var requestValues = Set.of(requestSourceConnect.getValue().split(";"));
                        var checkResult = Boolean.FALSE;
                        var resultSet = new HashSet<Boolean>();
                        int appearanceIndex = 0;
                        for (String requestValue : requestValues) {
                            if (tariffDto.getGracePeriodsDto().getCodesNeededToBeCheckedList().contains(requestValue)) {
                                resultSet.add(true);
                                appearanceIndex++;
                            }
                            if (tariffDto.getGracePeriodsDto().getCodesNotNeededToBeCheckedList().contains(requestValue)) {
                                resultSet.add(false);
                                appearanceIndex++;
                            }
                        }
                        if (appearanceIndex == 1 ||
                                (appearanceIndex > 1 && resultSet.size() == 1))
                            checkResult = resultSet.iterator().next();
                        for (Integer order : ordersList) {
                            var CheckedGraceBySourceDto = new CheckedGraceBySourceDto();
                            CheckedGraceBySourceDto.setOrder(order);
                            CheckedGraceBySourceDto.setValue(checkResult);
                            checkedGraceBySourceDtoList.add(CheckedGraceBySourceDto);
                        }
                        tariffDto.setCheckGraceBySource(checkedGraceBySourceDtoList);
                    }
                }
            }
        }
    }


//    private IndividualTariffDto mapIndividualTariff(String serviceCode, String tariffCode) {
//        log.info("Searching {} {} for {}", serviceCode, tariffCode, LocalDateTime.now().toString());
//        List<IndividualTariffEntity> individualTariffEntities = individualTariffRepository.
//                getIndividualTariffByServiceAndTariffCode(serviceCode, tariffCode, LocalDateTime.now());
//
//        if (individualTariffEntities != null && individualTariffEntities.size() != 0) {
//            IndividualTariffDto individualTariffDto = new IndividualTariffDto();
//            ServiceTableDto serviceTableDto = new ServiceTableDto();
//
//            //общие данные тарифа на основе первой строки
//            individualTariffDto.setServiceGroupCode(individualTariffEntities.get(0).getItSgCode());
//            individualTariffDto.setServiceGroupName(individualTariffEntities.get(0).getItSgLabel());
//            individualTariffDto.setServiceCode(individualTariffEntities.get(0).getItSgiCode());
//            individualTariffDto.setServiceName(individualTariffEntities.get(0).getItSgiLabel());
//            individualTariffDto.setCounterCode(individualTariffEntities.get(0).getItCtCode());
//            individualTariffDto.setCounterName(individualTariffEntities.get(0).getItCtLabel());
//            individualTariffDto.setTariffCode(individualTariffEntities.get(0).getItTariffCode());
//            individualTariffDto.setTariffName(individualTariffEntities.get(0).getItTariffName());
//            individualTariffDto.setTariffBegDate(individualTariffEntities.get(0).getTBegDate());
//            individualTariffDto.setTariffEndDate(individualTariffEntities.get(0).getTEndDate());
//            serviceTableDto.setVariant(individualTariffEntities.get(0).getTvSortOrder());
//            individualTariffDto.getServiceTables().add(serviceTableDto);
//
//            for (int i = 0; i < individualTariffEntities.size(); i++) {
//                if (serviceTableDto.getVariant() != individualTariffEntities.get(i).getTvSortOrder()) {
//                    serviceTableDto = new ServiceTableDto();
//                    serviceTableDto.setVariant(individualTariffEntities.get(i).getTvSortOrder());
//                }
//
//                ServiceParamDto paramDto = new ServiceParamDto();
//                paramDto.setCode(individualTariffEntities.get(i).getTpPmCode());
//                paramDto.setOrder(individualTariffEntities.get(i).getTpSortOrder());
//                paramDto.setType(individualTariffEntities.get(i).getTpGGuideType());
//                paramDto.setLabel(individualTariffEntities.get(i).getTpPmLabel());
//                paramDto.setValueSet(individualTariffEntities.get(i).isPvIsNot() ? "NOT " : "" + individualTariffEntities.get(i).getPvVtCode());
//                paramDto.setValueType(individualTariffEntities.get(i).getTpGGuideType());
//
//                switch (paramDto.getType().toUpperCase()) {
//                    case "STRING":
//                        paramDto.setSValue(individualTariffEntities.get(i).getValueString());
//                        break;
//                    case "FLOAT":
//                        if (individualTariffEntities.get(i).getValueFloatMax() != null) {
//                            paramDto.setFValueMIN(individualTariffEntities.get(i).getValueFloatMin());
//                            paramDto.setFValueMAX(individualTariffEntities.get(i).getValueFloatMax());
//                        }
//                        paramDto.setFValue(individualTariffEntities.get(i).getValueFloat());
//                        break;
//                    case "BOOLEAN":
//                        if (individualTariffEntities.get(i).getValueBoolean() == 0)
//                            paramDto.getBValue().add(false);
//                        else if (individualTariffEntities.get(i).getValueBoolean() == 1)
//                            paramDto.getBValue().add(true);
//                        else if (individualTariffEntities.get(i).getValueBoolean() == 2) {
//                            paramDto.getBValue().add(false);
//                            paramDto.getBValue().add(true);
//                        }
//                        break;
//                    case "INT":
//                        if (individualTariffEntities.get(i).getValueIntMax() != null) {
//                            paramDto.setIValueMIN(individualTariffEntities.get(i).getValueIntMin());
//                            paramDto.setIValueMAX(individualTariffEntities.get(i).getValueIntMax());
//                        }
//                        paramDto.setIValue(individualTariffEntities.get(i).getValueInt());
//                        break;
//                    case "DATE":
//                        if (individualTariffEntities.get(i).getValueDateMax() != null) {
//                            paramDto.setDValueMIN(individualTariffEntities.get(i).getValueDateMin());
//                            paramDto.setDValueMAX(individualTariffEntities.get(i).getValueDateMax());
//                        }
//                        paramDto.setDValue(individualTariffEntities.get(i).getValueDate());
//                        break;
//                }
//
//                if (individualTariffEntities.get(i).getTpType().equals("variant")) {
//                    serviceTableDto.getVariantParams().add(paramDto);
//                } else if (individualTariffEntities.get(i).getTpType().equals("column")
//                        || individualTariffEntities.get(i).getTpType().equals("line")
//                        || individualTariffEntities.get(i).getTpType().equals("value")) {
//                    ServiceCellDto cellDto;
//
//                    if (serviceTableDto.getCells().size() != 0
//                            && serviceTableDto.getCells().get(serviceTableDto.getCells().size() - 1).getColumn() == individualTariffEntities.get(i).getTvcOrderColumn()
//                            && serviceTableDto.getCells().get(serviceTableDto.getCells().size() - 1).getLine() == individualTariffEntities.get(i).getTvcOrderLine()) {
//                        cellDto = serviceTableDto.getCells().get(serviceTableDto.getCells().size() - 1);
//                    } else {
//                        cellDto = new ServiceCellDto();
//                        cellDto.setColumn(individualTariffEntities.get(i).getTvcOrderColumn());
//                        cellDto.setLine(individualTariffEntities.get(i).getTvcOrderLine());
//                        serviceTableDto.getCells().add(cellDto);
//                    }
//                    switch (individualTariffEntities.get(i).getTpType()) {
//                        case "column":
//                            cellDto.getColumnParams().add(paramDto);
//                            break;
//                        case "line":
//                            cellDto.getLineParams().add(paramDto);
//                            break;
//                        case "value":
//                            cellDto.getValueParams().add(paramDto);
//                            break;
//                    }
//                }
//
//
//            }
//            return individualTariffDto;
//        }
//        return null;
//    }


}
