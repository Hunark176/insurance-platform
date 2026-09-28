package de.hunar.insurance.policy;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface PolicyApi {
    PolicySnapshot getSnapshot(Long policyId);
    List<PolicySnapshot> getSnapshots(Collection<Long> policyIds);
    boolean isActiveAt(Long policyId, LocalDate date);
}
