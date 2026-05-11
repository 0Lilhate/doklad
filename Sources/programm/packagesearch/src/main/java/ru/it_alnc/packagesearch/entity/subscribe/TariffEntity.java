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
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "tariff")
@Getter
@Setter
public class TariffEntity implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "code")
    private String code;
    @Column(name = "label")
    private String label;
    @Column(name = "begdate")
    private LocalDateTime begDate;
    @Column(name = "enddate")
    private LocalDateTime endDate;
    @Column(name = "status")
    private String status;
    @Column(name = "issimple")
    private boolean isSimple;
    @Column(name = "ratetype")
    private String rateType;
    @Column(name = "entryratetype")
    private String entryRateType;
    @Column(name = "entrycurrency")
    private String entryCurrency;
    @Column(name = "alllinecurrency")
    private String allLineCurrency;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariffheader", referencedColumnName = "tariffheader")
    private ServiceLine27277Entity serviceLine;
    @OneToMany(mappedBy = "tariff", fetch = FetchType.EAGER)
    @BatchSize(size = 150)
    private Set<TariffLineEntity> tariffLines;
}
