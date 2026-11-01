package de.hunar.insurance.shared.web;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ApiRequestLoggingFilterTest {
    private final ApiRequestLoggingFilter filter = new ApiRequestLoggingFilter();

    @Test
    void apiRequestGetsCorrelationIdAndPreservesResponseStatus() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/check");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND));

        assertThat(response.getHeader("X-Request-Id"))
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void nonApiRequestDoesNotGetApiCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {});

        assertThat(response.getHeader("X-Request-Id")).isNull();
    }
}
