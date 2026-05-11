/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.query.graceperiod;


import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.it_alnc.packagesearch.dto.graceperiod.GracePeriodDto;
import ru.it_alnc.packagesearch.dto.graceperiod.GracePeriodRequestDto;
import ru.it_alnc.packagesearch.dto.subscribe.SubscribeFilterParamDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnBean(GracePeriodReaderInitializer.class)
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GracePeriodQueryBuilder  {

    @Autowired
    private GracePeriodReader gracePeriodReader;

    @Getter
    private List<GracePeriodDto> gracePeriods;

    public static final String DATETYPE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public void init(GracePeriodRequestDto requestDto) throws CloneNotSupportedException {
        if (gracePeriodReader.getGracePeriodDtoList() == null) {
            gracePeriodReader.readAllGracePeriods(LocalDateTime.now());
            log.error("Empty grace period cache while cloning, rereading it!");
        }
        List<GracePeriodDto> gracePeriods = new ArrayList<>();
        ZonedDateTime filterDate;
        if(requestDto.getCalculationDate() != null) {
            filterDate = requestDto.getCalculationDate();
        } else {
            filterDate = ZonedDateTime.now();
        }
        List<GracePeriodDto> filteredList = gracePeriodReader.getGracePeriodDtoList().stream().filter(
                gracePeriodDto -> (gracePeriodDto.getBegDate().isBefore(filterDate)|| gracePeriodDto.getBegDate().isEqual(filterDate)) &&
                        gracePeriodDto.getEndDate().isAfter(filterDate)
        ).toList();
        for (GracePeriodDto gracePeriodDto : filteredList) {
            gracePeriods.add(gracePeriodDto.clone());
        }
        this.gracePeriods = gracePeriods;
    }

    public void filterGracePeriodsByRequest(GracePeriodRequestDto gracePeriodRequest) {
        this.gracePeriods = this.gracePeriods.stream().filter(
                gracePeriodDto -> "F".equals(gracePeriodDto.getClientCommonType()) &&
                        Objects.equals(gracePeriodRequest.getService(),gracePeriodDto.getService())
        ).collect(Collectors.toCollection(ArrayList::new));
    }

/*    public void sortGracePeriodsByGPID(){
        this.gracePeriods = this.gracePeriods.stream()
                                            .sorted()
                                            .collect(Collectors.toCollection(ArrayList::new));
    }*/


    public void filterGracePeriodsByDynamicParams(GracePeriodRequestDto gracePeriodRequestDto) {
        List<GracePeriodDto> filteredGracePeriods = new ArrayList<>();
        for (GracePeriodDto gracePeriodDto : this.gracePeriods) {
            boolean isParamsOk = true;
            if (gracePeriodDto.getParamList() != null) {
                for (SubscribeFilterParamDto subscribeParamDto : gracePeriodDto.getParamList()) {
                    Optional<GracePeriodRequestDto.ParamMap> requestOperationFilterParam = Optional.empty();

                    if (gracePeriodRequestDto.getOperationParams() != null)
                        requestOperationFilterParam = gracePeriodRequestDto.getOperationParams()
                                .stream().filter(paramMap -> paramMap.getCode().equals(subscribeParamDto.getCode().split("\\.")[1])).findFirst();


                    if (!requestOperationFilterParam.isPresent()
                            && subscribeParamDto.getCode().split("\\.")[0].equals("OPERATION")
                            && !subscribeParamDto.getVType().equals("NOT_EMPTY"))
                        continue;
                    subscribeParamDto.setCompareType(subscribeParamDto.getVType());
                    subscribeParamDto.setObjectType(subscribeParamDto.getCode().split("\\.")[0]);
                    if (subscribeParamDto.getCode().split("\\.")[0].equals("OPERATION")
                            && !compareParam(requestOperationFilterParam.orElse(new GracePeriodRequestDto.ParamMap()), subscribeParamDto)) {
                        log.debug("\t{} Product pre-set operation filter param {} mismatch!", gracePeriodDto.getGpId(), subscribeParamDto.getCode());
                        isParamsOk = false;
                    }


                }
            }
            if(isParamsOk) filteredGracePeriods.add(gracePeriodDto);
        }
        this.gracePeriods = filteredGracePeriods;
    }


    private boolean compareParam(GracePeriodRequestDto.ParamMap requestParam, SubscribeFilterParamDto filterParam) {
        if("ALL".equals(filterParam.getCompareType()))
            return true;
        if (filterParam.getCompareType().equals("NOT_EMPTY") && requestParam.getValue() != null && !requestParam.getValue().isEmpty())
            return true;

        switch (filterParam.getType().toUpperCase()) {
            //28310 - null в boolean означает "любой", т.е. сравнение с таким параметром всегда истинное
            case "BOOLEANTYPE":
                if(filterParam.getCompareType().equals("BOOLEAN")){
                    if(requestParam.getValue() == null || requestParam.getValue().isEmpty())
                        return false;
                    return Boolean.valueOf(requestParam.getValue()).equals(filterParam.getVBoolean());
                }
                if(filterParam.getCompareType().equals("BOOLEAN_OR_NULL")){
                    if(requestParam.getValue() == null || requestParam.getValue().isEmpty())
                        return true;
                    return Boolean.valueOf(requestParam.getValue()).equals(filterParam.getVBoolean());
                }
                if (filterParam.getVBoolean() == null
                        || Boolean.valueOf(requestParam.getValue()).equals(filterParam.getVBoolean()))
                    return true;
                break;
            case "STRINGTYPE":
                if(requestParam.getValue() == null || requestParam.getValue().isEmpty())
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

                if(filterParam.getCompareType().equals("NOT_EQUAL"))
                    return !Objects.equals(requestParam.getValue(), filterParam.getVString());

                if(filterParam.getCompareType().equals("EQUAL"))
                    return Objects.equals(filterParam.getVString(),requestParam.getValue());
                break;
            case "CODETYPE":
                Set<String> targetParamList = filterParam.getVCodeList() != null ?
                        new HashSet<>(Arrays.asList(filterParam.getVCodeList().split(";")))
                        : null;
                Set<String> requestParamList = requestParam.getValue() != null
                        ? new HashSet<>(Arrays.asList(requestParam.getValue().split(";")))
                        : null;
                switch (filterParam.getCompareType().toUpperCase()) {
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
                        if (targetParamList == null || targetParamList.containsAll(requestParamList))
                            return true;
                        break;
                    case "IN_NOTNULL":
                        if (targetParamList != null && targetParamList.containsAll(requestParamList))
                            return true;
                        break;
                    case "NOT_IN":
                        if (targetParamList == null || !targetParamList.containsAll(requestParamList))
                            return true;
                        break;
                    case "NOT_IN_NOTNULL":
                        if (targetParamList != null && !targetParamList.containsAll(requestParamList))
                            return true;
                        break;

                    case "IN_EXT":
                        if (targetParamList == null || Sets.intersection(requestParamList, targetParamList).size() != 0)
                            return true;
                        break;
                    case "IN_EXT_NOTNULL":
                        if (targetParamList != null && Sets.intersection(requestParamList, targetParamList).size() != 0)
                            return true;
                        break;
                    case "NOT_IN_EXT":
                        if (targetParamList == null || Sets.intersection(requestParamList, targetParamList).size() == 0)
                            return true;
                        break;
                    case "NOT_IN_EXT_NOTNULL":
                        if (targetParamList != null && Sets.intersection(requestParamList, targetParamList).size() == 0)
                            return true;
                        break;

                    case "INCLUDE":
                        if (targetParamList == null || (targetParamList.containsAll(requestParamList) && requestParamList.containsAll(targetParamList)))
                            return true;
                        break;
                    case "INCLUDE_NOTNULL":
                        if (targetParamList != null && (targetParamList.containsAll(requestParamList) && requestParamList.containsAll(targetParamList)))
                            return true;
                        break;
                    case "NOT_INCLUDE":
                        if (targetParamList == null || !(targetParamList.containsAll(requestParamList) && requestParamList.containsAll(targetParamList)))
                            return true;
                        break;
                    case "NOT_INCLUDE_NOTNULL":
                        if (targetParamList != null && !(targetParamList.containsAll(requestParamList) && requestParamList.containsAll(targetParamList)))
                            return true;
                        break;

                    case "INCLUDE_EXT":
                        if (targetParamList == null || targetParamList.containsAll(requestParamList))
                            return true;
                        break;
                    case "INCLUDE_EXT_NOTNULL":
                        if (targetParamList != null && targetParamList.containsAll(requestParamList))
                            return true;
                        break;
                    case "NOT_INCLUDE_EXT":
                        if (targetParamList == null || !targetParamList.containsAll(requestParamList))
                            return true;
                        break;
                    case "NOT_INCLUDE_EXT_NOTNULL":
                        if (targetParamList != null && !targetParamList.containsAll(requestParamList))
                            return true;
                        break;
                }
                break;
            case "DATETYPE":
                if(requestParam.getValue() == null || requestParam.getValue().isEmpty()){
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
                if(requestParam.getValue() == null || requestParam.getValue().isEmpty())
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




}
