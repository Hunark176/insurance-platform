package de.hunar.insurance.shared.config;

import de.hunar.insurance.shared.security.DemoUserProperties;
import de.hunar.insurance.shared.security.OpenApiSecurity;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Insurance Platform API",
        version = "v1",
        description = "Modular monolith insurance domain API"
),
        // Relative URL: behind the TLS-terminating proxy the app only sees http, so a generated
        // absolute URL would make Swagger UI call http:// from an https:// page (mixed content).
        servers = @Server(url = "/"))
@SecurityScheme(name = OpenApiSecurity.SCHEME, type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfig {

    /**
     * Adds the role model and, if publishing is enabled, the demo logins to the API description.
     */
    @Bean
    OpenApiCustomizer demoAccountsDescription(DemoUserProperties properties) {
        return openApi -> {
            StringBuilder description = new StringBuilder("""
                    Modular monolith insurance domain API.

                    **Zugriff:** Lesende Endpunkte sind öffentlich. Schreibende Endpunkte \
                    (Schloss-Symbol) erfordern eine Anmeldung über **Authorize**:
                    `CUSTOMER` meldet Schäden, `CLERK` bearbeitet Schäden, Policen und Kunden, \
                    `ADMIN` darf zusätzlich Produkte anlegen, Auszahlungen ausführen und den Audit-Trail lesen.
                    """);
            if (properties.publishCredentials()) {
                description.append("\n**Demo-Zugänge** (Benutzername / Passwort):\n\n")
                        .append(properties.users().stream()
                                .map(user -> "- `%s` / `%s` (%s)".formatted(
                                        user.username(), user.password(), user.role()))
                                .collect(Collectors.joining("\n")));
            }
            openApi.getInfo().setDescription(description.toString());
        };
    }
}
