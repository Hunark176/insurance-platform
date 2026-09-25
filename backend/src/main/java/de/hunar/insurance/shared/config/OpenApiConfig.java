package de.hunar.insurance.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Insurance Platform API",
        version = "v1",
        description = "Modular monolith insurance domain API"
))
public class OpenApiConfig {
}
