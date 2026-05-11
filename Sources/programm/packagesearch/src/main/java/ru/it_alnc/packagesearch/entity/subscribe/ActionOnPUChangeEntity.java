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
import org.hibernate.annotations.BatchSize;
import ru.it_alnc.packagesearch.entity.ProductEntity;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "actiononpuchange")
public class ActionOnPUChangeEntity {
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
    @Column(name = "status")
    private String status;
    @Column(name = "begdate")
    private LocalDateTime begDate;
    @Column(name = "enddate")
    private LocalDateTime endDate;
    @ManyToOne
    @JoinColumn(name = "product")
    private ProductEntity product;
    @OneToMany(mappedBy = "actionOnPUChange", fetch = FetchType.LAZY)
    @BatchSize(size = 175)
    private Set<ActionOnPUChangeLineEntity> actionOnPuChangeLines;
}
