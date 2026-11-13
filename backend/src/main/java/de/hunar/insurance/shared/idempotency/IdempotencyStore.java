package de.hunar.insurance.shared.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Persistence of idempotency records.
 *
 * <p>Every operation runs in its own short transaction ({@code REQUIRES_NEW}), independent of the
 * business transaction, so a reservation is visible to concurrent requests before the business
 * request runs. A failed insert (unique key taken by a concurrent request) rolls back only its own
 * transaction; the existing record is then read in a fresh one.</p>
 */
@Component
@RequiredArgsConstructor
class IdempotencyStore {
    private final IdempotencyRecordRepository repository;
    private final PlatformTransactionManager transactionManager;

    /** Outcome of trying to reserve a key. */
    sealed interface Reservation permits Reserved, Existing {
    }

    /** The key was free and is now reserved for this request. */
    record Reserved(Long recordId) implements Reservation {
    }

    /** A record for the key already exists (completed or still in progress). */
    record Existing(IdempotencyRecord record) implements Reservation {
    }

    Reservation reserve(String scopeKey, String requestHash, Instant now, Duration staleAfter) {
        Optional<IdempotencyRecord> existing = inNewTransaction(() -> repository.findByScopeKey(scopeKey));
        if (existing.isPresent()) {
            IdempotencyRecord found = existing.get();
            boolean abandoned = !found.isCompleted() && found.getCreatedAt().isBefore(now.minus(staleAfter));
            if (!abandoned) {
                return new Existing(found);
            }
            // A crashed request never finished; allow a new attempt.
            inNewTransaction(() -> {
                repository.deleteById(found.getId());
                return null;
            });
        }
        try {
            Long id = inNewTransaction(() ->
                    repository.saveAndFlush(new IdempotencyRecord(scopeKey, requestHash, now)).getId());
            return new Reserved(id);
        } catch (DataIntegrityViolationException concurrentFirstRequest) {
            // Another request reserved the same key between the lookup and the insert.
            return new Existing(inNewTransaction(() -> repository.findByScopeKey(scopeKey))
                    .orElseThrow(() -> concurrentFirstRequest));
        }
    }

    void complete(Long recordId, int status, String contentType, String body, Instant now) {
        inNewTransaction(() -> {
            repository.findById(recordId).ifPresent(record -> record.complete(status, contentType, body, now));
            return null;
        });
    }

    void release(Long recordId) {
        inNewTransaction(() -> {
            repository.deleteById(recordId);
            return null;
        });
    }

    int deleteCreatedBefore(Instant before) {
        return inNewTransaction(() -> repository.deleteCreatedBefore(before));
    }

    private <T> T inNewTransaction(java.util.function.Supplier<T> work) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template.execute(status -> work.get());
    }
}
