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

@Entity
@Getter
@Setter
@Table(name = "SERVICELOGDATA")
@Deprecated
public class ServiceLogDataEntity {
    @Id
    @SequenceGenerator(name = "SEQ", sequenceName = "PRCId", allocationSize = 1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "SEQ")
    @Column(name = "ID")
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "SERVICELOG")
    private ServiceLogEntity serviceLog;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "DATA")
    @Lob
    private String data;

}


