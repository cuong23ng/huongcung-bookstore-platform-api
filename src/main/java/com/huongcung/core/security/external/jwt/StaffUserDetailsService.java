package com.huongcung.core.security.external.jwt;

import com.huongcung.core.security.model.dto.CustomUserDetails;
import com.huongcung.core.user.model.entity.StaffEntity;
import com.huongcung.core.user.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("staffUserDetailsService")
@RequiredArgsConstructor
@Slf4j
public class StaffUserDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading staff by email: {}", email);

        StaffEntity staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Staff not found with email: " + email));

        if (!staff.getIsActive()) {
            throw new UsernameNotFoundException("Staff account is inactive: " + email);
        }

        log.debug("Staff found: {} with ID: {}, type: {}", email, staff.getId(), staff.getStaffType());

        return CustomUserDetails.create(staff);
    }
}

