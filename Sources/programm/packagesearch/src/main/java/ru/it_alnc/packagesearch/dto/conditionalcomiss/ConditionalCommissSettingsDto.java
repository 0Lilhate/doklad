/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.conditionalcomiss;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class ConditionalCommissSettingsDto implements Cloneable{
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime begDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime endDate;

    private List<ParamMap> cardTypeList;

    private List<ParamMap> cardContractTypeList;

    @Override
    public ConditionalCommissSettingsDto clone() {
        try {
            ConditionalCommissSettingsDto clone = (ConditionalCommissSettingsDto) super.clone();

            clone.cardTypeList = cloneParamMapList(cardTypeList);
            clone.cardContractTypeList = cloneParamMapList(cardContractTypeList);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning failed", e);
        }
    }

    private List<ParamMap> cloneParamMapList(List<ParamMap> list) {
        if (list == null) return null;
        return list.stream()
                .map(param -> param == null ? null : param.clone())
                .toList();
    }


    @Getter
    @Setter
    @ToString
    public static class ParamMap implements Cloneable {
        String code;
        String valueType;

        @Override
        public ParamMap clone() {
            try {
                return (ParamMap) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError("Cloning ParamMap failed", e);
            }
        }
    }

}
