package de.hunar.insurance.policy.events;

import de.hunar.insurance.shared.domain.Money;

public record PolicyIssuedEvent(Long policyId, Long customerId, Money premium) {
}
