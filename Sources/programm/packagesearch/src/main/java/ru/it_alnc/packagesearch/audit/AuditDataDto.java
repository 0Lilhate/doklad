package ru.it_alnc.packagesearch.audit;

import lombok.Builder;
import lombok.Data;
import ru.it_alnc.ita_forms.audit.service.AuditService;

//Dto для более удобной передачи кучи параметров из контроллера в следующий класс
@Data
@Builder
public class AuditDataDto {
    String clientPin;
    String auditUUID;
    String auditKey;
    String auditOperation;
    String path;
    AuditService.RequestState requestState;
    String processKey;
    String message;
    String stacktrace;
}
