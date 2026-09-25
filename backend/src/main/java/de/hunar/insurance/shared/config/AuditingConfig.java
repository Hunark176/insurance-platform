package de.hunar.insurance.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class AuditingConfig {
    @Bean
    AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).filter(authentication -> authentication.isAuthenticated())
                .map(authentication -> authentication.getName());
    }
}
