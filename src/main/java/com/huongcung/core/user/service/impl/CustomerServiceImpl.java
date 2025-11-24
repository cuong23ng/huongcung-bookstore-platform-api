package com.huongcung.core.user.service.impl;

import com.huongcung.core.security.model.dto.CustomUserDetails;
import com.huongcung.core.user.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Repository
public class CustomerServiceImpl extends UserServiceImpl implements CustomerService {

    @Override
    public CustomUserDetails getCurrentUser() {
        UserDetails userDetails = super.getCurrentUser();
        CustomUserDetails customUserDetails = null;
        if (userDetails instanceof CustomUserDetails) {
            customUserDetails = (CustomUserDetails) userDetails;
            log.debug("Extracted customer ID from authentication: {}", customUserDetails.getId());
        }
        return customUserDetails;
    }
}
