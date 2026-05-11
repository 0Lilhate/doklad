package ru.it_alnc.packagesearch.audit;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.it_alnc.ita_forms.audit.service.AuditService;
/*
Обёртка сервиса аудит для уменьшения кол-ва аргументов при вызове записи в аудит
 */

@Component
@Slf4j
public class AuditServiceWrapper {

    private final AuditService auditService;

    @Value("${service.audit-enabled}")
    private boolean auditEnabled;

    public static final String SERVICE_NAME = "packageSearch";

    public AuditServiceWrapper(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostConstruct
    private void init() {
        log.info("AuditServiceWrapper bean created. service.audit-enabled={}", auditEnabled);
    }

    public void auditRequest(AuditDataDto auditDataDto, JsonNode body, String message) {
        log.debug("Audit request: {}", auditDataDto);
        auditService.auditRequest(
                auditDataDto.getAuditKey(),
                auditDataDto.getAuditOperation(),
                auditDataDto.getClientPin(),
                SERVICE_NAME,
                auditDataDto.getProcessKey(),
                auditDataDto.getAuditUUID(),
                null,
                null,
                auditDataDto.getRequestState(),
                message,
                auditDataDto.getStacktrace(),
                body != null ? body.toString() : null);
    }


}
