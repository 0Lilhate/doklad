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

@Entity
@Table(name = "individualtariff")
@Getter
@Setter
public class IndividualTariffEntity {
    @Id
    private Long id;
    @Column(name = "orgstruct")
    private String orgStruct;
    @Column(name = "clientcommontype")
    private String clientCommonType;
    @Column(name = "code")
    private String code;
    @Column(name = "label")
    private String label;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "obj", insertable = false, updatable = false)
    private ServiceLine27277Entity serviceLineEntity;
}
