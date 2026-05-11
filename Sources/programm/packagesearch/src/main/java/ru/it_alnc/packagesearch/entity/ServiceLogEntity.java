/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "SERVICELOG")
@Deprecated
public class ServiceLogEntity {
    @Id
    @SequenceGenerator(name = "SEQ", sequenceName = "PRCId", allocationSize = 1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "SEQ")
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CREATEDON")
    @CreationTimestamp
    private LocalDateTime createdOn;

    @Column(name = "MODIFIEDON")
    @UpdateTimestamp
    private LocalDateTime modifiedOn;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "APPLICATIONNUMBER")
    private String applicationNumber;

    @Column(name = "SERVICETYPE")
    private String serviceType;

    @OneToMany(mappedBy = "serviceLog")
    private List<ServiceLogDataEntity> serviceLogData;

    @Column(name = "SOURCESYSTEM")
    private Long sourceSystem;

    @Column(name = "MESSAGEID")
    private String messageId;

    @Column(name = "REQUESTID")
    private String requestId;

    @Column(name = "REQUESTDATE")
    private LocalDateTime requestDate;

    @Column(name = "RESPONSEID")
    private String responseId;

    @Column(name = "RESPONSEDATE")
    private LocalDateTime responseDate;
}
