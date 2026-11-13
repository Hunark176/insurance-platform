package de.hunar.insurance.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.net.URI;

/**
 * Answers authentication and authorization failures of the security filter chain with the
 * same {@code ProblemDetail} format as the rest of the API.
 *
 * <p>Without this, a 401 had an empty body and a 403 from {@code denyAll} used Spring's default
 * error JSON. Browsers calling with {@code X-Requested-With: XMLHttpRequest} receive no
 * {@code WWW-Authenticate} challenge, so they do not open their native login dialog; other
 * clients (curl, Swagger) still get the Basic challenge.</p>
 */
public class ProblemDetailSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private static final String REALM_CHALLENGE = "Basic realm=\"Insurance Platform\"";

    private final ObjectMapper objectMapper;

    public ProblemDetailSecurityHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        if (!"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setHeader("WWW-Authenticate", REALM_CHALLENGE);
        }
        write(request, response, HttpStatus.UNAUTHORIZED, "Authentication is required");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, "Access is denied");
    }

    private void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String detail)
            throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
