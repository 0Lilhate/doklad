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
@Table(name = "v_puparamlist_rest")
@Getter
@Setter
public class PuParamEntity {
    @Id
    @Column(name = "uuid")
    private long uuid;

    @Column(name = "p_id")
    private long productId;

    @Column(name = "v_num")
    private Integer version;

    @Column(name = "pg_code")
    private String paramGroupCode;

    @Column(name = "pg_label")
    private String paramGroupLabel;

    @Column(name = "pg_begdate")
    private LocalDateTime pgBegDate;

    @Column(name = "pg_enddate")
    private LocalDateTime pgEndDate;

    @Column(name = "order")
    private Integer sortOrder;

    @Column(name = "code")
    private String paramCode;
    @Column(name = "label")
    private String paramLabel;
    @Column(name = "type")
    private String paramType;
    @Column(name = "guide")
    private String paramGuide;
    @Column(name = "vtype")
    private String paramValueType;
    @Column(name = "vtypelabel")
    private String paramValueTypeLabel;

    @Column(name = "vboolean")
    private Integer vBoolean;
    @Column(name = "vstring")
    private String vString;
    @Column(name = "vdate")
    private LocalDateTime vDate;
    @Column(name = "vdatemin")
    private LocalDateTime vDateMin;
    @Column(name = "vdatemax")
    private LocalDateTime vDateMax;
    @Column(name = "vint")
    private Long vInt;
    @Column(name = "vintmin")
    private Long vIntMin;
    @Column(name = "vintmax")
    private Long vIntMax;
    @Column(name = "vfloat")
    private BigDecimal vFloat;
    @Column(name = "vfloatmin")
    private BigDecimal vFloatMin;
    @Column(name = "vfloatmax")
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
