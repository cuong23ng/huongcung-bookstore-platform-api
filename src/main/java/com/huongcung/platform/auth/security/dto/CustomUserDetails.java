package com.huongcung.platform.auth.security.dto;

import com.huongcung.core.user.entity.CustomerEntity;
import com.huongcung.core.user.entity.StaffEntity;
import com.huongcung.core.user.entity.UserEntity;
import com.huongcung.core.user.enumeration.StaffType;
import com.huongcung.platform.auth.enumeration.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    
    private Long id;
    private String email;
    private String password;
    private boolean isActive;
    private boolean emailVerified;
    private Collection<? extends GrantedAuthority> authorities;
    
    /**
     * Create CustomUserDetails from UserEntity
     * @param user the UserEntity
     * @return CustomUserDetails instance
     */
    public static CustomUserDetails create(UserEntity user) {
        List<GrantedAuthority> authorities = getAuthorities(user);
        
        return new CustomUserDetails(
            user.getId(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getIsActive(),
            user.getEmailVerified(),
            authorities
        );
    }
    
    /**
     * Get authorities based on user type and role
     * @param user the UserEntity
     * @return list of authorities
     */
    private static List<GrantedAuthority> getAuthorities(UserEntity user) {
        if (user instanceof CustomerEntity) {
            return Collections.singletonList(new SimpleGrantedAuthority(UserRole.CUSTOMER.getSpringSecurityRole()));
        } else if (user instanceof StaffEntity staff) {
            StaffType staffType = staff.getStaffType();

            switch (staffType) {
                case ADMIN:
                    return Collections.singletonList(new SimpleGrantedAuthority(UserRole.ADMIN.getSpringSecurityRole()));
                case STORE_MANAGER:
                    return Collections.singletonList(new SimpleGrantedAuthority(UserRole.STORE_MANAGER.getSpringSecurityRole()));
                case SUPPORT_AGENT:
                    return Collections.singletonList(new SimpleGrantedAuthority(UserRole.SUPPORT_AGENT.getSpringSecurityRole()));
                default:
                    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_STAFF"));
            }
        }

        return Collections.emptyList();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        // For development: only check if user is active
        // For production: implement email verification flow
        return isActive;
    }
}
