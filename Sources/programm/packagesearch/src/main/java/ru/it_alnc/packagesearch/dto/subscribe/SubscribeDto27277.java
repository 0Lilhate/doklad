/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SubscribeDto27277 implements Cloneable {
    @Override
    public SubscribeDto27277 clone() throws CloneNotSupportedException {
        SubscribeDto27277 clonedSubscribe = (SubscribeDto27277) super.clone();
        if (this.getClientTypeList() != null)
            clonedSubscribe.setClientTypeList(new ArrayList<>(this.getClientTypeList()));
        if (this.getSegmentList() != null)
            clonedSubscribe.setSegmentList(new ArrayList<>(this.getSegmentList()));
        if (this.getAccountTypeList() != null)
            clonedSubscribe.setAccountTypeList(new ArrayList<>(this.getAccountTypeList()));
        if (this.getBranchList() != null)
            clonedSubscribe.setBranchList(new ArrayList<>(this.getBranchList()));

        if(this.getPuParamList() != null) {
            List<PuParamDto> puParamList = new ArrayList<>();
            for (PuParamDto puParamDto : this.getPuParamList()) {
                PuParamDto clonedPuParamDto = new PuParamDto();
                clonedPuParamDto.setId(puParamDto.getId());
                clonedPuParamDto.setOrder(puParamDto.getOrder());
                clonedPuParamDto.setCode(puParamDto.getCode());
                clonedPuParamDto.setLabel(puParamDto.getLabel());
                clonedPuParamDto.setGuide(puParamDto.getGuide());
                clonedPuParamDto.setType(puParamDto.getType());
                clonedPuParamDto.setVType(puParamDto.getVType());
                clonedPuParamDto.setVTypeLabel(puParamDto.getVTypeLabel());
                clonedPuParamDto.setVBoolean(puParamDto.getVBoolean());
                clonedPuParamDto.setVString(puParamDto.getVString());
                clonedPuParamDto.setVDate(puParamDto.getVDate());
                clonedPuParamDto.setVDateMin(puParamDto.getVDateMin());
                clonedPuParamDto.setVDateMax(puParamDto.getVDateMax());
                clonedPuParamDto.setVInt(puParamDto.getVInt());
                clonedPuParamDto.setVIntMin(puParamDto.getVIntMin());
                clonedPuParamDto.setVIntMax(puParamDto.getVIntMax());
                clonedPuParamDto.setVFloat(puParamDto.getVFloat());
                clonedPuParamDto.setVFloatMin(puParamDto.getVFloatMin());
                clonedPuParamDto.setVFloatMax(puParamDto.getVFloatMax());
                clonedPuParamDto.setVCodePattern(puParamDto.getVCodePattern());
                clonedPuParamDto.setVCodeList(puParamDto.getVCodeList());
                clonedPuParamDto.setVLabelList(puParamDto.getVLabelList());
                clonedPuParamDto.setPuGroupCode(puParamDto.getPuGroupCode());
                clonedPuParamDto.setPuGroupLabel(puParamDto.getPuGroupLabel());
                clonedPuParamDto.setVersion(puParamDto.getVersion());

                puParamList.add(clonedPuParamDto);
//            puParamList.add(puParamDto.clone());
            }
            clonedSubscribe.setPuParamList(puParamList);
        }

        if(this.getServiceGroupList() != null) {
            List<ServiceGroupDto> serviceGroupList = new ArrayList<>();
            for (ServiceGroupDto serviceGroupDto : this.getServiceGroupList()) {
                ServiceGroupDto clonedServiceGroupDto = serviceGroupDto.clone();

                serviceGroupList.add(clonedServiceGroupDto);
//            serviceGroupList.add(serviceGroupDto.clone());
            }
            clonedSubscribe.setServiceGroupList(serviceGroupList);
        }

        if(this.getFilterParamList() != null){ //Добавил клонирование
            List<SubscribeFilterParamDto> filterParamList = new ArrayList<>();
            for(SubscribeFilterParamDto subscribeFilterParamDto : this.getFilterParamList()){
                SubscribeFilterParamDto clonedSubscribeFilterParamDto = subscribeFilterParamDto.clone();
                filterParamList.add(clonedSubscribeFilterParamDto);
            }
            clonedSubscribe.setFilterParamList(filterParamList);
        }

        return clonedSubscribe;
    }

    @JsonIgnore
    Long id;

    String code;
    String category;
    String label;
    String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime begDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime endDate;
    Integer priority;
    Integer maxPackagesCodeNo;
    String parentCode;
    String periodConnection;
    Boolean isIndividual;
    Boolean isMarketing;
    Boolean isIndependent;
    Boolean isLinked2Account;
    Boolean isCloseOnAccount;
    Boolean isMultyClient;
    Boolean isMultyAccount;
    Boolean isCheckPUChange;
    Boolean isMultiplePackagesCodePeriod;
    String excludePUGroup;
    String clientCommonType;
    Boolean isMultiplePackagesCode;
    Long maxPackagesCode;
    List<String> clientTypeList;
    List<String> segmentList;
    List<String> accountTypeList;
    List<String> branchList;
    List<PuParamDto> puParamList = new ArrayList<>();
    List<ServiceGroupDto> serviceGroupList = new ArrayList<>();
    //@JsonIgnore
    private List<SubscribeFilterParamDto> filterParamList = new ArrayList<>();
    @JsonProperty("actionOnPUChange")
    private List<ActionOnPUChangeDto> actionOnPUChangeList = new ArrayList<>();
    @JsonIgnore
    private List<VersionDto> versionList = new ArrayList<>();
}
