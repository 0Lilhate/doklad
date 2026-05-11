package ru.it_alnc.packagesearch.audit;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import ru.it_alnc.ita_forms.audit.service.AuditService;

import java.util.*;

@Component
public class DtoFactory {

    public static final Integer ERRORMESSAGEMAXLENGTH = 250;

    public static AuditDataDto createAuditData(
            HttpHeaders requestHeaders,
            HttpServletRequest request, AuditService.RequestState requestState) {
        return AuditDataDto.builder()
                .clientPin(null) //никаких клиентов
                .auditUUID(requestHeaders.getFirst("audituuid"))
                .auditKey(requestHeaders.getFirst("auditkey"))
                .auditOperation(requestHeaders.getFirst("auditoperation"))
                .path(request.getRequestURI())
                .processKey(UUID.randomUUID().toString())
                .requestState(requestState)
                .build();
    }

}
