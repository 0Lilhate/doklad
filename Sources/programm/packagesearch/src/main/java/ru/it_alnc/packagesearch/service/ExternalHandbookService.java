/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.it_alnc.packagesearch.dto.HandbookDto;
import ru.it_alnc.packagesearch.dto.SearchCriteriaDto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExternalHandbookService {
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${it-alnc.hdbk-service-url}")
    private String externalHdbkUrl;

    private HttpClient httpClient = HttpClients.createDefault();

    @CacheEvict(value = "handbook", allEntries = true)
    @Scheduled(fixedRate = 90000)
    public void cleanCache(){
        log.debug("Emptying handbook cache");
    }

    @Cacheable("handbook")
    public List<HandbookDto> getHandbook(String parentCode) {
        log.debug("Getting handbook {} into cache", parentCode);
        String getExtHdbkUrl = externalHdbkUrl + "/internal/getHdbk?parentCode=%s&account=%s";
        getExtHdbkUrl = String.format(getExtHdbkUrl, parentCode, "alfa");
        HttpGet httpGet = new HttpGet(getExtHdbkUrl);
        List<HandbookDto> result = null;

        try {
            HttpResponse resp = httpClient.execute(httpGet);
            InputStream is = resp.getEntity().getContent();
            String hdbkJsonString = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            result = objectMapper.readValue(hdbkJsonString, new TypeReference<List<HandbookDto>>(){});
        } catch (IOException e) {
            log.error("Can't obtain external handbook by url: {}", getExtHdbkUrl);
            e.printStackTrace();
        }

        return result;
    }

    public List<HandbookDto> findHandbook(SearchCriteriaDto searchCriteriaDto) throws JsonProcessingException {
        String findExtHdbkUrl = externalHdbkUrl + "/searchStd";
        HttpPost httpPost = new HttpPost(findExtHdbkUrl);
        String searchJson = objectMapper.writeValueAsString(searchCriteriaDto);
//        StringRequestEntity requestEntity = new StringRequestEntity();
        StringEntity requestEntity = new StringEntity(searchJson, ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);
        List<HandbookDto> result = new ArrayList<>();

        try {
            HttpResponse resp = httpClient.execute(httpPost);
            InputStream is = resp.getEntity().getContent();
            String hdbkJsonString = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            result = objectMapper.readValue(hdbkJsonString, new TypeReference<List<HandbookDto>>(){});
        } catch (IOException e) {
            log.error("Can't find info in external handbook by url {} and criteria {}", findExtHdbkUrl, searchJson);
            e.printStackTrace();
        }

        return result;
    }
}
