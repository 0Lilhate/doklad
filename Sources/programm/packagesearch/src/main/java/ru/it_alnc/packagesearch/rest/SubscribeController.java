/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.it_alnc.packagesearch.config.ThreadLocalBeanFactoryPP;
import ru.it_alnc.packagesearch.dto.ErrorDto;
import ru.it_alnc.packagesearch.dto.FL.FLLightSubscribeDto;
import ru.it_alnc.packagesearch.dto.FL.LightRequestDto;
import ru.it_alnc.packagesearch.dto.GetTariffDto;
import ru.it_alnc.packagesearch.dto.TariffDto;
import ru.it_alnc.packagesearch.dto.checkInfo.CheckInfoPackageDto;
import ru.it_alnc.packagesearch.dto.checkInfo.CheckInfoRequestDto;
import ru.it_alnc.packagesearch.dto.conditionalcomiss.ConditionalComisRequestDto;
import ru.it_alnc.packagesearch.dto.conditionalcomiss.ConditionalComisResponseDto;
import ru.it_alnc.packagesearch.dto.graceperiod.GracePeriodRequestDto;
import ru.it_alnc.packagesearch.dto.graceperiod.GracePeriodResponseDto;
import ru.it_alnc.packagesearch.dto.subscribe.*;
import ru.it_alnc.packagesearch.exception.*;
import ru.it_alnc.packagesearch.service.*;
import ru.it_alnc.packagesearch.service.util.RequestDtoSanitizer;
import ru.it_alnc.packagesearch.validator.PackageSearchRequestValidator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@Scope(ThreadLocalBeanFactoryPP.SCOPE_THREAD)
public class SubscribeController {

    SubscribeService subscribeService;
    GracePeriodService gracePeriodService;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final ServiceGuideService serviceGuideService;
    private final RequestLogService requestLogService;
    private final PackageSearchRequestValidator packageSearchRequestValidator;
    private final ConditionalComissService conditionalComissService;

    public SubscribeController(ServiceGuideService serviceGuideService,
                               RequestLogService requestLogService, PackageSearchRequestValidator packageSearchRequestValidator,
                               ConditionalComissService conditionalComissService) {
        this.serviceGuideService = serviceGuideService;
        this.requestLogService = requestLogService;
        this.packageSearchRequestValidator = packageSearchRequestValidator;
        this.conditionalComissService = conditionalComissService;
    }

