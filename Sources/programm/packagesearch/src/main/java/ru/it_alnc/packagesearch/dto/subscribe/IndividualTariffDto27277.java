/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndividualTariffDto27277 implements Cloneable {
    private String code;
    private String label;
    @JsonIgnore
    private String clientCommonType;
    private ServiceLineDto serviceLine;

    @Override
    public IndividualTariffDto27277 clone() throws CloneNotSupportedException {
        IndividualTariffDto27277 clonedIndividualTariffDto = (IndividualTariffDto27277) super.clone();
        clonedIndividualTariffDto.setServiceLine(serviceLine.clone());
        return clonedIndividualTariffDto;
    }
}
