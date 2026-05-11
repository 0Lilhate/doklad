/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.service.util;


import ru.it_alnc.packagesearch.dto.conditionalcomiss.ConditionalComisRequestDto;

import java.util.HashSet;
import java.util.stream.Collectors;

public class RequestDtoSanitizer {

    public static ConditionalComisRequestDto sanitize(ConditionalComisRequestDto requestDto) {
        if (requestDto == null) {
            return new ConditionalComisRequestDto();
        }
        if(requestDto.getServiceCodes() == null)
            requestDto.setServiceCodes(new HashSet<>());
        requestDto.setServiceCodes(requestDto.getServiceCodes().stream()
                .filter(
                        code -> !code.isBlank()
                ).collect(Collectors.toSet()));
        if (requestDto.getCardContractType() != null && requestDto.getCardContractType().isBlank())
            requestDto.setCardContractType(null);
        if (requestDto.getCardType() != null && requestDto.getCardType().isBlank())
            requestDto.setCardType(null);
        return requestDto;

    }
}
