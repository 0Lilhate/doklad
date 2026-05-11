/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity.subscribe;

import lombok.Getter;
import lombok.Setter;
import ru.it_alnc.packagesearch.entity.ParamEntity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paramvalue")
@Getter
@Setter
public class ParamValueEntity {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "obj")
    private Long product;

    @Column(name = "objtable")
    private String objTable;

    @Column(name = "sortorder")
    private Integer sortOrder;

    @ManyToOne
    @JoinColumn(name = "param")
    private ParamEntity param;

    @ManyToOne
    @JoinColumn(name = "valuetype")
    private ValueTypeEntity valueTypeEntity;


    @Column(name = "valueboolean")
    private Integer valueBoolean;

    @Column(name = "valuestring")
    private String valueString;

    @Column(name = "valuedate")
    private LocalDateTime valueDate;
    @Column(name = "valuedatemin")
    private LocalDateTime valueDateMin;
    @Column(name = "valuedatemax")
    private LocalDateTime valueDateMax;

    @Column(name = "valueint")
    private Long valueInt;
    @Column(name = "valueintmin")
    private Long valueIntMin;
    @Column(name = "valueintmax")
    private Long valueIntMax;

    @Column(name = "valuefloat")
    private BigDecimal valueFloat;
    @Column(name = "valuefloatmin")
    private BigDecimal valueFloatMin;
    @Column(name = "valuefloatmax")
    private BigDecimal valueFloatMax;

    @Column(name = "codepattern")
    private String codePattern;
    @Column(name = "codelist")
    private String codeList;
    @Column(name = "labellist")
    private String labelList;
}
