/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.it_alnc.packagesearch.query.conditionalcomiss.ConditionalComissReaderInitializer;
import ru.it_alnc.packagesearch.query.graceperiod.GracePeriodReaderInitializer;
import ru.it_alnc.packagesearch.query.subscribe.SubscribeReaderInitializer;
import ru.it_alnc.packagesearch.repository.ViewDao;

import java.time.LocalDateTime;

@Component
@Slf4j
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RequiredArgsConstructor
public class MainCacheInitializer {

    private final SubscribeReaderInitializer subscribeReaderInitializer;

    private final GracePeriodReaderInitializer gracePeriodReaderInitializer;

    private final ConditionalComissReaderInitializer conditionalComissReaderInitializer;

    private final ViewDao viewDao;

    @Scheduled(cron = "0 */1 * * * *")
    private void checkCacheUpdateNeeded(){
        log.info("Main check cache update needed");
        subscribeReaderInitializer.checkAndRefreshCacheIfNeeded();
        gracePeriodReaderInitializer.checkAndRefreshCacheIfNeeded();
        conditionalComissReaderInitializer.checkAndRefreshCacheIfNeeded();
        log.info("Main check cache update needed finished");
    }

    @Scheduled(cron = "0 0 * * * *")
    private void refreshCacheHourly() {
        try {
            log.info("refreshing cache by hour trigger");
            Integer counter = viewDao.getTrigger();
            subscribeReaderInitializer.setCounter(counter);
            conditionalComissReaderInitializer.setCounter(counter);

            subscribeReaderInitializer.refreshCache();
            gracePeriodReaderInitializer.refreshCache();
            conditionalComissReaderInitializer.refreshCache();
            log.info("cache updating done");
        } catch (Exception e) {
            log.error("Error acquired during subs hourly cache update! can't update!");
        }
    }
}
