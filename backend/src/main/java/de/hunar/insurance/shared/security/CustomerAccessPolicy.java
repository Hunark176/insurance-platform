package de.hunar.insurance.shared.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Decides whether the current user may act for a given customer (object-level authorization).
 *
 * <p>Role checks alone answer "may a customer report claims at all?". This policy answers
 * "may <em>this</em> customer report a claim for <em>that</em> policy?" and prevents a customer
 * from acting on another customer's data by sending a foreign customer number or policy id.</p>
 *
 * <ul>
 *     <li>Clerks and administrators may act for every customer.</li>
 *     <li>A user whose only business role is CUSTOMER may act only for the customer number
 *         configured for their login ({@code insurance.security.users[].customer-number}).
 *         Without a configured number they may act for nobody.</li>
 *     <li>Calls without an authenticated user (internal or system work) are not restricted;
 *         HTTP requests that change data always require authentication in
 *         {@link SecurityConfig}.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class CustomerAccessPolicy {
    private static final Set<String> STAFF_AUTHORITIES =
            Set.of("ROLE_" + Roles.CLERK, "ROLE_" + Roles.ADMIN);

    private final DemoUserProperties properties;

    /**
     * Checks whether the current user may act for the given customer.
     *
     * @param customerNumber customer that owns the affected data
     * @return {@code true} if access is allowed
     */
    public boolean canActFor(String customerNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return true;
        }
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        if (authorities.stream().anyMatch(STAFF_AUTHORITIES::contains)) {
            return true;
        }
        return properties.users().stream()
                .filter(user -> user.username().equals(authentication.getName()))
                .map(DemoUserProperties.DemoUser::customerNumber)
                .filter(Objects::nonNull)
                .anyMatch(own -> own.equals(customerNumber));
    }
}
