/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class AbstractCatalogProduct {
    private @Setter String code;
    private @Setter String label;
    private @Setter String nodeType;
    private @Setter List<String> departmentGroups;
    private @Setter List<String> segmentGroups;
//    private @Setter List<DiscountDto> discounts;
//    private @Setter Map<String, ParamGroupDto> paramGroups = new HashMap<>();
//    private @Setter Map<String, ParamGroupDto> tableGroupDtos = new HashMap<>();
    private @Setter LocalDateTime endDate;
    private @Setter LocalDateTime begDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractCatalogProduct that = (AbstractCatalogProduct) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (departmentGroups != null ? !departmentGroups.equals(that.departmentGroups) : that.departmentGroups != null)
            return false;
        if (segmentGroups != null ? !segmentGroups.equals(that.segmentGroups) : that.segmentGroups != null)
            return false;
//        if (discounts != null ? !discounts.equals(that.discounts) : that.discounts != null) return false;
//        if (paramGroups != null ? !paramGroups.equals(that.paramGroups) : that.paramGroups != null) return false;
//        if (tableGroupDtos != null ? !tableGroupDtos.equals(that.tableGroupDtos) : that.tableGroupDtos != null)
//            return false;
        return endDate != null ? endDate.equals(that.endDate) : that.endDate == null;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (departmentGroups != null ? departmentGroups.hashCode() : 0);
        result = 31 * result + (segmentGroups != null ? segmentGroups.hashCode() : 0);
//        result = 31 * result + (discounts != null ? discounts.hashCode() : 0);
//        result = 31 * result + (paramGroups != null ? paramGroups.hashCode() : 0);
//        result = 31 * result + (tableGroupDtos != null ? tableGroupDtos.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }
}
