/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.it_alnc.packagesearch.entity.subscribe.ServiceGuideEntity29735;

import java.util.List;

@Component
public class ServiceGuideDao {
    //todo - перекинуть все в hikari.schema
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schema;

    private final String serviceGuideQuery = "SELECT " +
            " SG.ClientCommonType as sgClientCommonType, " +
            " SG.Code as sgCode, " +
            " SG.label as sgLabel, " +
            " SGI.serviceguide AS serviceguide, " +
            " SGI.ClientCommonType, " +
            " SGI.Code, " +
            " SGI.Label, " +
            " SGI.indexcode, " +
            " SGI.ServiceType, " +
            " SGI.BegDate, " +
            " SGI.EndDate, " +
            " SGI.SortOrder, " +
            " SGI.ismultycopiescalcallowed " +
            "FROM " +
            " serviceguide SG, " +
            " serviceguideitem SGI " +
            "WHERE " +
            " SGI.ServiceGuide = SG.Id " +
            " and SGI.Status = 'Approved' " +
            "ORDER BY " +
            " serviceguide";

    public List<ServiceGuideEntity29735> getAllActiveServiceGuides() {
        RowMapper<ServiceGuideEntity29735> serviceGuideMapper = new ServiceGuideMapper();
        List<ServiceGuideEntity29735> result = jdbcTemplate.query(serviceGuideQuery, serviceGuideMapper);
        return result;
    }
}
