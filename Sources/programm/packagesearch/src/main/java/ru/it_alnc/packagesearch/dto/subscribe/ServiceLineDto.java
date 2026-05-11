/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import ru.it_alnc.packagesearch.dto.FL.BegEndDatesDto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
//@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class ServiceLineDto implements Cloneable {
    @JsonIgnore
    private Long id;
    private Integer order;
    private String serviceGuide;
    private String service;
    private String serviceLabel;
    @JsonIgnore
    private Long obj;
    private Boolean isDeleted;
    @JsonIgnore
    private String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime begDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime endDate;
    private Integer priority;
    private String contextType;
    private String contextTypeLabel;
    private String serviceType;
    private String serviceTypeLabel;
    private String tariffType;
    private String tariffTypeLabel;
    private Integer limit4FreeQuantity;
    private BigDecimal limit4FreeSumAmount;
    private String limit4FreeSumCurrency;
    private String indexCode;
    private String ndsType;
    private String ndsTypeLabel;
    private Integer ndsRate;
    private Boolean isNoEntry;
    private String entryEventType;
    private String entryEventTypeLabel;
    private String rateType;
    private String rateTypeLabel;
    private Boolean isSingleServiceInOper;
    private Boolean isAggregateMultyService;
    private Boolean isAggregateSummary;
    private Integer aggregateSummaryNum;
    private String paramOperAmount;
    private String paramOperCurrency;
    private Boolean isManualActivate;
    private String enablePeriodCode;
    private String enablePeriodLabel;
    private String periodicPayType;
    private String periodicPayTypeLabel;
    private String periodicPartUsedPayType;
    private String periodicPartUsedPayTypeLabel;
    private Boolean isPeriodicROO;
    private String periodicROOWaitPeriod;
    private String periodicROOWaitPeriodLabel;
    private String gracePeriodCode;
    private String gracePeriodLabel;
    private Boolean isPeriodicCheckOperations;
    private Boolean isPeriodicCheckRest;
    private Boolean isSplitGraceByPeriods;
    private String pu4PriceService;

    private String prefPeriod;
    private String prefPeriodLabel;
    private String prefPeriodEndAction;
    private String prefPeriodEndActionLabel;
    private String trialPeriod;
    private String trialPeriodLabel;
    private String trialPeriodEndAction;
    private String trialPeriodEndActionLabel;
    private Boolean isMultyCopiesCalcAllowed;


    @JsonIgnore
    private String serviceGuideItemCode; //значение для фильтрации уже маппленных SL
    @JsonIgnore
    private String puServiceGroupCode;  //значение для фильтрации уже маппленных SL
    @JsonIgnore
    private String puServiceGroupProductCode;  //значение для фильтрации уже маппленных
    @JsonIgnore
    private String objTable;
    @JsonIgnore
    private Long pu4PriceServiceLong;
    @JsonIgnore
    private List<BegEndDatesDto> flPeriodsList;

    private Integer isAutoProlongation;

    private String periodicConnLinkType;
    private Boolean isNotPayFromSUOToSD;
    private Boolean isCheckCardActive;
    private Boolean isProlongateIgnoreCheckErr;
    private Boolean isPayFeeIfProlongationCheckErr;
    private Boolean isEnableAddPrefAfterConn;
    private Boolean isTunePrefBegDate;
    private String paramPrefBegDate;
    private String onChangePUActType;
    private Boolean isSyncEntry;

    private String prefPeriodPayType;
    private String prefPeriodRuleType;

    private String paymentDaysROOCode;

    private Integer isNotRedoCommissOnEQError;
    private Integer IsProlongNoWaitEQResponse;

    private String entryMode;

    private String discount2Package;
    private BigDecimal discountRate;
    private BigDecimal discountAmt;
    private String discountCur;
    private BigDecimal discountAmtAbs;
    private String discountAmtCur;

    private Boolean isNotifyKafka;
    private Boolean isIgnoreEqError;
    private Boolean isSupportCalcResult;

    private List<ParamGracePeriodDto> paramGracePeriod = new ArrayList<>();

    private List<PlannedOperationSettingDto> plannedOperationSettingsList = new ArrayList<>();

    private List<SubscribeParamDto> paramList = new ArrayList<>();
//    private List<CounterDto> counterList = new ArrayList<>();
    private List<SubscribeTariffDto> tariffList = new ArrayList<>();
    private List<CheckSetLineDto> checkSetLineList = new ArrayList<>();

    @JsonIgnore
    private List<SubscribeFilterParamDto> filterParams;

    @Override
    public ServiceLineDto clone() throws CloneNotSupportedException {
        ServiceLineDto clonedServiceLineDto = (ServiceLineDto) super.clone();

        if (this.getParamList() != null) {
            List<SubscribeParamDto> subscribeParamDtoList = new ArrayList<>();
            for (SubscribeParamDto subscribeParamDto : this.getParamList()) {
                subscribeParamDtoList.add(subscribeParamDto.clone());
            }
            clonedServiceLineDto.setParamList(subscribeParamDtoList);
        }

//        if (this.getCounterList() != null) {
//            List<CounterDto> counterDtoList = new ArrayList<>();
//            for (CounterDto counterDto : this.getCounterList()) {
//                counterDtoList.add(counterDto.clone());
//            }
//            clonedServiceLineDto.setCounterList(counterDtoList);
//        }

        if (this.getTariffList() != null) {
            List<SubscribeTariffDto> subscribeTariffDtoList = new ArrayList<>();
            for (SubscribeTariffDto subscribeTariffDto : this.getTariffList()) {
                subscribeTariffDtoList.add(subscribeTariffDto.clone());
            }
            clonedServiceLineDto.setTariffList(subscribeTariffDtoList);
        }

        if (this.getCheckSetLineList() != null) {
            List<CheckSetLineDto> checkSetLineDtoList = new ArrayList<>();
            for (CheckSetLineDto checkSetLineDto : this.getCheckSetLineList()) {
                checkSetLineDtoList.add(checkSetLineDto.clone());
            }
            clonedServiceLineDto.setCheckSetLineList(checkSetLineDtoList);
        }

        return clonedServiceLineDto;
    }
}
