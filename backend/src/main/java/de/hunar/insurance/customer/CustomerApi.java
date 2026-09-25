package de.hunar.insurance.customer;

public interface CustomerApi {
    CustomerSummary findById(Long customerId);
    CustomerSummary findByNumber(String customerNumber);
    boolean exists(Long customerId);
}
