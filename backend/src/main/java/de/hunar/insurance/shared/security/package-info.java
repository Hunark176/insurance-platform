/**
 * Authentication, role model and object-level access rules.
 *
 * <p>Exposed as named interface {@code shared::security} so application modules can use
 * {@link de.hunar.insurance.shared.security.CustomerAccessPolicy} for ownership checks.</p>
 */
@org.springframework.modulith.NamedInterface("security")
package de.hunar.insurance.shared.security;
