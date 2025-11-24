package com.huongcung.core.user.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDetails getCurrentUser();
}
