package com.huongcung.core.security.enumeration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum representing user roles with Spring Security integration
 */
public enum UserRole {
    
    ADMIN("ADMIN", "ROLE_ADMIN", "Administrator"),
    STORE_MANAGER("STORE_MANAGER", "ROLE_STORE_MANAGER", "Store Manager"),
    SUPPORT_AGENT("SUPPORT_AGENT", "ROLE_SUPPORT_AGENT", "Support Agent"),
    CUSTOMER("CUSTOMER", "ROLE_CUSTOMER", "Customer");
    
    private final String code;
    private final String springSecurityRole;
    private final String displayName;
    
    UserRole(String code, String springSecurityRole, String displayName) {
        this.code = code;
        this.springSecurityRole = springSecurityRole;
        this.displayName = displayName;
    }
    
    /**
     * Get the business logic code for this role
     * @return the role code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Get the Spring Security role string
     * @return the Spring Security role
     */
    public String getSpringSecurityRole() {
        return springSecurityRole;
    }
    
    /**
     * Get the human-readable display name
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Find UserRole by Spring Security role string
     * @param springSecurityRole the Spring Security role
     * @return UserRole or null if not found
     */
    public static UserRole fromSpringSecurityRole(String springSecurityRole) {
        return Arrays.stream(values())
                .filter(role -> role.springSecurityRole.equals(springSecurityRole))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Find UserRole by business code
     * @param code the business code
     * @return UserRole or null if not found
     */
    public static UserRole fromCode(String code) {
        return Arrays.stream(values())
                .filter(role -> role.code.equals(code))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get all Spring Security roles as a list
     * @return list of Spring Security role strings
     */
    public static List<String> getAllSpringSecurityRoles() {
        return Arrays.stream(values())
                .map(UserRole::getSpringSecurityRole)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all business codes as a list
     * @return list of business codes
     */
    public static List<String> getAllCodes() {
        return Arrays.stream(values())
                .map(UserRole::getCode)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if a Spring Security role is valid
     * @param springSecurityRole the role to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidSpringSecurityRole(String springSecurityRole) {
        return fromSpringSecurityRole(springSecurityRole) != null;
    }
    
    /**
     * Check if a business code is valid
     * @param code the code to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }
    
    @Override
    public String toString() {
        return code;
    }
}
