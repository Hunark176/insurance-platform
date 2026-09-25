package de.hunar.insurance.policy;

import java.time.LocalDate;

public interface PolicyApi {
    PolicySnapshot getSnapshot(Long policyId);
    boolean isActiveAt(Long policyId, LocalDate date);
}
