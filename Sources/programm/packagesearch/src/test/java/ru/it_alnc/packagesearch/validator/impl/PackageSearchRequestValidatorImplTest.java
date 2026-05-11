package ru.it_alnc.packagesearch.validator.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.it_alnc.packagesearch.dto.checkInfo.CheckInfoRequestDto;
import ru.it_alnc.packagesearch.dto.graceperiod.GracePeriodRequestDto;
import ru.it_alnc.packagesearch.exception.CustomErrorMessageException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

@ExtendWith(MockitoExtension.class)
class PackageSearchRequestValidatorImplTest {
    private static final String SERVICE = "service";
    private static final String PACKAGE_CODE = "packageCode";
    private static final String EMPTY_PACKAGE_CODE = "emptyPackageCode";
    private static final String GRACE_PERIOD_PARAM_VALUE = "value";
    private static final String CLIENT_SERVICE_CHANNEL = "CLIENTSERVICECHANNEL";
    private static final String IS_NEW_CARD = "ISNEWCARD";
    private static final String CARD_TYPE = "CARDTYPE";

    private final PackageSearchRequestValidatorImpl validator = new PackageSearchRequestValidatorImpl();

    @Test
    void validateSuccess() {
        //GIVEN
        CheckInfoRequestDto checkInfoRequestDto = new CheckInfoRequestDto();
        checkInfoRequestDto.setPackageCodes(List.of(PACKAGE_CODE));
        checkInfoRequestDto.setService(SERVICE);

        //THEN
        assertThatNoException().isThrownBy(() -> validator.validate(checkInfoRequestDto));
    }

    @Test
    void validateEmptyPackageCodes() {
        //GIVEN
        CheckInfoRequestDto checkInfoRequestDto = new CheckInfoRequestDto();

        //THEN
        assertThatExceptionOfType(CustomErrorMessageException.class)
                .isThrownBy(() -> validator.validate(checkInfoRequestDto));
    }


    @Test
    void validateEmptyService() {
        //GIVEN
        CheckInfoRequestDto checkInfoRequestDto = new CheckInfoRequestDto();
        checkInfoRequestDto.setPackageCodes(List.of(PACKAGE_CODE));

        //THEN
        assertThatExceptionOfType(CustomErrorMessageException.class)
                .isThrownBy(() -> validator.validate(checkInfoRequestDto));
    }

    @Test
    void validateGracePeriodWithEmptyService() {
        //GIVEN
        GracePeriodRequestDto gracePeriodRequestDto = new GracePeriodRequestDto();

        //THEN
        assertThatExceptionOfType(CustomErrorMessageException.class)
                .isThrownBy(() -> validator.validate(gracePeriodRequestDto));
    }

    @Test
    void validateGracePeriodWithInvalidClientServiceChannel() {
        //GIVEN
        GracePeriodRequestDto gracePeriodRequestDto = new GracePeriodRequestDto();
        gracePeriodRequestDto.setService(SERVICE);
        GracePeriodRequestDto.ParamMap paramMap = new GracePeriodRequestDto.ParamMap();
        paramMap.setCode(CLIENT_SERVICE_CHANNEL);
        gracePeriodRequestDto.setOperationParams(List.of(paramMap));

        //THEN
        assertThatExceptionOfType(CustomErrorMessageException.class)
                .isThrownBy(() -> validator.validate(gracePeriodRequestDto));
    }

    @Test
    void validateGracePeriodWithInvalidIsNewCard() {
        //GIVEN
        GracePeriodRequestDto gracePeriodRequestDto = new GracePeriodRequestDto();
        gracePeriodRequestDto.setService(SERVICE);
        GracePeriodRequestDto.ParamMap paramMap = new GracePeriodRequestDto.ParamMap();
        paramMap.setCode(IS_NEW_CARD);
        gracePeriodRequestDto.setOperationParams(List.of(paramMap));

        //THEN
        assertThatExceptionOfType(CustomErrorMessageException.class)
                .isThrownBy(() -> validator.validate(gracePeriodRequestDto));
    }

    @Test
    void validateGracePeriodWithInvalidCardType() {
        //GIVEN
        GracePeriodRequestDto gracePeriodRequestDto = new GracePeriodRequestDto();
        gracePeriodRequestDto.setService(SERVICE);
        GracePeriodRequestDto.ParamMap paramMap = new GracePeriodRequestDto.ParamMap();
        paramMap.setCode(CARD_TYPE);
        gracePeriodRequestDto.setOperationParams(List.of(paramMap));

        //THEN
        assertThatExceptionOfType(CustomErrorMessageException.class)
                .isThrownBy(() -> validator.validate(gracePeriodRequestDto));
    }

    @Test
    void validateGracePeriodWithValidClassifiedParam() {
        //GIVEN
        GracePeriodRequestDto gracePeriodRequestDto = new GracePeriodRequestDto();
        gracePeriodRequestDto.setService(SERVICE);
        GracePeriodRequestDto.ParamMap paramMap = new GracePeriodRequestDto.ParamMap();
        paramMap.setCode(CARD_TYPE);
        paramMap.setValue(GRACE_PERIOD_PARAM_VALUE);
        gracePeriodRequestDto.setOperationParams(List.of(paramMap));

        //THEN
        assertThatNoException()
                .isThrownBy(() -> validator.validate(gracePeriodRequestDto));
    }

    @Test
    void validateGracePeriodWithoutClassifiedParams() {
        //GIVEN
        GracePeriodRequestDto gracePeriodRequestDto = new GracePeriodRequestDto();
        gracePeriodRequestDto.setService(SERVICE);
        GracePeriodRequestDto.ParamMap paramMap = new GracePeriodRequestDto.ParamMap();
        paramMap.setCode(EMPTY_PACKAGE_CODE);
        gracePeriodRequestDto.setOperationParams(List.of(paramMap));

        //THEN
        assertThatNoException()
                .isThrownBy(() -> validator.validate(gracePeriodRequestDto));
    }
}