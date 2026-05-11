/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.rest;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.it_alnc.packagesearch.service.RequestLogService;

@Hidden
@RestController
@Slf4j
public class LogController {


    @Autowired
    RequestLogService logService;

    @Hidden
    @GetMapping("/log")
    public ResponseEntity log(@RequestParam(required = false) Boolean state) {
        if(state == null){
            return ResponseEntity.status(200).body(logService.getLogRequests() ? "logging requests" : "not logging requests");
        }
        logService.setLogRequests(state);
        log.info("changed request logging to state: {}",logService.getLogRequests());
        return ResponseEntity.status(200).body(logService.getLogRequests() ? "logging requests now" : "don't logging requests now");
    }
}
