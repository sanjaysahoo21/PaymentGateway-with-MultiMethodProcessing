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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
     * Configure CORS to allow frontend requests.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:3002",
            "http://localhost:5173",
            "http://localhost:5174"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
