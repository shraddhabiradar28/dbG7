package com.dbtraining.reconx.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ============================================================================
 * SecurityConfig — TICKET-ADV073 + TICKET-ADV074
 * ============================================================================
 * Stateless JWT auth + role-based access control across
 * ADMIN / TRADER / VIEWER / RECON_ANALYST.
 * ============================================================================
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // ====================================================================
        // Day-1 permissive default — replace with TICKET-ADV073 + ADV074 rules.
        // ====================================================================
        // TODO(TICKET-ADV073 + ADV074): swap this permitAll() block for the
        //   stateless JWT + role-based chain shown in the Javadoc above.
        // ====================================================================

        return http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.disable())) // allow /h2 in dev
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
