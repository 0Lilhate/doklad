/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity.subscribe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "counterguide")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class CounterGuideEntity {

    @Id
    Long id;

    @Column(name = "code")
    String code;

    @Column(name = "label")
    String label;

    @Column(name = "currency")
    String currency;

    @Column(name = "periodcode")
    String periodCode;

    @Column(name = "iscalendarborder")
    Integer isCalendarBorder;

    @Column(name = "contexttype")
    String contextType;
}
