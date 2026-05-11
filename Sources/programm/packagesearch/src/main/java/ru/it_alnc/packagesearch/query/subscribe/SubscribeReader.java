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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.it_alnc.packagesearch.dto.GroupGuideDto;
import ru.it_alnc.packagesearch.dto.subscribe.*;
import ru.it_alnc.packagesearch.entity.GroupGuideEntity;
import ru.it_alnc.packagesearch.entity.ProductEntity;
import ru.it_alnc.packagesearch.entity.VersionEntity;
import ru.it_alnc.packagesearch.entity.subscribe.*;
import ru.it_alnc.packagesearch.repository.*;

import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SubscribeReader {
    @Getter
    private List<SubscribeDto27277> allActiveSubscribes;
    @Getter
    private List<IndividualTariffDto27277> allActiveTariffs;
    @Getter
    private List<GroupGuideDto> allGroupGuides;
    @Getter
    private List<ServiceLineDto> allActiveSLs;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private IndividualTariffRepository individualTariffRepository;
    @Autowired
    private PuParamRepository puParamRepository;
    @Autowired
    private ServiceLineRepository serviceLineRepository;
    @Autowired
    private GroupGuideRepository groupGuideRepository;

    @Transactional(readOnly = true)
    public List<SubscribeDto27277> readAllSubscribes() {
        //this.targetDate = LocalDateTime.now();
        log.info("Start full subscribes reading");
        List<ProductEntity> protoSubscribes = productRepository.getAllActiveAndFutureSubscribes();
        List<SubscribeDto27277> result = mapSubscribesDto27277(protoSubscribes);
        this.allActiveSubscribes = new ArrayList(result);
        log.info("Done subscribes reading");
        return result;
    }

    @Transactional(readOnly = true)
    public List<ServiceLineDto> readAllSLs() {
        //this.targetDate = LocalDateTime.now();
        log.info("  Start full SLs reading");
        List<ServiceLine27277Entity> protoSLs = serviceLineRepository.findAllServiceLines();
        List<ServiceLineDto> result = mapServiceLines(protoSLs, OffsetDateTime.now());
        this.allActiveSLs = new ArrayList<>(result);
        log.info("  Done SLs reading");
        return result;
    }


    @Transactional(readOnly = true)
    public List<IndividualTariffDto27277> readAllTariffs() {
        //this.targetDate = LocalDateTime.now();
        log.info(" Start tariffs reading");
        List<IndividualTariffDto27277> individualTariffs = searchAllNonArchiveTariffs();
        this.allActiveTariffs = new ArrayList(individualTariffs);
        log.info(" Done tariffs reading");
        return individualTariffs;
    }


    @Transactional(readOnly = true)
    public LocalDateTime initGroupGuideCache() {
        log.info("   Start GroupGuides reading");
        List<GroupGuideEntity> groupGuideEntities = groupGuideRepository.getGroupGuideEntitiesForInitCache();
        List<GroupGuideDto> groupGuideDtoList = new ArrayList<>();
        for (GroupGuideEntity gge : groupGuideEntities) {
            groupGuideDtoList.add(mapGroupGuide(gge));
        }
        this.allGroupGuides = groupGuideDtoList;
        log.info("   End GroupGuides reading");
        return LocalDateTime.now();
    }

    @Transactional(readOnly = true)
    public LocalDateTime updateGroupGuidesCache(List<GroupGuideEntity> ggeList) {//todo: упорядоченно обновить кеш
        log.info("   Start GroupGuides cache update");
        ggeList.sort((Comparator.comparingInt(GroupGuideEntity::getId)));//todo: проверить, что сортируются по возрастанию id
        for (GroupGuideEntity groupGuideEntity : ggeList) {
            switch (groupGuideEntity.getAction()) {
                case "DELETE":
                    Optional<GroupGuideDto> groupGuideForRemove = this.allGroupGuides.stream().filter(
                            groupGuideDto -> groupGuideDto.getGuideCode().equals(groupGuideEntity.getGuideCode()) &&
                                    groupGuideDto.getGroupCode().equals(groupGuideEntity.getGroupCode()) &&
                                    groupGuideDto.getBegDate().equals(groupGuideEntity.getBegDate()) &&
                                    groupGuideDto.getEndDate().equals(groupGuideEntity.getEndDate())
                    ).findFirst();
                    groupGuideForRemove.ifPresent(groupGuideDto -> this.allGroupGuides.remove(groupGuideDto));
                    break;
                case "UPDATE":
                case "INSERT":
                    Optional<GroupGuideDto> groupGuideDtoForUpdateOrInsert = this.allGroupGuides.stream().filter(
                            groupGuideDto -> groupGuideDto.getGuideCode().equals(groupGuideEntity.getGuideCode()) &&
                                    groupGuideDto.getGroupCode().equals(groupGuideEntity.getGroupCode()) &&
                                    groupGuideDto.getBegDate().equals(groupGuideEntity.getBegDate()) &&
                                    groupGuideDto.getEndDate().equals(groupGuideEntity.getEndDate())
                    ).findFirst();
                    groupGuideDtoForUpdateOrInsert.ifPresent(groupGuideDto -> groupGuideDto.setLinkCodeList(groupGuideEntity.getLinkCodeList()));//todo: проверить, что обновится в кеше
                    break;
            }
        }
        return LocalDateTime.now();
    }

    @Transactional(readOnly = true)
    public List<SubscribeDto27277> readAllSubscribesSingleRequest(OffsetDateTime requestTime) {
        //this.targetDate = targetDate;
        log.info("Start subscribes reading on archive date {}", requestTime.toLocalDateTime());
        List<ProductEntity> protoSubscribes = productRepository.getArchiveSubscribes(requestTime.toLocalDateTime());
        SubscribeRequest27277 subscribeRequest27277 = new SubscribeRequest27277();
        subscribeRequest27277.setSearchOnDate(requestTime);
        List<SubscribeDto27277> result = mapSubscribesDto27277(protoSubscribes, subscribeRequest27277);
        log.info("Done subscribes reading");
        return result;
    }

    @Transactional(readOnly = true)
    public List<IndividualTariffDto27277> readAllTariffsSingleRequest(LocalDateTime targetDate) {
        //this.targetDate = targetDate;
        log.info("Start tariffs reading on archive date {}", targetDate);
        List<IndividualTariffDto27277> individualTariffs = searchAllArchiveTariffs(targetDate);
        log.info("Done tariffs reading");
        return individualTariffs;
    }

    @Transactional(readOnly = true)
    public List<ServiceLineDto> readAllSLsSingleRequest(LocalDateTime targetDate) {
        //this.targetDate = targetDate;
        log.info("Start serviceLine reading on archive date {}", targetDate);
        List<ServiceLine27277Entity> serviceLinesEntity = serviceLineRepository.findAllArchiveServiceLines(targetDate);
        List<ServiceLineDto> mappedSls = mapServiceLines(serviceLinesEntity, targetDate.atOffset(ZoneOffset.UTC));
        log.info("Done serviceLines reading");
        return mappedSls;
    }

    public List<IndividualTariffDto27277> searchAllArchiveTariffs(LocalDateTime requestTargetDate) {
        List<IndividualTariffEntity> individualTariffEntities = individualTariffRepository.findAll();
        List<IndividualTariffDto27277> result = new ArrayList<>();

        for (IndividualTariffEntity individualTariffEntity : individualTariffEntities) {
            if (individualTariffEntity.getServiceLineEntity() == null
                    || !individualTariffEntity.getServiceLineEntity().getStatus().equals("Approved")
                    || individualTariffEntity.getServiceLineEntity().getBegDate().isAfter(requestTargetDate)
                    || individualTariffEntity.getServiceLineEntity().getEndDate().isBefore(requestTargetDate)
            )
                continue;
            SubscribeRequest27277 subscribeRequest27277 = new SubscribeRequest27277();
            subscribeRequest27277.setSearchOnDate(OffsetDateTime.from(requestTargetDate.atOffset(ZoneOffset.UTC)));
            log.debug("search date : {}", subscribeRequest27277.getSearchOnDate());
            IndividualTariffDto27277 individualTariffDto = new IndividualTariffDto27277();
            individualTariffDto.setCode(individualTariffEntity.getCode());
            individualTariffDto.setLabel(individualTariffEntity.getLabel());
            individualTariffDto.setClientCommonType(individualTariffEntity.getClientCommonType());
            individualTariffDto.setServiceLine(mapServiceLine(individualTariffEntity.getServiceLineEntity(), subscribeRequest27277));
            result.add(individualTariffDto);
        }

        return result;
    }

    public List<IndividualTariffDto27277> searchTariffsFromDB(IndividualTariffDBRequest individualTariffDBRequest) {
        List<IndividualTariffEntity> individualTariffEntities;
        if (individualTariffDBRequest.getIndividualTariffCodes() == null || individualTariffDBRequest.getIndividualTariffCodes().isEmpty()) {
            individualTariffEntities = individualTariffRepository.findByDate(individualTariffDBRequest.getSearchOnDate().toLocalDateTime());
        } else {
            individualTariffEntities = individualTariffRepository.findByCodesAndDate(individualTariffDBRequest.getIndividualTariffCodes(), individualTariffDBRequest.getSearchOnDate().toLocalDateTime());
        }

        List<IndividualTariffDto27277> result = new ArrayList<>();

        for (IndividualTariffEntity individualTariffEntity : individualTariffEntities) {
            if (individualTariffEntity.getServiceLineEntity() == null
                    || !individualTariffEntity.getServiceLineEntity().getStatus().equals("Approved")
                    || individualTariffEntity.getServiceLineEntity().getBegDate().isAfter(individualTariffDBRequest.getSearchOnDate().toLocalDateTime())
                    || individualTariffEntity.getServiceLineEntity().getEndDate().isBefore(individualTariffDBRequest.getSearchOnDate().toLocalDateTime())
            )
                continue;
            SubscribeRequest27277 subscribeRequest27277 = new SubscribeRequest27277();
            subscribeRequest27277.setSearchOnDate(individualTariffDBRequest.getSearchOnDate());
            log.debug("search date : {}", subscribeRequest27277.getSearchOnDate());
            IndividualTariffDto27277 individualTariffDto = new IndividualTariffDto27277();
            individualTariffDto.setCode(individualTariffEntity.getCode());
            individualTariffDto.setLabel(individualTariffEntity.getLabel());
            individualTariffDto.setClientCommonType(individualTariffEntity.getClientCommonType());
            individualTariffDto.setServiceLine(mapServiceLine(individualTariffEntity.getServiceLineEntity(), subscribeRequest27277));
            result.add(individualTariffDto);
        }
        return result;
    }

    public List<IndividualTariffDto27277> searchAllNonArchiveTariffs() {
        List<IndividualTariffEntity> individualTariffEntities = individualTariffRepository.findAll();
        List<IndividualTariffDto27277> result = new ArrayList<>();

        for (IndividualTariffEntity individualTariffEntity : individualTariffEntities) {
            if (individualTariffEntity.getServiceLineEntity() == null
                    || !individualTariffEntity.getServiceLineEntity().getStatus().equals("Approved")
                    || individualTariffEntity.getServiceLineEntity().getEndDate().isBefore(LocalDateTime.now())
            )
                continue;

            IndividualTariffDto27277 individualTariffDto = new IndividualTariffDto27277();
            individualTariffDto.setCode(individualTariffEntity.getCode());
            individualTariffDto.setLabel(individualTariffEntity.getLabel());
            individualTariffDto.setClientCommonType(individualTariffEntity.getClientCommonType());
            individualTariffDto.setServiceLine(mapServiceLine(individualTariffEntity.getServiceLineEntity()));
            result.add(individualTariffDto);
        }

        return result;
    }

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    public List<SubscribeDto27277> mapSubscribesDto27277(List<ProductEntity> protoSubscribes) {
        SubscribeRequest27277 subscribeRequest27277 = new SubscribeRequest27277();
        subscribeRequest27277.setSearchOnDate(OffsetDateTime.now());
        return mapSubscribesDto27277(protoSubscribes, subscribeRequest27277);
    }

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    public List<SubscribeDto27277> mapSubscribesDto27277(List<ProductEntity> protoSubscribes, SubscribeRequest27277 subscribeRequest) {
        List<SubscribeDto27277> result = new ArrayList<>();
        for (ProductEntity activeProduct : protoSubscribes) {
            if (subscribeRequest.getPackageCodes() != null && !subscribeRequest.getPackageCodes().contains(activeProduct.getCode())) {
                continue;
            }
            SubscribeDto27277 subscribeDto = new SubscribeDto27277();
//            Optional<VersionEntity> optionalActiveVersion = activeProduct.getVersions().stream().filter(x -> ((x.getBeginDate().isBefore(targetDate) || x.getBeginDate().isEqual(targetDate)) &&
//                    (x.getEndDate().isAfter(targetDate) || x.getEndDate().isEqual(targetDate)) && x.getStatus().equals("Approved"))).findFirst();
//            if (!optionalActiveVersion.isPresent())
//                continue;
//            VersionEntity activeVersion = optionalActiveVersion.get();
            if (
                // activeProduct.getBeginDate().isAfter(targetDate) ||
                    activeProduct.getEndDate().isBefore(subscribeRequest.getSearchOnDate().toLocalDateTime())) {
                continue;
            }
            LocalDateTime begDate = activeProduct.getBeginDate();
            LocalDateTime endDate = activeProduct.getEndDate();


            subscribeDto.setId(activeProduct.getId());
            subscribeDto.setCode(activeProduct.getCode());
            subscribeDto.setCategory(activeProduct.getCategory());
            subscribeDto.setLabel(activeProduct.getLabel());
            subscribeDto.setDescription(activeProduct.getDescription());
            subscribeDto.setBegDate(ZonedDateTime.of(begDate, ZoneId.systemDefault())); //todo: а это вообще надо?
            subscribeDto.setEndDate(ZonedDateTime.of(endDate, ZoneId.systemDefault()));
            subscribeDto.setPriority(activeProduct.getPriority());
            subscribeDto.setIsMarketing(activeProduct.isMarketing());
            subscribeDto.setIsIndividual(activeProduct.isIndividual());
            subscribeDto.setIsIndependent(activeProduct.isIndependent());
            subscribeDto.setExcludePUGroup(activeProduct.getExcludePUGroup());
            subscribeDto.setClientCommonType(activeProduct.getClientCommonType());
            subscribeDto.setClientTypeList(new ArrayList<>(activeProduct.getClientTypeList()));
            subscribeDto.setBranchList(new ArrayList<>(activeProduct.getBranchList()));
            subscribeDto.setAccountTypeList(new ArrayList<>(activeProduct.getAccountTypeList()));
            subscribeDto.setSegmentList(new ArrayList<>(activeProduct.getSegmentList()));
            subscribeDto.setIsLinked2Account(activeProduct.isLinked2Account());
            subscribeDto.setIsCloseOnAccount(activeProduct.isCloseOnAccount());
            subscribeDto.setIsMultyClient(activeProduct.isMultyClient());
            subscribeDto.setIsMultyAccount(activeProduct.isMultyAccount());
            subscribeDto.setIsCheckPUChange(activeProduct.getIsCheckPuChange());
            subscribeDto.setIsMultiplePackagesCode(activeProduct.getIsMultiplePackagesCode());
            subscribeDto.setMaxPackagesCode(activeProduct.getMaxPackagesCode());
            subscribeDto.setPeriodConnection(activeProduct.getPeriodConnection());
            subscribeDto.setMaxPackagesCodeNo(activeProduct.getMaxPackagesCodeNo());
            subscribeDto.setIsMultiplePackagesCodePeriod(activeProduct.getIsMultiplePackagesCodePeriod());
            if (activeProduct.getVersions() != null) {
                List<VersionDto> versionList = new ArrayList<>();
                for (VersionEntity version : activeProduct.getVersions()) {
                    if (!"Approved".equals(version.getStatus())) {
                        continue;
                    }
                    VersionDto versionDto = new VersionDto();
                    versionDto.setVersion(version.getVersionNum());
                    versionDto.setBeginDate(version.getBeginDate());
                    versionDto.setStatus(version.getStatus());
                    versionDto.setEndDate(version.getEndDate());
                    versionList.add(versionDto);
                }
                subscribeDto.getVersionList().addAll(versionList);
            }
            Predicate<VersionEntity> activeVersionPredicate = x -> (
                    //x.getBeginDate().isBefore(targetDate) || x.getBeginDate().isEqual(targetDate)) &&
                    (x.getEndDate().isAfter(subscribeRequest.getSearchOnDate().toLocalDateTime()))
                            //|| x.getEndDate().isEqual(targetDate))
                            &&
                            x.getStatus().equals("Approved"));
            List<PuParamEntity> productPuParams;
            if (activeProduct.getVersions() != null //todo: проверить как эти версии работают (шаг2)
                    && activeProduct.getVersions().stream().anyMatch(activeVersionPredicate)
                    && !subscribeRequest.isSkipPackageParam()) {
                if (subscribeRequest.getSearchOnDate().isBefore(OffsetDateTime.now())) {
                    productPuParams = puParamRepository.getActualSimpleListSubscribeParams(activeProduct.getId());
                } else {
                    productPuParams = puParamRepository.getArchiveListSubscribeParams(activeProduct.getId(), subscribeRequest.getSearchOnDate().toLocalDateTime());
                }
                List<PuParamDto> puParamList = new ArrayList<>();
                for (PuParamEntity puParamEntity : productPuParams) {
                    PuParamDto puParamDto = new PuParamDto();

                    puParamDto.setId(Long.valueOf(puParamEntity.getUuid()));
                    puParamDto.setPuGroupCode(puParamEntity.getParamGroupCode());
                    puParamDto.setPuGroupLabel(puParamEntity.getParamGroupLabel());
                    puParamDto.setOrder(puParamEntity.getSortOrder());
                    puParamDto.setCode(puParamEntity.getParamCode());
                    puParamDto.setLabel(puParamEntity.getParamLabel());
                    puParamDto.setGuide(puParamEntity.getParamGuide());
                    puParamDto.setVersion(puParamEntity.getVersion());
                    puParamDto.setType(puParamEntity.getParamType());
                    puParamDto.setVType(puParamEntity.getParamValueType());
                    puParamDto.setVTypeLabel(puParamEntity.getParamValueTypeLabel());
                    puParamDto.setIsGroupGuide(puParamEntity.getIsGroupGuide());
                    switch (puParamDto.getType()) {
                        case "BOOLEANTYPE":
                            if (puParamEntity.getVBoolean() != null)
                                puParamDto.setVBoolean(puParamEntity.getVBoolean() == 1);
                            break;
                        case "STRINGTYPE":
                            //todo - мб поменять сразу на списки?
                            puParamDto.setVCodeList(puParamEntity.getVCodeList());
                            puParamDto.setVCodePattern(puParamEntity.getVCodePattern());
                            puParamDto.setVLabelList(puParamEntity.getVLabelList());
                            puParamDto.setVString(puParamEntity.getVString());
                            break;
                        case "DATETYPE":
                            if (puParamEntity.getVDateMin() != null) {
                                puParamDto.setVDateMin(mapToZonedDateTime(puParamEntity.getVDateMin()));
                                puParamDto.setVDateMax(mapToZonedDateTime(puParamEntity.getVDateMax()));
                            } else
                                puParamDto.setVDate(mapToZonedDateTime(puParamEntity.getVDate()));
                            break;
                        case "INTTYPE":
                            if (puParamEntity.getVIntMin() != null) {
                                puParamDto.setVIntMin(puParamEntity.getVIntMin());
                                puParamDto.setVIntMax(puParamEntity.getVIntMax());
                            } else
                                puParamDto.setVInt(puParamEntity.getVInt());
                            break;
                        case "FLOATTYPE":
                            if (puParamEntity.getVFloatMin() != null) {
                                puParamDto.setVFloatMin(puParamEntity.getVFloatMin());
                                puParamDto.setVFloatMax(puParamEntity.getVFloatMax());
                            } else
                                puParamDto.setVFloat(puParamEntity.getVFloat());
                            break;
                        default:
                        case "CODETYPE":
                            puParamDto.setVCodePattern(puParamEntity.getVCodePattern());
                            puParamDto.setVCodeList(puParamEntity.getVCodeList());
                            puParamDto.setVLabelList(puParamEntity.getVLabelList());
                            resolveGroupGuides(puParamDto, subscribeRequest.getSearchOnDate().toLocalDateTime());
                            break;
                    }
                    puParamList.add(puParamDto);
                }
                subscribeDto.setPuParamList(puParamList);

            }

            if (activeProduct.getPuServiceGroups() != null) {
                List<ServiceGroupDto> serviceGroupList = new ArrayList<>();
                for (PUServiceGroupEntity puServiceGroupEntity : activeProduct.getPuServiceGroups()) {
                    if (puServiceGroupEntity.isDeleted())
                        continue;
                    ServiceGroupDto serviceGroupDto =
                            new ServiceGroupDto();
                    serviceGroupDto.setId(puServiceGroupEntity.getId());
                    serviceGroupDto.setOrder(puServiceGroupEntity.getSortorder());
                    serviceGroupDto.setCode(puServiceGroupEntity.getCode());
                    serviceGroupDto.setLabel(puServiceGroupEntity.getLabel());


                    List<ServiceLineDto> serviceLineList = new ArrayList<>();

                    //29462 skip inconvenient SLs
                    if (subscribeRequest.isSkipService()) {
                        List<ServiceLine27277Entity> compatibleSLs = new ArrayList<>();
                        compatibleSLs = serviceLineRepository.findAllBySGAndTypeService(serviceGroupDto.getId(), "3");
                        if (activeProduct.isIndependent()) {
                            compatibleSLs.addAll(serviceLineRepository.findAllBySGAndTypeService(serviceGroupDto.getId(), "4"));
                        }
                        for (ServiceLine27277Entity serviceLineEntity : compatibleSLs) {
                            //check dates
                            LocalDateTime entityBegDate = (serviceLineEntity.getBegDate());
                            LocalDateTime entityEndDate = (serviceLineEntity.getEndDate());
                            LocalDateTime currentDate = (subscribeRequest.getSearchOnDate().toLocalDateTime()); //todo (шаг 3) убрать фильтрацию SLs тут, фильтровать при запросе внутри SQB
                            if (
                                //entityBegDate.isAfter(currentDate) ||
                                    entityEndDate.isBefore(currentDate)
                                            || !serviceLineEntity.getStatus().equals("Approved"))
                                continue;
                            ServiceLineDto serviceLineDto = mapServiceLine(serviceLineEntity, subscribeRequest);
                            serviceLineList.add(serviceLineDto);
                        }
                    } else if (puServiceGroupEntity.getServiceLines() != null) {
                        for (ServiceLine27277Entity serviceLineEntity : puServiceGroupEntity.getServiceLines()) {
                            //check dates
                            LocalDateTime entityBegDate = (serviceLineEntity.getBegDate());
                            LocalDateTime entityEndDate = (serviceLineEntity.getEndDate());
                            LocalDateTime currentDate = (subscribeRequest.getSearchOnDate().toLocalDateTime()); //todo (шаг 3) убрать фильтрацию SLs тут, фильтровать при запросе внутри SQB
                            if (
                                // entityBegDate.isAfter(currentDate) ||
                                    entityEndDate.isBefore(currentDate)
                                            || !serviceLineEntity.getStatus().equals("Approved"))
                                continue;
                            ServiceLineDto serviceLineDto = mapServiceLine(serviceLineEntity, subscribeRequest);
                            serviceLineList.add(serviceLineDto);
                        }
                    }
                    serviceGroupDto.setServiceLineList(serviceLineList);

                    serviceGroupList.add(serviceGroupDto);
                }
                subscribeDto.setServiceGroupList(serviceGroupList);
            }

            if (activeProduct.getActionOnPUChangeEntities() != null) {
                activeProduct.setActionOnPUChangeEntities(
                        activeProduct.getActionOnPUChangeEntities().stream().filter(
                                actionOnPUChangeEntity -> "Approved".equals(actionOnPUChangeEntity.getStatus())
                                        && actionOnPUChangeEntity.getEndDate().isAfter(LocalDateTime.now())
                        ).collect(Collectors.toSet())
                );
                List<ActionOnPUChangeDto> actionOnPUChangeList = new ArrayList<>();
                for (ActionOnPUChangeEntity entity : activeProduct.getActionOnPUChangeEntities()) {
                    ActionOnPUChangeDto actionOnPUChangeDto = new ActionOnPUChangeDto();
                    actionOnPUChangeDto.setId(entity.getId());
                    actionOnPUChangeDto.setStatus(entity.getStatus());
                    actionOnPUChangeDto.setBegDate(ZonedDateTime.of(entity.getBegDate(), ZoneId.systemDefault()));
                    actionOnPUChangeDto.setEndDate(ZonedDateTime.of(entity.getEndDate(), ZoneId.systemDefault()));
//                    actionOnPUChangeDto.setCreatedOn(ZonedDateTime.of(entity.getCreatedOn(),ZoneId.systemDefault()));
//                    actionOnPUChangeDto.setCreatedBy(entity.getCreatedBy());
//                    actionOnPUChangeDto.setCreatedByName(entity.getCreatedByName());
//                    actionOnPUChangeDto.setModifiedOn(ZonedDateTime.of(entity.getModifiedOn(),ZoneId.systemDefault()));
//                    actionOnPUChangeDto.setModifiedBy(entity.getModifiedBy());
//                    actionOnPUChangeDto.setModifiedByName(entity.getModifiedByName());
                    List<ActionOnPUChangeLineDto> lineDtoList = new ArrayList<>();
                    for (ActionOnPUChangeLineEntity actionOnPUChangeLineEntity : entity.getActionOnPuChangeLines()) {
                        ActionOnPUChangeLineDto actionOnPUChangeLineDto = new ActionOnPUChangeLineDto();
                        actionOnPUChangeLineDto.setTargetPUCodeList(actionOnPUChangeLineEntity.getTargetPUCodeList());
                        actionOnPUChangeLineDto.setActionTypeCode(actionOnPUChangeLineEntity.getActionTypeCode());
//                        actionOnPUChangeLineDto.setId(actionOnPUChangeLineEntity.getId());
//                        actionOnPUChangeLineDto.setCreatedOn(ZonedDateTime.of(actionOnPUChangeLineEntity.getCreatedOn(),ZoneId.systemDefault()));
//                        actionOnPUChangeLineDto.setCreatedBy(actionOnPUChangeLineEntity.getCreatedBy());
//                        actionOnPUChangeLineDto.setCreatedByName(actionOnPUChangeLineEntity.getCreatedByName());
//                        actionOnPUChangeLineDto.setModifiedOn(ZonedDateTime.of(actionOnPUChangeLineEntity.getModifiedOn(),ZoneId.systemDefault()));
//                        actionOnPUChangeLineDto.setModifiedBy(actionOnPUChangeLineEntity.getModifiedBy());
//                        actionOnPUChangeLineDto.setModifiedByName(actionOnPUChangeLineEntity.getModifiedByName());
//                        actionOnPUChangeLineDto.setTargetPULabelList(actionOnPUChangeLineEntity.getTargetPULabelList());
//                        actionOnPUChangeLineDto.setActionTypeLabel(actionOnPUChangeLineEntity.getActionTypeLabel());
                        lineDtoList.add(actionOnPUChangeLineDto);
                    }
                    actionOnPUChangeDto.setActionOnPuChangeLines(lineDtoList);
                    actionOnPUChangeList.add(actionOnPUChangeDto);
                }
                subscribeDto.setActionOnPUChangeList(actionOnPUChangeList);
            }

            if (activeProduct.getFilterParams() != null && activeProduct.getFilterParams().size() != 0) {
                List<SubscribeFilterParamDto> productFilterParams = new ArrayList<>();
                for (ParamValueEntity filterParamEntity : activeProduct.getFilterParams()) {
                    productFilterParams.add((SubscribeFilterParamDto) resolveGroupGuides(convertFilterParam(filterParamEntity), subscribeRequest.getSearchOnDate().toLocalDateTime()));
                }
                subscribeDto.setFilterParamList(productFilterParams);
            }

            result.add(subscribeDto);
        }
        return result;
    }

    public ServiceLineDto mapServiceLine(ServiceLine27277Entity serviceLineEntity) {
        SubscribeRequest27277 subscribeRequest27277 = new SubscribeRequest27277();
        subscribeRequest27277.setSearchOnDate(OffsetDateTime.now());
        return mapServiceLine(serviceLineEntity, subscribeRequest27277);
    }

    public List<ServiceLineDto> mapServiceLines(List<ServiceLine27277Entity> serviceLine27277EntityList, OffsetDateTime odt) {
        List<ServiceLineDto> serviceLineDtoList = new ArrayList<>();
        SubscribeRequest27277 subscribeRequest27277 = new SubscribeRequest27277();
        subscribeRequest27277.setSearchOnDate(odt);
        for (ServiceLine27277Entity serviceLine27277Entity : serviceLine27277EntityList) {
            serviceLineDtoList.add(mapServiceLine(serviceLine27277Entity, subscribeRequest27277));
        }
        return serviceLineDtoList;
    }

    public ServiceLineDto mapServiceLine(ServiceLine27277Entity serviceLineEntity, SubscribeRequest27277 subscribeRequest) {
        ServiceLineDto serviceLineDto = new ServiceLineDto();
        serviceLineDto.setId(serviceLineEntity.getId());
        serviceLineDto.setOrder(serviceLineEntity.getSortOrder());
        serviceLineDto.setStatus(serviceLineEntity.getStatus());
        //32068
        serviceLineDto.setPrefPeriod(serviceLineEntity.getPrefPeriod());
        serviceLineDto.setPrefPeriodLabel(serviceLineEntity.getPrefPeriodLabel());
        serviceLineDto.setPrefPeriodEndAction(serviceLineEntity.getPrefPeriodEndAction());
        serviceLineDto.setPrefPeriodEndActionLabel(serviceLineEntity.getPrefPeriodEndActionLabel());
        serviceLineDto.setTrialPeriod(serviceLineEntity.getTrialPeriod());
        serviceLineDto.setTrialPeriodLabel(serviceLineEntity.getTrialPeriodLabel());
        serviceLineDto.setTrialPeriodEndAction(serviceLineEntity.getTrialPeriodEndAction());
        serviceLineDto.setTrialPeriodEndActionLabel(serviceLineEntity.getTrialPeriodEndActionLabel());
        //


        serviceLineDto.setObj(serviceLineEntity.getObj());
        serviceLineDto.setObjTable(serviceLineEntity.getObjTable());
        serviceLineDto.setIsAutoProlongation(serviceLineEntity.getIsAutoProlongation());
        serviceLineDto.setPrefPeriodPayType(serviceLineEntity.getPrefPeriodPayType());
        serviceLineDto.setPrefPeriodRuleType(serviceLineEntity.getPrefPeriodRuleType());

        //33116
        serviceLineDto.setPaymentDaysROOCode(serviceLineEntity.getPaymentDaysRooCode());

        //34394
        serviceLineDto.setIsNotRedoCommissOnEQError(serviceLineEntity.getIsNotRedoCommissOnEQError());
        serviceLineDto.setIsProlongNoWaitEQResponse(serviceLineEntity.getIsProlongNoWaitEQResponse());

        //34092
        serviceLineDto.setEntryMode(serviceLineEntity.getEntryMode());

        Optional.ofNullable(serviceLineEntity.getServiceGuideItem())
                .map(ServiceGuideItemEntity::getIsMultyCopiesCalcAllowed)
                .map(this::extractBooleanFromInteger)
                .ifPresent(serviceLineDto::setIsMultyCopiesCalcAllowed);

        // Маппинг ParamGracePeriod
        if (serviceLineEntity.getParamGracePeriods() != null && !serviceLineEntity.getParamGracePeriods().isEmpty()) {
            var gracePeriodList = new ArrayList<ParamGracePeriodDto>();
            for (ParamGracePeriodEntity paramEntity : serviceLineEntity.getParamGracePeriods()) {
                var dto = new ParamGracePeriodDto();
                dto.setSourceCodeList(paramEntity.getSourceCode());
                dto.setSourceLabelList(paramEntity.getSourceLabel());
                dto.setCheckGrace(paramEntity.getCheckGrace());
                gracePeriodList.add(dto);
            }
            serviceLineDto.setParamGracePeriod(gracePeriodList);
        }

        if (serviceLineEntity.getPlannedOperationSettings() != null && !serviceLineEntity.getPlannedOperationSettings().isEmpty()) {
            var plannedOperations = new ArrayList<PlannedOperationSettingDto>();
            for (PlannedOperationSettingEntity plannedEntity : serviceLineEntity.getPlannedOperationSettings()) {
                var dto = new PlannedOperationSettingDto();
                dto.setPlannedOperationType(plannedEntity.getPlannedOperationType());
                dto.setPlannedOperationTypeLabel(plannedEntity.getPlannedOperationTypeLabel());
                dto.setEntryMode(plannedEntity.getEntryMode());
                if (2 == plannedEntity.getIsNeedSuoSd())
                    dto.setIsNeedSuoSd(null);
                else
                    dto.setIsNeedSuoSd(1 == plannedEntity.getIsNeedSuoSd());
                plannedOperations.add(dto);
            }
            serviceLineDto.setPlannedOperationSettingsList(plannedOperations);
        }


        if (serviceLineEntity.getServiceGuide() != null)
            serviceLineDto.setServiceGuide(serviceLineEntity.getServiceGuide().getCode());
        if (serviceLineEntity.getServiceGuideItem() != null)
            serviceLineDto.setServiceGuideItemCode(serviceLineEntity.getServiceGuideItem().getCode());
        if (serviceLineEntity.getPuServiceGroup() != null) {
            serviceLineDto.setPuServiceGroupCode(serviceLineEntity.getPuServiceGroup().getCode());
            serviceLineDto.setPuServiceGroupProductCode(serviceLineEntity.getPuServiceGroup().getProduct().getCode());
        }

        if (serviceLineEntity.getPu4PriceService() != null) {
            serviceLineDto.setPu4PriceService(productRepository.getById(serviceLineEntity.getPu4PriceService()).getCode());
            serviceLineDto.setPu4PriceServiceLong(serviceLineEntity.getPu4PriceService());
        }
        if (serviceLineEntity.getServiceGuideItem() != null) {
            serviceLineDto.setService(serviceLineEntity.getServiceGuideItem().getCode());
            serviceLineDto.setServiceLabel(serviceLineEntity.getServiceGuideItem().getLabel());
            serviceLineDto.setServiceType(serviceLineEntity.getServiceTypeEntity().getCode());
            if (serviceLineEntity.getServiceTypeEntity() != null)
                serviceLineDto.setServiceTypeLabel(serviceLineEntity.getServiceTypeEntity().getLabel());
        }
        serviceLineDto.setBegDate(ZonedDateTime.of(serviceLineEntity.getBegDate(), ZoneId.systemDefault()));
        serviceLineDto.setEndDate(ZonedDateTime.of(serviceLineEntity.getEndDate(), ZoneId.systemDefault()));
        serviceLineDto.setPriority(serviceLineEntity.getPriority());
        serviceLineDto.setContextType(serviceLineEntity.getContextType());
        if (serviceLineEntity.getContextTypeEntity() != null)
            serviceLineDto.setContextTypeLabel(serviceLineEntity.getContextTypeEntity().getLabel());
        serviceLineDto.setTariffType(serviceLineEntity.getTariffType());
        if (serviceLineEntity.getTariffTypeEntity() != null)
            serviceLineDto.setTariffTypeLabel(serviceLineEntity.getTariffTypeEntity().getLabel());
        serviceLineDto.setLimit4FreeQuantity(serviceLineEntity.getLimit4FreeQuantity());
        serviceLineDto.setLimit4FreeSumAmount(serviceLineEntity.getLimit4FreeSumAmount());
        serviceLineDto.setLimit4FreeSumCurrency(serviceLineEntity.getLimit4FreeSumCurrency());
        serviceLineDto.setIndexCode(serviceLineEntity.getIndexCode());
        serviceLineDto.setNdsType(serviceLineEntity.getNdsTypeEntity().getType());
        if (serviceLineEntity.getNdsTypeEntity() != null) {
            serviceLineDto.setNdsTypeLabel(serviceLineEntity.getNdsTypeEntity().getLabel());
            serviceLineDto.setNdsRate(serviceLineEntity.getNdsTypeEntity().getRate());
        }
//                            serviceLineDto.getNdsRate(serviceLineEntity.getNds); "nsdRate": 0
        serviceLineDto.setIsNoEntry(serviceLineEntity.getIsNoEntry());
        serviceLineDto.setEntryEventType(serviceLineEntity.getEntryEventType());
        if (serviceLineEntity.getEntryEventTypeEntity() != null)
            serviceLineDto.setEntryEventTypeLabel(serviceLineEntity.getEntryEventTypeEntity().getLabel());
        serviceLineDto.setRateType(serviceLineEntity.getRateType());
        if (serviceLineEntity.getRateTypeEntity() != null)
            serviceLineDto.setRateTypeLabel(serviceLineEntity.getRateTypeEntity().getLabel());
        serviceLineDto.setIsSingleServiceInOper(serviceLineEntity.getIsSingleServiceInOper());
        serviceLineDto.setIsAggregateMultyService(serviceLineEntity.getIsAggregateMultiService());
        serviceLineDto.setIsAggregateSummary(serviceLineEntity.getIsAggregateSummary());
        serviceLineDto.setAggregateSummaryNum(serviceLineEntity.getAggregateSummaryNum());
        if (serviceLineEntity.getParamOperAmount() != null)
            serviceLineDto.setParamOperAmount(serviceLineEntity.getParamOperAmount().getObjectType()
                    + "." + serviceLineEntity.getParamOperAmount().getCode());
        if (serviceLineEntity.getParamOperCurrency() != null)
            serviceLineDto.setParamOperCurrency(serviceLineEntity.getParamOperCurrency().getObjectType()
                    + "." + serviceLineEntity.getParamOperCurrency().getCode());
        serviceLineDto.setIsManualActivate(serviceLineEntity.getIsManualActivate());
        serviceLineDto.setEnablePeriodCode(serviceLineEntity.getEnablePeriodCode());
        serviceLineDto.setEnablePeriodLabel(serviceLineEntity.getEnablePeriodLabel());
        serviceLineDto.setPeriodicPayType(serviceLineEntity.getPeriodicPayType());
        if (serviceLineEntity.getPeriodicPayTypeEntity() != null)
            serviceLineDto.setPeriodicPayTypeLabel(serviceLineEntity.getPeriodicPayTypeEntity().getLabel());
        serviceLineDto.setPeriodicPartUsedPayType(serviceLineEntity.getPeriodicPartUsedPayType());
        if (serviceLineEntity.getPeriodicPartUsedPayTypeEntity() != null)
            serviceLineDto.setPeriodicPartUsedPayTypeLabel(serviceLineEntity.getPeriodicPartUsedPayTypeEntity().getLabel());
        serviceLineDto.setIsPeriodicROO(serviceLineEntity.getIsPeriodicRoo());
        serviceLineDto.setPeriodicROOWaitPeriod(serviceLineEntity.getPeriodicRooWaitPeriod());
        serviceLineDto.setPeriodicROOWaitPeriodLabel(serviceLineEntity.getPeriodicRooWaitPeriodLabel());
        serviceLineDto.setGracePeriodCode(serviceLineEntity.getGracePeriod());
        serviceLineDto.setGracePeriodLabel(serviceLineEntity.getGracePeriodLabel());
        serviceLineDto.setIsPeriodicCheckOperations(serviceLineEntity.getIsPeriodicCheckOperations());
        serviceLineDto.setIsPeriodicCheckRest(serviceLineEntity.getIsPeriodicCheckRest());
//        serviceLineDto.setIsSplitGraceByPeriods(serviceLineEntity.getIsSplitGraceByPeriods());
        //32998
        if (serviceLineEntity.getParamPrefBegDate() != null)
            serviceLineDto.setParamPrefBegDate(serviceLineEntity.getParamPrefBegDate().getObjectType()
                    + "." + serviceLineEntity.getParamPrefBegDate().getCode());
        serviceLineDto.setPeriodicConnLinkType(serviceLineEntity.getPeriodicConnLinkType());
        serviceLineDto.setIsNotPayFromSUOToSD(serviceLineEntity.getIsNotPayFromSUOToSD());
        serviceLineDto.setIsCheckCardActive(serviceLineEntity.getIsCheckCardActive());
        serviceLineDto.setIsProlongateIgnoreCheckErr(serviceLineEntity.getIsProlongateIgnoreCheckErr());
        serviceLineDto.setIsPayFeeIfProlongationCheckErr(serviceLineEntity.getIsPayFeeIfProlongationCheckErr());
        serviceLineDto.setIsEnableAddPrefAfterConn(serviceLineEntity.getIsEnableAddPrefAfterConn());
        serviceLineDto.setIsTunePrefBegDate(serviceLineEntity.getIsTunePrefBegDate());
        serviceLineDto.setOnChangePUActType(serviceLineEntity.getOnChangePUActType());
        serviceLineDto.setIsSyncEntry(serviceLineEntity.getIsSyncEntry());

        serviceLineDto.setDiscount2Package(serviceLineEntity.getDiscount2Package());
        serviceLineDto.setDiscountRate(serviceLineEntity.getDiscountRate());
        serviceLineDto.setDiscountAmt(serviceLineEntity.getDiscountAmt());
        serviceLineDto.setDiscountCur(serviceLineEntity.getDiscountCur());
        serviceLineDto.setDiscountAmtAbs(serviceLineEntity.getDiscountAmtAbs());
        serviceLineDto.setDiscountAmtCur(serviceLineEntity.getDiscountAmtCur());

        serviceLineDto.setIsNotifyKafka(extractBooleanFromInteger(serviceLineEntity.getIsNotifyKafka()));
        serviceLineDto.setIsIgnoreEqError(extractBooleanFromInteger(serviceLineEntity.getIsIgnoreEqError()));
        serviceLineDto.setIsSupportCalcResult(extractBooleanFromInteger(serviceLineEntity.getIsSupportCalcResult()));


        if (serviceLineEntity.getFilterParams() != null) {
            List<SubscribeFilterParamDto> serviceLineFilterParams = new ArrayList<>();
            for (ParamValueEntity filterParamEntity : serviceLineEntity.getFilterParams()) {
                serviceLineFilterParams.add((SubscribeFilterParamDto) resolveGroupGuides(convertFilterParam(filterParamEntity), subscribeRequest.getSearchOnDate().toLocalDateTime()));
            }
            serviceLineDto.setFilterParams(serviceLineFilterParams);
        }
        if (serviceLineEntity.getCounters() != null) {
//            List<CounterDto> counters = new ArrayList<>();
//            for (CounterEntity counterEntity : serviceLineEntity.getCounters()) {
//                CounterDto counterDto = new CounterDto();
//
//                counterDto.setId(counterEntity.getId());
//                counterDto.setCode(counterEntity.getCode());
//                counterDto.setLabel(counterEntity.getLabel());
//                counterDto.setType(counterEntity.getCounterType());
//                if (counterEntity.getCounterTypeEntity() != null)
//                    counterDto.setTypeLabel(counterEntity.getCounterTypeEntity().getLabel());
//                if (counterEntity.getParent() != null)
//                    counterDto.setParentCode(counterEntity.getParent().getCode());
//                counterDto.setCurrency(counterEntity.getCurrency());
//                counterDto.setPeriod(counterEntity.getPeriod() + counterEntity.getPeriodType());
////                counterDto.setIsCalendarBorder(counterEntity.getIsCalendarBorder() ? 1 : 0);
//                counterDto.setIsUseQuantity(counterEntity.getIsUseQuantity());
//                counterDto.setIsUseSum(counterEntity.getIsUseSum());
//                counterDto.setIsUseFee(counterEntity.getIsUseFee());
//                counterDto.setIsUseNDS(counterEntity.getIsUseNds());
//                counterDto.setContextType(counterEntity.getContextType());
//                if (counterEntity.getContextTypeEntity() != null)
//                    counterDto.setContextTypeLabel(counterEntity.getContextTypeEntity().getLabel());
//                counterDto.setIsCollectOnAccount(counterEntity.getIsCollectOnAccount());
//                counterDto.setIsCollectOnPact(counterEntity.getIsCollectOnPact());
//                counterDto.setIsCollectOnClient(counterEntity.getIsCollectOnClient());
//                counterDto.setIsCollectOnClientGroup(counterEntity.getIsCollectOnClientGroup());
//                counterDto.setArchiveLength(counterEntity.getArchiveLength());
//                counterDto.setIsGetFromIT(counterEntity.getIsGetFromIT());
//                counterDto.setIsGetFromS(counterEntity.getIsGetFromS());
//                counterDto.setIsGetFromPS(counterEntity.getIsGetFromPS());
//                counterDto.setIsGetFromTP(counterEntity.getIsGetFromTP());
//                counterDto.setIsPutToIT(counterEntity.getIsPutToIT());
//                counterDto.setIsPutToS(counterEntity.getIsPutToS());
//                counterDto.setIsPutToPS(counterEntity.getIsPutToPS());
//                counterDto.setIsPutToPS(counterEntity.getIsPutToTP());
//
//                counters.add(counterDto);
//            }
//            serviceLineDto.setCounterList(counters);
        }
        if (serviceLineEntity.getParamList() != null) {
            List<SubscribeParamDto> serviceLineParams = new ArrayList<>();
            for (SetLineParamEntity setLineParamEntity : serviceLineEntity.getParamList()) {
                SubscribeParamDto serviceLineParam = new SubscribeParamDto();

                serviceLineParam.setId(Long.valueOf(setLineParamEntity.getUuid()));
                serviceLineParam.setOrder(setLineParamEntity.getOrder());
                serviceLineParam.setCode(setLineParamEntity.getCode());
                serviceLineParam.setLabel(setLineParamEntity.getLabel());
                serviceLineParam.setGuide(setLineParamEntity.getGuide());
                serviceLineParam.setGuide(setLineParamEntity.getGuide());
                serviceLineParam.setType(setLineParamEntity.getType());
                serviceLineParam.setVType(setLineParamEntity.getVType());
                serviceLineParam.setVTypeLabel(setLineParamEntity.getVTypeLabel());
                serviceLineParam.setIsGroupGuide(setLineParamEntity.getIsGroupGuide());
                switch (setLineParamEntity.getType()) {
                    case "BOOLEANTYPE":
                        serviceLineParam.setVBoolean(setLineParamEntity.getVBoolean());
                        break;
                    case "STRINGTYPE":
                        //todo - мб поменять сразу на списки?
                        serviceLineParam.setVCodeList(setLineParamEntity.getVCodeList());
                        serviceLineParam.setVCodePattern(setLineParamEntity.getVCodePattern());
                        serviceLineParam.setVLabelList(setLineParamEntity.getVLabelList());
                        serviceLineParam.setVString(setLineParamEntity.getVString());
                        break;
                    case "DATETYPE":
                        if (setLineParamEntity.getVDateMin() != null) {
                            serviceLineParam.setVDateMin(mapToZonedDateTime(setLineParamEntity.getVDateMin()));
                            serviceLineParam.setVDateMax(mapToZonedDateTime(setLineParamEntity.getVDateMax()));
                        } else
                            serviceLineParam.setVDate(mapToZonedDateTime(setLineParamEntity.getVDate()));
                        break;
                    case "INTTYPE":
                        if (setLineParamEntity.getVIntMin() != null) {
                            serviceLineParam.setVIntMin(setLineParamEntity.getVIntMin());
                            serviceLineParam.setVIntMax(setLineParamEntity.getVIntMax());
                        } else
                            serviceLineParam.setVInt(setLineParamEntity.getVInt());
                        break;
                    case "FLOATTYPE":
                        if (setLineParamEntity.getVFloatMin() != null) {
                            serviceLineParam.setVFloatMin(setLineParamEntity.getVFloatMin());
                            serviceLineParam.setVFloatMax(setLineParamEntity.getVFloatMax());
                        } else
                            serviceLineParam.setVFloat(setLineParamEntity.getVFloat());
                        break;
                    case "CODETYPE":
                        serviceLineParam.setVCodePattern(setLineParamEntity.getVCodePattern());
                        serviceLineParam.setVCodeList(setLineParamEntity.getVCodeList());
                        serviceLineParam.setVLabelList(setLineParamEntity.getVLabelList());
                        resolveGroupGuides(serviceLineParam, subscribeRequest.getSearchOnDate().toLocalDateTime());
                        break;
                }
                serviceLineParams.add(serviceLineParam);
            }
            serviceLineDto.setParamList(serviceLineParams);
        }

        if (serviceLineEntity.getTariffs() != null) {
            List<SubscribeTariffDto> tariffs = new ArrayList<>();
            for (TariffEntity tariffEntity : serviceLineEntity.getTariffs()) {
                //check dates
                LocalDateTime entityBegDate = (tariffEntity.getBegDate());
                LocalDateTime entityEndDate = (tariffEntity.getEndDate());
                LocalDateTime currentDate = (subscribeRequest.getSearchOnDate().toLocalDateTime()); //todo: шаг Х убрать здесь фильтрацию по дате, фильтровать потом при получении запроса
                if (
                    //     entityBegDate.isAfter(currentDate) ||
                        entityEndDate.isBefore(currentDate)
                                || !tariffEntity.getStatus().equals("Approved"))
                    continue;

                SubscribeTariffDto subscribeTariffDto = new SubscribeTariffDto();
                subscribeTariffDto.setId(tariffEntity.getId());
                subscribeTariffDto.setCode(tariffEntity.getCode());
                subscribeTariffDto.setLabel(tariffEntity.getLabel());
                subscribeTariffDto.setSimple(tariffEntity.isSimple());
                subscribeTariffDto.setBegDate(ZonedDateTime.of(tariffEntity.getBegDate(), ZoneId.systemDefault()));
                subscribeTariffDto.setEndDate(ZonedDateTime.of(tariffEntity.getEndDate(), ZoneId.systemDefault()));
                subscribeTariffDto.setRateType(tariffEntity.getRateType());
                subscribeTariffDto.setEntryRateType(tariffEntity.getEntryRateType());
                subscribeTariffDto.setEntryCurrency(tariffEntity.getEntryCurrency());
                subscribeTariffDto.setAllLineCurrency(tariffEntity.getAllLineCurrency());
                if (!subscribeRequest.isSkipServiceTariffLine() && tariffEntity.getTariffLines() != null) {
                    List<SubscribeTariffLineDto> tariffLines = new ArrayList<>();
                    List<CheckedGraceBySourceDto> checkedGraceBySourceDtoList = new ArrayList<>();
                    for (TariffLineEntity tariffLineEntity : tariffEntity.getTariffLines()) {
                        SubscribeTariffLineDto subscribeTariffLineDto = new SubscribeTariffLineDto();
                        subscribeTariffLineDto.setId(tariffLineEntity.getId());
                        subscribeTariffLineDto.setOrder(tariffLineEntity.getSortOrder());
                        subscribeTariffLineDto.setAmountFix(tariffLineEntity.getAmountFix());
                        subscribeTariffLineDto.setAmountMin(tariffLineEntity.getAmountMin());
                        subscribeTariffLineDto.setAmountMax(tariffLineEntity.getAmountMax());
                        subscribeTariffLineDto.setRate(tariffLineEntity.getRate());
                        subscribeTariffLineDto.setCurrency(tariffLineEntity.getCurrency());
                        subscribeTariffLineDto.setEntryCurrency(tariffLineEntity.getEntryCurrency());
                        subscribeTariffLineDto.setPreferenceCode(tariffLineEntity.getPreferenceCode());
                        subscribeTariffLineDto.setPreferenceLabel(tariffLineEntity.getPreferenceLabel());

                        if (tariffLineEntity.getFilterParams() != null) {
                            List<SubscribeFilterParamDto> tariffLineFilterParams = new ArrayList<>();
                            for (ParamValueEntity filterParamEntity : tariffLineEntity.getFilterParams()) {
                                tariffLineFilterParams.add((SubscribeFilterParamDto) resolveGroupGuides(convertFilterParam(filterParamEntity), subscribeRequest.getSearchOnDate().toLocalDateTime()));
                            }
                            subscribeTariffLineDto.setFilterParams(tariffLineFilterParams);
                        }


                        List<SubscribeParamDto> tariffLineParams = new ArrayList<>();
                        for (TariffLineParamEntity tariffLineParamEntity : tariffLineEntity.getTariffLineParams()) {

                            //37934
                            if (tariffLineParamEntity.getTariffPart() == 4)
                                continue;

                            TariffParamDto tariffLineParam = new TariffParamDto();

                            tariffLineParam.setId(tariffLineParamEntity.getUuid());
                            tariffLineParam.setTariffPart(tariffLineParamEntity.getTariffPart());
                            tariffLineParam.setOrder(tariffLineParamEntity.getOrder());
                            tariffLineParam.setCode(tariffLineParamEntity.getCode());
                            tariffLineParam.setLabel(tariffLineParamEntity.getLabel());
                            tariffLineParam.setGuide(tariffLineParamEntity.getGuide());
                            tariffLineParam.setGuide(tariffLineParamEntity.getGuide());
                            tariffLineParam.setType(tariffLineParamEntity.getType());
                            tariffLineParam.setVType(tariffLineParamEntity.getVType());
                            tariffLineParam.setVTypeLabel(tariffLineParamEntity.getVTypeLabel());
                            tariffLineParam.setIsGroupGuide(tariffLineParamEntity.getIsGroupGuide());
                            switch (tariffLineParamEntity.getType()) {
                                case "BOOLEANTYPE":
                                    tariffLineParam.setVBoolean(tariffLineParamEntity.getVBoolean());
                                    break;
                                case "STRINGTYPE":
                                    //todo - мб поменять сразу на списки?
                                    tariffLineParam.setVCodeList(tariffLineParamEntity.getVCodeList());
                                    tariffLineParam.setVCodePattern(tariffLineParamEntity.getVCodePattern());
                                    tariffLineParam.setVLabelList(tariffLineParamEntity.getVLabelList());
                                    tariffLineParam.setVString(tariffLineParamEntity.getVString());
                                    break;
                                case "DATETYPE":
                                    if (tariffLineParamEntity.getVDateMin() != null) {
                                        tariffLineParam.setVDateMin(mapToZonedDateTime(tariffLineParamEntity.getVDateMin()));
                                        tariffLineParam.setVDateMax(mapToZonedDateTime(tariffLineParamEntity.getVDateMax()));
                                    } else
                                        tariffLineParam.setVDate(mapToZonedDateTime(tariffLineParamEntity.getVDate()));
                                    break;
                                case "INTTYPE":
                                    if (tariffLineParamEntity.getVIntMin() != null) {
                                        tariffLineParam.setVIntMin(tariffLineParamEntity.getVIntMin());
                                        tariffLineParam.setVIntMax(tariffLineParamEntity.getVIntMax());
                                    } else
                                        tariffLineParam.setVInt(tariffLineParamEntity.getVInt());
                                    break;
                                case "FLOATTYPE":
                                    if (tariffLineParamEntity.getVFloatMin() != null) {
                                        tariffLineParam.setVFloatMin(tariffLineParamEntity.getVFloatMin());
                                        tariffLineParam.setVFloatMax(tariffLineParamEntity.getVFloatMax());
                                    } else
                                        tariffLineParam.setVFloat(tariffLineParamEntity.getVFloat());
                                    break;
                                case "CODETYPE":
                                    tariffLineParam.setVCodePattern(tariffLineParamEntity.getVCodePattern());
                                    tariffLineParam.setVCodeList(tariffLineParamEntity.getVCodeList());
                                    tariffLineParam.setVLabelList(tariffLineParamEntity.getVLabelList());
                                    resolveGroupGuides(tariffLineParam, subscribeRequest.getSearchOnDate().toLocalDateTime());
                                    break;
                            }
                            tariffLineParams.add(tariffLineParam);
                            if ("OPERATION.SOURCECONNECT".equals(tariffLineParamEntity.getCode())) {
                                var checkedGracePeriod = resolveParamGracePeriods(serviceLineDto.getParamGracePeriod(), tariffLineParamEntity);
                                checkedGraceBySourceDtoList.add(checkedGracePeriod);
                            }
                        }
                        subscribeTariffLineDto.setParamList(tariffLineParams);
                        for (SubscribeParamDto tariffParamDto : subscribeTariffLineDto.getParamList()) {
                            subscribeTariffLineDto.getFilterParams().add(convertParamToFilter(tariffParamDto));
                        }
                        tariffLines.add(subscribeTariffLineDto);
                    }
                    subscribeTariffDto.setGracePeriodsDto(mapParamGracePeriods(serviceLineDto.getParamGracePeriod()));
                    subscribeTariffDto.getGracePeriodsDto().setCheckedGraceBySourceDtoList(checkedGraceBySourceDtoList);
                    subscribeTariffDto.setSubscribeTariffLineDtoList(tariffLines);
                }
                tariffs.add(subscribeTariffDto);
            }
            serviceLineDto.setTariffList(tariffs);
        }
        if (serviceLineEntity.getCheckSet() != null && serviceLineEntity.getCheckSet().getCheckSetLines() != null) {
            List<CheckSetLineDto> checkSetLines = new ArrayList<>();
            for (CheckSetLineEntity checkSetLineEntity : serviceLineEntity.getCheckSet().getCheckSetLines()) {
                //check dates
                LocalDateTime entityBegDate = (serviceLineEntity.getBegDate());
                LocalDateTime entityEndDate = (serviceLineEntity.getEndDate());
                LocalDateTime currentDate = (subscribeRequest.getSearchOnDate().toLocalDateTime()); //todo шаг X убрать фильтрацию по дате тут, фильтровать при запросе внутри SQB
                if (
                    //entityBegDate.isAfter(currentDate) ||
                        entityEndDate.isBefore(currentDate))
                    continue;

                CheckSetLineDto checkSetLineDto = new CheckSetLineDto();
                checkSetLineDto.setId(checkSetLineEntity.getId());
                checkSetLineDto.setBegDate(ZonedDateTime.of(checkSetLineEntity.getBegDate(), ZoneId.systemDefault()));
                checkSetLineDto.setEndDate(ZonedDateTime.of(checkSetLineEntity.getEndDate(), ZoneId.systemDefault()));

                if (checkSetLineEntity.getFilterParams() != null) {
                    List<SubscribeFilterParamDto> tariffLineFilterParams = new ArrayList<>();
                    for (ParamValueEntity filterParamEntity : checkSetLineEntity.getFilterParams()) {
                        tariffLineFilterParams.add((SubscribeFilterParamDto) resolveGroupGuides(convertFilterParam(filterParamEntity), currentDate));
                    }
                    checkSetLineDto.setFilterParams(tariffLineFilterParams);
                }

                if (checkSetLineEntity.getCheckSetLineParams() != null) {
                    List<SubscribeParamDto> checkSetLineParams = new ArrayList<>();
                    for (SetLineParamEntity setLineParamEntity : checkSetLineEntity.getCheckSetLineParams()) {
                        SubscribeParamDto setLineParam = new SubscribeParamDto();

                        setLineParam.setId(Long.valueOf(setLineParamEntity.getUuid()));
                        setLineParam.setOrder(setLineParamEntity.getOrder());
                        setLineParam.setCode(setLineParamEntity.getCode());
                        setLineParam.setLabel(setLineParamEntity.getLabel());
                        setLineParam.setGuide(setLineParamEntity.getGuide());
                        setLineParam.setGuide(setLineParamEntity.getGuide());
                        setLineParam.setType(setLineParamEntity.getType());
                        setLineParam.setVType(setLineParamEntity.getVType());
                        setLineParam.setVTypeLabel(setLineParamEntity.getVTypeLabel());
                        setLineParam.setIsGroupGuide(setLineParamEntity.getIsGroupGuide());
                        switch (setLineParamEntity.getType()) {
                            case "BOOLEANTYPE":
                                setLineParam.setVBoolean(setLineParamEntity.getVBoolean());
                                break;
                            case "STRINGTYPE":
                                //todo - мб поменять сразу на списки?
                                setLineParam.setVCodeList(setLineParamEntity.getVCodeList());
                                setLineParam.setVCodePattern(setLineParamEntity.getVCodePattern());
                                setLineParam.setVLabelList(setLineParamEntity.getVLabelList());
                                setLineParam.setVString(setLineParamEntity.getVString());
                                break;
                            case "DATETYPE":
                                if (setLineParamEntity.getVDateMin() != null) {
                                    setLineParam.setVDateMin(mapToZonedDateTime(setLineParamEntity.getVDateMin()));
                                    setLineParam.setVDateMax(mapToZonedDateTime(setLineParamEntity.getVDateMax()));
                                } else
                                    setLineParam.setVDate(mapToZonedDateTime(setLineParamEntity.getVDate()));
                                break;
                            case "INTTYPE":
                                if (setLineParamEntity.getVIntMin() != null) {
                                    setLineParam.setVIntMin(setLineParamEntity.getVIntMin());
                                    setLineParam.setVIntMax(setLineParamEntity.getVIntMax());
                                } else
                                    setLineParam.setVInt(setLineParamEntity.getVInt());
                                break;
                            case "FLOATTYPE":
                                if (setLineParamEntity.getVFloatMin() != null) {
                                    setLineParam.setVFloatMin(setLineParamEntity.getVFloatMin());
                                    setLineParam.setVFloatMax(setLineParamEntity.getVFloatMax());
                                } else
                                    setLineParam.setVFloat(setLineParamEntity.getVFloat());
                                break;
                            case "CODETYPE":
                                setLineParam.setVCodePattern(setLineParamEntity.getVCodePattern());
                                setLineParam.setVCodeList(setLineParamEntity.getVCodeList());
                                setLineParam.setVLabelList(setLineParamEntity.getVLabelList());
                                resolveGroupGuides(setLineParam, currentDate);
                                break;
                        }
                        checkSetLineParams.add(setLineParam);
                    }
                    checkSetLineDto.setParamList(checkSetLineParams);
                }
                checkSetLines.add(checkSetLineDto);
            }
            serviceLineDto.setCheckSetLineList(checkSetLines);
        }

        return serviceLineDto;
    }


    private SubscribeFilterParamDto convertParamToFilter(SubscribeParamDto tariffParamDto) {
        SubscribeFilterParamDto filterParamDto = new SubscribeFilterParamDto();
        filterParamDto.setId(tariffParamDto.getId());
        filterParamDto.setOrder(tariffParamDto.getOrder());
        filterParamDto.setCode(tariffParamDto.getCode().split("\\.")[1]);
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
        filterParamDto.setObjectType(tariffParamDto.getCode().split("\\.")[0]);
        filterParamDto.setIsGroupGuide(tariffParamDto.getIsGroupGuide());
        filterParamDto.setIsGroupResolved(tariffParamDto.getIsGroupResolved());
        filterParamDto.setListOfGroupGuideDtos(tariffParamDto.getListOfGroupGuideDtos());
        filterParamDto.setTariffPart(((TariffParamDto) tariffParamDto).getTariffPart());
        return filterParamDto;
    }

    private SubscribeFilterParamDto convertFilterParam(ParamValueEntity filterParamEntity) {
        SubscribeFilterParamDto result = new SubscribeFilterParamDto();
        result.setCompareType(filterParamEntity.getValueTypeEntity().getCode());
        result.setOrder(filterParamEntity.getSortOrder());
        result.setCode(filterParamEntity.getParam().getCode());
        result.setObjectType(filterParamEntity.getParam().getObjectType());
//        result.setLabel();
//        result.setGuide();
        result.setType(filterParamEntity.getValueTypeEntity().getGuideType());
        result.setVType(filterParamEntity.getValueTypeEntity().getCode());
        //result.setType(filterParamEntity.);
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
        result.setIsGroupGuide(filterParamEntity.getParam().getGuideEntity().getIsGroupGuide());

        //todo - мб перегнать в list сразу? в ответ все равно не попадает
        // вместе с *(1)
        result.setVCodePattern(filterParamEntity.getCodePattern());
        result.setVCodeList(filterParamEntity.getCodeList());
        result.setVLabelList(filterParamEntity.getLabelList());
        return result;
    }

    private GroupGuideDto mapGroupGuide(GroupGuideEntity gge) {
        GroupGuideDto groupGuideDto = new GroupGuideDto();
        groupGuideDto.setGroupCode(gge.getGroupCode());
        groupGuideDto.setGuideCode(gge.getGuideCode());
        groupGuideDto.setBegDate(mapToZonedDateTime(gge.getBegDate()));
        groupGuideDto.setEndDate(mapToZonedDateTime(gge.getEndDate()));
        //groupGuideDto.setBegDate(ZonedDateTime.of(gge.getBegDate(), ZoneId.systemDefault())); если когда-нибудь понадобится хранить в нормальном формате
        //groupGuideDto.setEndDate(ZonedDateTime.of(gge.getEndDate(), ZoneId.systemDefault()));
        groupGuideDto.setLinkCodeList(gge.getLinkCodeList());
        return groupGuideDto;
    }

    private SubscribeParamDto resolveGroupGuides(SubscribeParamDto paramDto, LocalDateTime ldt) {
        if (paramDto.getIsGroupGuide()) {
            paramDto.setListOfGroupGuideDtos(allGroupGuides.stream().filter(
                    groupGuideDto -> groupGuideDto.getEndDate().isAfter(ldt.atZone(ZoneId.systemDefault())) &&
                            groupGuideDto.getGuideCode().equals(paramDto.getGuide()) &&
                            paramDto.getVCodeList() != null &&
                            Arrays.asList(paramDto.getVCodeList().split(";")).contains(groupGuideDto.getGroupCode())
            ).collect(Collectors.toCollection(ArrayList::new)));
        }
        paramDto.setIsGroupResolved(true);
        return paramDto;
    }

    private MappedGracePeriodsDto mapParamGracePeriods(List<ParamGracePeriodDto> paramGraceList) {
        var gracePeriodsDto = new MappedGracePeriodsDto();
        List<String> codesNeededToBeCheckedList = new ArrayList<>();
        List<String> codesNotNeededToBeCheckedList = new ArrayList<>();
        for (ParamGracePeriodDto paramGracePeriodDto : paramGraceList) {
            String[] paramsFromGrace = paramGracePeriodDto.getSourceCodeList().split(";"); //requestParamList
            for (String paramFromGrace : paramsFromGrace) {
                if (paramGracePeriodDto.getCheckGrace())
                    codesNeededToBeCheckedList.add(paramFromGrace);
                else
                    codesNotNeededToBeCheckedList.add(paramFromGrace);
            }
        }
        gracePeriodsDto.setCodesNeededToBeCheckedList(codesNeededToBeCheckedList);
        gracePeriodsDto.setCodesNotNeededToBeCheckedList(codesNotNeededToBeCheckedList);
        return gracePeriodsDto;
    }

    private CheckedGraceBySourceDto resolveParamGracePeriods(List<ParamGracePeriodDto> paramGraceList, TariffLineParamEntity tariffLineParamEntity) {
        var checkedGraceBySourceDto = new CheckedGraceBySourceDto();
        checkedGraceBySourceDto.setOrder(tariffLineParamEntity.getTariffLine().getSortOrder());
        checkedGraceBySourceDto.setValue(false);
        var resultSet = new HashSet<Boolean>();
        if (paramGraceList == null || paramGraceList.isEmpty()) {
            checkedGraceBySourceDto.setValue(false); //возвращаем по умолчанию false, если настроек нет
            return checkedGraceBySourceDto;
        }
        int appearanceIndex = 0;
        for (ParamGracePeriodDto paramGracePeriodDto : paramGraceList) {
            Boolean checkResult = false;
            Set<String> targetParamList = tariffLineParamEntity.getVCodeList() != null ?
                    new HashSet<>(Arrays.asList(tariffLineParamEntity.getVCodeList().split(";")))
                    : null;
            Set<String> requestParamList = paramGracePeriodDto.getSourceCodeList() != null
                    ? new HashSet<>(Arrays.asList(paramGracePeriodDto.getSourceCodeList().split(";")))
                    : null;
            switch (tariffLineParamEntity.getVType().toUpperCase()) {
                case "ALL":
                    checkResult = true;
                    break;
                case "NOT_EMPTY":
                    if (paramGracePeriodDto.getSourceCodeList() != null && !paramGracePeriodDto.getSourceCodeList().isEmpty())
                        checkResult = true;
                case "IN":
                    if (targetParamList == null || targetParamList.containsAll(requestParamList))
                        checkResult = true;
                    break;
                case "IN_NOTNULL":
                    if (targetParamList != null && targetParamList.containsAll(requestParamList))
                        checkResult = true;
                    break;
                case "NOT_IN":
                    if (targetParamList == null || !targetParamList.containsAll(requestParamList))
                        checkResult = true;
                    break;
                case "NOT_IN_NOTNULL":
                    if (targetParamList != null && !targetParamList.containsAll(requestParamList))
                        checkResult = true;
                    break;

                case "IN_EXT":
                    if (targetParamList == null || Sets.intersection(requestParamList, targetParamList).size() != 0)
                        checkResult = true;
                    break;
                case "IN_EXT_NOTNULL":
                    if (targetParamList != null && Sets.intersection(requestParamList, targetParamList).size() != 0)
                        checkResult = true;
                    break;
                case "NOT_IN_EXT":
                    if (targetParamList == null || Sets.intersection(requestParamList, targetParamList).size() == 0)
                        checkResult = true;
                    break;
                case "NOT_IN_EXT_NOTNULL":
                    if (targetParamList != null && Sets.intersection(requestParamList, targetParamList).size() == 0)
                        checkResult = true;
                    break;

                case "INCLUDE":
                    if (targetParamList == null || (targetParamList.containsAll(requestParamList) && requestParamList.containsAll(targetParamList)))
                        checkResult = true;
                    break;
                case "INCLUDE_NOTNULL":
                    if (targetParamList != null && (targetParamList.containsAll(requestParamList) && requestParamList.containsAll(targetParamList)))
                        checkResult = true;
                    break;
                case "NOT_INCLUDE":
                    if (targetParamList == null || !(targetParamList.containsAll(requestParamList) && requestParamList.containsAll(targetParamList)))
                        checkResult = true;
                    break;
                case "NOT_INCLUDE_NOTNULL":
                    if (targetParamList != null && !(targetParamList.containsAll(requestParamList) && requestParamList.containsAll(targetParamList)))
                        checkResult = true;
                    break;
                case "INCLUDE_EXT":
                    if (targetParamList == null || targetParamList.containsAll(requestParamList))
                        checkResult = true;
                    break;
                case "INCLUDE_EXT_NOTNULL":
                    if (targetParamList != null && targetParamList.containsAll(requestParamList))
                        checkResult = true;
                    break;
                case "NOT_INCLUDE_EXT":
                    if (targetParamList == null || !targetParamList.containsAll(requestParamList))
                        checkResult = true;
                    break;
                case "NOT_INCLUDE_EXT_NOTNULL":
                    if (targetParamList != null && !targetParamList.containsAll(requestParamList))
                        checkResult = true;
                    break;
                case "EMPTY":
                    checkResult = paramGracePeriodDto.getSourceCodeList() == null || paramGracePeriodDto.getSourceCodeList().isEmpty();
                    break;
                default:
                    throw new UnsupportedOperationException("Strange param in gracePeriod resolving process, operator = " + tariffLineParamEntity.getVType().toUpperCase());
            }
            if (checkResult) {
                appearanceIndex++;
                resultSet.add(paramGracePeriodDto.getCheckGrace());
            }
        }
        if (appearanceIndex == 1 ||
                (appearanceIndex > 1 && resultSet.size() == 1))
            checkedGraceBySourceDto.setValue(resultSet.iterator().next());
        return checkedGraceBySourceDto;
    }

    /*
    В бд prc для хранения различных boolean признаков используется int
    Записи могут содержать в себе null, 0, 1 и 2
    2 = null
     */
    private Boolean extractBooleanFromInteger(Integer intFromDB) {
        if (intFromDB == null || intFromDB == 2)
            return null;
        return intFromDB == 1;
    }

    private ZonedDateTime mapToZonedDateTime(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(ldt -> ZonedDateTime.of(ldt, ZoneId.systemDefault()))
                .orElse(null);
    }
}
