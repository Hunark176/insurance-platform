package de.hunar.insurance.shared.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

/**
 * Stored outcome of a request sent with an {@code Idempotency-Key}.
 *
 * <p>A record without {@link #statusCode} is still in progress. Once the request finished with a
 * non-5xx status, status, content type and body are stored so a retry with the same key gets the
 * same answer instead of executing the request again.</p>
 */
@Getter
@Entity
@Table(name = "idempotency_records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyRecord {
    /** Upper bound for stored response bodies; larger responses are not replayed. */
    public static final int MAX_BODY_LENGTH = 1_000_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** User, method, path and key; unique so concurrent first requests cannot both run. */
    @Column(name = "scope_key", nullable = false, unique = true, length = 400)
    private String scopeKey;

    /** SHA-256 of method, path, query and body, to detect a key reused for another request. */
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "content_type", length = 200)
    private String contentType;

    @Column(name = "response_body", length = MAX_BODY_LENGTH)
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    IdempotencyRecord(String scopeKey, String requestHash, Instant createdAt) {
        this.scopeKey = Objects.requireNonNull(scopeKey);
        this.requestHash = Objects.requireNonNull(requestHash);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    boolean isCompleted() {
        return statusCode != null;
    }

    void complete(int status, String type, String body, Instant now) {
        this.statusCode = status;
        this.contentType = type;
        this.responseBody = body;
        this.completedAt = now;
    }
}
