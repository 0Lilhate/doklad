/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity.subscribe.types;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "ratetype")
@Getter
@Setter
public class RateTypeEntity {
    @Id
    @Column(name = "code")
    private String code;
    @Column(name = "status")
    private String status;
    @Column(name = "label")
    private String label;
    @Column(name = "type")
    private Integer type;
    @Column(name = "crosscurrency")
    private String crossCurrency;
    @Column(name = "isinner")
    private Integer isInner;
    @Column(name = "currencylist")
    private String currencyList;
    @Column(name = "currencylistwidget")
    private String currencyListWidget;
}

