/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity.subscribe;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "v_paramvalue_rest")
@Getter
@Setter
public class SetLineParamEntity {
    @Id
    @Column(name = "uuid")
    private long uuid;
    @Column(name = "order")
    private Integer order;
    @Column(name = "code")
    private String code;
    @Column(name = "label")
    private String label;
    @Column(name = "type")
    private String type;
    @Column(name = "guide")
    private String guide;
    @Column(name = "vtype")
    private String vType;
    @Column(name = "vtypelabel")
    private String vTypeLabel;

    @Column(name = "vvalueboolean")
    private Boolean vBoolean;
    @Column(name = "vvaluestring")
    private String vString;
    @Column(name = "vvaluedate")
    private LocalDateTime vDate;
    @Column(name = "vvaluedatemin")
    private LocalDateTime vDateMin;
    @Column(name = "vvaluedatemax")
    private LocalDateTime vDateMax;
    @Column(name = "vvalueint")
    private Long vInt;
    @Column(name = "vvalueintmin")
    private Long vIntMin;
    @Column(name = "vvalueintmax")
    private Long vIntMax;
    @Column(name = "vvaluefloat")
    private BigDecimal vFloat;
    @Column(name = "vvaluefloatmin")
    private BigDecimal vFloatMin;
    @Column(name = "vvaluefloatmax")
    private BigDecimal vFloatMax;
    @Column(name = "vcodepattern")
    private String vCodePattern;
    @Column(name = "vcodelist")
    private String vCodeList;
    @Column(name = "vlabellist")
    private String vLabelList;
    @Column(name = "isgroupguide")
    private Boolean isGroupGuide;
}
