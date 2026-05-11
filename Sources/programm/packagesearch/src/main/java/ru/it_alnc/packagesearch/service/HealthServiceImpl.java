/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.it_alnc.ita_forms.audit.health.AuditKafkaHealthcheck;
import ru.it_alnc.ita_forms.core.context.health.service.IHealthService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
@Slf4j
public class HealthServiceImpl implements IHealthService {
    @Autowired
    private AuditKafkaHealthcheck kafkaHealthcheck;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Override
    public boolean checkModuleHealthReadiness() {
        return checkDBHealth();
        // && checkKafkaAuditHealth();
    }

    @Override
    public boolean checkModuleHealthLiveness() {
        return true;
    }

    public boolean checkKafkaAuditHealth(){
        return kafkaHealthcheck.checkKafkaAuditHealth();
    }


    private boolean checkDBHealth() {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
        } catch (SQLException se) {
            log.error("DB is not available!");
            return false;
        }
        return true;
    }
}
