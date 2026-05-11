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
import org.hibernate.annotations.BatchSize;
import ru.it_alnc.packagesearch.entity.conditionalcomiss.ConditionalComisSettingsEntity;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "serviceguideitem")
@Getter
@Setter
public class ServiceGuideItemEntity {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "code")
    private String code;
    @Column(name = "label")
    private String label;
    @Column(name = "description")
    private String description;
    @Column(name = "servicetype")
    private String serviceType;
    @Column(name = "status")
    private String status;
    @Column(name = "begdate")
    private LocalDateTime begDate;
    @Column(name = "enddate")
    private LocalDateTime endDate;
    @Column(name = "ismultycopiescalcallowed")
    private Integer isMultyCopiesCalcAllowed;
    @OneToMany(mappedBy = "serviceGuideItemEntity", fetch = FetchType.LAZY)
    @BatchSize(size = 150)
    private Set<ConditionalComisSettingsEntity> conditionalComisSettings;
}
