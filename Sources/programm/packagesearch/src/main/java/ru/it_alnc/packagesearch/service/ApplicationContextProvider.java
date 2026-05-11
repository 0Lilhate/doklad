/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    static AtomicReference<ApplicationContext> contextRef = new AtomicReference<ApplicationContext>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext == null)
            throw new AssertionError("context is null");

        synchronized (ApplicationContextProvider.class) {
            if (contextRef.get() == null) {
                ApplicationContext old = contextRef.getAndSet(applicationContext);
                if (old != null) {
                    throw new AssertionError("the context aware hook should invoke only once.");
                }
            }
        }
    }

    public static <T extends Object> T getBean(Class<T> beanClass) {
            T bean = contextRef.get().getBean(beanClass);
            log.debug("Using {} for {}", bean.toString(), Thread.currentThread().getName());
            return bean;
    }


}
