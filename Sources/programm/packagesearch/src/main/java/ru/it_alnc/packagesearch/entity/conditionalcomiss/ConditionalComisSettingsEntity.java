/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity.conditionalcomiss;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.it_alnc.packagesearch.entity.subscribe.ServiceGuideItemEntity;
import ru.it_alnc.packagesearch.entity.subscribe.ValueTypeEntity;

import java.time.LocalDateTime;

@Table(name = "conditionalcommissettings")
@Entity
@Getter
@Setter
public class ConditionalComisSettingsEntity {

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "begdate")
    private LocalDateTime begDate;
    @Column(name = "enddate")
    private LocalDateTime endDate;
    @Column(name = "modifiedon")
    private LocalDateTime modifiedOn;
    @Column(name = "status")
    private String status;
    @Column(name = "cardtype")
    private String cardType;
    @Column(name = "cardcontracttype")
    private String cardContractType;
    @ManyToOne
    @JoinColumn(name = "serviceguideitem")
    private ServiceGuideItemEntity serviceGuideItemEntity;
    @OneToOne
    @JoinColumn(name = "cardtypevaluetype")
    private ValueTypeEntity cardTypevalueTypeEntity;
    @OneToOne
    @JoinColumn(name = "cardcontracttypevaluetype")
    private ValueTypeEntity cardContractvalueTypeEntity;




}
