/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.service.db;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.it_alnc.packagesearch.dto.subscribe.IndividualTariffDBRequest;
import ru.it_alnc.packagesearch.dto.subscribe.IndividualTariffDto27277;
import ru.it_alnc.packagesearch.query.subscribe.SubscribeReader;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndividualTariffService { //todo: убрать отсюда subscribeReader, но пока так

    private final SubscribeReader subscribeReader;

    public List<IndividualTariffDto27277> searchIndividualTariffs(IndividualTariffDBRequest requestDto){
        if(requestDto.getSearchOnDate() == null)
            requestDto.setSearchOnDate(OffsetDateTime.now());
        return subscribeReader.searchTariffsFromDB(requestDto);
    }
}
