/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity.subscribe;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "tariffline")
@Getter
@Setter
public class TariffLineEntity {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "sortorder")
    private int sortOrder;
    @Column(name = "currency")
    private String currency;
    @Column(name = "amountfix")
    private BigDecimal amountFix;
    @Column(name = "rate")
    private Float rate;
    @Column(name = "amountmin")
    private BigDecimal amountMin;
    @Column(name = "amountmax")
    private BigDecimal amountMax;
    @Column(name = "entrycurrency")
    private String entryCurrency;
    @Column(name = "preferencecode")
    private String preferenceCode;
    @Column(name = "preferencelabel")
    private String preferenceLabel;
    @OneToMany(mappedBy = "tariffLine")
    @BatchSize(size = 150)
    private List<TariffLineParamEntity> tariffLineParams;
    @OneToMany
    @JoinColumn(name = "obj")
    @BatchSize(size = 150)
    private List<ParamValueEntity> filterParams;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff")
    private TariffEntity tariff;
}
/*    Integer order;
    String currency;
    Integer amountFix;
    Integer rate;
    Integer amountMIN;
    Integer amountMAX;
*/