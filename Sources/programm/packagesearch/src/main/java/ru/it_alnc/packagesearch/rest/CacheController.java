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
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.it_alnc.packagesearch.query.graceperiod.GracePeriodReaderInitializer;
import ru.it_alnc.packagesearch.query.subscribe.SubscribeReaderInitializer;


@Hidden
@RestController
@Slf4j
public class CacheController {

    @Autowired
    private SubscribeReaderInitializer subscribeReaderInitializer;

    @Autowired
    private GracePeriodReaderInitializer gracePeriodReaderInitializer;

    @Hidden
    @GetMapping("/cache/subscribes/update")
    public ResponseEntity updateSubscribeCache() {
        subscribeReaderInitializer.refreshCacheByRest();
        return ResponseEntity.ok().build();
    }

    @Hidden
    @GetMapping("/cache/subscribes/time")
    public ResponseEntity getSubscribeCacheTime() {
        return ResponseEntity.ok().body(subscribeReaderInitializer.getLastCacheUpdate());
    }

    @Hidden
    @GetMapping("/cache/graceperiod/update")
    public ResponseEntity updateGracePeriodCache() {
        gracePeriodReaderInitializer.refreshCache();
        return ResponseEntity.ok().build();
    }

    @Hidden
    @GetMapping("/cache/graceperiod/time")
    public ResponseEntity getGracePeriodCacheTime() {
        return ResponseEntity.ok(gracePeriodReaderInitializer.getCacheReadingTime());
    }

}

