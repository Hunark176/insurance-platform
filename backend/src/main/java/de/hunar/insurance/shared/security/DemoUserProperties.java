package de.hunar.insurance.shared.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Login accounts of the public demo environment.
 *
 * <p>Passwords are supplied per environment; the {@code prod} profile has no defaults so the
 * application refuses to start without explicitly configured credentials.</p>
 *
 * @param users demo accounts, one per role
 * @param publishCredentials whether usernames and passwords are shown in Swagger and to the frontend
 */
@Validated
@ConfigurationProperties(prefix = "insurance.security")
public record DemoUserProperties(@NotEmpty List<@Valid DemoUser> users, boolean publishCredentials) {

    public DemoUserProperties {
        users = users == null ? List.of() : List.copyOf(users);
    }

    /**
     * A single demo login.
     *
     * @param username login name
     * @param password raw password; encoded on startup
     * @param role one of {@link Roles}
     * @param customerNumber customer this login represents; only relevant for the CUSTOMER role,
     *                       see {@link CustomerAccessPolicy}
     */
    public record DemoUser(
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank @Pattern(regexp = Roles.CUSTOMER + "|" + Roles.CLERK + "|" + Roles.ADMIN) String role,
            String customerNumber
    ) {
    }
}
