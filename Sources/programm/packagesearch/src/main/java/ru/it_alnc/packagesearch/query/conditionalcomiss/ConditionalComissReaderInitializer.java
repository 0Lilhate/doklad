/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.query.conditionalcomiss;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.it_alnc.packagesearch.repository.ConditionalComisSettingsRepository;
import ru.it_alnc.packagesearch.repository.ViewDao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RequiredArgsConstructor
public class ConditionalComissReaderInitializer {

    private final ConditionalComissReader conditionalComissReader;

    private final ConditionalComisSettingsRepository conditionalComisSettingsRepository;

    private final ViewDao viewDao;

    @Setter
    private Integer counter = -1;

    @PostConstruct
    private void init() {
        conditionalComissReader.updateConditionalComissDtoList();
        this.counter = viewDao.getTrigger();
    }

    //каждую минуту проверяем необходимость обновления кеша
    public void checkAndRefreshCacheIfNeeded() {
        log.info("Check if conditional cache is needed to be refreshed");
        try {
            var newCounter = viewDao.getTrigger();
            if(!Objects.equals(counter, newCounter)) {
                conditionalComissReader.updateConditionalComissDtoList();
                return;
            }
            LocalDateTime ldt = LocalDateTime.now();
            List<LocalDateTime> newConditionalComissDateList = conditionalComisSettingsRepository.findNewSettings(ldt.minusMinutes(1), ldt.plusMinutes(1));
            if(!newConditionalComissDateList.isEmpty()){
                conditionalComissReader.updateConditionalComissDtoList();
            } else {
                log.debug("Nothing happened with conditionalComiss DB, I swear");
            }

        } catch (EmptyResultDataAccessException e) {
            log.error("Can't refresh conditionalComiss: {}", e.getMessage());
        }
    }

    public void refreshCache(){
        try {
            conditionalComissReader.updateConditionalComissDtoList();
        } catch (EmptyResultDataAccessException e) {
            log.error("Can't refresh conditionalComiss by force: {}", e.getMessage());
        }
    }

}
