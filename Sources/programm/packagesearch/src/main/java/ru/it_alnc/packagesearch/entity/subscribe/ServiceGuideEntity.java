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
@Table(name = "serviceguide")
@Getter
@Setter
public class ServiceGuideEntity {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "code")
    private String code;
}
