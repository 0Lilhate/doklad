package ru.it_alnc.packagesearch.validator;

import ru.it_alnc.packagesearch.dto.checkInfo.CheckInfoRequestDto;
import ru.it_alnc.packagesearch.dto.graceperiod.GracePeriodRequestDto;
import ru.it_alnc.packagesearch.exception.CustomErrorMessageException;

/**
 * Валидатор запросов
 */
public interface PackageSearchRequestValidator {
    /**
     * Осуществить валидацию
     *
     * @param checkInfoRequestDto данные для валидации
     * @throws CustomErrorMessageException сообщение об ошибке
     */
    void validate(CheckInfoRequestDto checkInfoRequestDto) throws CustomErrorMessageException;

    /**
     * Осуществить валидацию
     *
     * @param gracePeriodRequestDto данные для валидации
     * @throws CustomErrorMessageException сообщение об ошибке
     */
    void validate(GracePeriodRequestDto gracePeriodRequestDto) throws CustomErrorMessageException;
}
