package com.example.aisupport.repository;

import com.example.aisupport.BaseIntegrationTest;
import com.example.aisupport.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("should save customer with auto-generated timestamps")
    void shouldSaveCustomerWithTimestamps() {
        Customer customer = new Customer();
        customer.setFirstName("Alice");
        customer.setLastName("Johnson");
        customer.setEmail("alice@example.com");

        Customer saved = customerRepository.save(customer);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("should find customer by email")
    void shouldFindByEmail() {
        Customer customer = new Customer();
        customer.setFirstName("Bob");
        customer.setLastName("Brown");
        customer.setEmail("bob@example.com");
        customerRepository.save(customer);

        Optional<Customer> found = customerRepository.findByEmail("bob@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("should check existence by email")
    void shouldCheckExistsByEmail() {
        Customer customer = new Customer();
        customer.setFirstName("Carol");
        customer.setLastName("White");
        customer.setEmail("carol@example.com");
        customerRepository.save(customer);

        assertThat(customerRepository.existsByEmail("carol@example.com")).isTrue();
        assertThat(customerRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("should return empty optional for non-existent email")
    void shouldReturnEmptyForNonExistentEmail() {
        Optional<Customer> found = customerRepository.findByEmail("nobody@example.com");

        assertThat(found).isEmpty();
    }
}
