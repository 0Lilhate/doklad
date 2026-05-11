/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity.subscribe;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import ru.it_alnc.packagesearch.entity.ParamEntity;
import ru.it_alnc.packagesearch.entity.subscribe.types.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "serviceline")
@Getter
@Setter
public class ServiceLine27277Entity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "begdate", nullable = false)
    private LocalDateTime begDate;
    @Basic
    @Column(name = "enddate", nullable = false)
    private LocalDateTime endDate;
    @Basic
    @Column(name = "modifiedon", nullable = false)
    private LocalDateTime modifiedOn;
    @Column(name = "status")
    private String status;

    @ManyToOne
    @JoinColumn(name = "serviceguideitem")
    private ServiceGuideItemEntity serviceGuideItem;
    @ManyToOne
    @JoinColumn(name = "serviceguide")
    private ServiceGuideEntity serviceGuide;
    @OneToMany
    @JoinColumn(name = "obj")
    @BatchSize(size = 150)
    private Set<ParamValueEntity> filterParams;
    @OneToMany
    @JoinColumn(name = "obj")
    @BatchSize(size = 150)
    private Set<SetLineParamEntity> paramList;
    @ManyToOne
    @JoinColumn(name = "enablecheckset")
    private CheckSetEntity checkSet;
    @OneToMany
    @JoinColumn(name = "serviceline")
    @BatchSize(size = 150)
    private Set<CounterEntity> counters;

    @Column(name = "objtable")
    private String objTable;
    @Column(name = "obj")
    private Long obj;
    @OneToMany(mappedBy = "serviceLine")
    @BatchSize(size = 150)
    private Set<ParamGracePeriodEntity> paramGracePeriods;
    @OneToMany(mappedBy = "serviceLine")
    @BatchSize(size = 150)
    private Set<PlannedOperationSettingEntity> plannedOperationSettings;
    @Column(name = "pu4priceservice")
    private Long pu4PriceService;
    @Basic
    @Column(name = "priority", nullable = true)
    private Integer priority;
    @Basic
    @Column(name = "sortorder", nullable = true)
    private Integer sortOrder;
    @Basic
    @Column(name = "contexttype", insertable = false, updatable = false, nullable = false, length = 1)
    private String contextType;
    @ManyToOne
    @JoinColumn(name = "contexttype")
    private ContextTypeEntity contextTypeEntity;
    @ManyToOne
    @JoinColumn(name = "servicetype")
    private ServiceTypeEntity serviceTypeEntity;
    @Basic
    @Column(name = "tarifftype", insertable = false, updatable = false, nullable = false, length = 1)
    private String tariffType;
    @ManyToOne
    @JoinColumn(name = "tarifftype")
    private TariffTypeEntity tariffTypeEntity;
    @Basic
    @Column(name = "indexcode", nullable = true, length = 50)
    private String indexCode;
    @Basic
    @Column(name = "ndstype", insertable = false, updatable = false, nullable = false, length = 1)
    private String ndsType;
    @ManyToOne
    @JoinColumn(name = "ndstype")
    private NdsTypeEntity ndsTypeEntity;
    @OneToMany(mappedBy = "serviceLine")
    @BatchSize(size = 150)
    private Set<TariffEntity> tariffs;
    @Column(name = "tariffheader")
    private Long tarriffHeader;
    @Basic
    @Column(name = "isnoentry", nullable = false)
    private Boolean isNoEntry;
    @Basic
    @Column(name = "entryeventtype", insertable = false, updatable = false, nullable = true, length = 1)
    private String entryEventType;
    @ManyToOne
    @JoinColumn(name = "entryeventtype")
    private EntryEventTypeEntity entryEventTypeEntity;
    @Basic
    @Column(name = "ratetype", insertable = false, updatable = false, nullable = true, length = 1)
    private String rateType;
    @ManyToOne
    @JoinColumn(name = "ratetype")
    private RateTypeEntity rateTypeEntity;
    @Basic
    @Column(name = "issingleserviceinoper", nullable = false)
    private Boolean isSingleServiceInOper;
    @Basic
    @Column(name = "isaggregatemultyservice", nullable = false)
    private Boolean isAggregateMultiService;
    @Basic
    @Column(name = "isaggregatesummary", nullable = false)
    private Boolean isAggregateSummary;
    @Basic
    @Column(name = "aggregatesummarynum", nullable = true)
    private Integer aggregateSummaryNum;
    @ManyToOne
    @JoinColumn(name = "paramoperamount")
    private ParamEntity paramOperAmount;
    @ManyToOne
    @JoinColumn(name = "paramopercurrency")
    private ParamEntity paramOperCurrency;
    @Basic
    @Column(name = "limit4freequantity", nullable = true)
    private Integer limit4FreeQuantity;
    @Basic
    @Column(name = "limit4freesumamount", nullable = true, precision = 4)
    private BigDecimal limit4FreeSumAmount;
    @Basic
    @Column(name = "limit4freesumcurrency", nullable = true, length = 50)
    private String limit4FreeSumCurrency;
    @Basic
    @Column(name = "ismanualactivate", nullable = false)
    private Boolean isManualActivate;
    @Basic
    @Column(name = "enableperiodcode", nullable = true, length = 250)
    private String enablePeriodCode;
    @Basic
    @Column(name = "enableperiodlabel", nullable = true, length = 2000)
    private String enablePeriodLabel;
    @Basic
    @Column(name = "periodicpaytype", insertable = false, updatable = false, nullable = true, length = 1)
    private String periodicPayType;
    @ManyToOne
    @JoinColumn(name = "periodicpaytype")
    private PeriodicPayTypeEntity periodicPayTypeEntity;
    @Basic
    @Column(name = "periodicpartusedpaytype", insertable = false, updatable = false, nullable = true, length = 1)
    private String periodicPartUsedPayType;
    @ManyToOne
    @JoinColumn(name = "periodicpartusedpaytype")
    private PeriodicPartUsedPayTypeEntity periodicPartUsedPayTypeEntity;
    @Basic
    @Column(name = "isperiodicroo", nullable = false)
    private Boolean isPeriodicRoo;
    @Basic
    @Column(name = "periodicroowaitperiod", nullable = true, length = 50)
    private String periodicRooWaitPeriod;
    @Basic
    @Column(name = "periodicroowaitperiodlabel", nullable = true, length = 250)
    private String periodicRooWaitPeriodLabel;
    @Basic
    @Column(name = "isperiodiccheckoperations", nullable = false)
    private Boolean isPeriodicCheckOperations;
    @Basic
    @Column(name = "isperiodiccheckrest", nullable = false)
    private Boolean isPeriodicCheckRest;
