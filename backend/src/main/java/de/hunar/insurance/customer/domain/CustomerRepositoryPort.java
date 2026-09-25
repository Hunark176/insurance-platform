package de.hunar.insurance.customer.domain;

import java.util.List;
import java.util.Optional;

public interface CustomerRepositoryPort {
    Customer save(Customer customer);
    List<Customer> findAll();
    Optional<Customer> findById(Long id);
    Optional<Customer> findByCustomerNumber(String number);
    boolean existsById(Long id);
}
