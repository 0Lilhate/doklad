/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.it_alnc.packagesearch.dto.subscribe.IndividualTariffDBRequest;
import ru.it_alnc.packagesearch.dto.subscribe.IndividualTariffDto27277;
import ru.it_alnc.packagesearch.service.RequestLogService;
import ru.it_alnc.packagesearch.service.db.IndividualTariffService;

import java.util.List;

@RestController
@RequestMapping("/db")
@Slf4j
@AllArgsConstructor
public class DBController {

    private final IndividualTariffService individualTariffService;

    private final RequestLogService requestLogService;

    private final ObjectMapper objectMapper;


    @PostMapping("/IndividualTariff/search")
    public ResponseEntity searchIndividualTariff(@RequestBody IndividualTariffDBRequest individualTariffRequest) {
        if(requestLogService.getLogRequests()) {
            try {
                log.info("Received individualTariff request: {}", objectMapper.writeValueAsString(individualTariffRequest));
            } catch (JsonProcessingException e) {
                log.warn("Can't deserialize individualTariff request");
                log.info(individualTariffRequest.toString());
            }
        }
            List<IndividualTariffDto27277> result = individualTariffService.searchIndividualTariffs(individualTariffRequest);
            log.info("individualTariff search done");
            return new ResponseEntity(result, HttpStatus.OK);
    }
}
