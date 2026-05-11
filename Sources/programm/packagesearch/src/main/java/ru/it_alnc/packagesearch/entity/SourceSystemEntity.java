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

@Entity
@Getter
@Setter
@Table(name = "SOURCESYSTEM")
@Deprecated
public class SourceSystemEntity {
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

    @Column(name = "CODE")
    private String code;

    @Column(name = "LABEL")
    private String label;

}
