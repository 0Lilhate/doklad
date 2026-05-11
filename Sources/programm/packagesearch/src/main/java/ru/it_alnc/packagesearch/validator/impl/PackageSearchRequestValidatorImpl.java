package ru.it_alnc.packagesearch.validator.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.it_alnc.packagesearch.dto.ErrorDto;
import ru.it_alnc.packagesearch.dto.checkInfo.CheckInfoRequestDto;
import ru.it_alnc.packagesearch.dto.graceperiod.GracePeriodRequestDto;
import ru.it_alnc.packagesearch.exception.CustomErrorMessageException;
import ru.it_alnc.packagesearch.validator.PackageSearchRequestValidator;

@Component
public class PackageSearchRequestValidatorImpl implements PackageSearchRequestValidator {
    private static final String CLIENT_SERVICE_CHANNEL = "CLIENTSERVICECHANNEL";
    private static final String IS_NEW_CARD = "ISNEWCARD";
    private static final String CARD_TYPE = "CARDTYPE";
    private static final String CLIENT_SERVICE_CHANNEL_ERROR_MESSAGE = "Для подбора grace-периода не передан обязательный параметр clientServiceChannel (Канал подключения услуги)";
    private static final String NEW_CARD_ERROR_MESSAGE = "Для подбора grace-периода не передан обязательный параметр newCard (Признак новой карты)";
    private static final String CARD_TYPE_ERROR_MESSAGE = "Для подбора grace-периода не передан обязательный параметр cardType (Тип карты)";

    @Override
    public void validate(CheckInfoRequestDto checkInfoRequestDto) throws CustomErrorMessageException {
        if (CollectionUtils.isEmpty(checkInfoRequestDto.getPackageCodes())) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("094");
            errorDto.setMessage("Не указан список пакетов клиента в параметре packageList");
            throw new CustomErrorMessageException(errorDto);
        }
        if (StringUtils.isBlank(checkInfoRequestDto.getService())) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("095");
            errorDto.setMessage("Не указан код услуги в параметре service");
            throw new CustomErrorMessageException(errorDto);
        }
    }

    @Override
    public void validate(GracePeriodRequestDto gracePeriodRequestDto) throws CustomErrorMessageException {
        if (gracePeriodRequestDto.getService() == null || gracePeriodRequestDto.getService().isEmpty()) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setMessage("Для подбора grace-периода не передан обязательный параметр service (Код услуги)");
            errorDto.setCode("101");
            throw new CustomErrorMessageException(errorDto);
        }

        validateGracePeriodParams(gracePeriodRequestDto);
    }

    private void validateGracePeriodParams(GracePeriodRequestDto gracePeriodRequestDto) {
        gracePeriodRequestDto.getOperationParams()
                .forEach(this::validateGracePeriodParam);
    }

    private void validateGracePeriodParam(GracePeriodRequestDto.ParamMap paramMap) {
        if (isParamWithNameAndIsEmpty(paramMap, CLIENT_SERVICE_CHANNEL)) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("101");
            errorDto.setMessage(CLIENT_SERVICE_CHANNEL_ERROR_MESSAGE);
            throw new CustomErrorMessageException(errorDto);
        }

        if (isParamWithNameAndIsEmpty(paramMap, IS_NEW_CARD)) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("101");
            errorDto.setMessage(NEW_CARD_ERROR_MESSAGE);
            throw new CustomErrorMessageException(errorDto);
        }

        if (isParamWithNameAndIsEmpty(paramMap, CARD_TYPE)) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("101");
            errorDto.setMessage(CARD_TYPE_ERROR_MESSAGE);
            throw new CustomErrorMessageException(errorDto);
        }
    }

    private boolean isParamWithNameAndIsEmpty(GracePeriodRequestDto.ParamMap paramMap, String paramName) {
        if (!paramMap.getCode().equals(paramName)) {
            return false;
        }

        return !StringUtils.isNotBlank(paramMap.getValue());
    }
}
