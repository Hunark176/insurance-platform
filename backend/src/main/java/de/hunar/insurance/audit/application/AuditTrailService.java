package de.hunar.insurance.audit.application;

import de.hunar.insurance.audit.AuditContext;
import de.hunar.insurance.audit.AuditTrailApi;
import de.hunar.insurance.audit.domain.AuditEntry;
import de.hunar.insurance.audit.domain.AuditEntryRepository;
import de.hunar.insurance.audit.dto.AuditEntryResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class AuditTrailService implements AuditTrailApi {
    private final AuditEntryRepository repository;
    private final Clock clock;

    @Override
    public AuditContext captureContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                ? authentication.getName()
                : "SYSTEM";
        return new AuditContext(actor, MDC.get("requestId"));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String action, String entityType, String entityId, String summary, AuditContext context) {
        repository.save(new AuditEntry(
                clock.instant(),
                context.actor(),
                action,
                entityType,
                entityId,
                context.requestId(),
                summary
        ));
    }

    @Transactional(readOnly = true)
    public Page<AuditEntryResponse> findForEntity(String entityType, String entityId, Pageable pageable) {
        return repository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(entityType, entityId, pageable)
                .map(entry -> new AuditEntryResponse(
                        entry.getId(),
                        entry.getOccurredAt(),
                        entry.getActor(),
                        entry.getAction(),
                        entry.getEntityType(),
                        entry.getEntityId(),
                        entry.getRequestId(),
                        entry.getSummary()
                ));
    }
}
