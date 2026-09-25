package de.hunar.insurance.customer.infrastructure;

import de.hunar.insurance.customer.domain.Customer;
import de.hunar.insurance.customer.domain.CustomerRepository;
import de.hunar.insurance.customer.domain.CustomerRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {
    private final CustomerRepository jpaRepository;
    public CustomerRepositoryAdapter(CustomerRepository jpaRepository) { this.jpaRepository = jpaRepository; }
    public Customer save(Customer customer) { return jpaRepository.save(customer); }
    public List<Customer> findAll() { return jpaRepository.findAll(); }
    public Optional<Customer> findById(Long id) { return jpaRepository.findById(id); }
    public Optional<Customer> findByCustomerNumber(String number) { return jpaRepository.findByCustomerNumber(number); }
    public boolean existsById(Long id) { return jpaRepository.existsById(id); }
}
