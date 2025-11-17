package com.huongcung.core.security.configuration;

import com.huongcung.core.security.enumeration.UserRole;
import com.huongcung.core.security.external.jwt.CustomerUserDetailsService;
import com.huongcung.core.security.external.jwt.StaffUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfiguration {

    private final CustomerUserDetailsService customerUserDetailsService;

    private final StaffUserDetailsService staffUserDetailsService;

    private final OncePerRequestFilter oncePerRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean("customerAuthenticationProvider")
    public AuthenticationProvider customerAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customerUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean("staffAuthenticationProvider")
    public AuthenticationProvider staffAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(staffUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    /**
     * Primary authentication manager that uses both customer and staff providers.
     * This is used by HttpSecurity and will try both providers when authenticating.
     */
    @Bean
    @Primary
    public AuthenticationManager authenticationManager() {
        List<AuthenticationProvider> providers = new ArrayList<>();
        providers.add(customerAuthenticationProvider());
        providers.add(staffAuthenticationProvider());
        return new ProviderManager(providers);
    }
    
    @Bean("customerAuthenticationManager")
    public AuthenticationManager customerAuthenticationManager() {
        return new ProviderManager(Collections.singletonList(customerAuthenticationProvider()));
    }
    
    @Bean("staffAuthenticationManager")
    public AuthenticationManager staffAuthenticationManager() {
        return new ProviderManager(Collections.singletonList(staffAuthenticationProvider()));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/books/**").permitAll()
                .requestMatchers("/api/books/search").permitAll()
                .requestMatchers("/api/checkout/ghn/**").permitAll() // GHN address lookup endpoints
                .requestMatchers("/actuator/health").permitAll()
                //.requestMatchers("/api/admin/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole(UserRole.ADMIN.getCode())
                
                // Store Manager endpoints
                .requestMatchers("/api/store-manager/**").hasAnyRole(UserRole.ADMIN.getCode(), UserRole.STORE_MANAGER.getCode())
                
                // Support Agent endpoints
                .requestMatchers("/api/support/**").hasAnyRole(UserRole.ADMIN.getCode(), UserRole.SUPPORT_AGENT.getCode())
                
                // Customer endpoints
                .requestMatchers("/api/customer/**").hasRole(UserRole.CUSTOMER.getCode())
                .requestMatchers("/api/checkout/orders").hasAnyRole(
                    UserRole.CUSTOMER.getCode(), 
                    UserRole.ADMIN.getCode(), 
                    UserRole.STORE_MANAGER.getCode(), 
                    UserRole.SUPPORT_AGENT.getCode())
                .requestMatchers("/api/orders/**").hasAnyRole(
                    UserRole.CUSTOMER.getCode(), 
                    UserRole.ADMIN.getCode(), 
                    UserRole.STORE_MANAGER.getCode(), 
                    UserRole.SUPPORT_AGENT.getCode())
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(customerAuthenticationProvider())
            .authenticationProvider(staffAuthenticationProvider())
            .addFilterBefore(oncePerRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
