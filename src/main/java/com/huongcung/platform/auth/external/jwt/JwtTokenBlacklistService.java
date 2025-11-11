package com.huongcung.platform.auth.external.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing blacklisted JWT tokens
 */
@Service
@Slf4j
public class JwtTokenBlacklistService {
    
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    
    /**
     * Add a token to the blacklist
     * @param token the JWT token to blacklist
     */
    public void blacklistToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            blacklistedTokens.add(token);
            log.debug("Token blacklisted: {}", token.substring(0, Math.min(20, token.length())) + "...");
        }
    }
    
    /**
     * Check if a token is blacklisted
     * @param token the JWT token to check
     * @return true if the token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
    
    /**
     * Remove a token from the blacklist (for testing purposes)
     * @param token the JWT token to remove from blacklist
     */
    public void removeFromBlacklist(String token) {
        blacklistedTokens.remove(token);
        log.debug("Token removed from blacklist: {}", token.substring(0, Math.min(20, token.length())) + "...");
    }
    
    /**
     * Get the number of blacklisted tokens
     * @return the count of blacklisted tokens
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
    
    /**
     * Clear all blacklisted tokens (for testing purposes)
     */
    public void clearBlacklist() {
        blacklistedTokens.clear();
        log.debug("Token blacklist cleared");
    }
}
