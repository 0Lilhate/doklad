/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.it_alnc.packagesearch.dto.*;
import ru.it_alnc.packagesearch.dto.FL.FLLightSubscribeDto;
import ru.it_alnc.packagesearch.dto.FL.LightRequestDto;
import ru.it_alnc.packagesearch.dto.checkInfo.CheckInfoPackageDto;
import ru.it_alnc.packagesearch.dto.checkInfo.CheckInfoRequestDto;
import ru.it_alnc.packagesearch.dto.checkInfo.CheckInfoServiceLineDto;
import ru.it_alnc.packagesearch.dto.subscribe.*;
import ru.it_alnc.packagesearch.entity.subscribe.TariffGetTariffEntity;
import ru.it_alnc.packagesearch.entity.subscribe.TariffLineEntity;
import ru.it_alnc.packagesearch.entity.subscribe.TariffLineParamEntity;
import ru.it_alnc.packagesearch.entity.subscribe.types.RateTypeEntity;
import ru.it_alnc.packagesearch.exception.CustomErrorMessageException;
import ru.it_alnc.packagesearch.exception.NoBranchGroupException;
import ru.it_alnc.packagesearch.exception.NonExistPUException;
import ru.it_alnc.packagesearch.exception.NonExistServiceException;
import ru.it_alnc.packagesearch.query.subscribe.SubscribeQueryBuilder;
import ru.it_alnc.packagesearch.repository.RateTypeRepository;
import ru.it_alnc.packagesearch.repository.TariffRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SubscribeService {

    private final SubscribeQueryBuilder subscribeQueryBuilder = ApplicationContextProvider.getBean(SubscribeQueryBuilder.class);

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private RateTypeRepository rateTypeRepository;

    @Autowired
    private ExternalHandbookService externalHandbookService;

    public void initSQBWithRequest(SubscribeRequest27277 request27277) throws CloneNotSupportedException {
        this.subscribeQueryBuilder.init(request27277);
    }

    public void initSQBWithRequest(IndividualTariffRequest individualTariffRequest) throws CloneNotSupportedException {
        this.subscribeQueryBuilder.init(individualTariffRequest);
    }

    public void initSQBWithRequest(LightRequestDto lightRequestDto) throws CloneNotSupportedException {
        this.subscribeQueryBuilder.init(lightRequestDto);
    }

    public void initSQBWithRequest(CheckInfoRequestDto checkInfoRequestDto) throws CloneNotSupportedException {
        this.subscribeQueryBuilder.init(checkInfoRequestDto);
    }

    public void initSQBWithRequest(PackageListRequestDto packageListRequestDto) throws CloneNotSupportedException {
        this.subscribeQueryBuilder.init(packageListRequestDto);
    }

    public void initSQBWithRequest(LightSubscribeRequestDto lightSubscribeRequestDto) throws CloneNotSupportedException {
        this.subscribeQueryBuilder.init(lightSubscribeRequestDto);
    }

    public TariffDto findOneTariffLine(GetTariffDto getTariffDto) {
        if (getTariffDto.getCode() == null) return null; //проверка на наличие TariffCode в запросе
        TariffDto result = new TariffDto();
        List<TariffGetTariffEntity> tariffEntityList;
        Set<TariffLineEntity> tariffLineEntitySet = new HashSet<>();
        if (getTariffDto.getBegDate() != null) {
            tariffEntityList = tariffRepository.findAllByCodeAndBegDateEqualsAndStatus(getTariffDto.getCode(), getTariffDto.getBegDate(), "Approved");
        } else {
            tariffEntityList = tariffRepository.findAllByCodeAndStatusEquals(getTariffDto.getCode(), "Approved");
        }
        if (tariffEntityList.isEmpty()) return null;
        //берём только нужные TariffLines
        LocalDateTime targetDate = LocalDateTime.now();
        if (getTariffDto.getBegDate() != null) {
            targetDate = getTariffDto.getBegDate();
        }
        LocalDateTime finalTargetDate = targetDate;
        Optional<TariffGetTariffEntity> optionalTariffLineEntity = tariffEntityList.stream().filter(
                tariffGetTariffEntity -> (tariffGetTariffEntity.getBegDate().isBefore(finalTargetDate) || tariffGetTariffEntity.getBegDate().isEqual(finalTargetDate))
                        && (tariffGetTariffEntity.getEndDate().isEqual(finalTargetDate) || tariffGetTariffEntity.getEndDate().isAfter(finalTargetDate))
        ).findFirst();
        if (optionalTariffLineEntity.isEmpty()) return null;
        if (getTariffDto.getLineSortOrder() != null) {
            for (TariffLineEntity tariffLineEntity : optionalTariffLineEntity.get().getTariffLines()) {
                if (getTariffDto.getLineSortOrder().contains(tariffLineEntity.getSortOrder())) {
                    tariffLineEntitySet.add(tariffLineEntity);
                }
            }
        } else {
            tariffLineEntitySet = optionalTariffLineEntity.get().getTariffLines();
        }
        //Set<TariffLineEntity> tariffLines = tariffLineEntitySet;
        List<TariffLineDto> tariffLineDtoList = new ArrayList<>();
        for (TariffLineEntity tariffLineEntity : tariffLineEntitySet) {
            TariffLineDto tariffLineDto = new TariffLineDto();
            List<TariffParamDto30214> paramDto30214List = new ArrayList<>();
            //заполняем tariffParamDto и кладём в List, после кладём в TariffLine
            for (TariffLineParamEntity tariffLineParamEntity : tariffLineEntity.getTariffLineParams()) {

                if (tariffLineParamEntity.getTariffPart() == 4)
                    continue;

                TariffParamDto30214 tariffParamDto = new TariffParamDto30214();
                tariffParamDto.setTariffPart(tariffLineParamEntity.getTariffPart());
                tariffParamDto.setOrder(tariffLineParamEntity.getOrder());
                tariffParamDto.setCode(tariffLineParamEntity.getCode());
                tariffParamDto.setLabel(tariffLineParamEntity.getLabel());
                tariffParamDto.setGuide(tariffLineParamEntity.getGuide());
                tariffParamDto.setType(tariffLineParamEntity.getType());
                tariffParamDto.setVType(tariffLineParamEntity.getVType());
                tariffParamDto.setVTypeLabel(tariffLineParamEntity.getVTypeLabel());

                tariffParamDto.setVBoolean(tariffLineParamEntity.getVBoolean());
                tariffParamDto.setVString(tariffLineParamEntity.getVString());

                tariffParamDto.setVDate(mapToZonedDateTime(tariffLineParamEntity.getVDate()));
                tariffParamDto.setVDateMin(mapToZonedDateTime(tariffLineParamEntity.getVDateMin()));
                tariffParamDto.setVDateMax(mapToZonedDateTime(tariffLineParamEntity.getVDateMax()));

                tariffParamDto.setVInt(tariffLineParamEntity.getVInt());
                tariffParamDto.setVIntMin(tariffLineParamEntity.getVIntMin());
                tariffParamDto.setVIntMax(tariffLineParamEntity.getVIntMax());

                tariffParamDto.setVFloat(tariffLineParamEntity.getVFloat());
                tariffParamDto.setVFloatMax(tariffLineParamEntity.getVFloatMax());
                tariffParamDto.setVFloatMin(tariffLineParamEntity.getVFloatMin());

                tariffParamDto.setVCodePattern(tariffLineParamEntity.getVCodePattern());
                tariffParamDto.setVCodeList(tariffLineParamEntity.getVCodeList());
                tariffParamDto.setVLabelList(tariffLineParamEntity.getVLabelList());
                paramDto30214List.add(tariffParamDto);
            } //заполнили paramList
            tariffLineDto.setOrder(tariffLineEntity.getSortOrder());
            tariffLineDto.setCurrency(tariffLineEntity.getCurrency());
            tariffLineDto.setAmountFix(tariffLineEntity.getAmountFix());
            tariffLineDto.setRate(tariffLineEntity.getRate());
            tariffLineDto.setAmountMin(tariffLineEntity.getAmountMin());
            tariffLineDto.setAmountMax(tariffLineEntity.getAmountMax());
            tariffLineDto.setEntryCurrency(tariffLineEntity.getEntryCurrency());
            tariffLineDto.setPreferenceCode(tariffLineEntity.getPreferenceCode());
            tariffLineDto.setPreferenceLabel(tariffLineEntity.getPreferenceLabel());
            tariffLineDto.setParamList(paramDto30214List);
            tariffLineDtoList.add(tariffLineDto);
        } //заполняем тариф
        result.setCode(optionalTariffLineEntity.get().getCode());
        result.setLabel(optionalTariffLineEntity.get().getLabel());
        result.setBegDate(optionalTariffLineEntity.get().getBegDate().atZone(ZoneId.systemDefault()));
        result.setEndDate(optionalTariffLineEntity.get().getEndDate().atZone(ZoneId.systemDefault()));
        result.setIsSimple(optionalTariffLineEntity.get().isSimple());
        result.setRateType(optionalTariffLineEntity.get().getRateType());
        result.setEntryCurrency(optionalTariffLineEntity.get().getEntryCurrency());
        result.setEntryRateType(optionalTariffLineEntity.get().getEntryRateType());
        result.setAllLineCurrency(optionalTariffLineEntity.get().getAllLineCurrency());
        result.setTariffLineList(tariffLineDtoList);
        return result; //возвращаем тариф
    }

    //30698 lightweight response
    public ServicePackageList searchServicePackagesInfo(SubscribeRequest27277 subscribeRequest, Boolean isSkipPuParams) throws NonExistPUException, NoBranchGroupException, CloneNotSupportedException, NonExistServiceException {
        ServicePackageList result = searchServicePackages(subscribeRequest);

        if (subscribeRequest.getPackageCodes() == null || subscribeRequest.getPackageCodes().size() == 0)
            subscribeQueryBuilder.filterByHavingOwnPrice();
        result.setServicePackageList(subscribeQueryBuilder.getSubscribes());

        List<SubscribeDto27277> trimmedSubscribeList = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : result.getServicePackageList()) {
            SubscribeDto27277 trimmedSubscribeDto = subscribeDto.clone();  //todo: а клонирование тут вообще зачем, если мы режем данные и после их не используем? попробовать убрать
            if (isSkipPuParams)
                trimmedSubscribeDto.setPuParamList(null);
            trimmedSubscribeDto.setPriority(null);
            trimmedSubscribeDto.setParentCode(null);
            trimmedSubscribeDto.setIsIndependent(null);
            trimmedSubscribeDto.setIsLinked2Account(null);
            trimmedSubscribeDto.setIsCloseOnAccount(null);
            trimmedSubscribeDto.setIsMultyAccount(null);
            trimmedSubscribeDto.setClientCommonType(null);
            trimmedSubscribeDto.setClientTypeList(null);
            trimmedSubscribeDto.setSegmentList(null);
            trimmedSubscribeDto.setAccountTypeList(null);
            trimmedSubscribeDto.setBranchList(null);
            trimmedSubscribeDto.setServiceGroupList(null);
            trimmedSubscribeList.add(trimmedSubscribeDto);
        }
        result.setServicePackageList(trimmedSubscribeList);
        return result;
    }

    public ServicePackageList searchServicePackages(SubscribeRequest27277 subscribeRequest) throws CloneNotSupportedException, NonExistPUException, NonExistServiceException, NoBranchGroupException {
        log.debug("Search start");
        ServicePackageList result = new ServicePackageList();


        log.debug("\tSQB init {} finish init subscribe list size={}", Integer.toHexString(subscribeQueryBuilder.hashCode()), subscribeQueryBuilder.getSubscribes().size());
        List<SubscribeDto27277> subscribes = searchSubscribes(subscribeRequest);
        result.setServicePackageList(subscribes);
        log.debug("\tSubscribe filtering finish");

        if ((subscribeRequest.getIndividualTariffCodes() != null && !subscribeRequest.getIndividualTariffCodes().isEmpty())
                || subscribeRequest.isGetIndividualTariff()) {
            List<IndividualTariffDto27277> tariffs = searchTariffs(subscribeRequest);
            result.setIndividualTariffList(tariffs);
            log.debug("\tTariff filtering finish");
        }

        try {
            if (subscribeRequest.isSkipPackageFilter() || subscribeRequest.isSkipService() || subscribeRequest.isSkipPackageParam()
                    || subscribeRequest.isSkipServiceParam() || subscribeRequest.isSkipServiceCounter()
                    || subscribeRequest.isSkipServiceTariffLine())
                removePackageParams(result, subscribeRequest);

            if (!subscribeRequest.getIsFull()) {
                cleanseResponse(result);
            }
            if (!subscribeRequest.isGetActionOnPUChange()) {
                removeActionOnPUChange(result);
            }
            log.debug("Search finish");
        } catch (CloneNotSupportedException e) {
            log.error("Can't cleanse response:");
            e.printStackTrace();
        }
        if (subscribeRequest.isReturnBasicGuide()) setBasicGuideToVCodeList(result);
        return result;
    }

    private void removeActionOnPUChange(ServicePackageList servicePackageList) throws CloneNotSupportedException {
        List<SubscribeDto27277> subscribes = new ArrayList<>();
        List<IndividualTariffDto27277> tariffs = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : servicePackageList.getServicePackageList()) {
            SubscribeDto27277 subscribeDtoCopy = subscribeDto.clone();
            subscribeDtoCopy.setActionOnPUChangeList(null);
            subscribes.add(subscribeDtoCopy);
        }
        servicePackageList.setServicePackageList(subscribes);
    }

    public List<IndividualTariffDto27277> searchTariffs(SubscribeRequest27277 subscribeRequest) throws CloneNotSupportedException, NonExistPUException {

        if (subscribeRequest.getIndividualTariffCodes() != null && !subscribeRequest.getIndividualTariffCodes().isEmpty()) {
            subscribeQueryBuilder.filterTariffsByCodes(subscribeRequest.getIndividualTariffCodes());

            if (subscribeRequest.isForceServiceFilter()) {
                if (subscribeRequest.getServiceGroups() != null && !subscribeRequest.getServiceGroups().isEmpty()) {
                    subscribeQueryBuilder.filterTariffsByServiceGroups(subscribeRequest.getServiceGroups());
                }
                if (subscribeRequest.getServices() != null && !subscribeRequest.getServices().isEmpty()) {
                    subscribeQueryBuilder.filterTariffsByServices(subscribeRequest.getServices());
                }
            }
        } else {
            if (subscribeRequest.getServiceGroups() != null && !subscribeRequest.getServiceGroups().isEmpty()) {
                subscribeQueryBuilder.filterTariffsByServiceGroups(subscribeRequest.getServiceGroups());
            }
            if (subscribeRequest.getServices() != null && !subscribeRequest.getServices().isEmpty()) {
                subscribeQueryBuilder.filterTariffsByServices(subscribeRequest.getServices());
            }
        }
        if (subscribeRequest.isOnlySimpleTariffs()) {
            subscribeQueryBuilder.filterTariffsByIsSimple();
        }

        if (subscribeRequest.getClientParams() != null) {
            Optional<SubscribeRequest27277.ParamMap> optionalCommonType = subscribeRequest.getClientParams()
                    .stream().filter(x -> x.getCode().equals("COMMONTYPE")).findFirst();

            //28774 mega insane crutch
            optionalCommonType.ifPresent(paramMap -> {
                if (paramMap.getValue().toUpperCase().equals("P")) {
                    log.warn("Translating P commontype to U for ind tariffs");
                    paramMap.setValue("U");
                }
            });

            optionalCommonType.ifPresent(paramMap -> subscribeQueryBuilder.filterTariffsByClientCommonType(List.of(paramMap.getValue())));
        }

        if (subscribeRequest.isSkipService())
            subscribeQueryBuilder.filterTariffsByServiceType(Set.of("3", "4"));

        //todo:когда нибудь утащить это в sqb
        for (IndividualTariffDto27277 individualTariffDto27277 : subscribeQueryBuilder.getTariffs()) {
            if (individualTariffDto27277.getServiceLine().getServiceType().equals("4"))
                individualTariffDto27277.getServiceLine().setServiceType("3");
            individualTariffDto27277.getServiceLine().setPu4PriceService(individualTariffDto27277.getServiceLine().getPuServiceGroupProductCode());
            individualTariffDto27277.getServiceLine().setServiceTypeLabel("Цена своего Пакета");
        }
        return subscribeQueryBuilder.getTariffs();
    }

    public List<IndividualTariffDto27277> searchIndividualTariffs(IndividualTariffRequest individualTariffRequest) throws CloneNotSupportedException {


        if (individualTariffRequest.getIndividualTariffCodes() != null && !individualTariffRequest.getIndividualTariffCodes().isEmpty()) {
            return subscribeQueryBuilder.searchTariffsByCodes30737(individualTariffRequest.getIndividualTariffCodes());
        }

        if (individualTariffRequest.getServices() != null && individualTariffRequest.getServices().size() != 0) {
            subscribeQueryBuilder.filterTariffsByServices(individualTariffRequest.getServices());
        }

        if (individualTariffRequest.isOnlySimpleTariffs()) {
            subscribeQueryBuilder.filterTariffsByIsSimple();
        }
        return subscribeQueryBuilder.getTariffs();
    }

    public List<SubscribeDto27277> searchSubscribes(SubscribeRequest27277 subscribeRequest) throws CloneNotSupportedException, NonExistPUException, NonExistServiceException, NoBranchGroupException {
        //todo (после окончательной сборки алгоритма) - вынести хранение загруженных подписок в кафку
        if (subscribeRequest.getClientPUCodesAsList() != null && !subscribeRequest.getClientPUCodesAsList().isEmpty())
            this.subscribeQueryBuilder.checkClientPuCodes(subscribeRequest.getClientPUCodesAsList());
        this.subscribeQueryBuilder.addPriceInOtherPu(subscribeRequest);
        log.debug("\t\tfilter PriceInOtherPu finish, {} subscribes list size={}", Integer.toHexString(this.subscribeQueryBuilder.hashCode()), this.subscribeQueryBuilder.getSubscribes().size());

        if (subscribeRequest.getPackageCodes() != null && !subscribeRequest.getPackageCodes().isEmpty()) {
            this.subscribeQueryBuilder.filterByCodes(subscribeRequest.getPackageCodes());
            log.debug("\t\tfilter codes finish, {} subscribes list size={}", Integer.toHexString(this.subscribeQueryBuilder.hashCode()), this.subscribeQueryBuilder.getSubscribes().size());

            if (subscribeRequest.isForceServiceFilter()
                    && subscribeRequest.getServices() != null && !subscribeRequest.getServices().isEmpty()) {
                this.subscribeQueryBuilder.filterByServiceLines(subscribeRequest.getServices());
                log.debug("\t\tfilter serviceLine finish, {} subscribes list size={}", Integer.toHexString(this.subscribeQueryBuilder.hashCode()), this.subscribeQueryBuilder.getSubscribes().size());
            }

            //29462 filter for package code list
            //todo: проверить что все вот эти проверки ТОЧНО НЕ ЗАВИСЯТ ОТ ГРУППИРОВОЧНЫХ СПРАВОЧНИКОВ!
            if (subscribeRequest.getClientParams() != null && subscribeRequest.isCheckPackageCode()) {
                Optional<SubscribeRequest27277.ParamMap> optionalClientType = subscribeRequest.getClientParams()
                        .stream().filter(x -> x.getCode().equals("TYPE")).findFirst();
                Optional<SubscribeRequest27277.ParamMap> optionalClientSegment = subscribeRequest.getClientParams()
                        .stream().filter(x -> x.getCode().equals("SEGMENT")).findFirst();
                Optional<SubscribeRequest27277.ParamMap> optionalCommonType = subscribeRequest.getClientParams()
                        .stream().filter(x -> x.getCode().equals("COMMONTYPE")).findFirst();

                //28774 mega insane crutch
                optionalCommonType.ifPresent(paramMap -> {
                    if ("P".equals(paramMap.getValue().toUpperCase())) {
                        log.warn("Translating P commontype to U");
                        paramMap.setValue("U");
                    }
                });

                optionalClientType.ifPresent(paramMap -> this.subscribeQueryBuilder.filterByClientType(paramMap.getValue()));
                optionalClientSegment.ifPresent(paramMap -> this.subscribeQueryBuilder.filterByClientSegment(paramMap.getValue()));
                optionalCommonType.ifPresent(paramMap -> this.subscribeQueryBuilder.filterByClientCommonType(List.of(paramMap.getValue())));
                log.debug("\t\tfilters client type, segment and commonType finish, {} subscribes list size={}", Integer.toHexString(this.subscribeQueryBuilder.hashCode()), this.subscribeQueryBuilder.getSubscribes().size());
            }

            if (subscribeRequest.getAccountParams() != null && subscribeRequest.isCheckPackageCode()) {
                Optional<SubscribeRequest27277.ParamMap> optionalAccountBranch = subscribeRequest.getAccountParams()
                        .stream().filter(x -> x.getCode().equals("BRANCH")).findFirst();
                Optional<SubscribeRequest27277.ParamMap> optionalAccountType = subscribeRequest.getAccountParams()
                        .stream().filter(x -> x.getCode().equals("TYPE")).findFirst();

                if (optionalAccountBranch.isPresent() && !optionalAccountBranch.get().getValue().isEmpty()) {
                    String branchGroup = resolveBranchIntoBranchGroup(optionalAccountBranch.get().getValue());
                    subscribeQueryBuilder.filterByAccountBranch(optionalAccountBranch.get().getValue(), branchGroup);
                }
                optionalAccountType.ifPresent(paramMap -> subscribeQueryBuilder.filterByAccountType(paramMap.getValue()));
                log.debug("\t\tfilters account branch and accountType finish, {} subscribes list size={}", Integer.toHexString(this.subscribeQueryBuilder.hashCode()), this.subscribeQueryBuilder.getSubscribes().size());
            }

            if ((subscribeRequest.getClientParams() != null || subscribeRequest.getAccountParams() != null
                    || subscribeRequest.getCardParams() != null || subscribeRequest.getOperationParams() != null
                    || subscribeRequest.getProductParams() != null)
                //    && subscribeRequest.isCheckPackageCode()
            ) {
                this.subscribeQueryBuilder.presetParamsFilter(subscribeRequest);
                log.debug("\t\tfilter preset params finish, {} subscribes list size={}", Integer.toHexString(this.subscribeQueryBuilder.hashCode()), this.subscribeQueryBuilder.getSubscribes().size());
            }
            if (subscribeRequest.getServiceTypes() != null && !subscribeRequest.getServiceTypes().isEmpty()) {
                this.subscribeQueryBuilder.filterServiceLinesByTypesInPackages(subscribeRequest.getServiceTypes());
            }
            if (subscribeRequest.isCheckGraceBySource()) {
                this.subscribeQueryBuilder.resolveGracePeriods(subscribeRequest);
            }
            return this.subscribeQueryBuilder.getSubscribes();
        }

        if (subscribeRequest.getServices() != null && subscribeRequest.getServices().size() != 0)
            this.subscribeQueryBuilder.checkServiceCodes(subscribeRequest.getServices());
        log.debug("\t\tfilter ServiceCode finish, {} subscribes list size={}", Integer.toHexString(this.subscribeQueryBuilder.hashCode()), this.subscribeQueryBuilder.getSubscribes().size());
        if (subscribeRequest.isSkipDependentPUIfNoPUCode() && (subscribeRequest.getClientPUCodesAsList() == null || subscribeRequest.getClientPUCodesAsList().isEmpty())) {
            this.subscribeQueryBuilder.filterDependentPUs();
        }
        log.debug("\t\tfilter SkipDependentPUIfNoPUCode finish, {} subscribes list size={}", Integer.toHexString(this.subscribeQueryBuilder.hashCode()), this.subscribeQueryBuilder.getSubscribes().size());

        if (subscribeRequest.isMarketing()) {
            this.subscribeQueryBuilder.filterByMarketing(subscribeRequest.isMarketing());
        }
//        if (subscribeRequest.getClientPUCode() != null) {
//            this.subscribeQueryBuilder.filterByClientPUCode(subscribeRequest.getClientPUCode());
//        }
        log.debug("\t\tfilter isMarketing finish, {} subscribes list size={}", Integer.toHexString(this.subscribeQueryBuilder.hashCode()), this.subscribeQueryBuilder.getSubscribes().size());

        if (subscribeRequest.getPackageCategories() != null && subscribeRequest.getPackageCategories().size() != 0) {
            this.subscribeQueryBuilder.filterByCategory(subscribeRequest.getPackageCategories());
        }
        if (subscribeRequest.getServiceGroups() != null && subscribeRequest.getServiceGroups().size() != 0) {
            this.subscribeQueryBuilder.filterByServiceGroups(subscribeRequest.getServiceGroups());
        }
        if (subscribeRequest.getServices() != null && subscribeRequest.getServices().size() != 0) {
            this.subscribeQueryBuilder.filterByServiceLines(subscribeRequest.getServices());
        }
        log.debug("\t\tfilter cat, SG and serviceLine finish, {} subscribes list size={}", Integer.toHexString(this.subscribeQueryBuilder.hashCode()), this.subscribeQueryBuilder.getSubscribes().size());
        if (subscribeRequest.getClientParams() != null) {
            Optional<SubscribeRequest27277.ParamMap> optionalClientType = subscribeRequest.getClientParams()
                    .stream().filter(x -> x.getCode().equals("TYPE")).findFirst();
            Optional<SubscribeRequest27277.ParamMap> optionalClientSegment = subscribeRequest.getClientParams()
                    .stream().filter(x -> x.getCode().equals("SEGMENT")).findFirst();
            Optional<SubscribeRequest27277.ParamMap> optionalCommonType = subscribeRequest.getClientParams()
                    .stream().filter(x -> x.getCode().equals("COMMONTYPE")).findFirst();

            //28774 mega insane crutch
            optionalCommonType.ifPresent(paramMap -> {
                if (paramMap.getValue().toUpperCase().equals("P")) {
                    log.warn("Translating P commontype to U");
                    paramMap.setValue("U");
                }
            });

            optionalClientType.ifPresent(paramMap -> this.subscribeQueryBuilder.filterByClientType(paramMap.getValue()));
            optionalClientSegment.ifPresent(paramMap -> this.subscribeQueryBuilder.filterByClientSegment(paramMap.getValue()));
            optionalCommonType.ifPresent(paramMap -> this.subscribeQueryBuilder.filterByClientCommonType(List.of(paramMap.getValue())));
        }

        if (subscribeRequest.getAccountParams() != null) {
            Optional<SubscribeRequest27277.ParamMap> optionalAccountBranch = subscribeRequest.getAccountParams()
                    .stream().filter(x -> x.getCode().equals("BRANCH")).findFirst();
            Optional<SubscribeRequest27277.ParamMap> optionalAccountType = subscribeRequest.getAccountParams()
                    .stream().filter(x -> x.getCode().equals("TYPE")).findFirst();

            if (optionalAccountBranch.isPresent() && !optionalAccountBranch.get().getValue().isEmpty()) {
                String branchGroup = resolveBranchIntoBranchGroup(optionalAccountBranch.get().getValue());
                subscribeQueryBuilder.filterByAccountBranch(optionalAccountBranch.get().getValue(), branchGroup);
            }
            optionalAccountType.ifPresent(paramMap -> subscribeQueryBuilder.filterByAccountType(paramMap.getValue()));
        }

        if ((subscribeRequest.getClientParams() != null || subscribeRequest.getAccountParams() != null
                || subscribeRequest.getCardParams() != null || subscribeRequest.getOperationParams() != null
                || subscribeRequest.getProductParams() != null)) {
            this.subscribeQueryBuilder.presetParamsFilter(subscribeRequest);
            log.debug("\t\tfilter preset params finish, {} subscribes list size={}", Integer.toHexString(this.subscribeQueryBuilder.hashCode()), this.subscribeQueryBuilder.getSubscribes().size());
        }
        if (subscribeRequest.getServiceTypes() != null && !subscribeRequest.getServiceTypes().isEmpty()) {
            this.subscribeQueryBuilder.filterServiceLinesByTypesInPackages(subscribeRequest.getServiceTypes());
        }
        if (subscribeRequest.isCheckGraceBySource()) {
            this.subscribeQueryBuilder.resolveGracePeriods(subscribeRequest);
        }
        return this.subscribeQueryBuilder.getSubscribes();
//        throw new UnsupportedOperationException("Under construction");
    }

    //remove field that dont need in isFull=0 response
    private void cleanseResponse(ServicePackageList servicePackageList) throws CloneNotSupportedException {
        List<SubscribeDto27277> subscribes = new ArrayList<>();
        List<IndividualTariffDto27277> tariffs = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : servicePackageList.getServicePackageList()) {
            SubscribeDto27277 subscribeDtoCopy = subscribeDto.clone();
            subscribeDtoCopy.setDescription(null);
            subscribeDtoCopy.setEndDate(null);
            subscribeDtoCopy.setBegDate(null);
            subscribeDtoCopy.setClientCommonType(null);
            subscribeDtoCopy.setParentCode(null);
            subscribeDtoCopy.setClientTypeList(null);
            subscribeDtoCopy.setSegmentList(null);
            subscribeDtoCopy.setAccountTypeList(null);
            subscribeDtoCopy.setBranchList(null);
            subscribeDtoCopy.setFilterParamList(null);

            if (subscribeDtoCopy.getPuParamList() != null)
                for (PuParamDto puParamDto : subscribeDtoCopy.getPuParamList()) {
                    puParamDto.setPuGroupLabel(null);
                    puParamDto.setOrder(null);
                }

            if (subscribeDtoCopy.getServiceGroupList() != null)
                for (ServiceGroupDto serviceGroupDto : subscribeDtoCopy.getServiceGroupList()) {
                    serviceGroupDto.setOrder(null);
                    serviceGroupDto.setLabel(null);

                    if (serviceGroupDto.getServiceLineList() != null)
                        for (ServiceLineDto serviceLineDto : serviceGroupDto.getServiceLineList()) {
                            cleanseServiceLineDto(serviceLineDto);
                        }
                }
            subscribes.add(subscribeDtoCopy);
        }

        if (servicePackageList.getIndividualTariffList() != null)
            for (IndividualTariffDto27277 individualTariffDto : servicePackageList.getIndividualTariffList()) {
                IndividualTariffDto27277 individualTariffCopy = individualTariffDto.clone();
                cleanseServiceLineDto(individualTariffCopy.getServiceLine());
                tariffs.add(individualTariffCopy);
            }

        servicePackageList.setServicePackageList(subscribes);
        servicePackageList.setIndividualTariffList(tariffs);
    }

    private String resolveBranchIntoBranchGroup(String branchCode) throws NoBranchGroupException {
        log.debug("Resolving branch {} into group", branchCode);
        List<HandbookDto> branchHadbook = Optional.ofNullable(externalHandbookService.getHandbook("Branch"))
                .orElse(Collections.emptyList());
        String tariffGroupCode = "";
        for (HandbookDto handbookDto : branchHadbook) {
            Optional<DescriptorDto> optDesc = handbookDto.getDescriptors().stream()
                    .filter(desc -> desc.getRefName().equals("CatalogCode") && desc.getValue() != null && desc.getValue().equals(branchCode))
                    .findFirst();
            if (optDesc.isPresent()) {
                tariffGroupCode = handbookDto.getDescriptors().stream()
                        .filter(desc -> desc.getRefName().equals("LINKBRANCH") && desc.getValue() != null).findFirst()
                        .orElseThrow(() -> new NoBranchGroupException("No branch tariff group for " + branchCode))
                        .getValue();
                break;
            }
        }
        String finalTariffGroupCode = tariffGroupCode;
        HandbookDto branchGroupHdbkDto = branchHadbook.stream()
                .filter(hdbk -> hdbk.getId().equals(finalTariffGroupCode)).findFirst()
                .orElseThrow(() -> new NoBranchGroupException("Non-existing branch tariff group " + finalTariffGroupCode));
        String branchGroupCode = branchGroupHdbkDto.getDescriptors().stream().filter(desc -> desc.getRefName().equals("CatalogCode")).findFirst()
                .orElseThrow(() -> new NoBranchGroupException("Non catalogCode in branch tariff group " + finalTariffGroupCode)).getValue();
        log.debug("{} resolved in {} branch group", branchCode, branchGroupCode);
        return branchGroupCode;
    }

    //29462 remove params that dont need in isSkipPackageParam=true response
    private void removePackageParams(ServicePackageList servicePackageList, SubscribeRequest27277 subscribeRequest) throws CloneNotSupportedException {
        List<SubscribeDto27277> subscribes = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto : servicePackageList.getServicePackageList()) {
            SubscribeDto27277 subscribeDtoCopy = subscribeDto.clone();

            if (subscribeRequest.isSkipPackageFilter()) {
                if (subscribeDtoCopy.getClientTypeList() != null)
                    subscribeDtoCopy.setClientTypeList(null);
                if (subscribeDtoCopy.getSegmentList() != null)
                    subscribeDtoCopy.setSegmentList(null);
                if (subscribeDtoCopy.getAccountTypeList() != null)
                    subscribeDtoCopy.setAccountTypeList(null);
                if (subscribeDtoCopy.getBranchList() != null)
                    subscribeDtoCopy.setBranchList(null);
                if (subscribeDtoCopy.getFilterParamList() != null)
                    subscribeDtoCopy.setFilterParamList(null);
            }

            if (subscribeDtoCopy.getPuParamList() != null && subscribeRequest.isSkipPackageParam())
                subscribeDtoCopy.setPuParamList(null);

            if (subscribeDtoCopy.getServiceGroupList() != null)
                for (ServiceGroupDto serviceGroupDto : subscribeDtoCopy.getServiceGroupList()) {

                    if (serviceGroupDto.getServiceLineList() != null) {
                        if (subscribeRequest.isSkipService()) {
                            List<ServiceLineDto> serviceLines = new ArrayList<>();
                            for (ServiceLineDto serviceLineDto : serviceGroupDto.getServiceLineList()) {
                                if (serviceLineDto.getServiceType().equals("3"))
                                    serviceLines.add(serviceLineDto.clone());
                                else if (subscribeDtoCopy.getIsIndependent() && serviceLineDto.getServiceType().equals("4"))
                                    serviceLines.add(serviceLineDto.clone());
                            }
                            serviceGroupDto.setServiceLineList(serviceLines);
                        }

                        for (ServiceLineDto serviceLineDto : serviceGroupDto.getServiceLineList()) {

                            if (serviceLineDto.getParamList() != null && subscribeRequest.isSkipServiceParam())
                                serviceLineDto.setParamList(null);

//                            if (serviceLineDto.getCounterList() != null && subscribeRequest.isSkipServiceCounter())
//                                serviceLineDto.setCounterList(null);

                            if (serviceLineDto.getTariffList() != null)
                                for (SubscribeTariffDto tariffDto : serviceLineDto.getTariffList()) {
                                    if (tariffDto.getSubscribeTariffLineDtoList() != null && subscribeRequest.isSkipServiceTariffLine())
                                        tariffDto.setSubscribeTariffLineDtoList(null);
/*
                                    for (SubscribeTariffLineDto tariffLineDto : tariffDto.getSubscribeTariffLineDtoList()) {
                                        if (tariffLineDto.getParamList() != null)
                                            tariffLineDto.setParamList(null);
                                    }
*/
                                }
                        }
                    }
                }
            subscribes.add(subscribeDtoCopy);
        }


        if (servicePackageList.getIndividualTariffList() != null && (subscribeRequest.isSkipServiceParam()
                || subscribeRequest.isSkipServiceCounter() || subscribeRequest.isSkipServiceTariffLine())) {
            List<IndividualTariffDto27277> individualTariffs = new ArrayList<>();
            for (IndividualTariffDto27277 individualTariffDto : servicePackageList.getIndividualTariffList()) {
                IndividualTariffDto27277 individualTariffDtoCopy = individualTariffDto.clone();
                if (individualTariffDtoCopy.getServiceLine().getParamList() != null && subscribeRequest.isSkipServiceParam())
                    individualTariffDtoCopy.getServiceLine().setParamList(null);

//                if (individualTariffDtoCopy.getServiceLine().getCounterList() != null && subscribeRequest.isSkipServiceCounter())
//                    individualTariffDtoCopy.getServiceLine().setCounterList(null);

                if (individualTariffDtoCopy.getServiceLine().getTariffList() != null)
                    for (SubscribeTariffDto tariffDto : individualTariffDtoCopy.getServiceLine().getTariffList()) {
                        if (tariffDto.getSubscribeTariffLineDtoList() != null && subscribeRequest.isSkipServiceTariffLine())
                            tariffDto.setSubscribeTariffLineDtoList(null);
                    }
                individualTariffs.add(individualTariffDtoCopy);
            }
            servicePackageList.setIndividualTariffList(individualTariffs);
        }
        servicePackageList.setServicePackageList(subscribes);
    }

    private void cleanseServiceLineDto(ServiceLineDto serviceLineDto) {
        serviceLineDto.setOrder(null);
        serviceLineDto.setIsDeleted(null);
        serviceLineDto.setBegDate(null);
        serviceLineDto.setEndDate(null);
        serviceLineDto.setContextType(null);
        serviceLineDto.setNdsRate(null);
        serviceLineDto.setIsNoEntry(null);
        serviceLineDto.setEntryEventType(null);
        serviceLineDto.setRateType(null);
        serviceLineDto.setIsSingleServiceInOper(null);
        serviceLineDto.setIsAggregateSummary(null);
        serviceLineDto.setAggregateSummaryNum(null);
        serviceLineDto.setParamOperAmount(null);
        serviceLineDto.setParamOperCurrency(null);
        serviceLineDto.setPeriodicPartUsedPayType(null);
        serviceLineDto.setIsPeriodicROO(null);
        serviceLineDto.setPeriodicROOWaitPeriod(null);
        serviceLineDto.setPeriodicROOWaitPeriodLabel(null);
        serviceLineDto.setIsPeriodicCheckOperations(null);
        serviceLineDto.setIsPeriodicCheckRest(null);
        serviceLineDto.setIsSplitGraceByPeriods(null);
        serviceLineDto.setPu4PriceService(null);
        //32068
        serviceLineDto.setTrialPeriodEndActionLabel(null);
        serviceLineDto.setTrialPeriodLabel(null);
        serviceLineDto.setPrefPeriodLabel(null);
        serviceLineDto.setPrefPeriodEndActionLabel(null);

        if (serviceLineDto.getParamList() != null)
            for (SubscribeParamDto paramDto : serviceLineDto.getParamList()) {
                paramDto.setOrder(null);
            }

//        if (serviceLineDto.getCounterList() != null)
//            for (CounterDto counterDto : serviceLineDto.getCounterList()) {
//                counterDto.setCode(null);
//                counterDto.setLabel(null);
//                counterDto.setParentCode(null);
//                counterDto.setIsUseQuantity(null);
//                counterDto.setIsUseSum(null);
//                counterDto.setIsUseFee(null);
//                counterDto.setIsUseNDS(null);
//                counterDto.setIsCollectOnAccount(null);
//                counterDto.setIsCollectOnPact(null);
//                counterDto.setIsCollectOnClient(null);
//                counterDto.setIsCollectOnClientGroup(null);
//                counterDto.setArchiveLength(null);
//                counterDto.setIsGetFromIT(null);
//                counterDto.setIsGetFromS(null);
//                counterDto.setIsGetFromPS(null);
//                counterDto.setIsGetFromTP(null);
//                counterDto.setIsPutToIT(null);
//                counterDto.setIsPutToS(null);
//                counterDto.setIsPutToPS(null);
//                counterDto.setIsPutToTP(null);
//            }

        if (serviceLineDto.getTariffList() != null)
            for (SubscribeTariffDto tariffDto : serviceLineDto.getTariffList()) {
                tariffDto.setLabel(null);
                tariffDto.setBegDate(null);
                tariffDto.setEndDate(null);
            }

        serviceLineDto.setCheckSetLineList(null);
    }
    /*27277
    подписки - продукты с категорией "TARIFF_PLAN", "SERVICE_PACKAGE", "SUBSCRIPTION"
    puParamList - списочные параметры продукта (v_productListParam)
    serviceLineList - нужна будет вьюха?
        tariffList - из табличной группы Тариф? (или таблицы tariff - tariffline? тогда тоже нужна вью для TariffLineParamValue)
        counterList - тарантул?
        parmList - списочные параметры из всех групп?
        checkSetLineList - таблица checksetline
    * */


    public List<FLLightSubscribeDto> packageLightSearch(LightRequestDto lightRequestDto) throws CloneNotSupportedException {
        return this.subscribeQueryBuilder.filterSubsForFL(lightRequestDto);
    }

    public List<CheckInfoPackageDto> getCheckInfoList(CheckInfoRequestDto requestDto) throws CustomErrorMessageException {
        this.subscribeQueryBuilder.filterByCodes(requestDto.getPackageCodes());
        log.debug("checkInfo filterBySubs done, size of subs is {}", this.subscribeQueryBuilder.getSubscribes().size());
        List<CheckInfoPackageDto> packageList = new ArrayList<>();
        for (SubscribeDto27277 subscribeDto27277 : this.subscribeQueryBuilder.getSubscribes()) {
            CheckInfoPackageDto packageDto = new CheckInfoPackageDto();
            packageDto.setPackageCode(subscribeDto27277.getCode());
            Optional<ServiceLineDto> serviceLineDtoOptional = this.subscribeQueryBuilder.getServiceLines().stream().filter(
                    serviceLineDto -> serviceLineDto.getObj().equals(subscribeDto27277.getId()) &&
                            serviceLineDto.getStatus().equals("Approved") &&
                            serviceLineDto.getServiceGuideItemCode().equals(requestDto.getService())
            ).findFirst();
            if (serviceLineDtoOptional.isEmpty()) {
                packageList.add(packageDto);
                continue;
            }
            CheckInfoServiceLineDto checkInfoServiceLineDto = new CheckInfoServiceLineDto();
            checkInfoServiceLineDto.setBegDate(serviceLineDtoOptional.get().getBegDate());
            checkInfoServiceLineDto.setEndDate(serviceLineDtoOptional.get().getEndDate());
            checkInfoServiceLineDto.setCheckSetLineList(serviceLineDtoOptional.get().getCheckSetLineList());
            packageDto.setServiceLine(checkInfoServiceLineDto);
            packageList.add(packageDto);

        }

        for (CheckInfoPackageDto checkInfoPackageDto : packageList) {
            if (checkInfoPackageDto.getServiceLine() == null) continue;
            checkSetVerifyParam(requestDto, checkInfoPackageDto.getServiceLine());
        }
        return packageList;
    }

    private void checkSetVerifyParam(CheckInfoRequestDto requestDto, CheckInfoServiceLineDto serviceLineDto) {
        for (CheckSetLineDto checkSetLineDto : serviceLineDto.getCheckSetLineList()) {
            if (checkSetLineDto.getParamList() != null && !checkSetLineDto.getParamList().isEmpty()) {
                for (SubscribeParamDto subscribeParamDto : checkSetLineDto.getParamList()) {
                    subscribeParamDto.setIsCheckPass(Boolean.TRUE);
                }
            }
            if (checkSetLineDto.getFilterParams() != null && !checkSetLineDto.getFilterParams().isEmpty()) {
                for (SubscribeFilterParamDto subscribeParamDto : checkSetLineDto.getFilterParams()) {
                    Optional<CheckInfoRequestDto.ParamMap> requestClientFilterParam = Optional.empty();
                    Optional<CheckInfoRequestDto.ParamMap> requestAccountFilterParam = Optional.empty();
                    Optional<CheckInfoRequestDto.ParamMap> requestCardFilterParam = Optional.empty();
                    Optional<CheckInfoRequestDto.ParamMap> requestOperationFilterParam = Optional.empty();

                    switch (subscribeParamDto.getObjectType()) {
                        case "CLIENT":
                            if (requestDto.getClientParams() != null)
                                requestClientFilterParam = requestDto.getClientParams()
                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                            if (!subscribeQueryBuilder.checkInfoCompareParam(requestClientFilterParam.orElse(new CheckInfoRequestDto.ParamMap()), subscribeParamDto)) {
                                Optional<SubscribeParamDto> subscribeParam = checkSetLineDto.getParamList().stream().filter(
                                                subscribeParamDto1 -> subscribeParamDto1.getCode().split("\\.")[1].equals(subscribeParamDto.getCode()) &&
                                                        subscribeParamDto1.getCode().split("\\.")[0].equals(subscribeParamDto.getObjectType()))
                                        .findFirst();
                                subscribeParam.ifPresent(paramDto -> paramDto.setIsCheckPass(Boolean.FALSE));
                                //log.debug("\t{} CheckSet from {} pre-set CLIENT filter param {} mismatch!",checkSetLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                            }
//                            else {
//                                subscribeParamDto.setIsCheckPass(Boolean.TRUE);
//                            }
                            break;
                        case "ACCOUNT":
                            if (requestDto.getAccountParams() != null)
                                requestAccountFilterParam = requestDto.getAccountParams()
                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                            if (!subscribeQueryBuilder.checkInfoCompareParam(requestAccountFilterParam.orElse(new CheckInfoRequestDto.ParamMap()), subscribeParamDto)) {
                                //log.debug("\t{} CheckSet from {} pre-set ACCOUNT filter param {} mismatch!",checkSetLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                Optional<SubscribeParamDto> subscribeParam = checkSetLineDto.getParamList().stream().filter(
                                                subscribeParamDto1 -> subscribeParamDto1.getCode().split("\\.")[1].equals(subscribeParamDto.getCode()) &&
                                                        subscribeParamDto1.getCode().split("\\.")[0].equals(subscribeParamDto.getObjectType()))
                                        .findFirst();
                                subscribeParam.ifPresent(paramDto -> paramDto.setIsCheckPass(Boolean.FALSE));
                            }
                            break;
                        case "CARD":
                            if (requestDto.getCardParams() != null)
                                requestCardFilterParam = requestDto.getCardParams()
                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                            if (!subscribeQueryBuilder.checkInfoCompareParam(requestCardFilterParam.orElse(new CheckInfoRequestDto.ParamMap()), subscribeParamDto)) {
                                //log.debug("\t{} CheckSet from {} pre-set CARD filter param {} mismatch!",checkSetLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                Optional<SubscribeParamDto> subscribeParam = checkSetLineDto.getParamList().stream().filter(
                                                subscribeParamDto1 -> subscribeParamDto1.getCode().split("\\.")[1].equals(subscribeParamDto.getCode()) &&
                                                        subscribeParamDto1.getCode().split("\\.")[0].equals(subscribeParamDto.getObjectType()))
                                        .findFirst();
                                subscribeParam.ifPresent(paramDto -> paramDto.setIsCheckPass(Boolean.FALSE));
                            }
                            break;
                        case "OPERATION":
                            if (requestDto.getOperationParams() != null)
                                requestOperationFilterParam = requestDto.getOperationParams()
                                        .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode())).findFirst();
                            if (!subscribeQueryBuilder.checkInfoCompareParam(requestOperationFilterParam.orElse(new CheckInfoRequestDto.ParamMap()), subscribeParamDto)) {
                                //log.debug("\t{} CheckSet from {} pre-set OPERATION filter param {} mismatch!",checkSetLineDto.getId(), subscribeDto.getCode(), subscribeParamDto.getCode());
                                Optional<SubscribeParamDto> subscribeParam = checkSetLineDto.getParamList().stream().filter(
                                                subscribeParamDto1 -> subscribeParamDto1.getCode().split("\\.")[1].equals(subscribeParamDto.getCode()) &&
                                                        subscribeParamDto1.getCode().split("\\.")[0].equals(subscribeParamDto.getObjectType()))
                                        .findFirst();
                                subscribeParam.ifPresent(paramDto -> paramDto.setIsCheckPass(Boolean.FALSE));
                            }
                            break;

                    }
                }
            }
        }
    }

    public List<LightSubscribeDto> getPackageList(PackageListRequestDto requestDto) {
        List<SubscribeDto27277> filteredSubs = this.subscribeQueryBuilder.getSubscribes().stream().filter(
                subscribeDto27277 -> requestDto.getClientCommonType().equals(subscribeDto27277.getClientCommonType()) &&
                        requestDto.getPackageCategory().equals(subscribeDto27277.getCategory())
        ).collect(Collectors.toCollection(ArrayList::new));
        List<LightSubscribeDto> lightSubscribeDtoList = new ArrayList<>();
        for (SubscribeDto27277 subDto : filteredSubs) {
            lightSubscribeDtoList.add(new LightSubscribeDto(subDto));
        }
        return lightSubscribeDtoList;
    }


    public List<RateTypeDto> getRateTypes(List<String> codes) {
        List<RateTypeDto> rateTypeDtoList = new ArrayList<>();
        List<RateTypeEntity> rateTypeEntityList = rateTypeRepository.findAllByStatusEqualsAndCodeIn("Approved", codes);
        for (RateTypeEntity rateTypeEntity : rateTypeEntityList) {
            rateTypeDtoList.add(mapRateType(rateTypeEntity));
        }
        return rateTypeDtoList;
    }

    public List<RateTypeDto> getRateTypes() {
        List<RateTypeDto> rateTypeDtoList = new ArrayList<>();
        List<RateTypeEntity> rateTypeEntityList = rateTypeRepository.findAllByStatusEquals("Approved");
        for (RateTypeEntity rateTypeEntity : rateTypeEntityList) {
            rateTypeDtoList.add(mapRateType(rateTypeEntity));
        }
        return rateTypeDtoList;
    }

    public RateTypeDto mapRateType(RateTypeEntity entity) {
        RateTypeDto rateTypeDto = new RateTypeDto();
        rateTypeDto.setCode(entity.getCode());
        rateTypeDto.setCrossCurrency(entity.getCrossCurrency());
        rateTypeDto.setType(entity.getType());
        rateTypeDto.setIsInner(entity.getIsInner().equals(1));
        rateTypeDto.setLabel(entity.getLabel());
        rateTypeDto.setCurrencyListWidget(entity.getCurrencyListWidget());
        rateTypeDto.setCurrencyList(entity.getCurrencyList());
        return rateTypeDto;
    }

    private void setBasicGuideToVCodeList(ServicePackageList result) {
        for (SubscribeDto27277 subscribeDto27277 : result.getServicePackageList()) {
            if (!subscribeDto27277.getPuParamList().isEmpty()) { //if тут не нужен
                for (PuParamDto puParamDto : subscribeDto27277.getPuParamList()) { //
                    if (puParamDto.getListOfGroupGuideDtos().isEmpty()) continue;
                    puParamDto.setVCodeList(puParamDto.getListOfGroupGuideDtos().stream().map(
                            GroupGuideDto::getLinkCodeList
                    ).collect(Collectors.joining(";")));
                }
            }
            if (!subscribeDto27277.getFilterParamList().isEmpty()) { //if тут не нужен
                for (SubscribeFilterParamDto subscribeFilterParamDto : subscribeDto27277.getFilterParamList()) {
                    if (subscribeFilterParamDto.getListOfGroupGuideDtos().isEmpty()) continue;
                    subscribeFilterParamDto.setVCodeList(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().map(
                            GroupGuideDto::getLinkCodeList
                    ).collect(Collectors.joining(";")));
                }
            }
            for (ServiceGroupDto serviceGroupDto : subscribeDto27277.getServiceGroupList()) {   //todo: остановился тут, а зачем я вообще тут? чистить SL?
                for (ServiceLineDto serviceLineDto : serviceGroupDto.getServiceLineList()) {
                    for (SubscribeFilterParamDto subscribeFilterParamDto : serviceLineDto.getFilterParams()) {
                        if (subscribeFilterParamDto.getListOfGroupGuideDtos().isEmpty()) continue;
                        subscribeFilterParamDto.setVCodeList(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().map(
                                GroupGuideDto::getLinkCodeList
                        ).collect(Collectors.joining(";")));
                    }
                    for (SubscribeTariffDto subscribeTariffDto : serviceLineDto.getTariffList()) {
                        for (SubscribeTariffLineDto tariffLineDto : subscribeTariffDto.getSubscribeTariffLineDtoList()) {
                            for (SubscribeFilterParamDto subscribeFilterParamDto : tariffLineDto.getFilterParams()) {
                                if (subscribeFilterParamDto.getListOfGroupGuideDtos().isEmpty()) continue;
                                subscribeFilterParamDto.setVCodeList(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().map(
                                        GroupGuideDto::getLinkCodeList
                                ).collect(Collectors.joining(";")));
                            }

                        }
                    }
                    for (CheckSetLineDto checkSetLineDto : serviceLineDto.getCheckSetLineList()) {
                        for (SubscribeFilterParamDto subscribeFilterParamDto : checkSetLineDto.getFilterParams()) {
                            if (subscribeFilterParamDto.getListOfGroupGuideDtos().isEmpty()) continue;
                            subscribeFilterParamDto.setVCodeList(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().map(
                                    GroupGuideDto::getLinkCodeList
                            ).collect(Collectors.joining(";")));
                        }
                    }
                }
            }

        }
        if (result.getIndividualTariffList() == null) return;
        for (IndividualTariffDto27277 individualTariffDto27277 : result.getIndividualTariffList()) {
            for (SubscribeFilterParamDto subscribeFilterParamDto : individualTariffDto27277.getServiceLine().getFilterParams()) {
                if (subscribeFilterParamDto.getListOfGroupGuideDtos().isEmpty()) continue;
                subscribeFilterParamDto.setVCodeList(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().map(
                        GroupGuideDto::getLinkCodeList
                ).collect(Collectors.joining(";")));
            }
            for (SubscribeTariffDto subscribeTariffDto : individualTariffDto27277.getServiceLine().getTariffList()) {
                for (SubscribeTariffLineDto tariffLineDto : subscribeTariffDto.getSubscribeTariffLineDtoList()) {
                    for (SubscribeFilterParamDto subscribeFilterParamDto : tariffLineDto.getFilterParams()) {
                        if (subscribeFilterParamDto.getListOfGroupGuideDtos().isEmpty()) continue;
                        subscribeFilterParamDto.setVCodeList(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().map(
                                GroupGuideDto::getLinkCodeList
                        ).collect(Collectors.joining(";")));
                    }

                }
            }
            for (CheckSetLineDto checkSetLineDto : individualTariffDto27277.getServiceLine().getCheckSetLineList()) {
                for (SubscribeFilterParamDto subscribeFilterParamDto : checkSetLineDto.getFilterParams()) {
                    if (subscribeFilterParamDto.getListOfGroupGuideDtos().isEmpty()) continue;
                    subscribeFilterParamDto.setVCodeList(subscribeFilterParamDto.getListOfGroupGuideDtos().stream().map(
                            GroupGuideDto::getLinkCodeList
                    ).collect(Collectors.joining(";")));
                }
            }
        }
    }

    private ZonedDateTime mapToZonedDateTime(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(ldt -> ZonedDateTime.of(ldt, ZoneId.systemDefault()))
                .orElse(null);
    }

    public LightServicePackageList getLightPackageList(LightSubscribeRequestDto lightSubscribeRequestDto) {
        ServicePackageList servicePackageList = getServicePackageListForLightRequest(lightSubscribeRequestDto);
        return cleanseResponseForLightRequest(servicePackageList);
    }

    private ServicePackageList getServicePackageListForLightRequest(LightSubscribeRequestDto lightSubscribeRequestDto) {
        ServicePackageList servicePackageList = new ServicePackageList();
        if (lightSubscribeRequestDto == null) {
            servicePackageList.setServicePackageList(subscribeQueryBuilder.getSubscribes());
            servicePackageList.setIndividualTariffList(subscribeQueryBuilder.getTariffs());
            return servicePackageList;
        }
        if (lightSubscribeRequestDto.getClientCommonType() != null && !lightSubscribeRequestDto.getClientCommonType().isEmpty())
            subscribeQueryBuilder.filterByClientCommonType(lightSubscribeRequestDto.getClientCommonType());
        if (lightSubscribeRequestDto.getPackageCategory() != null && !lightSubscribeRequestDto.getPackageCategory().isEmpty())
            subscribeQueryBuilder.filterByCategory(lightSubscribeRequestDto.getPackageCategory());
        servicePackageList.setServicePackageList(subscribeQueryBuilder.getSubscribes());

        if (lightSubscribeRequestDto.getIndividualTariff() == null ||
                (lightSubscribeRequestDto.getIndividualTariff().getGetIndividualTariff() != null &&
                        !lightSubscribeRequestDto.getIndividualTariff().getGetIndividualTariff()))
            return servicePackageList;

        if (lightSubscribeRequestDto.getClientCommonType() != null && !lightSubscribeRequestDto.getClientCommonType().isEmpty())
            subscribeQueryBuilder.filterTariffsByClientCommonType(lightSubscribeRequestDto.getClientCommonType());

        if (lightSubscribeRequestDto.getIndividualTariff().getService() != null &&
                !lightSubscribeRequestDto.getIndividualTariff().getService().isEmpty())
            subscribeQueryBuilder.filterTariffsByServices(lightSubscribeRequestDto.getIndividualTariff().getService());

        if (lightSubscribeRequestDto.getIndividualTariff().getServiceType() != null &&
                !lightSubscribeRequestDto.getIndividualTariff().getServiceType().isEmpty())
            subscribeQueryBuilder.filterTariffsByServiceType(lightSubscribeRequestDto.getIndividualTariff().getServiceType());

        servicePackageList.setIndividualTariffList(subscribeQueryBuilder.getTariffs());
        return servicePackageList;
    }

    private LightServicePackageList cleanseResponseForLightRequest(ServicePackageList servicePackageList) {
        LightServicePackageList lightServicePackageList = new LightServicePackageList();

        // Преобразование подписок: создаем LightPackageDto только с полями code, category, label
        if (servicePackageList.getServicePackageList() != null) {
            List<LightPackageDto> lightPackages = new ArrayList<>();
            for (SubscribeDto27277 subscribeDto27277 : servicePackageList.getServicePackageList()) {
                LightPackageDto lightPackage = new LightPackageDto();
                lightPackage.setCode(subscribeDto27277.getCode());
                lightPackage.setCategory(subscribeDto27277.getCategory());
                lightPackage.setLabel(subscribeDto27277.getLabel());
                lightPackages.add(lightPackage);
            }
            lightServicePackageList.setServicePackageList(lightPackages);
        }

        // Преобразование индивидуальных тарифов: создаем плоскую структуру LightIndividualTariffDto
        if (servicePackageList.getIndividualTariffList() != null) {
            List<LightIndividualTariffDto> lightTariffs = new ArrayList<>();
            for (IndividualTariffDto27277 individualTariff : servicePackageList.getIndividualTariffList()) {
                LightIndividualTariffDto lightTariff = new LightIndividualTariffDto();
                lightTariff.setCode(individualTariff.getCode());
                lightTariff.setLabel(individualTariff.getLabel());

                // Извлекаем поля из serviceLine и делаем их плоскими
                ServiceLineDto serviceLine = individualTariff.getServiceLine();
                if (serviceLine != null) {
                    lightTariff.setService(serviceLine.getService());
                    lightTariff.setServiceLabel(serviceLine.getServiceLabel());
                    lightTariff.setServiceType(serviceLine.getServiceType());
                    lightTariff.setTariffType(serviceLine.getTariffType());
                    lightTariff.setIndexCode(serviceLine.getIndexCode());
                }
                lightTariffs.add(lightTariff);
            }
            lightServicePackageList.setIndividualTariffList(lightTariffs);
        }

        return lightServicePackageList;
    }
}
