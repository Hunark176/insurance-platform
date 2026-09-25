package de.hunar.insurance.underwriting;

public interface UnderwritingApi {
    boolean canIssue(Long customerId, Long productId);
}
