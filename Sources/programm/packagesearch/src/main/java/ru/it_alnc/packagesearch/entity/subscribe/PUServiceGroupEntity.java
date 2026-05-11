/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.entity.subscribe;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import ru.it_alnc.packagesearch.entity.ProductEntity;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "puservicegroup")
@Getter
@Setter
public class PUServiceGroupEntity {
    @Id
    @Column(name = "id")
    private long id;
    @ManyToOne
    @JoinColumn(name = "product")
    private ProductEntity product;
    @OneToMany(mappedBy = "puServiceGroup", fetch = FetchType.LAZY)
    @BatchSize(size = 175)
    private Set<ServiceLine27277Entity> serviceLines;
    @Column(name = "sortorder")
    private Integer sortorder;
    @Column(name = "code")
    private String code;
    @Column(name = "label")
    private String label;
    @Column(name = "isdeleted")
    private boolean isDeleted;
}
