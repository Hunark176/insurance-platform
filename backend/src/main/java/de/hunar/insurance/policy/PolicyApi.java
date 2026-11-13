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
     * Evaluates whether a loss on the given date is covered (loss-occurrence principle).
     *
     * <p>Only the loss date matters: a cancelled or expired policy still covers losses that
     * occurred while it was in force, so such claims can be reported and settled later.</p>
     *
     * @param policyId policy identifier
     * @param lossDate date on which the loss occurred
     * @return {@code true} when the date lies between the start of the term and the end of
     *         coverage (end of term or cancellation date), both inclusive
     * @throws de.hunar.insurance.shared.web.ResourceNotFoundException if the policy does not exist
     */
    boolean isCoveredOn(Long policyId, LocalDate lossDate);

    /**
     * Locks the policy for the rest of the caller's transaction and returns its snapshot.
     *
     * <p>Claim approval uses this before summing the amounts already approved in a policy
     * year, so two concurrent approvals for the same policy cannot both pass the annual
     * aggregate check. Must be called inside an existing transaction.</p>
     *
     * @param policyId policy identifier
     * @return snapshot of the locked policy
     * @throws de.hunar.insurance.shared.web.ResourceNotFoundException if the policy does not exist
     * @throws org.springframework.transaction.IllegalTransactionStateException if no transaction is active
     */
    PolicySnapshot lockForClaimApproval(Long policyId);
}
