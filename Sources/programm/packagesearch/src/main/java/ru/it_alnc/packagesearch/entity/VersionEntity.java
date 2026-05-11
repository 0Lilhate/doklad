/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "version")
public class VersionEntity {
    @Id
    @Column(name = "ID")
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "PRODUCT")
    private ProductEntity product;

    @Column(name = "NUM")
    private Integer versionNum;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "BEGDATE")
    private LocalDateTime beginDate;

    @Column(name = "ENDDATE")
    private LocalDateTime endDate;
}
