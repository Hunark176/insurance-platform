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

/**
 * Captures actor/request context and persists append-only audit entries.
 *
 * <p>Audit writes require an existing business transaction and therefore roll back with it.</p>
 */
@Service
@RequiredArgsConstructor
public class AuditTrailService implements AuditTrailApi {
    private final AuditEntryRepository repository;
    private final Clock clock;

    /**
     * Captures the authenticated user and current request ID, falling back to {@code SYSTEM}.
     *
     * @return context that can be attached to a business event or audit entry
     */
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

    /**
     * Appends an audit entry within the caller's transaction.
     *
     * @param action action that occurred
     * @param entityType affected entity type
     * @param entityId affected entity identifier
     * @param summary data-minimized change description
     * @param context actor and request context
     * @throws IllegalStateException if called without an active transaction
     */
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

    /**
     * Reads a page of audit entries for one entity, newest first as requested by the caller.
     *
     * @param entityType entity type filter
     * @param entityId entity identifier filter
     * @param pageable page and sort configuration
     * @return matching audit entries
     */
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