    @PostMapping("/package/search")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ServicePackageList.class)))
    public ResponseEntity<?> searchSubscribes(@RequestBody SubscribeRequest27277 subscribeRequestDto) throws JsonProcessingException, CloneNotSupportedException {
        this.subscribeService = ApplicationContextProvider.getBean(SubscribeService.class);
        this.subscribeService.initSQBWithRequest(subscribeRequestDto);
        if (requestLogService.getLogRequests()) {
            try {
                log.info("Received request: {}", objectMapper.writeValueAsString(subscribeRequestDto));
            } catch (JsonProcessingException e) {
                log.warn("Can't deserialize request");
                log.info(subscribeRequestDto.toString());
            }
        }
        //log.debug("Using {}", subscribeService.toString());
        try {
            ServicePackageList result = subscribeService.searchServicePackages(subscribeRequestDto);
            if (requestLogService.getLogRequests())
                log.info("Request processing finished");
            return new ResponseEntity<ServicePackageList>(result, HttpStatus.OK);
        } catch (NonExistPUException e) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("040");
            errorDto.setMessage("Не найден Пакет услуг " + e.getMessage());
            errorDto.setSource("PACKAGESEARCH");
            log.error("NonExistPUException {}", objectMapper.convertValue(errorDto, JsonNode.class));
            return new ResponseEntity<>(errorDto, HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (NonExistServiceException e) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("041");
            errorDto.setMessage("Не найдена Услуга с кодом " + e.getMessage());
            errorDto.setSource("PACKAGESEARCH");
            log.error("NonExistServiceException {}", objectMapper.convertValue(errorDto, JsonNode.class));
            return new ResponseEntity<>(errorDto, HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (NoBranchGroupException e) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("042");
            errorDto.setMessage("Не найдена Тарифная группа " + e.getMessage());
            errorDto.setSource("PACKAGESEARCH");
            log.error("NoBranchGroupException {}", objectMapper.convertValue(errorDto, JsonNode.class));
            return new ResponseEntity<>(errorDto, HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Метод, возвращающий урезанный и максимально легкий ответ с "шапками" подписок. Алгоритм поиска такой же
     */
    @PostMapping("/package/searchInfo")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ServicePackageList.class)))
    public ResponseEntity<?> searchSubscribesInfo(@RequestParam(required = false, defaultValue = "false") Boolean isSkipPuParams, @RequestBody SubscribeRequest27277 subscribeRequestDto) throws CloneNotSupportedException {
        this.subscribeService = ApplicationContextProvider.getBean(SubscribeService.class);
        this.subscribeService.initSQBWithRequest(subscribeRequestDto);
        if (requestLogService.getLogRequests()) {
            try {
                log.info("Received searchInfo request: {}", objectMapper.writeValueAsString(subscribeRequestDto));
            } catch (JsonProcessingException e) {
                log.warn("Can't deserialize request");
                log.info(subscribeRequestDto.toString());
            }
        }
        try {
            ServicePackageList result = subscribeService.searchServicePackagesInfo(subscribeRequestDto, isSkipPuParams);
            JsonNode resultJson = objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY).convertValue(result, JsonNode.class);
            if (requestLogService.getLogRequests())
                log.info("Info request processing finished");
            return new ResponseEntity<>(resultJson, HttpStatus.OK);
        } catch (NonExistPUException e) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("040");
            errorDto.setMessage("Не найден Пакет услуг " + e.getMessage());
            errorDto.setSource("PACKAGESEARCH");
            return new ResponseEntity<>(errorDto, HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (NonExistServiceException e) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("041");
            errorDto.setMessage("Не найдена Услуга с кодом " + e.getMessage());
            errorDto.setSource("PACKAGESEARCH");
            return new ResponseEntity<>(errorDto, HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (NoBranchGroupException e) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("042");
            errorDto.setMessage("Не найдена Тарифная группа " + e.getMessage());
            errorDto.setSource("PACKAGESEARCH");
            return new ResponseEntity<>(errorDto, HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (CloneNotSupportedException
                //| JsonProcessingException
                e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/service/search")
    public ResponseEntity<ServiceGuideListDto> testServiceGuides(@RequestBody SearchServiceGuideDto serviceGuideRequestDto) { //на каждый запрос лезет в базу, адекватно
        if (requestLogService.getLogRequests()) {
            try {
                log.info("Received serviceGuide request: {}", objectMapper.writeValueAsString(serviceGuideRequestDto));
            } catch (JsonProcessingException e) {
                log.warn("Can't deserialize serviceGuide request");
                log.info(serviceGuideRequestDto.toString());
            }
        }

        ServiceGuideListDto serviceGuideListDto = new ServiceGuideListDto();
        List<ServiceGuideDto> result = serviceGuideService.searchSGs(serviceGuideRequestDto);
        serviceGuideListDto.setServicePackageList(result);
        if (requestLogService.getLogRequests())
            log.info("ServiceGuide search done");
        return ResponseEntity.ok(serviceGuideListDto);
    }

    @PostMapping("/conditionalCommis/search")
    public ResponseEntity<ConditionalComisResponseDto> searchConditionalComiss(@RequestBody ConditionalComisRequestDto requestDto) throws NonExistantConditionalSettingException, NonSuitableConditionalSettingException { //на каждый запрос лезет в базу, адекватно
        if (requestLogService.getLogRequests()) {
            try {
                log.info("Received conditionalCommiss request: {}", objectMapper.writeValueAsString(requestDto));
            } catch (JsonProcessingException e) {
                log.warn("Can't deserialize conditionalCommiss request");
                log.info(requestDto.toString());
            }
        }
        requestDto = RequestDtoSanitizer.sanitize(requestDto);
        ConditionalComisResponseDto result = conditionalComissService.findConditionalComisSettings(requestDto);
        if (requestLogService.getLogRequests())
            log.info("conditionalComiss search done");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/tariff/get")
    public ResponseEntity<TariffDto> getTariff(@RequestBody GetTariffDto getTariffDto) { //на каждый запрос лезет в базу, адекватно
        this.subscribeService = ApplicationContextProvider.getBean(SubscribeService.class);
        if (requestLogService.getLogRequests()) {
            try {
                log.info("Received tariff request: {}", objectMapper.writeValueAsString(getTariffDto));
            } catch (JsonProcessingException e) {
                log.warn("Can't deserialize tariff request");
                log.info(getTariffDto.toString());
            }
        }
        //написать свой метод для тарифа
        TariffDto result = subscribeService.findOneTariffLine(getTariffDto);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        if (requestLogService.getLogRequests())
            log.info("tariffGet search done");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/FL/serviceLine/checkInfo")
    public ResponseEntity<List<CheckInfoPackageDto>> checkInfo(@RequestBody CheckInfoRequestDto requestDto) throws CloneNotSupportedException {
        if (requestLogService.getLogRequests()) {
            try {
                log.info("Received FL checkInfo request: {}", objectMapper.writeValueAsString(requestDto));
            } catch (JsonProcessingException e) {
                log.warn("Can't deserialize FL checkInfo request");
                log.info(requestDto.toString());
            }
        }

        packageSearchRequestValidator.validate(requestDto);

        this.subscribeService = ApplicationContextProvider.getBean(SubscribeService.class);
        this.subscribeService.initSQBWithRequest(requestDto);
        //TODO: переделать на CommonsRequestLoggingFilter
        List<CheckInfoPackageDto> result = subscribeService.getCheckInfoList(requestDto);
        if (requestLogService.getLogRequests())
            log.info("FL checkInfo search done");
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    @PostMapping("/FL/packageLightSearch")
    public ResponseEntity packageLightSearch(@RequestBody LightRequestDto lightRequestDto) throws CloneNotSupportedException {
        this.subscribeService = ApplicationContextProvider.getBean(SubscribeService.class);
        this.subscribeService.initSQBWithRequest(lightRequestDto);
        if (requestLogService.getLogRequests()) {
            try {
                log.info("Received /FL/packageLightSearch request: {}", objectMapper.writeValueAsString(lightRequestDto));
            } catch (JsonProcessingException e) {
                log.warn("Can't deserialize request");
                log.info(lightRequestDto.toString());
            }
        }
        try {
            List<FLLightSubscribeDto> result = subscribeService.packageLightSearch(lightRequestDto);
            if (requestLogService.getLogRequests())
                log.info("LightSearch request processing finished");
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/FL/gracePeriod")
    public ResponseEntity gracePeriodSearch(@RequestBody GracePeriodRequestDto gracePeriodRequestDto) throws CloneNotSupportedException {
        if (requestLogService.getLogRequests()) {
            try {
                log.info("Received /FL/subscription/gracePeriod request: {}", objectMapper.writeValueAsString(gracePeriodRequestDto));
            } catch (JsonProcessingException e) {
                log.warn("Can't deserialize request");
                log.info(gracePeriodRequestDto.toString());
            }
        }

        packageSearchRequestValidator.validate(gracePeriodRequestDto);

        this.gracePeriodService = ApplicationContextProvider.getBean(GracePeriodService.class);
        this.gracePeriodService.filterGQBByRequest(gracePeriodRequestDto);
        try {
            long timeBefore = System.currentTimeMillis();
            GracePeriodResponseDto result = gracePeriodService.gracePeriodSearch(gracePeriodRequestDto);
            if (requestLogService.getLogRequests())
                log.info("GracePeriod request processing finished, duration is {}ms", System.currentTimeMillis() - timeBefore);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (ExceptionCustomErrorDto ex) {
            return new ResponseEntity<>(ex.getErrorDto(), ex.getHttpStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/package/list")
    public ResponseEntity getPackageList(@RequestBody PackageListRequestDto packageListRequestDto) throws CloneNotSupportedException {
        this.subscribeService = ApplicationContextProvider.getBean(SubscribeService.class);
        this.subscribeService.initSQBWithRequest(packageListRequestDto);
        if (requestLogService.getLogRequests()) {
            try {
                log.info("Received packageList request: {}", objectMapper.writeValueAsString(packageListRequestDto));
            } catch (JsonProcessingException e) {
                log.warn("Can't deserialize request");
                log.info(packageListRequestDto.toString());
            }
        }
        try {
            List<LightSubscribeDto> result = subscribeService.getPackageList(packageListRequestDto);
            if (requestLogService.getLogRequests())
                log.info("packageList request processing finished, size of filtered subs {}", result.size());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/rateType")
    public ResponseEntity returnRateType(@RequestBody JsonNode requestBody) {
        this.subscribeService = ApplicationContextProvider.getBean(SubscribeService.class);
        Map<String, String> requestMap = objectMapper.convertValue(requestBody, Map.class);
        if (requestMap.get("codeList") == null)
            return new ResponseEntity(subscribeService.getRateTypes(), HttpStatus.OK);
        return new ResponseEntity(subscribeService.getRateTypes(Arrays.asList(requestMap.get("codeList").split(";"))), HttpStatus.OK);
    }

    @PostMapping("/getPackageAndIndividualTariffCode")
    public ResponseEntity<LightServicePackageList> getLightPackagesAndIndividualTariffs(@RequestBody LightSubscribeRequestDto requestBody) throws CloneNotSupportedException {
        this.subscribeService = ApplicationContextProvider.getBean(SubscribeService.class);
        this.subscribeService.initSQBWithRequest(requestBody);
        LightServicePackageList result = this.subscribeService.getLightPackageList(requestBody);
        return new ResponseEntity(result, HttpStatus.OK);
    }
}
