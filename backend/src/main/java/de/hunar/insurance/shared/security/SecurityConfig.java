package de.hunar.insurance.shared.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Security model of the public demo: documentation and most reads are open, every write requires
 * HTTP Basic authentication, and role checks are declared with {@code @PreAuthorize} on the
 * controller methods.
 *
 * <p>Requests sent with {@code X-Requested-With: XMLHttpRequest} receive a plain 401 without a
 * {@code WWW-Authenticate} challenge, so the browser does not open its own login dialog.</p>
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(DemoUserProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless HTTP Basic without cookies: no CSRF token is needed.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.DELETE, "/api/**").denyAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/demo-accounts").permitAll()
                        // Reads that still need a login; the required role is checked on the method.
                        .requestMatchers(HttpMethod.GET, "/api/auth/me", "/api/audit-trail/**", "/api/customers")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService users(DemoUserProperties properties, PasswordEncoder encoder) {
        List<UserDetails> users = properties.users().stream()
                .map(user -> User.withUsername(user.username())
                        .password(encoder.encode(user.password()))
                        .roles(user.role())
                        .build())
                .toList();
        return new InMemoryUserDetailsManager(users);
    }
}
