/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ViewDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schema;
//todo переделать на параметризованные запросы вида SELECT DISTINCT PG_CODE FROM %s.V_PRODUCTLISTPARAM WHERE P_CODE = '?' -> jdbcTemplate.queryForObject(query, String.class, code)
    private final String labelQuery = "SELECT LABEL FROM %s.%s WHERE CODE = '%s'";
    //    private final String listGroupQuery = "SELECT DISTINCT PG_CODE FROM %s.V_PRODUCTLISTPARAM WHERE P_CODE = '%s'";
//    private final String tableGroupQuery = "SELECT DISTINCT PG_CODE FROM %s.V_PRODUCTTABLEPARAM WHERE P_CODE = '%s' " +
//            "AND PG_BEGDATE < CURRENT_DATE AND PG_ENDDATE > CURRENT_DATE";
//    private final String versionQuery = "SELECT DISTINCT P_VERSION FROM %s.V_PRODUCTLISTPARAM WHERE P_CODE = '%s' " +
//            "AND P_VERSIONBEGDATE < CURRENT_DATE AND P_VERSIONENDDATE > CURRENT_DATE";
    private final String listGroupQuery = "SELECT DISTINCT PG_CODE FROM %s.V_PRODUCTLISTPARAM WHERE P_CODE = '%s' " +
            " AND PG_BEGDATE <= timestamp '%s' AND PG_ENDDATE >= timestamp '%s' AND PG_STATUS = 'Approved' ";
    private final String tableGroupQuery = "SELECT DISTINCT PG_CODE FROM %s.V_PRODUCTTABLEPARAM WHERE P_CODE = '%s' " +
            " AND P_VERSION = '%s' AND PG_BEGDATE <= timestamp '%s' AND PG_ENDDATE >= timestamp '%s' AND PG_STATUS = 'Approved' ";
    private final String versionQuery = "SELECT NUM FROM %s.VERSION WHERE PRODUCT = %d " +
            " AND BEGDATE <= timestamp '%s' AND ENDDATE >= timestamp '%s' AND STATUS = 'Approved'";
    private final String targetDateQuery = "SELECT TESTDATE from %s.TESTPARAM FETCH FIRST 1 ROW ONLY";
    private final String tableGroupLabelQuery = "SELECT DISTINCT PG_LABEL FROM %s.V_PRODUCTTABLEPARAM vp WHERE vp.PG_CODE = '%s' " +
            "AND vp.P_CODE = '%s' AND vp.P_VERSION = '%s' " +
            "AND PG_BEGDATE <= timestamp '%s' AND PG_ENDDATE >= timestamp '%s'";
    private final String tariffDatesQuery = "SELECT DISTINCT PG_BEGDATE, PG_ENDDATE " +
            "FROM %s.V_PRODUCTTABLEPARAM vp " +
            "WHERE PG_CODE = 'TARIFF' AND PG_STATUS = 'Approved' AND P_CODE = '%s' AND P_VERSION = %d " +
            "AND PG_BEGDATE <= timestamp '%s' AND PG_ENDDATE >= timestamp '%s'";
    private final String productTypeQuery = "SELECT DISTINCT VALUELIST " +
            "FROM %s.V_PRODUCTLISTPARAM " +
            "WHERE P_CODE = '%s' AND PM_CODE = 'PRODUCTTYPE'";
    private final String productPactPrintFormQuery = "SELECT DISTINCT VALUESTRING " +
            "FROM %s.V_PRODUCTLISTPARAM " +
            "WHERE P_CODE = '%s' AND PM_CODE = 'PACT_PRINT_FORM'";
    private final String refreshTriggerRequest = "SELECT PROP FROM %s.DATAREFRESH ";
    private final String initialSystemRequest = "SELECT VALUELIST FROM %s.V_PRODUCTLISTPARAM vp " +
            "WHERE P_CODE = '%s' AND P_VERSION = %d AND PG_CODE = 'ONSITE_PARAMS' " +
            "AND PG_BEGDATE <= timestamp '%s' AND PG_ENDDATE >= timestamp '%s' AND PG_STATUS = 'Approved' AND " +
            "PM_CODE = 'SOURCESYSTEM'";

    //17635 ID_FM
    private final String idFmQuery = "SELECT VALUESTRING FROM %s.V_PRODUCTLISTPARAM WHERE P_CODE = '%s' AND PM_CODE = 'ID_FM' AND P_VERSION = %d";
    //17662 PRODUCT_INFO_MESSAGE
    private final String productInfoMessageQuery = "SELECT VALUESTRING FROM %s.V_PRODUCTLISTPARAM WHERE P_CODE = '%s' AND PM_CODE = 'PRODUCT_INFO_MESSAGE' AND P_VERSION = %d";

    //18418 subscribe ids by segment
    private final String segmentSubscribeQuery = "SELECT P_CODE FROM %s.V_PRODUCTLISTPARAM " +
            "WHERE PG_CODE = 'PRODUCT_PROPERTIES' AND PM_CODE = 'SEGMENT' " +
            "AND P_BEGDATE <= timestamp '%s' AND P_ENDDATE >= timestamp '%s' " +
            "AND P_VERSIONBEGDATE <= timestamp '%s' AND P_VERSIONENDDATE >= timestamp '%s' " +
            "AND PG_BEGDATE <= timestamp '%s' AND PG_ENDDATE >= timestamp '%s' " +
            "AND VALUESTRING LIKE '%%%s%%'";

    //29091 time trigger requests
    private final String productTimeTrigger = "SELECT begdate FROM %s.product p where p.status = 'Approved' AND p.begdate BETWEEN timestamp '%s' " +
            "AND timestamp '%s' LIMIT 1";
    private final String versionTimeTrigger = "SELECT begdate FROM %s.version v where v.status = 'Approved' AND v.begdate BETWEEN timestamp '%s' " +
            "AND timestamp '%s' AND v.modifiedon BETWEEN timestamp '%s' AND timestamp '%s' LIMIT 1";
    private final String tariffTimeTrigger = "SELECT begdate FROM %s.tariff t where t.status = 'Approved' AND t.begdate BETWEEN timestamp '%s' " +
            "AND timestamp '%s' AND t.modifiedon BETWEEN timestamp '%s' AND timestamp '%s' LIMIT 1";

    public String getLabel(String table, String code) {
        String query = String.format(labelQuery, schema, table, code);
        String label = jdbcTemplate.queryForObject(query, String.class);
        return label;
    }

    public String getTableGroupLabel(String code, String productCode, String version, LocalDateTime targetDate) {
        String timestampStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String query = String.format(tableGroupLabelQuery, schema, code, productCode, version, timestampStr, timestampStr);
        String label = jdbcTemplate.queryForObject(query, String.class);
        return label;
    }

    public List<String> getListGroups(String productCode, LocalDateTime targetDate) {
        String timestampStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String query = String.format(listGroupQuery, schema, productCode, timestampStr, timestampStr);
        List<String> result = jdbcTemplate.queryForList(query, String.class);
        return result;
    }

    public List<String> getTableGroups(String productCode, String version, LocalDateTime targetDate) {
        String timestampStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String query = String.format(tableGroupQuery, schema, productCode, version, timestampStr, timestampStr);
        List<String> result = jdbcTemplate.queryForList(query, String.class);
        return result;
    }

    public Integer getTrigger() {
        String query = String.format(refreshTriggerRequest, schema);
        Integer result = jdbcTemplate.queryForObject(query, Integer.class);
        return result;
    }

    @Deprecated
    public Integer getVersion(int productId, LocalDateTime targetDate) {
        String timestampStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String query = String.format(versionQuery, schema, productId, timestampStr, timestampStr);
        List<Integer> result = jdbcTemplate.queryForList(query, Integer.class);
        return result.get(0);
    }

    public List<String> getSourceSystem(String productCode, int version, LocalDateTime targetDate) {
        String timestampStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String query = String.format(initialSystemRequest, schema, productCode, version, timestampStr, timestampStr);
        List<String> result = jdbcTemplate.queryForList(query, String.class);
        return result;
    }

    public LocalDateTime getTargetDate() {
        String query = String.format(targetDateQuery, schema);
        LocalDateTime targetDate = jdbcTemplate.queryForObject(query, LocalDateTime.class);
        return targetDate;
    }

    public Map<String, Object> getTariffDates(String productCode, int version, LocalDateTime targetDate) {
        String timestampStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String query = String.format(tariffDatesQuery, schema, productCode, version, timestampStr, timestampStr);
        Map<String, Object> result = jdbcTemplate.queryForMap(query);
        return result;
    }

    public List<String> getProductTypes(String productCode) {
        String query = String.format(productTypeQuery, schema, productCode);
        List<String> result = jdbcTemplate.queryForList(query, String.class);
        return result;
    }

    public List<String> getProductPactForm(String productCode) {
        String query = String.format(productPactPrintFormQuery, schema, productCode);
        List<String> result = jdbcTemplate.queryForList(query, String.class);
        return result;
    }

    public String getIdFm(String productCode, int version) {
        String query = String.format(idFmQuery, schema, productCode, version);
        try {
            String result = jdbcTemplate.queryForObject(query, String.class);
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.warn("No ID_FM for {} v {}, set null", productCode, version);
            return null;
        }
    }

    public String getProductInfoMessage(String productCode, int version) {
        String query = String.format(productInfoMessageQuery, schema, productCode, version);
        try {
            String result = jdbcTemplate.queryForObject(query, String.class);
            return result;
        } catch (EmptyResultDataAccessException e) {
//            log.warn("No PRODUCT_INFO_MESSAGE for {} v {}, set null", productCode, version);
            return null;
        }
    }

    public List<String> getSubscribeCodesBySegment(String clientSegment, LocalDateTime targetDate){
        String timestampStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String query = String.format(segmentSubscribeQuery, schema, timestampStr, timestampStr, timestampStr,
                timestampStr, timestampStr, timestampStr, clientSegment);
        List<String> result = jdbcTemplate.queryForList(query, String.class);
        return result;
    }

    public LocalDateTime getProductTimeTrigger(LocalDateTime lowDate, LocalDateTime highDate){
        String timestampLDStr = lowDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String timestampHDStr = highDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String query = String.format(productTimeTrigger, schema, timestampLDStr, timestampHDStr);
        try {
            LocalDateTime result = jdbcTemplate.queryForObject(query, LocalDateTime.class);
            return result;
        } catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public LocalDateTime getVersionTimeTrigger(LocalDateTime lowDate, LocalDateTime highDate) {
        String timestampLDStr = lowDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String timestampHDStr = highDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String query = String.format(versionTimeTrigger, schema, timestampLDStr, timestampHDStr, timestampLDStr, timestampHDStr);
        try {
            LocalDateTime result = jdbcTemplate.queryForObject(query, LocalDateTime.class);
            return result;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public LocalDateTime getTariffTimeTrigger(LocalDateTime lowDate, LocalDateTime highDate){
        String timestampLDStr = lowDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String timestampHDStr = highDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String query = String.format(tariffTimeTrigger, schema, timestampLDStr, timestampHDStr, timestampLDStr, timestampHDStr);
        try {
            LocalDateTime result = jdbcTemplate.queryForObject(query, LocalDateTime.class);
            return result;
        } catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public boolean checkForExistingServices(List<String> serviceCodes){
        StringBuilder stringBuilderQuery = new StringBuilder("SELECT sgi.code FROM serviceline sl JOIN serviceguideitem sgi " +
                " ON sl.serviceguideitem = sgi.id WHERE sgi.code in (");
        for(int i = 0; i<serviceCodes.size(); i++){
            stringBuilderQuery.append('\'').append(serviceCodes.get(i)).append('\'');
            if(i<serviceCodes.size()-1)
                stringBuilderQuery.append(',');
        }
        stringBuilderQuery.append(")");

        List<String> codes = jdbcTemplate.queryForList(stringBuilderQuery.toString(), String.class);

        return codes.size() != 0;
    }
}
