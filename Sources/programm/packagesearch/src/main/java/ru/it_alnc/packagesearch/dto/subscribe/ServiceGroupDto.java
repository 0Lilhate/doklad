/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ServiceGroupDto implements Cloneable {
    @JsonIgnore
    private Long id;
    private Integer order;
    private String code;
    private String label;
    private List<ServiceLineDto> serviceLineList = new ArrayList<>();

    @Override
    protected ServiceGroupDto clone() throws CloneNotSupportedException {
        ServiceGroupDto clonedServiceGroup = (ServiceGroupDto) super.clone();

        if(this.getServiceLineList() != null) {
            List<ServiceLineDto> serviceLineList = new ArrayList<>();
            for (ServiceLineDto serviceLineDto : this.getServiceLineList()) {
                serviceLineList.add(serviceLineDto.clone());
            }
            clonedServiceGroup.setServiceLineList(serviceLineList);
        }

        return clonedServiceGroup;
    }
}
