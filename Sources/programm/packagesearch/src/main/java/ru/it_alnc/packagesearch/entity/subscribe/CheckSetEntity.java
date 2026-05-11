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
import java.util.List;

@Entity
@Table(name = "checkset")
@Getter
@Setter
public class CheckSetEntity {
    @Id
    @Column(name = "id")
    private Long id;
    @OneToMany(mappedBy = "checkSetEntity")
    @BatchSize(size = 1500)
    private List<CheckSetLineEntity> checkSetLines;
}
