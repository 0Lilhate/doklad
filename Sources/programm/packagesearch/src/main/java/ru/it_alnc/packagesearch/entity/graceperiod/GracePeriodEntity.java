/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity.graceperiod;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.it_alnc.packagesearch.entity.ParamEntity;
import ru.it_alnc.packagesearch.entity.subscribe.CheckSetEntity;
import ru.it_alnc.packagesearch.entity.subscribe.ParamValueEntity;
import ru.it_alnc.packagesearch.entity.subscribe.ServiceGuideItemEntity;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "graceperiodguideline")
public class GracePeriodEntity {

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "modifiedon")
    private LocalDateTime modifiedOn;
    @Column(name = "clientcommontype")
    private String clientCommonType;
    @Column(name = "graceperiod")
    private String gracePeriod;
//    @Column(name = "serviceguideitem")
//    private Long serviceGuideItem;
    @Column(name = "begdate", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime begDate;
    @Column(name = "enddate", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime endDate;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "obj")
    private List<ParamValueEntity> paramValueList;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "serviceguideitem", referencedColumnName = "id")
    private ServiceGuideItemEntity serviceGuideItemEntity;
    @Column(name = "defaultpaymentduration")
    private String defaultPaymentDuration;
    @Column(name = "status")
    private String status;



}
