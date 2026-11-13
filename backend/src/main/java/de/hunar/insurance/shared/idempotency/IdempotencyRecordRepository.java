package de.hunar.insurance.shared.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {
    Optional<IdempotencyRecord> findByScopeKey(String scopeKey);

    @Modifying
    @Query("delete from IdempotencyRecord r where r.createdAt < :before")
    int deleteCreatedBefore(@Param("before") Instant before);
}
