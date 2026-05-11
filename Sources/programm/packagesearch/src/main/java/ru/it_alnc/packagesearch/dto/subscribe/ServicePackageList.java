/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.subscribe;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServicePackageList {
    private List<SubscribeDto27277> servicePackageList;
    private List<IndividualTariffDto27277> individualTariffList;
}
