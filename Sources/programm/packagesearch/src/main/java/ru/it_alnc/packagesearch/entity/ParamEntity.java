/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.it_alnc.packagesearch.entity.subscribe.ParamValueEntity;

import java.util.List;


@Entity
@Getter
@Setter
@Table(name = "param")
public class ParamEntity {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "objecttype")
    private String objectType;

    @Column(insertable = false, updatable = false, name = "guide")
    private int guide;

    @Column(name = "guidecode")
    private String guideCode;

    @Column(name = "guidelabel")
    private String guideLabel;

    @Column(name = "code")
    private String code;

    @Column(name = "label")
    private String label;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide")
    private GuideEntity guideEntity;

}
