package de.hunar.insurance.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limits API requests per client IP and minute.
 *
 * <p>The demo logins are public, so without a limit anyone could create thousands of
 * records or try passwords in a loop. Writes (POST, PATCH, PUT, DELETE) have a low limit,
 * reads a generous one. The filter runs before Spring Security, so failed login attempts
 * count as well. Exceeding a limit returns {@code 429 Too Many Requests} with
 * {@code Retry-After}.</p>
 *
 * <p>The client IP is the first {@code X-Forwarded-For} entry when the request comes from a
 * private or loopback address (Caddy in the same Docker network, which replaces the header
 * with the real client address); otherwise the remote address. Counters live in memory, so
 * limits apply per backend instance, which matches the single-instance deployment.</p>
 */
@Slf4j
@Component
@Order(SecurityProperties.DEFAULT_FILTER_ORDER - 10)
@ConditionalOnProperty(prefix = "insurance.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private final int writesPerMinute;
    private final int readsPerMinute;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(@Value("${insurance.rate-limit.writes-per-minute:30}") int writesPerMinute,
                           @Value("${insurance.rate-limit.reads-per-minute:300}") int readsPerMinute,
                           ObjectProvider<Clock> clock) {
        this.writesPerMinute = writesPerMinute;
        this.readsPerMinute = readsPerMinute;
        this.clock = clock.getIfAvailable(Clock::systemUTC);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        boolean write = isWrite(request.getMethod());
        String key = (write ? "w:" : "r:") + clientIp(request);
        long minute = clock.millis() / 60_000;
        int limit = write ? writesPerMinute : readsPerMinute;

        if (windows.size() > MAX_TRACKED_CLIENTS) {
            windows.values().removeIf(window -> window.minute < minute);
        }
        Window window = windows.compute(key, (k, current) ->
                current == null || current.minute != minute ? new Window(minute) : current);
        if (window.count.incrementAndGet() > limit) {
            long retryAfterSeconds = 60 - (clock.millis() / 1000) % 60;
            log.warn("Rate limit exceeded: kind={} limitPerMinute={} path={}",
                    write ? "write" : "read", limit, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                    {"type":"about:blank","title":"Too Many Requests","status":429,\
                    "detail":"Too many requests. Please retry in %d seconds."}""".formatted(retryAfterSeconds));
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean isWrite(String method) {
        return HttpMethod.POST.matches(method) || HttpMethod.PATCH.matches(method)
                || HttpMethod.PUT.matches(method) || HttpMethod.DELETE.matches(method);
    }

    static String clientIp(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank() && isInternal(remote)) {
            return forwarded.split(",")[0].trim();
        }
        return remote;
    }

    private static boolean isInternal(String address) {
        try {
            InetAddress inet = InetAddress.getByName(address);
            return inet.isLoopbackAddress() || inet.isSiteLocalAddress() || inet.isLinkLocalAddress();
        } catch (UnknownHostException | SecurityException exception) {
            return false;
        }
    }

    private static final class Window {
        private final long minute;
        private final AtomicInteger count = new AtomicInteger();

        private Window(long minute) {
            this.minute = minute;
        }
    }
}
