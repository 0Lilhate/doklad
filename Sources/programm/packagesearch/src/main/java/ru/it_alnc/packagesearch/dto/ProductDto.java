/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProductDto extends AbstractCatalogProduct implements Serializable, Cloneable {
    private Long id;
    private List<String> paramGroupCodes;
//    private List<ParentDto> parents;

    private String category;
    private Integer version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ProductDto that = (ProductDto) o;

        if (paramGroupCodes != null ? !paramGroupCodes.equals(that.paramGroupCodes) : that.paramGroupCodes != null)
            return false;
        return version != null ? version.equals(that.version) : that.version == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (paramGroupCodes != null ? paramGroupCodes.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

//Заполняет список кодами для отсева по названию групп
//    public void fillPGCodes(){
//        List<String> result = new ArrayList<>();
//        for(ParamGroupDto paramGroupDto : this.getParamGroups()) {
//            result.add(paramGroupDto.getCode());
//        }
//
//        for(ParamGroupDto paramGroupDto : this.getTableGroupDtos()) {
//            result.add(paramGroupDto.getCode());
//        }
//
//        this.paramGroupCodes = result;
//    }

    @Override
    public ProductDto clone() throws CloneNotSupportedException {
        ProductDto result = (ProductDto) super.clone();
//        result.setParamGroups(new HashMap<>(this.getParamGroups()));
//        Map<String, ParamGroupDto> cloneParamGroupDtos = new HashMap<>();
//
//        for(String key : this.getParamGroups().keySet()){
//            cloneParamGroupDtos.put(key, this.getParamGroups().get(key).clone());
//        }
//        result.setParamGroups(cloneParamGroupDtos);

//        Map<String, ParamGroupDto> cloneTableGroupDtos = new HashMap<>();
//        for(String key : this.getTableGroupDtos().keySet()) {
//            cloneTableGroupDtos.put(key, this.getTableGroupDtos().getOrDefault(key, new ParamGroupDto()).clone());
//        }
//        result.setTableGroupDtos(cloneTableGroupDtos);
        return result;
    }
}
