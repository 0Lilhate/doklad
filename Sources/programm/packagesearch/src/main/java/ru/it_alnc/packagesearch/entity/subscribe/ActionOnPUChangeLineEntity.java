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
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "actiononpuchangeline")
public class ActionOnPUChangeLineEntity {
    @Id
    private Long id;
    @Column(name = "createdon")
    private LocalDateTime createdOn;
    @Column(name = "createdby")
    private String createdBy;
    @Column(name = "createdbyname")
    private String createdByName;
    @Column(name = "modifiedon")
    private LocalDateTime modifiedOn;
    @Column(name = "modifiedby")
    private String modifiedBy;
    @Column(name = "modifiedbyname")
    private String modifiedByName;
    @Column(name = "targetpucodelist")
    private String targetPUCodeList;
    @Column(name = "targetpulabellist")
    private String targetPULabelList;
    @Column(name = "actiontypecode")
    private String actionTypeCode;
    @Column(name = "actiontypelabel")
    private String actionTypeLabel;
    @ManyToOne
    @JoinColumn(name = "actiononpuchange")
    private ActionOnPUChangeEntity actionOnPUChange;

}
