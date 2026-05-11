/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.it_alnc.packagesearch.config.ThreadLocalBeanFactoryPP;
import ru.it_alnc.packagesearch.dto.ErrorDto;
import ru.it_alnc.packagesearch.dto.subscribe.*;
import ru.it_alnc.packagesearch.exception.NonExistPUException;
import ru.it_alnc.packagesearch.service.ApplicationContextProvider;
import ru.it_alnc.packagesearch.service.RequestLogService;
import ru.it_alnc.packagesearch.service.ServiceGuideService;
import ru.it_alnc.packagesearch.service.SubscribeService;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@Scope(ThreadLocalBeanFactoryPP.SCOPE_THREAD)
public class IndividualTariffController {
    //@Autowired
    SubscribeService subscribeService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RequestLogService requestLogService;

    @PostMapping("/individualTariff/search")
    public ResponseEntity searchIndividualTariff(@RequestBody IndividualTariffRequest individualTariffRequest) throws CloneNotSupportedException {
        this.subscribeService = ApplicationContextProvider.getBean(SubscribeService.class);
        this.subscribeService.initSQBWithRequest(individualTariffRequest);
        if(requestLogService.getLogRequests()) {
            try {
                log.info("Received individualTariff request: {}", objectMapper.writeValueAsString(individualTariffRequest));
            } catch (JsonProcessingException e) {
                log.warn("Can't deserialize individualTariff request");
                log.info(individualTariffRequest.toString());
            }
        }

        try {
            List<IndividualTariffDto27277> result = subscribeService.searchIndividualTariffs(individualTariffRequest);
            log.info("individualTariff search done");
            return new ResponseEntity(result, HttpStatus.OK);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("500");
            errorDto.setMessage(e.getMessage());
            errorDto.setSource("PACKAGESEARCH");
            return new ResponseEntity<>(objectMapper.convertValue(errorDto, JsonNode.class),HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

}
