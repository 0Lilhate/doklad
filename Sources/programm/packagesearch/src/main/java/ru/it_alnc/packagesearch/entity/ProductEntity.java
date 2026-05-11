/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import ru.it_alnc.packagesearch.entity.subscribe.ActionOnPUChangeEntity;
import ru.it_alnc.packagesearch.entity.subscribe.PUServiceGroupEntity;
import ru.it_alnc.packagesearch.entity.subscribe.ParamValueEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "PRODUCT", catalog = "")
public class ProductEntity {
    @Id
    @Column(name = "ID")
    private Long id;
    @Column(name = "CODE")
    private String code;
    @Column(name = "CATEGORY")
    private String category;
    @Column(name = "CREATEDON")
    private LocalDateTime createdOn;
    @Column(name = "CREATEDBY")
    private String createdBy;
    @Column(name = "MODIFIEDON")
    private LocalDateTime modifiedOn;
    @Column(name = "MODIFIEDBY")
    private String modifiedBy;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "NODETYPE")
    private String nodeType;
    @Column(name = "clientcommontype")
    private String clientCommonType;
    @Column(name = "excludepugroup")
    private String excludePUGroup;
    @Column(name = "description")
    private String description;
    @Column(name = "periodconnection")
    private String periodConnection;
    @Column(name = "maxpackagescodeno")
    private Integer maxPackagesCodeNo;
    @Column(name = "priority")
    private Integer priority;
    @Column(name = "ismultiplepackagescodeperiod")
    private Boolean isMultiplePackagesCodePeriod;
    @Column(name = "ismarketing")
    private boolean isMarketing;
    @Column(name = "isindividual")
    private boolean isIndividual;
    @Column(name = "isindependent")
    private boolean isIndependent;
    @Column(name = "islinked2account")
    private boolean isLinked2Account;
    @Column(name = "iscloseonaccount")
    private boolean isCloseOnAccount;
    @Column(name = "ismultyclient")
    private boolean isMultyClient;
    @Column(name = "ismultyaccount")
    private boolean isMultyAccount;
    @Column(name = "ischeckpuchange")
    private Boolean isCheckPuChange;
    @Column(name = "BEGDATE")
    private LocalDateTime beginDate;
    @Column(name = "ENDDATE")
    private LocalDateTime endDate;
    @Column(name = "ismultiplepackagescode")
    private Boolean isMultiplePackagesCode;
    @Column(name = "maxpackagescode")
    private Long maxPackagesCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "clienttype2product", joinColumns = @JoinColumn(name = "product"))
    @Column(name = "clienttype")
    @BatchSize(size = 150)
    private Set<String> clientTypeList;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "accounttype2product", joinColumns = @JoinColumn(name = "product"))
    @Column(name = "accounttype")
    @BatchSize(size = 150)
    private Set<String> accountTypeList;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "branch2product", joinColumns = @JoinColumn(name = "product"))
    @Column(name = "branch")
    @BatchSize(size = 150)
    private Set<String> branchList;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "segment2product", joinColumns = @JoinColumn(name = "product"))
    @Column(name = "segment")
    @BatchSize(size = 150)
    private Set<String> segmentList;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "SEGMENTGROUP2PRODUCT", joinColumns = @JoinColumn(name = "PRODUCT"))
    @Column(name = "SEGMENTGROUP")
    @BatchSize(size = 150)
    private Set<String> segmentGroups;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "DEPARTMENTGROUP2PRODUCT", joinColumns = @JoinColumn(name = "PRODUCT"))
    @Column(name = "DEPARTMENTGROUP")
    @BatchSize(size = 150)
    private Set<String> departmentGroups;

    @Column(name = "LABEL")
    private String label;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    @BatchSize(size = 5)
    private Set<VersionEntity> versions;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    @BatchSize(size = 150)
    private Set<PUServiceGroupEntity> puServiceGroups;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    @BatchSize(size = 150)
    private Set<ActionOnPUChangeEntity> actionOnPUChangeEntities;

    @OneToMany
    @JoinColumn(name = "obj")
    @BatchSize(size = 150)
    private List<ParamValueEntity> filterParams;

    @Column(name = "PARENT")
    private Long parentId;
}
