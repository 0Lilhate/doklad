/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "groupguide")
@Getter
@Setter
public class GroupGuideEntity {

    @Id
    Integer id;
    @Column(name = "modifiedon")
    LocalDateTime modifiedOn;
    @Column(name = "guidecode")
    String guideCode;
    @Column(name = "groupcode")
    String groupCode;
    @Column(name = "linkcodelist")
    String linkCodeList;
    @Column(name = "action")
    String action;
    @Column(name = "begdate")
    LocalDateTime begDate;
    @Column(name = "enddate")
    LocalDateTime endDate;
}
