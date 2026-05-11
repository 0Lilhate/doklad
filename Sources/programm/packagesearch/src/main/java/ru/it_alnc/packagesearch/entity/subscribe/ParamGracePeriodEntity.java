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

@Entity
@Table(name = "checkgraceinchannel")
@Getter
@Setter
public class ParamGracePeriodEntity {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "sourcecodelist")
    private String sourceCode;

    @Column(name = "sourcelabellist")
    private String sourceLabel;

    @Column(name = "checkgrace")
    private Boolean checkGrace = false;

    @ManyToOne
    @JoinColumn(name = "serviceline")
    private ServiceLine27277Entity serviceLine;
} 