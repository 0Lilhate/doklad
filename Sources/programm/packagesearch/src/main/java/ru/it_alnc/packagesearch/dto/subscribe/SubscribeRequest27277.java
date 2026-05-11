/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.it_alnc.packagesearch.config.JsonOffsetDateTimeDeserializer;

import java.time.OffsetDateTime;
import java.util.*;

@Getter
@Setter
@ToString
public class SubscribeRequest27277 {
    @JsonDeserialize(using = JsonOffsetDateTimeDeserializer.class)
    OffsetDateTime searchOnDate;
    Boolean isFull = false;
    @JsonProperty("packageCode")
    List<String> packageCodes;
    @JsonProperty("individualTariffCode")
    List<String> individualTariffCodes;
    @JsonProperty("packageCategory")
    List<String> packageCategories;
    @JsonProperty("serviceGroup")
    List<String> serviceGroups;
    @JsonProperty("service")
    List<String> services;
    //32316 List of codes now
    //@JsonDeserialize(using = JsonNonemptyStringDeserializer.class) not need this now?
    @JsonProperty("clientPUCode")
    String clientPUCodes;
    @JsonProperty("isSkipDependentPUIfNoPUCode")
    boolean isSkipDependentPUIfNoPUCode = false;
    @JsonProperty("isMarketing")
    boolean isMarketing = false;
    @JsonProperty("getIndividualTariff")
    boolean isGetIndividualTariff = false;
    @JsonProperty("isVerifyServiceCheckSet")
    boolean isVerifyServiceCheckSet = false;
    @JsonProperty("isVerifyServiceParamList")
    boolean isVerifyServiceParamList = false;
    @JsonProperty("isVerifyServiceTariff")
    boolean isVerifyServiceTariff = false;
    @JsonProperty("isVerifyServiceTariffRequestParam")
    boolean isVerifyServiceTariffRequestParam = false;
    @JsonProperty("onlySimpleTariffs")
    boolean onlySimpleTariffs = false;
    @JsonProperty("CLIENT")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    List<ParamMap> clientParams = new ArrayList<>();
    @JsonProperty("ACCOUNT")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    List<ParamMap> accountParams = new ArrayList<>();
    @JsonProperty("CARD")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    List<ParamMap> cardParams = new ArrayList<>();
    @JsonProperty("OPERATION")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    List<ParamMap> operationParams = new ArrayList<>();
    @JsonProperty("PRODUCT")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    List<ParamMap> productParams = new ArrayList<>();

    @JsonProperty("isSkipPackageFilter")
    boolean isSkipPackageFilter = false;
    @JsonProperty("isSkipPackageParam")
    boolean isSkipPackageParam = false;
    @JsonProperty("isSkipService")
    boolean isSkipService = false;

    @JsonProperty("isSkipServiceParam")
    boolean isSkipServiceParam = false;
    @JsonProperty("isSkipServiceCounter")
    boolean isSkipServiceCounter = false;
    @JsonProperty("isSkipServiceTariffLine")
    boolean isSkipServiceTariffLine = false;
    @JsonProperty("isCheckPackageCode")
    boolean isCheckPackageCode = false;

    @JsonProperty("isForceServiceFilter")
    boolean isForceServiceFilter = false;

    @JsonProperty("isGetActionOnPUChange")
    boolean isGetActionOnPUChange = false;
    @JsonProperty("isReturnBasicGuide")
    boolean isReturnBasicGuide = false;

    @JsonProperty("isReturnEmptyBlocks")
    boolean isReturnEmptyBlocks = false;
    @JsonProperty("isCheckGraceBySource")
    private boolean isCheckGraceBySource = false;

    @JsonProperty("verifyMultyServiceTariffByType")
    List<String> verifyMultyServiceTariffByType = new ArrayList<>();

    @JsonProperty("serviceType")
    private Set<String> serviceTypes = new HashSet<>();

    @JsonIgnore
    @Schema(hidden = true)
    public List<String> getClientPUCodesAsList() {
        if (clientPUCodes == null) return new ArrayList<>();
        return Arrays.asList(clientPUCodes.split(";"));
    }

    @Getter
    @Setter
    @ToString
    public static class ParamMap {
        String code;
        String value;
    }
}
