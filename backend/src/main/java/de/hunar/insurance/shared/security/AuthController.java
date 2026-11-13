package de.hunar.insurance.shared.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * Login support for the frontend: credential check and the published demo accounts.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Anmeldung und Demo-Zugänge")
public class AuthController {
    private static final String ROLE_PREFIX = "ROLE_";

    private final DemoUserProperties properties;

    /**
     * Returns the authenticated user; used by the frontend to verify entered credentials.
     *
     * @param authentication current authentication
     * @return username, roles without the {@code ROLE_} prefix and, for customer logins, the
     *         customer number the login represents
     */
    @GetMapping("/me")
    @Operation(summary = "Angemeldeten Benutzer abfragen")
    @SecurityRequirement(name = OpenApiSecurity.SCHEME)
    public CurrentUser me(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(ROLE_PREFIX))
                .map(authority -> authority.substring(ROLE_PREFIX.length()))
                .sorted()
                .toList();
        String customerNumber = properties.users().stream()
                .filter(user -> user.username().equals(authentication.getName()))
                .map(DemoUserProperties.DemoUser::customerNumber)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        return new CurrentUser(authentication.getName(), roles, customerNumber);
    }

    /**
     * Lists the demo logins when publishing is enabled; otherwise returns an empty list.
     *
     * @return demo accounts including their passwords
     */
    @GetMapping("/demo-accounts")
    @Operation(summary = "Öffentliche Demo-Zugänge auflisten")
    public List<DemoAccount> demoAccounts() {
        if (!properties.publishCredentials()) {
            return List.of();
        }
        return properties.users().stream()
                .map(user -> new DemoAccount(user.username(), user.password(), user.role()))
                .toList();
    }

    /**
     * @param customerNumber customer represented by the login; {@code null} for staff logins
     */
    public record CurrentUser(String username, List<String> roles,
                              @Schema(types = {"string", "null"}) String customerNumber) {
    }

    public record DemoAccount(String username, String password, String role) {
    }
}
