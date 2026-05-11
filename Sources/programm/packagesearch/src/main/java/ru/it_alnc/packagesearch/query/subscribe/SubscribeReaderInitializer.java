/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.query.subscribe;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.it_alnc.packagesearch.entity.GroupGuideEntity;
import ru.it_alnc.packagesearch.repository.ActionOnPUChangeRepository;
import ru.it_alnc.packagesearch.repository.GroupGuideRepository;
import ru.it_alnc.packagesearch.repository.ProductRepository;
import ru.it_alnc.packagesearch.repository.ServiceLineRepository;
import ru.it_alnc.packagesearch.repository.ViewDao;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SubscribeReaderInitializer {
    @Autowired
    private SubscribeReader subscribeReader;

    @Autowired
    private ViewDao viewDao;

    @Autowired
    private ServiceLineRepository serviceLineRepository;

    @Autowired
    private ActionOnPUChangeRepository actionOnPUChangeRepository;

    @Autowired
    private GroupGuideRepository groupGuideRepository;

    @Setter
    private Integer counter = -1;
    @Autowired
    private ProductRepository productRepository;
    @Getter
    LocalDateTime lastCacheUpdate = LocalDateTime.now();

    @Setter
    @Getter
    private LocalDateTime groupGuideCacheReadDate;

    @PostConstruct
    private void init() { //todo: изменить, читать всегда всё (readAllActiveAndFutureSubscribes)
        this.counter = viewDao.getTrigger();
        if(subscribeReader.getAllGroupGuides() == null)
            this.groupGuideCacheReadDate = subscribeReader.initGroupGuideCache();
        if (subscribeReader.getAllActiveSubscribes() == null)
            subscribeReader.readAllSubscribes();
        if (subscribeReader.getAllActiveTariffs() == null)
            subscribeReader.readAllTariffs();
        if (subscribeReader.getAllActiveSLs() == null)
            subscribeReader.readAllSLs();
        log.info("Done cache reading");
        log.debug("Sizes of subs, tariffs, SLs : {}, {}, {}, ", subscribeReader.getAllActiveSubscribes().size(), subscribeReader.getAllActiveTariffs().size(), subscribeReader.getAllActiveSLs().size());
        this.lastCacheUpdate = LocalDateTime.now();
    }

    public void checkAndRefreshCacheIfNeeded() {
        try {
            boolean isGroupGuidesUpdated = this.groupGuideRefresh();
            log.info("checking if cache need to be updated");
            Integer newCounter = viewDao.getTrigger();
            log.info("local counter is {}, db counter is {}", counter, newCounter);
            if (!counter.equals(newCounter)) {
                log.info("updating cache by counter");
                groupGuideRefresh();
                subscribeReader.readAllSubscribes();
                subscribeReader.readAllTariffs();
                subscribeReader.readAllSLs();
                this.lastCacheUpdate = LocalDateTime.now();
                log.info("cache updating done");
                this.counter = newCounter;
                return;
            }
            if(isGroupGuidesUpdated){
                log.info("updating cache by group guides trigger");
                groupGuideRefresh();
                subscribeReader.readAllSubscribes();
                subscribeReader.readAllTariffs();
                subscribeReader.readAllSLs();
                this.lastCacheUpdate = LocalDateTime.now();
                log.info("cache updating done");
                return;
            }
            List<LocalDateTime> timeTriggerSL = serviceLineRepository.findNowService(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(1));
            if (!timeTriggerSL.isEmpty()) {
                log.info("updating cache by service line trigger");
                groupGuideRefresh();
                subscribeReader.readAllSubscribes();
                subscribeReader.readAllTariffs();
                subscribeReader.readAllSLs();
                this.lastCacheUpdate = LocalDateTime.now();
                log.info("cache updating done");
                return;
            }
            List<LocalDateTime> timeTriggerProduct = productRepository.findNowService(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(1));
            if (!timeTriggerProduct.isEmpty()) {
                log.info("updating cache by product trigger");
                groupGuideRefresh();
                subscribeReader.readAllSubscribes();
                subscribeReader.readAllTariffs();
                subscribeReader.readAllSLs();
                this.lastCacheUpdate = LocalDateTime.now();
                log.info("cache updating done");
                return;
            }
            List<LocalDateTime> timeTriggerAction = actionOnPUChangeRepository.findNowAction(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(1));
            if (!timeTriggerAction.isEmpty()) {
                log.info("updating cache by action on pu change trigger");
                groupGuideRefresh();
                subscribeReader.readAllSubscribes();
                subscribeReader.readAllTariffs();
                subscribeReader.readAllSLs();
                this.lastCacheUpdate = LocalDateTime.now();
                log.info("cache updating done");
                return;
            }
            LocalDateTime timeTriggerVersion = viewDao.getVersionTimeTrigger(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(1)); //todo: переписать
            if (timeTriggerVersion != null) {
                log.info("updating cache by version trigger");
                groupGuideRefresh();
                subscribeReader.readAllSubscribes();
                subscribeReader.readAllTariffs();
                subscribeReader.readAllSLs();
                this.lastCacheUpdate = LocalDateTime.now();
                log.info("cache updating done");
                return;
            }
            LocalDateTime timeTriggerTariff = viewDao.getTariffTimeTrigger(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(1)); //todo: переписать
            if (timeTriggerTariff != null) {
                log.info("updating cache by tariff trigger trigger");
                groupGuideRefresh();
                subscribeReader.readAllSubscribes();
                subscribeReader.readAllTariffs();
                subscribeReader.readAllSLs();
                this.lastCacheUpdate = LocalDateTime.now();
                log.info("cache updating done");
                return;
            }
            log.info("cache refresh not needed now");
        } catch (Exception e) {
            log.error("Error acquired during subs cache update! can't update!");
            e.printStackTrace();
        }
    }

    public void refreshCache() {
        try {
            log.info("refreshing cache by hour trigger");
            this.counter = viewDao.getTrigger();
            subscribeReader.readAllSubscribes();
            subscribeReader.readAllTariffs();
            subscribeReader.readAllSLs();
            this.lastCacheUpdate = LocalDateTime.now();
            log.info("cache updating done");
        } catch (Exception e) {
            log.error("Error acquired during subs hourly cache update! can't update!");
        }
    }

    public void refreshCacheByRest() {
        try {
            log.info("refreshing cache by rest");
            this.counter = viewDao.getTrigger();
            subscribeReader.readAllSubscribes();
            subscribeReader.readAllTariffs();
            subscribeReader.readAllSLs();
            this.lastCacheUpdate = LocalDateTime.now();
            log.info("cache updating done");
        } catch (Exception e) {
            log.error("Error acquired during subs rest cache update! can't update!");
        }
    }

    private boolean groupGuideRefresh(){
        try{
        LocalDateTime ggCheckCacheTime = LocalDateTime.now();
        List<GroupGuideEntity> checkGroupGuide = groupGuideRepository.findNewGroupGuideEntities(groupGuideCacheReadDate);
        if(!checkGroupGuide.isEmpty()){
            this.groupGuideCacheReadDate = ggCheckCacheTime;
            subscribeReader.updateGroupGuidesCache(checkGroupGuide);
            return true;
        } else {
            log.debug("Nothing happened with groupGuides, I swear");
            return false;
        }
    } catch (Exception e){
            log.warn("Can't refresh groupGuides cache: {}",e.getMessage());
            return false;
        }
    }

}
