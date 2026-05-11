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
@Table(name = "v_tarifflineparamvalue_rest")
@Getter
@Setter
public class TariffLineParamEntity {
    @Id
    @Column(name = "uuid")
    private Long uuid;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariffline")
    private TariffLineEntity tariffLine;
    @Column(name = "order")
    private Integer order;
    @Column(name = "tariffpart")
    private Integer tariffPart;
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

    @Column(name = "vboolean")
    private Boolean vBoolean;
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
