package de.hunar.insurance.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class ApiRequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(ApiRequestLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestId = UUID.randomUUID().toString();
        long startTime = System.nanoTime();
        MDC.put("requestId", requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException exception) {
            log.error("API request processing failed: method={} path={}",
                    request.getMethod(), request.getRequestURI(), exception);
            throw exception;
        } finally {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            int status = response.getStatus();
            if (status >= 500) {
                log.error("API request completed: method={} path={} status={} durationMs={}",
                        request.getMethod(), request.getRequestURI(), status, durationMs);
            } else if (status >= 400) {
                log.warn("API request completed: method={} path={} status={} durationMs={}",
                        request.getMethod(), request.getRequestURI(), status, durationMs);
            } else {
                log.info("API request completed: method={} path={} status={} durationMs={}",
                        request.getMethod(), request.getRequestURI(), status, durationMs);
            }
            MDC.remove("requestId");
        }
    }
}