//    @Basic
//    @Column(name = "issplitgracebyperiods")
//    private Boolean isSplitGraceByPeriods;
    @ManyToOne
    @JoinColumn(name = "puservicegroup")
    private PUServiceGroupEntity puServiceGroup;
    @Basic
    @Column(name = "graceperiod", nullable = true, length = 50)
    private String gracePeriod;
    @Basic
    @Column(name = "graceperiodlabel", nullable = true, length = 250)
    private String gracePeriodLabel;
    @Basic
    @Column(name = "prefperiod")
    private String prefPeriod;
    @Basic
    @Column(name = "prefperiodlabel")
    private String prefPeriodLabel;
    @Basic
    @Column(name = "prefperiodendaction")
    private String prefPeriodEndAction;
    @Basic
    @Column(name = "prefperiodendactionlabel")
    private String prefPeriodEndActionLabel;
    @Basic
    @Column(name = "trialperiod")
    private String trialPeriod;
    @Basic
    @Column(name = "trialperiodlabel")
    private String trialPeriodLabel;
    @Basic
    @Column(name = "trialperiodendaction")
    private String trialPeriodEndAction;
    @Basic
    @Column(name = "trialperiodendactionlabel")
    private String trialPeriodEndActionLabel;
    @Basic
    @Column(name = "isautoprolongation")
    private Integer isAutoProlongation;
    @Basic
    @Column(name = "periodicconnlinktype")
    private String periodicConnLinkType;
    @Basic
    @Column(name = "isnotpayfromsuotosd")
    private Boolean isNotPayFromSUOToSD;
    @Basic
    @Column(name = "ischeckcardactive")
    private Boolean isCheckCardActive;
    @Basic
    @Column(name = "isprolongateignorecheckerr")
    private Boolean isProlongateIgnoreCheckErr;
    @Basic
    @Column(name = "ispayfeeifprolongationcheckerr")
    private Boolean isPayFeeIfProlongationCheckErr;
    @Basic
    @Column(name = "isenableaddprefafterconn")
    private Boolean isEnableAddPrefAfterConn;
    @Basic
    @Column(name = "istuneprefbegdate")
    private Boolean isTunePrefBegDate;
    @ManyToOne
    @JoinColumn(name = "paramprefbegdate")
    private ParamEntity paramPrefBegDate;
    @Basic
    @Column(name = "onchangepuacttype")
    private String onChangePUActType;
    @Basic
    @Column(name = "issyncentry")
    private Boolean isSyncEntry;


    @Basic
    @Column(name = "paymentdaysroocode")
    private String paymentDaysRooCode;
    @Basic
    @Column(name = "prefperiodruletype")
    private String prefPeriodRuleType;
    @Basic
    @Column(name = "prefperiodpaytype")
    private String prefPeriodPayType;

    @Basic
    @Column(name = "isnotredocommissoneqerror")
    private Integer isNotRedoCommissOnEQError;
    @Basic
    @Column(name = "isprolongnowaiteqresponse")
    private Integer IsProlongNoWaitEQResponse;
    @Basic
    @Column(name = "entrymode")
    private String entryMode;

    @Basic
    @Column(name = "discount2package")
    private String discount2Package;
    @Basic
    @Column(name = "discountrate")
    private BigDecimal discountRate;
    @Basic
    @Column(name = "discountamt")
    private BigDecimal discountAmt;
    @Basic
    @Column(name = "discountcur")
    private String discountCur;
    @Basic
    @Column(name = "discountabsamt")
    private BigDecimal discountAmtAbs;
    @Basic
    @Column(name = "discountabscur")
    private String discountAmtCur;
    @Basic
    @Column(name = "isnotifykafka")
    private Integer isNotifyKafka;
    @Basic
    @Column(name = "isignoreeqerror")
    private Integer isIgnoreEqError;
    @Basic
    @Column(name = "issupportcalcresult")
    private Integer isSupportCalcResult;

}
