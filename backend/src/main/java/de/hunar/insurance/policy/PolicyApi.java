package de.hunar.insurance.policy;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Public policy-module contract used by claims and other consumers.
 *
 * <p>Policy data crosses the boundary as immutable snapshots rather than JPA entities.</p>
 */
public interface PolicyApi {
    /**
     * Loads a snapshot for a single policy.
     *
     * @param policyId policy identifier
     * @return immutable policy data required by the consumer
     * @throws de.hunar.insurance.shared.web.ResourceNotFoundException if the policy does not exist
     */
    PolicySnapshot getSnapshot(Long policyId);

    /**
     * Loads snapshots for the supplied identifiers in a batch.
     *
     * @param policyIds policy identifiers to resolve
     * @return snapshots for the policies found; missing identifiers are omitted
     */
    List<PolicySnapshot> getSnapshots(Collection<Long> policyIds);

    /**
     * Evaluates whether an active policy covers a date.
     *
     * @param policyId policy identifier
     * @param date date on which coverage is required
     * @return {@code true} when the policy is active and the date is within its coverage term
     * @throws de.hunar.insurance.shared.web.ResourceNotFoundException if the policy does not exist
     */
    boolean isActiveAt(Long policyId, LocalDate date);
}
