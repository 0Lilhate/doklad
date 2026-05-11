/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import ru.it_alnc.packagesearch.entity.subscribe.ServiceGuideEntity29735;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class ServiceGuideMapper implements RowMapper<ServiceGuideEntity29735> {
    @Override
    public ServiceGuideEntity29735 mapRow(ResultSet rs, int rowNum) throws SQLException {
        ServiceGuideEntity29735 serviceGuideEntity = new ServiceGuideEntity29735();
        serviceGuideEntity.setServiceGuideClientCommonType(rs.getString("sgclientcommontype"));
        serviceGuideEntity.setServiceGuideCode(rs.getString("sgcode"));
        serviceGuideEntity.setServiceGuideLabel(rs.getString("sglabel"));
        serviceGuideEntity.setServiceGuideId(rs.getLong("serviceguide"));
        serviceGuideEntity.setClientCommonType(rs.getString("clientcommontype"));
        serviceGuideEntity.setCode(rs.getString("code"));
        serviceGuideEntity.setLabel(rs.getString("label"));
        serviceGuideEntity.setIndexCode(rs.getString("indexcode"));
        serviceGuideEntity.setServiceType(rs.getString("servicetype"));
        serviceGuideEntity.setBegDate(rs.getTimestamp("begdate").toLocalDateTime());
        serviceGuideEntity.setEndDate(rs.getTimestamp("enddate").toLocalDateTime());
        serviceGuideEntity.setSortOrder(rs.getInt("sortorder"));
        serviceGuideEntity.setIsMultyCopiesCalcAllowed(rs.getInt("ismultycopiescalcallowed"));
        return serviceGuideEntity;
    }
}
