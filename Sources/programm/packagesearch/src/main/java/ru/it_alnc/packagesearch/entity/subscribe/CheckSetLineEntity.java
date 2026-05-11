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
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "checksetline")
@Getter
@Setter
public class CheckSetLineEntity {
    @Id
    @Column(name = "id")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "checkset")
    private CheckSetEntity checkSetEntity;
    @Column(name = "begdate")
    private LocalDateTime begDate;
    @Column(name = "enddate")
    private LocalDateTime endDate;
    @OneToMany
    @JoinColumn(name = "obj")
    @BatchSize(size = 150)
    private List<SetLineParamEntity> checkSetLineParams;
    @OneToMany
    @JoinColumn(name = "obj")
    @BatchSize(size = 150)
    private List<ParamValueEntity> filterParams;
}
