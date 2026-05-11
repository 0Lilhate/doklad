/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity.subscribe;

import lombok.Getter;
import lombok.Setter;
import ru.it_alnc.packagesearch.entity.subscribe.types.ContextTypeEntity;
import ru.it_alnc.packagesearch.entity.subscribe.types.CounterTypeEntity;

import jakarta.persistence.*;

@Entity
@Table(name = "counter")
@Getter
@Setter
public class CounterEntity {
    @Id
    @Column(name = "Id")
    private Long id;
    @Column(name = "code")
    private String code;
    @Column(name = "label")
    private String label;
    @Column(name = "countertype", insertable = false, updatable = false)
    private String counterType;
    @ManyToOne
    @JoinColumn(name = "countertype")
    private CounterTypeEntity counterTypeEntity;
    @ManyToOne
    @JoinColumn(name = "parent")
    private CounterEntity parent;
    @Column(name = "currency")
    private String currency;
    @Column(name = "periodtype")
    private String periodType;
    @Column(name = "periodvalue")
    private Integer period;
//    @Column(name = "iscalendarborder")
//    private Boolean isCalendarBorder;
    @Column(name = "isusequantity")
    private Boolean isUseQuantity;
    @Column(name = "isusesum")
    private Boolean isUseSum;
    @Column(name = "isusefee")
    private Boolean isUseFee;
    @Column(name = "isusends")
    private Boolean isUseNds;
    @Column(name = "contexttype", insertable = false, updatable = false)
    private String contextType;
    @ManyToOne
    @JoinColumn(name = "contexttype")
    private ContextTypeEntity contextTypeEntity;
    @Column(name = "iscollectonaccount")
    private Boolean isCollectOnAccount;
    @Column(name = "iscollectonpact")
    private Boolean isCollectOnPact;
    @Column(name = "iscollectonclient")
    private Boolean isCollectOnClient;
    @Column(name = "iscollectonclientgroup")
    private Boolean isCollectOnClientGroup;
    @Column(name = "archivelength")
    private Integer archiveLength;
    @Column(name = "isgetfromit")
    private Boolean isGetFromIT;
    @Column(name = "isgetfroms")
    private Boolean isGetFromS;
    @Column(name = "isgetfromps")
    private Boolean isGetFromPS;
    @Column(name = "isgetfromtp")
    private Boolean isGetFromTP;
    @Column(name = "isputtoit")
    private Boolean isPutToIT;
    @Column(name = "isputtos")
    private Boolean isPutToS;
    @Column(name = "isputtops")
    private Boolean isPutToPS;
    @Column(name = "isputtotp")
    private Boolean isPutToTP;

}
/*
        parentCode:
          description: "Код родительского счетчика. Заполняется только для счетчиков с типом type=S (Дочерний - расщепленный)."
          type: string
          */