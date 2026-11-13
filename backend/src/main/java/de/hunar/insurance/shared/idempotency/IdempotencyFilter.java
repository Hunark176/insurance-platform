package de.hunar.insurance.shared.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * Makes {@code POST /api/**} requests safe to retry when the client sends an
 * {@code Idempotency-Key} header.
 *
 * <ul>
 *     <li>First request with a key: runs normally; a non-5xx response (status, content type,
 *         body) is stored for 24 hours per user, path and key.</li>
 *     <li>Same key and same request again: the stored response is returned with
 *         {@code Idempotency-Replayed: true}; nothing is executed twice.</li>
 *     <li>Same key with a different body: {@code 422}, since that indicates a client error.</li>
 *     <li>Same key while the first request is still running: {@code 409}.</li>
 *     <li>5xx or exception: the key is released so the client can retry.</li>
 * </ul>
 *
 * <p>Requests without the header are not affected. The filter runs after Spring Security, so
 * the stored response is bound to the authenticated user. Only status, content type and body
 * are replayed.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "insurance.idempotency", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IdempotencyFilter extends OncePerRequestFilter {
    public static final String HEADER = "Idempotency-Key";
    public static final String REPLAYED_HEADER = "Idempotency-Replayed";
    private static final Pattern VALID_KEY = Pattern.compile("[A-Za-z0-9_.:-]{8,100}");

    private final IdempotencyStore store;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Duration retention;
    private final Duration staleAfter;

    public IdempotencyFilter(IdempotencyStore store, ObjectMapper objectMapper, ObjectProvider<Clock> clock,
                             @Value("${insurance.idempotency.retention:PT24H}") Duration retention,
                             @Value("${insurance.idempotency.stale-after:PT5M}") Duration staleAfter) {
        this.store = store;
        this.objectMapper = objectMapper;
        this.clock = clock.getIfAvailable(Clock::systemUTC);
        this.retention = retention;
        this.staleAfter = staleAfter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod())
                || !request.getRequestURI().startsWith("/api/")
                || request.getHeader(HEADER) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = request.getHeader(HEADER).trim();
        if (!VALID_KEY.matcher(key).matches()) {
            problem(request, response, HttpStatus.BAD_REQUEST,
                    "Idempotency-Key must be 8 to 100 characters of letters, digits, '_', '-', '.' or ':'");
            return;
        }
        CachedBodyRequest cachedRequest = new CachedBodyRequest(request);
        String scopeKey = currentUser() + "|POST|" + request.getRequestURI() + "|" + key;
        String requestHash = hash(request, cachedRequest.body);

        IdempotencyStore.Reservation reservation = store.reserve(scopeKey, requestHash, clock.instant(), staleAfter);
        if (reservation instanceof IdempotencyStore.Existing(IdempotencyRecord existing)) {
            answerExisting(request, response, existing, requestHash);
            return;
        }
        Long recordId = ((IdempotencyStore.Reserved) reservation).recordId();

        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(cachedRequest, cachedResponse);
        } catch (IOException | ServletException | RuntimeException failure) {
            store.release(recordId);
            throw failure;
        }
        int status = cachedResponse.getStatus();
        byte[] body = cachedResponse.getContentAsByteArray();
        if (status >= 500 || body.length > IdempotencyRecord.MAX_BODY_LENGTH) {
            store.release(recordId);
        } else {
            store.complete(recordId, status, cachedResponse.getContentType(),
                    new String(body, StandardCharsets.UTF_8), clock.instant());
        }
        cachedResponse.copyBodyToResponse();
    }

    private void answerExisting(HttpServletRequest request, HttpServletResponse response, IdempotencyRecord existing,
                                String requestHash) throws IOException {
        if (!existing.getRequestHash().equals(requestHash)) {
            problem(request, response, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Idempotency-Key was already used for a different request");
            return;
        }
        if (!existing.isCompleted()) {
            problem(request, response, HttpStatus.CONFLICT,
                    "A request with this Idempotency-Key is still being processed");
            return;
        }
        log.info("Idempotent replay: path={} status={}", request.getRequestURI(), existing.getStatusCode());
        response.setStatus(existing.getStatusCode());
        response.setHeader(REPLAYED_HEADER, "true");
        if (existing.getContentType() != null) {
            response.setContentType(existing.getContentType());
        }
        if (existing.getResponseBody() != null) {
            byte[] body = existing.getResponseBody().getBytes(StandardCharsets.UTF_8);
            response.setContentLength(body.length);
            response.getOutputStream().write(body);
        }
    }

    /** Removes expired records; replays are only guaranteed within the retention period. */
    @Scheduled(fixedDelayString = "${insurance.idempotency.cleanup-interval:PT1H}",
            initialDelayString = "${insurance.idempotency.cleanup-interval:PT1H}")
    void deleteExpiredRecords() {
        int deleted = store.deleteCreatedBefore(clock.instant().minus(retention));
        if (deleted > 0) {
            log.debug("Deleted expired idempotency records: count={}", deleted);
        }
    }

    private static String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || authentication instanceof AnonymousAuthenticationToken
                ? "anonymous" : authentication.getName();
    }

    private static String hash(HttpServletRequest request, byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String query = request.getQueryString() == null ? "" : request.getQueryString();
            digest.update((request.getRequestURI() + "?" + query + "\n").getBytes(StandardCharsets.UTF_8));
            digest.update(body);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is always available", impossible);
        }
    }

    private void problem(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String detail)
            throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), problem);
    }

    /** Request wrapper that reads the body once so it can be hashed and still be read by the controller. */
    private static final class CachedBodyRequest extends HttpServletRequestWrapper {
        private final byte[] body;

        private CachedBodyRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.body = request.getInputStream().readAllBytes();
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream source = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override public boolean isFinished() { return source.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener listener) { throw new UnsupportedOperationException(); }
                @Override public int read() { return source.read(); }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
