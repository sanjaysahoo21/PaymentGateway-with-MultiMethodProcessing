package com.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.repositories.MerchantRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the payment gateway.
 */
@Configuration
public class SecurityConfig {

    /**
     * Create merchant authentication filter bean.
     */
    @Bean
    public MerchantAuthenticationFilter merchantAuthenticationFilter(MerchantRepository merchantRepository,
                                                                     ObjectMapper objectMapper) {
        return new MerchantAuthenticationFilter(merchantRepository, objectMapper);
    }

    /**
     * Configure HTTP security chain with stateless authentication.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   MerchantAuthenticationFilter merchantAuthenticationFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/health", "/api/v1/test/merchant", "/api/v1/orders/*/public", "/api/v1/payments/public", "/api/v1/payments/*/public").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(merchantAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());
        return http.build();
    }
}
