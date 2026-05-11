/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.query.graceperiod;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.it_alnc.packagesearch.dto.graceperiod.GracePeriodDto;
import ru.it_alnc.packagesearch.entity.graceperiod.GracePeriodEntity;
import ru.it_alnc.packagesearch.repository.GracePeriodGuideLineRepository;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class GracePeriodReaderInitializer {
    @Autowired
    private GracePeriodReader gracePeriodReader;

    @Getter
    private LocalDateTime cacheReadingTime;

    @Autowired
    private GracePeriodGuideLineRepository gracePeriodGuideLineRepository;


    @PostConstruct
    private void init() {
        this.cacheReadingTime = LocalDateTime.now();
        gracePeriodReader.readAllGracePeriods(cacheReadingTime);
    }

    @Scheduled(cron = "0 0 1 * * *")
    protected void graceCacheRefresh() {
        try {
            LocalDateTime ldt = LocalDateTime.now();
            gracePeriodReader.readAllGracePeriods(ldt);
            this.cacheReadingTime = ldt;
        } catch (EmptyResultDataAccessException e) {
            log.error("Can't refresh grace periods: {}", e.getMessage());
        }
    }

    public void checkAndRefreshCacheIfNeeded() {
        log.info("Checking grace period cache refresh");
        try {
            LocalDateTime ldt = LocalDateTime.now();
            List<GracePeriodEntity> gracePeriodDtoList = gracePeriodGuideLineRepository.getModifiedGracePeriodLines(ldt.minusMinutes(1), ldt.plusMinutes(1));
            if(!gracePeriodDtoList.isEmpty()){
                gracePeriodReader.readAllGracePeriods(ldt);
                this.cacheReadingTime = ldt;
            } else {
                log.info("grace period cache not needed now");
                log.debug("Nothing happened with grace periods DB, I swear");
            }


        } catch (EmptyResultDataAccessException e) {
            log.error("Can't refresh grace periods: {}", e.getMessage());
        }
    }

    public void refreshCache(){
        try {
            LocalDateTime ldt = LocalDateTime.now();
            gracePeriodReader.readAllGracePeriods(ldt);
            this.cacheReadingTime = ldt;
        } catch (EmptyResultDataAccessException e) {
            log.error("Can't refresh grace periods: {}", e.getMessage());
        }
    }
}
