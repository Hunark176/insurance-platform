package de.hunar.insurance.shared.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitFilterTest {
    private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-11-13T10:00:05Z"));
    private RateLimitFilter filter;
    private FilterChain chain;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        Clock clock = new Clock() {
            @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
            @Override public Clock withZone(java.time.ZoneId zone) { return this; }
            @Override public Instant instant() { return now.get(); }
        };
        ObjectProvider<Clock> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable(any())).thenReturn(clock);
        filter = new RateLimitFilter(2, 3, provider);
        chain = mock(FilterChain.class);
    }

    @Test
    void writesAboveTheLimitAreRejectedWithRetryAfter() throws Exception {
        assertThat(call("POST", "203.0.113.7", null).getStatus()).isEqualTo(200);
        assertThat(call("PATCH", "203.0.113.7", null).getStatus()).isEqualTo(200);

        MockHttpServletResponse rejected = call("POST", "203.0.113.7", null);

        assertThat(rejected.getStatus()).isEqualTo(429);
        assertThat(rejected.getHeader("Retry-After")).isEqualTo("55");
        assertThat(rejected.getContentType()).startsWith("application/problem+json");
        assertThat(rejected.getContentAsString()).contains("\"status\":429");
        verify(chain, times(2)).doFilter(any(), any());
    }

    @Test
    void readsHaveTheirOwnHigherLimit() throws Exception {
        call("POST", "203.0.113.7", null);
        call("POST", "203.0.113.7", null);

        for (int i = 0; i < 3; i++) {
            assertThat(call("GET", "203.0.113.7", null).getStatus()).isEqualTo(200);
        }
        assertThat(call("GET", "203.0.113.7", null).getStatus()).isEqualTo(429);
    }

    @Test
    void clientsAreCountedSeparately() throws Exception {
        call("POST", "203.0.113.7", null);
        call("POST", "203.0.113.7", null);

        assertThat(call("POST", "203.0.113.7", null).getStatus()).isEqualTo(429);
        assertThat(call("POST", "198.51.100.4", null).getStatus()).isEqualTo(200);
    }

    @Test
    void theLimitResetsInTheNextMinute() throws Exception {
        call("POST", "203.0.113.7", null);
        call("POST", "203.0.113.7", null);
        assertThat(call("POST", "203.0.113.7", null).getStatus()).isEqualTo(429);

        now.set(Instant.parse("2026-11-13T10:01:00Z"));

        assertThat(call("POST", "203.0.113.7", null).getStatus()).isEqualTo(200);
    }

    @Test
    void forwardedAddressIsUsedOnlyBehindTheInternalProxy() {
        // Caddy in the Docker network forwards the real client address.
        assertThat(RateLimitFilter.clientIp(request("GET", "172.18.0.5", "203.0.113.7")))
                .isEqualTo("203.0.113.7");
        // A client talking directly cannot choose its own key by sending the header.
        assertThat(RateLimitFilter.clientIp(request("GET", "203.0.113.7", "10.0.0.1")))
                .isEqualTo("203.0.113.7");
    }

    @Test
    void requestsOutsideTheApiAreNotLimited() throws Exception {
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = request("GET", "203.0.113.7", null);
            request.setRequestURI("/swagger-ui/index.html");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, chain);
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }

    private MockHttpServletResponse call(String method, String remote, String forwarded) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request(method, remote, forwarded), response, chain);
        return response;
    }

    private static MockHttpServletRequest request(String method, String remote, String forwarded) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/api/claims");
        request.setRemoteAddr(remote);
        if (forwarded != null) {
            request.addHeader("X-Forwarded-For", forwarded);
        }
        return request;
    }
}
