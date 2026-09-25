package de.hunar.insurance.billing;

public interface BillingApi {
    boolean payoutExistsForClaim(Long claimId);
}
