package com.huongcung.core.security.external.jwt;

import com.huongcung.core.security.model.dto.CustomUserDetails;
import com.huongcung.core.user.model.entity.CustomerEntity;
import com.huongcung.core.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("customerUserDetailsService")
@RequiredArgsConstructor
@Slf4j
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading customer by email: {}", email);

        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with email: " + email));

        if (!customer.getIsActive()) {
            throw new UsernameNotFoundException("Customer account is inactive: " + email);
        }

        log.debug("Customer found: {} with ID: {}", email, customer.getId());

        return CustomUserDetails.create(customer);
    }
}

