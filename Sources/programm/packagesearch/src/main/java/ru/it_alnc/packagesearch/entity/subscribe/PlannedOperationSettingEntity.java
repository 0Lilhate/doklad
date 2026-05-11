/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity.subscribe;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "plannedoperationsettings")
@Getter
@Setter
public class PlannedOperationSettingEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "plannedoperationtype")
    private String plannedOperationType;

    @Column(name = "plannedoperationtypelabel")
    private String plannedOperationTypeLabel;

    @Column(name = "entrymode")
    private String entryMode;

    @Column(name = "isneedsuosd")
    private int isNeedSuoSd;

    @ManyToOne
    @JoinColumn(name = "serviceline")
    private ServiceLine27277Entity serviceLine;
}
